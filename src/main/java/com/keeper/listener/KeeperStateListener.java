package com.keeper.listener;

import com.keeper.event.KeeperStateEvent;

/**
 *@author huangdou
 *@at 2016年11月29日下午1:51:09
 *@version 0.0.1
 */
public interface KeeperStateListener {

	public void onEvent(KeeperStateEvent event);

}
