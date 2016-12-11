package com.keeper.expansion;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.keeper.client.KeeperClient;
import com.keeper.expansion.locks.KeeperLock;
import com.keeper.expansion.locks.KeeperMutexLock;

/**
 *@author huangdou
 *@at 2016年12月10日下午11:25:52
 *@version 0.0.1
 */
public class TestClientLock {

	static KeeperClient client ;
	@Before
	public void before(){
		client = new KeeperClient("127.0.0.1:2181");
	}
	
	@After
	public void after(){
		if (client != null){
			client.closeClient();
			client = null;
		}
	}
	@Test
	public void testLock() {
		final KeeperLock lock1 = new KeeperMutexLock("testlocka", client);
		final KeeperLock lock2 = new KeeperMutexLock("testlocka2", client);
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				final KeeperLock lock = new KeeperMutexLock("testlocka", client);
				lock.lock();
				System.out.println("locked 11");
				try {
					Thread.sleep(10000);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					lock.unlock();
				}
			}
		}).start();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				final KeeperLock lock = new KeeperMutexLock("testlocka", client);
				lock.lock();
				System.out.println("locked 22");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					lock.unlock();
				}
			}
		}).start();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				lock1.lock();
				System.out.println("locked 33");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					lock1.unlock();
				}
			}
		}).start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				final KeeperLock lock = new KeeperMutexLock("testlocka2", client);
				lock.lock();
				System.out.println("locked 111");
				try {
					Thread.sleep(10000);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					lock.unlock();
				}
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				final KeeperLock lock = new KeeperMutexLock("testlocka2", client);
				lock.lock();
				System.out.println("locked 222");
				try {
					Thread.sleep(10000);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					lock.unlock();
				}
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				lock2.lock();
				System.out.println("locked 333");
				try {
					Thread.sleep(10000);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					lock2.unlock();
				}
			}
		}).start();
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
