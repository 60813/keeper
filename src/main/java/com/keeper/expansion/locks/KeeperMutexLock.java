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

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import com.keeper.client.KeeperClient;
import com.keeper.client.exception.KeeperException;
import com.keeper.client.listener.KeeperChildListener;

/**
 *@author huangdou
 *@at 2016年12月5日下午3:02:52
 *@version 0.0.1
 */
public class KeeperMutexLock implements KeeperLock {
	private static final String LOCK_ROOT= "/lock_root";
	
	private String name ;
	
	private String lockPath ;
	
	private KeeperClient client ;
	
	private ThreadLocal<String> currentNodePath = new ThreadLocal<String>();
	private Map<String,Semaphore> waitThreadMap = new ConcurrentHashMap<String,Semaphore>();
	
	private void init (){
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

	@Override
	public void lock() {
		Semaphore semaphoreCurrentThread = new Semaphore(0);
		String returnPath = client.create(lockPath+"/", "".getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
		String[] array = returnPath.split("/");
		currentNodePath.set(returnPath);
		waitThreadMap.put(array[array.length-1], semaphoreCurrentThread);
		try {
			semaphoreCurrentThread.acquire();;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	

	@Override
	public boolean tryLock() {
		try {
			return tryLock(1, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		Semaphore semaphoreCurrentThread = new Semaphore(0);
		String returnPath = client.create(lockPath+"/", "".getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
		String[] array = returnPath.split("/");
		currentNodePath.set(returnPath);
		waitThreadMap.put(array[array.length-1], semaphoreCurrentThread);
		boolean isLocked = false ;
		if (time > 0){
			isLocked = semaphoreCurrentThread.tryAcquire(1, time, unit);
			
		}else {
			isLocked = tryLock();
		}
		if (!isLocked){
			client.delete(currentNodePath.get());
		}
		return isLocked ;
	}

	@Override
	public void unlock() {
		client.delete(currentNodePath.get());
		
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
					}
				}
				
			}
		}

		@Override
		public void onParentDelete(String path) {
			// TODO Auto-generated method stub
			
		}
	}
}
