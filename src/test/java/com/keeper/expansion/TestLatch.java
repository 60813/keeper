package com.keeper.expansion;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;

import com.keeper.client.KeeperClient;
import com.keeper.expansion.countdownlatch.KeeperCountDownLatch;
import com.keeper.expansion.semaphore.SemaphoreException;

/**
 *@author huangdou
 *@at 2016年12月22日下午2:11:39
 *@version 0.0.1
 */
public class TestLatch {
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
	
	class MyThread extends Thread{
		private KeeperCountDownLatch keeperCountDownLatch ;
		
		boolean waitUtilCountDown ;
		
		private int waitMilliseconds ;
		
		public void setWaitMilliseconds(int waitMilliseconds) {
			this.waitMilliseconds = waitMilliseconds;
		}

		public MyThread(KeeperCountDownLatch keeperCountDownLatch,String name,boolean waitUtilCountDown) {
			this.keeperCountDownLatch = keeperCountDownLatch;
			this.waitUtilCountDown = waitUtilCountDown ;
			this.setName(name);
		}

		@Override
		public void run() {
			try {
				if (waitUtilCountDown){
					keeperCountDownLatch.await();
					System.out.println(Thread.currentThread().getName() + " return util countdown " + new Date().toLocaleString());
				}else {
					if (!keeperCountDownLatch.await(waitMilliseconds,TimeUnit.MILLISECONDS)){
						System.out.println(Thread.currentThread().getName() + " returnd false util the specified waiting time elapses at " + new Date().toLocaleString());
					}else {
						System.out.println(Thread.currentThread().getName() + " returnd true util countdown " + new Date().toLocaleString());
					}
				}
				
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
	}
	
//	@Test
	public void testLock() throws InterruptedException, SemaphoreException {
		KeeperCountDownLatch latch = KeeperCountDownLatch.getOrCreate("latch02", 2, client) ;
		//一直等到countdown到0的线程1
		MyThread t1 = new MyThread(latch, "waitCountDownThread1", true);
		//指定较长时间，时间未到而countdown变为0，最终返回true的线程
		MyThread t2 = new MyThread(latch, "waitTimeDownThreadTrue", false);
		t2.setWaitMilliseconds(10000);
		//指定较短时间，时间到而countdown未变为0，最终返回false的线程
		MyThread t3 = new MyThread(latch, "waitTimeDownThreadFalse", false);
		t3.setWaitMilliseconds(3000);
		//一直等到countdown到0的线程2
		MyThread t4 = new MyThread(latch, "waitCountDownThread2", true);
		
		t1.start();t2.start();t3.start();t4.start();
		Thread.sleep(5000);
		latch.countDown();
		System.out.println("count down 1");
		latch.countDown();
		System.out.println("count down 2");
		Thread.sleep(5000l);
	}

}
