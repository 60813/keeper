package com.keeper.listener;

import java.util.List;

/**
 *@author huangdou
 *@at 2016年11月30日上午8:39:02
 *@version 0.0.1
 */
public interface KeeperChildListener {
	public void onChild(String parent,List<String> subs) ;
	
	public void onParentDelete(String path);
}
