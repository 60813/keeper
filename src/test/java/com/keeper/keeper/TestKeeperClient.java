package com.keeper.keeper;



import org.junit.Assert;
import org.junit.Test;

import com.keeper.KeeperClient;

/**
 *@author huangdou
 *@at 2016年12月2日上午10:43:36
 *@version 0.0.1
 */
public class TestKeeperClient {

	@Test
	public void test() {
		KeeperClient client = new KeeperClient("127.0.0.1:2183");
		client.create("/momoda", "abc".getBytes());
		
		Assert.assertTrue(client.exist("/momoda"));;
		
		byte[] bytes = client.read("/momoda");
		Assert.assertTrue(new String(bytes).equals("abc"));;
	}

}
