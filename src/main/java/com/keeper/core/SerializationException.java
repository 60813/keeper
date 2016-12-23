package com.keeper.core;
/**
 *@author huangdou
 *@at 2016年12月23日上午8:20:34
 *@version 0.0.1
 */
public class SerializationException extends RuntimeException {

	private static final long serialVersionUID = -895333630203716610L;
	
	public SerializationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SerializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializationException(String message) {
		super(message);
	}

	public SerializationException(Throwable cause) {
		super(cause);
	}

	public SerializationException() {
	}

}
