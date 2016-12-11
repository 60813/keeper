package com.keeper.client;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.keeper.client.KeeperClient;

/**
 *@author huangdou
 *@at 2016年12月3日下午2:36:58
 *@version 0.0.1
 */
public class TestClientCRUD {

	static KeeperClient client ;
	
	static String testPath =  "/test1";
	static String testData  = "hello world";
	
	
	@BeforeClass
	public static void createClient(){
		System.out.println("before");
		client = new KeeperClient("127.0.0.1:2181");
	}
	
	@AfterClass
	public static void closeClient(){
		if (client != null){
			client.closeClient();
			client = null;
		}
	}
	
	@Before
	public void before(){
		if (client.exist(testPath)){
			client.deleteRecurse(testPath);
		}
	}
	
	@After
	public void after(){
		if (client.exist(testPath)){
			client.deleteRecurse(testPath);
		}
	}
	
	@Test
	public void testCreate(){
		client.create(testPath, testData.getBytes());
		Assert.assertTrue(testData .equals( new String(client.read(testPath))));
	}
	
	@Test
	public void testRead(){
		client.create(testPath, testData.getBytes());
		Assert.assertTrue(testData .equals( new String(client.read(testPath))));
	}
	
	@Test
	public void testGetChildren(){
		client.createWtihParent("/A/B/C");
		List<String> children = client.getChildren("/A");
		Assert.assertArrayEquals(children.toArray(), new Object[]{"B"});
	}
	
	@Test
	public void testUpdate(){
		client.create(testPath, testData.getBytes());
		client.update(testPath, "A".getBytes());
		Assert.assertEquals("A", new String(client.read(testPath)));
	}
	
	@Test
	public void testDelete(){
		client.create(testPath, testData.getBytes());
		client.delete(testPath);
		Assert.assertTrue(!client.exist(testPath));
	}
	
}
