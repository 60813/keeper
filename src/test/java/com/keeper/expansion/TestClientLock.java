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
			client.closeConnection();
			client = null;
		}
	}
	@Test
	public void testLock() {
		final KeeperLock lock = new KeeperMutexLock("testlocka", client);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
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
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
