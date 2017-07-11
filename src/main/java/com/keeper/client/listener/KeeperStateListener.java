package com.keeper.client.listener;

import org.apache.zookeeper.Watcher;

/**
 *@author huangdou
 *@at 2016年11月29日下午1:51:09
 *@version 0.0.1
 */
public interface KeeperStateListener {

	public void onEvent(Watcher.Event.KeeperState event);

}
