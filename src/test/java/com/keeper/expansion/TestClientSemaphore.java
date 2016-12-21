package com.keeper.expansion;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.keeper.client.KeeperClient;
import com.keeper.expansion.semaphore.KeeperSemaphore;
import com.keeper.expansion.semaphore.KeeperSimpleSemaphore;
import com.keeper.expansion.semaphore.SemaphoreException;

/**
 *@author huangdou
 *@at 2016年12月19日上午10:39:45
 *@version 0.0.1
 */
public class TestClientSemaphore {

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
	public void testSemaphore() throws SemaphoreException, InterruptedException{
		KeeperSemaphore semaphore = KeeperSimpleSemaphore.getOrCreate("semaphore2", 3, client);
		for (int i=1;i<=16;i++){
			Thread myThread = new MyThread(i+"", semaphore);
			myThread.start();
		}
		Thread.sleep(100000);
	}
	
	class MyThread extends Thread{

		String name ;
		KeeperSemaphore semaphore ;
		
		public MyThread(String name, KeeperSemaphore semaphore) {
			super();
			this.name = name;
			this.semaphore = semaphore;
		}

		
		@Override
		public void run() {
			try {
				semaphore.acquire();
				System.out.println(Thread.currentThread().getName() + " got a release ,availablePermits is " +semaphore.availablePermits());
				Thread.sleep(new Random().nextInt(1000));
				semaphore.release();
				System.out.println(Thread.currentThread().getName() + " returnd a release ,availablePermits is " +semaphore.availablePermits());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	} 

}
