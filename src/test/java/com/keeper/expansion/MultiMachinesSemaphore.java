package com.keeper.expansion;

import java.util.Date;
import java.util.Random;

import com.keeper.client.KeeperClient;
import com.keeper.expansion.semaphore.KeeperSemaphore;
import com.keeper.expansion.semaphore.KeeperSimpleSemaphore;
import com.keeper.expansion.semaphore.SemaphoreException;

/**
 *@author huangdou
 *@at 2016年12月19日下午5:31:55
 *@version 0.0.1
 */
public class MultiMachinesSemaphore {

	final String path = "/semaphoreTest02";
//	@Test
	public void machine1() throws SemaphoreException, InterruptedException{
		KeeperClient client = new KeeperClient("127.0.0.1:2181");
		KeeperSemaphore semaphore = KeeperSimpleSemaphore.getOrCreate("testSemaphore11", 3, client);
		if (!client.exist(path)){
			client.create(path, "".getBytes());
		}
		for (int i=0;i<3;i++){
			MyThread mt = new MyThread("machine1-"+i, semaphore,client);
			mt.start();
		}
		Thread.sleep(100000);
	}
	
//	@Test
	public void machine2() throws SemaphoreException, InterruptedException{
		KeeperClient client = new KeeperClient("127.0.0.1:2181");
		KeeperSemaphore semaphore = KeeperSimpleSemaphore.getOrCreate("testSemaphore11", 3, client);
		for (int i=0;i<3;i++){
			MyThread mt = new MyThread("machine2-"+i, semaphore,client);
			mt.start();
		}
		Thread.sleep(100000);
	}
	
//	@Test
	public void machine3() throws SemaphoreException, InterruptedException{
		KeeperClient client = new KeeperClient("127.0.0.1:2181");
		KeeperSemaphore semaphore = KeeperSimpleSemaphore.getOrCreate("testSemaphore11", 3, client);
		for (int i=0;i<3;i++){
			MyThread mt = new MyThread("machine3-"+i, semaphore,client);
			mt.start();
		}
		Thread.sleep(100000);
	}
	
	class MyThread extends Thread{

		KeeperSemaphore semaphore ;
		
		KeeperClient client ;
		public MyThread(String name, KeeperSemaphore semaphore,KeeperClient client) {
			super(name);
			this.semaphore = semaphore;
			this.client = client ;
		}

		
		@Override
		public void run() {
			try {
				for (int i=0;i<10;i++){
					semaphore.acquire();
					System.out.println(Thread.currentThread().getName() + " got 1" + new Date().toLocaleString());
					Thread.sleep(new Random().nextInt(10000));
					semaphore.release();
					System.out.println(Thread.currentThread().getName() + " rls 1"+ new Date().toLocaleString());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	} 

}
