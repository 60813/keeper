package com.keeper.client.listener;

import com.keeper.client.event.KeeperStateEvent;

/**
 *@author huangdou
 *@at 2016年11月29日下午1:51:09
 *@version 0.0.1
 */
public interface KeeperStateListener {

	public void onEvent(KeeperStateEvent event);

}
