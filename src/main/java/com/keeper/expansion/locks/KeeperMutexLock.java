package com.keeper.expansion.locks;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import com.keeper.client.KeeperClient;
import com.keeper.client.exception.KeeperException;
import com.keeper.client.listener.KeeperChildListener;

/**
 * Thread safe
 *@author huangdou
 *@at 2016年12月5日下午3:02:52
 *@version 0.0.1
 */
public class KeeperMutexLock implements KeeperLock {
	private String name ;
	
	private String lockPath ;
	
	private KeeperClient client ;
	
	private Lock innerLock = new ReentrantLock() ;
	
	
	private ThreadLocal<String> currentNodePath = new ThreadLocal<String>();
	private ThreadLocal<Integer> lockCount = new ThreadLocal<Integer>();
	private Map<String,Semaphore> waitThreadMap = new ConcurrentHashMap<String,Semaphore>();
	
	private void innerLocked(){
		innerLock.lock();
	}
	
	private void innerUnlocked(){
		innerLock.unlock();
	}
	private synchronized void init (){
		if (name == null || "".equals(name.trim())||name.contains("/")){
			throw new IllegalArgumentException(String.format("path can not be %s", name));
		}
		lockPath = LOCK_ROOT + "/" + name ;
		if (!client.exist(lockPath)){
			try{
				client.createWtihParent(lockPath);
			}catch(KeeperException e){
				if (!(e.getProto() instanceof NodeExistsException)){
					throw e ;
				}
			}
		}
		client.listenChild(lockPath, new MutexLockKeeperChildListener(this));
	}

	public KeeperMutexLock(String name,KeeperClient client) {
		super();
		this.name = name;
		this.client = client ;
		init ();
	}
	
	public static KeeperLock getLock(String name,KeeperClient client){
		return new KeeperMutexLock(name, client);
	}

	@Override
	public void lock() throws InterruptedException {
		if (lockCount.get() != null && lockCount.get() > 0){
			lockCount.set(lockCount.get()+1);
			return ;
		}
		Semaphore semaphoreCurrentThread = new Semaphore(0);
		innerLock.lock();
		try{
			String returnPath = client.create(lockPath+"/", "".getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
			String[] array = returnPath.split("/");
			currentNodePath.set(returnPath);
			waitThreadMap.put(array[array.length-1], semaphoreCurrentThread);
		}finally {
			innerLock.unlock();
		}
		try {
			semaphoreCurrentThread.acquire();
			lockCount.set(1);
		} catch (InterruptedException e) {
			client.delete(currentNodePath.get());
			throw e;
		}
	}
	

	@Override
	public boolean tryLock() {
		try {
			return tryLock(0, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		if (lockCount.get() != null && lockCount.get() > 0){
			lockCount.set(lockCount.get()+1);
			return true;
		}
		Semaphore semaphoreCurrentThread = new Semaphore(0);
		innerLock.lock();
		try{
			String returnPath = client.create(lockPath+"/", "".getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
			String[] array = returnPath.split("/");
			currentNodePath.set(returnPath);
			waitThreadMap.put(array[array.length-1], semaphoreCurrentThread);
		}finally{
			innerLock.unlock();
		}
		boolean isLocked = false ;
		if (time > 0){
			try{
				isLocked = semaphoreCurrentThread.tryAcquire(1, time, unit);
			}catch(InterruptedException e){
				client.delete(currentNodePath.get());
				throw e ;
			}
			
		}else {
			isLocked = semaphoreCurrentThread.tryAcquire();
		}
		if (!isLocked){
			client.delete(currentNodePath.get());
		}else {
			lockCount.set(1);
		}
		return isLocked ;
	}

	@Override
	public void unlock() {
		if (lockCount.get() != null ){
			if (lockCount.get() > 1){
				lockCount.set(lockCount.get()-1);
			}else if (lockCount.get() == 1) {
				lockCount.set(null);
				client.delete(currentNodePath.get());
			}
			return;
		}
		throw new IllegalMonitorStateException();
	}

	@Override
	public Condition newCondition() {
		throw new KeeperException("newCondition is not yet supported!");
	}

	class MutexLockKeeperChildListener implements KeeperChildListener{

		KeeperMutexLock lock ;
		
		public MutexLockKeeperChildListener(KeeperMutexLock lock) {
			super();
			this.lock = lock;
		}

		@Override
		public void onChild(String parent, List<String> subs) {
			innerLocked();
			try{
				for (Entry<String, Semaphore> entry : waitThreadMap.entrySet()){
					if (subs.contains(entry.getKey())){
						Collections.sort(subs,new Comparator<String>() {
							@Override
							public int compare(String o1, String o2) {
								return Integer.parseInt(o1) - Integer.parseInt(o2);
							}
						});
						if (entry.getKey().equals(subs.get(0))){
							entry.getValue().release();
							waitThreadMap.remove(entry.getKey());
						}
					}
				}
			}
			finally{
				innerUnlocked();
			}
		}

		@Override
		public void onParentDelete(String path) {
		}
	}
}
