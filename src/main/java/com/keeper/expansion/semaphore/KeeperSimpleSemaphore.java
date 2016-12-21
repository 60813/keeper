package com.keeper.expansion.semaphore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.keeper.client.KeeperClient;
import com.keeper.client.exception.KeeperException;
import com.keeper.client.listener.KeeperChildListener;
import com.keeper.expansion.locks.KeeperLock;
import com.keeper.expansion.locks.KeeperMutexLock;

/**
 *@author huangdou
 *@at 2016年12月11日下午9:15:32
 *@version 0.0.1
 */
public class KeeperSimpleSemaphore implements KeeperSemaphore {
	Logger logger = LoggerFactory.getLogger(KeeperSimpleSemaphore.class);
	private String name ;
	
	private String semaphoreHome ;
	
	private KeeperLock semaphoreLock_init ;
	
	private KeeperClient client;
	
	private int permitsLimit ;
	
	private Lock innerLock = new ReentrantLock() ;
	
	private void innerLocked(){
		innerLock.lock();
	}
	
	private void innerUnlocked(){
		innerLock.unlock();
	}
	private ThreadLocal<List<String>> currentNodePath = new ThreadLocal<List<String>>();
	private Map<String,CountDownLatch> waitThreadMap = new ConcurrentHashMap<String,CountDownLatch>();

	private void firstCreateSemaphore(String path,int permits){
		client.createWtihParent(semaphoreHome);
		client.update(semaphoreHome, (permits+"").getBytes());
		permitsLimit = permits;
	}
	public void init(int permits){
		this.semaphoreHome = SEMAPHORE_ROOT+"/"+name;
		semaphoreLock_init = new KeeperMutexLock((SEMAPHORE_ROOT+"_"+name+"_init").replace("/", ""), client);
		
		try {
			semaphoreLock_init.lock();
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
		try{
			String semaphoreStr = new String(client.read(semaphoreHome));
			permitsLimit = Integer.parseInt(semaphoreStr);
		}catch (KeeperException e)
		{
			if (e.getProto() != null && e.getProto() instanceof NoNodeException){
				firstCreateSemaphore(semaphoreHome, permits);
			}else {
				throw e ;
			}
		}finally{
			semaphoreLock_init.unlock();
		}
		client.listenChild(semaphoreHome, new SimpleSemaphoreListener());
	}
	
	private KeeperSimpleSemaphore(String name,KeeperClient client,int permits) {
		super();
		this.name = name;
		this.client = client;
		init(permits);
	}

	/**
	 * get the KeeperSemaphore with the name in distributed, 
	 * or create if KeeperSemaphore not exist in distributed
	 * 
	 * @param name the KeeperSemaphore you want to get or create
	 * @param permits the initial number of permits available when create.
     *        This value may be negative, in which case releases
     *        must occur before any acquires will be granted.
	 * */
	public static KeeperSemaphore getOrCreate(String name,int createPermits,KeeperClient client) throws SemaphoreException{
		if (name == null || "".equals(name.trim())||name.contains("/")){
			throw new IllegalArgumentException(String.format("name can not be %s", name));
		}
		
		return new KeeperSimpleSemaphore(name, client, createPermits);
	}
	
	@Override
	public void acquire() throws InterruptedException {
		acquire(1);
	}

	@Override
	public void acquire(int n) throws InterruptedException {
		if (n < 1){
			throw new IllegalArgumentException("n must be greater than 0");
		}
		CountDownLatch latchCurrentThread = new CountDownLatch(n);
		List<String> paths = currentNodePath.get() == null? new ArrayList<String>() : currentNodePath.get();
		List<String> tempPaths = new ArrayList<String>();
		
		for (int i=0;i<n;i++){
			innerLocked();
			try{
				String returnPath = client.create(semaphoreHome+"/", "".getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
				paths.add(returnPath);
				tempPaths.add(returnPath);
				currentNodePath.set(paths);
				String[] array = returnPath.split("/");
				waitThreadMap.put(array[array.length-1], latchCurrentThread);
			}finally{
				innerUnlocked();
			}
		}
		
		try {
			latchCurrentThread.await();
		} catch (InterruptedException e) {
			clearNodePath(-1);
			throw new RuntimeException(e);
		}
	}
	
	private synchronized void clearNodePath(int n){
		if (currentNodePath.get() == null){
			return ;
		}
		List<String> currenPath = currentNodePath.get();
		Iterator<String> it = currenPath.iterator();
		
		if (n <0 || n > currenPath.size()){
			n = currenPath.size();
		}
		int i=0;
		while (it.hasNext()){
			if (i == n){
				break;
			}
			String path = it.next();
			try{
				client.delete(path);
			}catch(Throwable t){
				logger.warn(t.getMessage(),t);
			}
			it.remove();
			i++;
		}
		currentNodePath.set(currenPath);
	}
	
	@Override
	public boolean tryAcquire(int n) {
		try {
			return tryAcquire(n, 1, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
		
	}
	
	@Override
	public boolean tryAcquire() {
		try {
			return tryAcquire(0, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false ;
		}
	}

	@Override
	public boolean tryAcquire(long timeout, TimeUnit unit)
			throws InterruptedException {
		return tryAcquire(1, timeout, unit);
	}
	
	@Override
	public boolean tryAcquire(int n, long timeout, TimeUnit unit)
			throws InterruptedException {
		if (n < 1){
			throw new IllegalArgumentException("n must be greater than 0");
		}
		CountDownLatch latchCurrentThread = new CountDownLatch(n);
		List<String> paths = currentNodePath.get() == null? new ArrayList<String>() : currentNodePath.get();
		List<String> tempPaths = new ArrayList<String>();
		
		for (int i=0;i<n;i++){
			innerLocked();
			try{
				String returnPath = client.create(semaphoreHome+"/", "".getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
				System.out.println("subs on created : " + returnPath);
				paths.add(returnPath);
				tempPaths.add(returnPath);
				String[] array = returnPath.split("/");
				waitThreadMap.put(array[array.length-1], latchCurrentThread);
			}finally{
				innerUnlocked();
			}
		}
		
		boolean isLocked = false ;
		if (timeout > 0){
			try{
				isLocked = latchCurrentThread.await(timeout, unit);
			}catch(InterruptedException e){
				clearNodePath(-1);
				return false;
			}
		}else {
			long count = latchCurrentThread.getCount();
			isLocked = count ==0 ;
		}
		if (!isLocked){
			clearNodePath(-1);
		}
		return isLocked ;
	}
	
	@Override
	public void release(int n) {
		clearNodePath(n);
	}
	
	@Override
	public void release() {
		release(1);
	}
	
	class SimpleSemaphoreListener implements KeeperChildListener{
		@Override
		public void onChild(String parent, List<String> subs) {
			if (subs != null && subs.size() > 0){
				Collections.sort(subs,new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return Integer.parseInt(o1) - Integer.parseInt(o2);
					}
				});
				List<String> permitChildren = subs.subList(0, subs.size() < permitsLimit ? subs.size() : permitsLimit );
				for (String pc : permitChildren){
					innerLocked();
					try{
						if (waitThreadMap.containsKey(pc)){
							waitThreadMap.get(pc).countDown();
							waitThreadMap.remove(pc);
						}
					}finally{
						innerUnlocked();
					}
				}
			}
		}
		@Override
		public void onParentDelete(String path) {
		}
	}

	@Override
	public int availablePermits() {
		List<String> children = client.getChildren(semaphoreHome);
		if (children==null){
			return permitsLimit;
		}
		if (children.size()>permitsLimit){
			return 0;
		}
		return permitsLimit-children.size();
	}

}
