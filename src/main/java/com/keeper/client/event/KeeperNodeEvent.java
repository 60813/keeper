package com.keeper.client.event;
/**
 *@author huangdou
 *@at 2016年11月29日下午1:53:11
 *@version 0.0.1
 */
public class KeeperNodeEvent implements KeeperEvent {

	private String path ;
	
	private byte[] data ;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	
}
