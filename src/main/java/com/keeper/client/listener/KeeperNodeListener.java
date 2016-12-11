package com.keeper.client.listener;


/**
 *@author huangdou
 *@at 2016年11月30日上午8:38:43
 *@version 0.0.1
 */
public interface KeeperNodeListener {
	public void onData(String path,byte[] bytes);
	
	public void onDelete(String path);
}
