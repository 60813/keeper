package com.keeper.expansion.countdownlatch;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import com.keeper.client.KeeperClient;
import com.keeper.client.exception.KeeperException;
import com.keeper.client.listener.KeeperNodeListener;
import com.keeper.expansion.locks.KeeperLock;
import com.keeper.expansion.locks.KeeperMutexLock;
import com.keeper.expansion.semaphore.SemaphoreException;

/**
 * @author huangdou
 * @at 2016年12月5日上午10:28:39
 * @version 0.0.1
 */
public class KeeperCountDownLatch {
	public static final String LATCH_ROOT = "/latch_root";
	private String name;

	private KeeperClient client;

	private String latchPath;

	private KeeperLock latchLock;

	private Set<Semaphore> waitSemaphore = new CopyOnWriteArraySet<Semaphore>();

	public KeeperCountDownLatch(String name, KeeperClient client, int count) {
		this.name = name;
		this.client = client;
		init(count);
	}

	public static KeeperCountDownLatch getOrCreate(String name,
			int createCount, KeeperClient client) throws SemaphoreException {
		if (name == null || "".equals(name.trim()) || name.contains("/")) {
			throw new IllegalArgumentException(String.format(
					"name can not be %s", name));
		}
		if (createCount < 0) {
			throw new IllegalArgumentException(
					String.format("count can not be less than 0"));
		}

		return new KeeperCountDownLatch(name, client, createCount);
	}

	private void init(int count) {
		if (name == null || "".equals(name.trim()) || name.contains("/")) {
			throw new IllegalArgumentException(String.format(
					"path can not be %s", name));
		}
		latchLock = new KeeperMutexLock((LATCH_ROOT + "_" + name).replace("/",
				""), client);
		latchPath = LATCH_ROOT + "/" + name;
		try {
			latchLock.lock();
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
		try {
			if (!client.exist(latchPath)) {
				try {
					client.createWtihParent(latchPath);
					client.update(latchPath, (count + "").getBytes());
				} catch (KeeperException e) {
					if (!(e.getProto() instanceof NodeExistsException)) {
						throw e;
					}
				}
			}
		} finally {
			latchLock.unlock();
		}
		client.listenNode(latchPath, new LatchListener());
	}

	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		Semaphore semaphore = new Semaphore(0);
		latchLock.lock();
		try {
			int count = Integer.parseInt(new String(client.read(latchPath)));
			if (count == 0) {
				return true;
			} else {
				waitSemaphore.add(semaphore);
			}
		} catch (KeeperException e) {
			if (!(e.getProto() instanceof NoNodeException)) {
				throw new RuntimeException("Latch Has been destroyed!");
			}
		} finally {
			latchLock.unlock();
		}
		
		boolean isLocked = false ;
		if (timeout > 0){
			isLocked = semaphore.tryAcquire(timeout, unit);
		}else {
			isLocked = semaphore.tryAcquire();
		}
		return isLocked;
	}

	public void await() throws InterruptedException {
		Semaphore semaphore = new Semaphore(0);
		latchLock.lock();
		try {
			int count = Integer.parseInt(new String(client.read(latchPath)));
			if (count == 0) {
				return;
			} else {
				waitSemaphore.add(semaphore);
			}
		} catch (KeeperException e) {
			if (!(e.getProto() instanceof NoNodeException)) {
				throw new RuntimeException("Latch Has been destroyed!");
			}
		} finally {
			latchLock.unlock();
		}
		semaphore.acquire();
	}

	public void destroy() {
		try {
			latchLock.lock();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		try {
			client.delete(latchPath);
		} catch (KeeperException e) {
			if (!(e.getProto() instanceof NoNodeException)) {
				throw new RuntimeException("Latch Has been destroyed!");
			}
		} finally {
			latchLock.unlock();
		}

	}

	public void countDown() {
		try {
			latchLock.lock();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		try {
			int count = Integer.parseInt(new String(client.read(latchPath)));
			if (count > 0) {
				client.update(latchPath, ((count--) + "").getBytes());
			}
		} catch (KeeperException e) {
			if (!(e.getProto() instanceof NoNodeException)) {
				throw new RuntimeException("Latch Has been destroyed!");
			}
		} finally {
			latchLock.unlock();
		}
	}

	public long getCount() {
		try {
			return Integer.parseInt(new String(client.read(latchPath)));
		} catch (KeeperException e) {
			if (!(e.getProto() instanceof NoNodeException)) {
				throw new RuntimeException("Latch Has been destroyed!");
			}
			throw e ;
		} 
	}

	class LatchListener implements KeeperNodeListener {
		@Override
		public void onData(String path, byte[] bytes) {
			if (bytes != null && Integer.parseInt(new String(bytes)) == 0) {
				for (Semaphore semaphore : waitSemaphore) {
					semaphore.release();
				}
			}
		}
		@Override
		public void onDelete(String path) {
			for (Semaphore semaphore : waitSemaphore){
				semaphore.release();
			}
		}

	}

}
