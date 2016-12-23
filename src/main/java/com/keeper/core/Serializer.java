package com.keeper.core;
/**
 *@author huangdou
 *@at 2016年12月23日上午8:09:05
 *@version 0.0.1
 */
public interface Serializer<T> {
	byte[] serialize(T t);
	
	T deserialize(byte[] bs,Class<T> c);
}
