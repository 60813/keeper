package com.keeper.client;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.keeper.client.KeeperClient;
import com.keeper.client.listener.KeeperChildListener;
import com.keeper.client.listener.KeeperNodeListener;
import com.keeper.server.KeeperSimpleServer;

/**
 *@author huangdou
 *@at 2016年12月3日下午2:36:58
 *@version 0.0.1
 */
public class TestClientListen {

	static KeeperClient client ;
	
	static String testPath =  "/test1";
	static String testData  = "hello world"; 
	
	
	@BeforeClass
	public static void createServer(){
		KeeperSimpleServer server = new KeeperSimpleServer("d:\\zktmp\\snap", "d:\\zktmp\\datalog");
		server.startZkServer();
	}
	
	@AfterClass
	public static void closeClient(){
		
	}
	
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
	public void testNodeListen() throws InterruptedException{
		final AtomicInteger onDataCall = new AtomicInteger();
		final AtomicInteger onDeleteCall = new AtomicInteger();
		client.listenNode(testPath, new KeeperNodeListener() {
			public void onDelete(String path) {
				System.out.println(testPath + " is deleted");
				onDeleteCall.addAndGet(1);
			}
			
			public void onData(String path, byte[] bytes) {
				System.out.println(testPath + " is created or updated with the data : " + new String(bytes));
				onDataCall.addAndGet(1);
			};
		});
		
		client.create(testPath, testData.getBytes());
		Thread.sleep(100);
		client.update(testPath, (testData+"xx").getBytes());
		Thread.sleep(100);
		client.delete(testPath);
		Thread.sleep(100);
		Assert.assertEquals(onDataCall.get(), 2);
		Assert.assertEquals(onDeleteCall.get(), 1);
	}
	
	@Test
	public void testChildListen() throws InterruptedException{
		final AtomicInteger onChildCall = new AtomicInteger();
		client.listenChild(testPath, new KeeperChildListener() {
			
			public void onParentDelete(String path) {
				System.out.println("parent " + path +" was been deleted!");
			}
			
			public void onChild(String parent, List<String> subs) {
				System.out.println("child added or removed : " + subs);
				onChildCall.addAndGet(1);
			}
		});
		
		client.delete(testPath);
		Thread.sleep(1000);
		client.create(testPath, testData.getBytes());
		Thread.sleep(1000);
		client.create(testPath+"/"+"A", testData.getBytes());
		Thread.sleep(1000);
		client.create(testPath+"/"+"B", testData.getBytes());
		Thread.sleep(1000);
		client.delete(testPath+"/"+"B");
		Thread.sleep(1000);
		
		client.deleteRecurse(testPath);
		Thread.sleep(1000);
	}
}
