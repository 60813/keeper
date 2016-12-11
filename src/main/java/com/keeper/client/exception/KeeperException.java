package com.keeper.client.exception;
/**
 *@author huangdou
 *@at 2016年11月28日下午3:18:34
 *@version 0.0.1
 */
public class KeeperException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8974884702080322436L;

	private Throwable proto;
	public Throwable getProto(){
		return proto ;
	}
	public KeeperException() {
		// TODO Auto-generated constructor stub
	}

	public KeeperException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public KeeperException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public KeeperException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public KeeperException(Throwable cause) {
		super(cause);
		this.proto = cause ;
		// TODO Auto-generated constructor stub
	}
	
	

}
