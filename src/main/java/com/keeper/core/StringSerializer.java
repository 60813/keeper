package com.keeper.core;

import java.io.UnsupportedEncodingException;

/**
 *@author huangdou
 *@at 2016年12月23日上午8:16:26
 *@version 0.0.1
 */
public class StringSerializer implements Serializer<String> {
	String encoding = "UTF8";
	
	private static StringSerializer inner = new StringSerializer() ;
	
	private StringSerializer(){
		
	}
	
	public static StringSerializer getStringSerializer(){
		return inner;
	}

	@Override
	public byte[] serialize(String t) {
		if (t == null){
			return null;
		}
		try {
			return t.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			throw new SerializationException("UnsupportedEncodingException : " + t);
		}
	}

	@Override
	public String deserialize(byte[] bs, Class<String> c) {
		if (bs == null){
			return null;
		}
		try {
			return new String(bs,encoding);
		} catch (UnsupportedEncodingException e) {
			throw new SerializationException("UnsupportedEncodingException : " + bs);
		}
	}

	

}
