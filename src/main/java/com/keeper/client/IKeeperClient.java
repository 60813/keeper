package com.keeper.client;

import java.util.Comparator;
import java.util.List;

import org.apache.zookeeper.CreateMode;

import com.keeper.client.listener.KeeperChildListener;
import com.keeper.client.listener.KeeperNodeListener;
import com.keeper.client.listener.KeeperStateListener;

/**
 *@author huangdou
 *@at 2016年11月29日上午8:32:58
 *@version 0.0.1
 */
public interface IKeeperClient {

	String DEFAULT_CONNECTION_STRING = "127.0.0.1:2181";
	
	int DEFAULT_SESSION_TIMEOUT = 50000;
	
	int DEFAULT_CONNECT_TIMEOUT = 10000;
	
	int DEFAULT_CONCURRENT_PROCESS = 3;
	
	boolean exist(String path);
	
	String create(String path,byte[] bytes);
	
	String create(String path, byte[] bytes,CreateMode createMode);
	
	String createWtihParent(String path);
	
	String createSequential(String path, byte[] bytes);
	
	byte[] read(String path);
	
	void update(String path,byte[] bytes);
	
	boolean delete(String path);
	
	boolean deleteRecurse(String path);
	
	List<String> getChildren(String parent);
	
	List<String> getSortedChildren(String parent) ;
	
	List<String> getSortedChildren(String parent,Comparator<String> comparator);
	
	void listenNode(String path,KeeperNodeListener keeperNodeListener);
	
	void listenChild(String path,KeeperChildListener keeperChildListener);
	
	void listenState(KeeperStateListener keeperStateListener);
	
	
}
