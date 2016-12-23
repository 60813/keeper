package com.keeper.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author huangdou
 * @at 2016年12月23日上午9:10:06
 * @version 0.0.1
 */
public class ObjectSerializer<T> implements Serializer<T> {

	@Override
	public byte[] serialize(T t) {
		byte[] bytes = null;
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(t);
			bytes = bo.toByteArray();
			bo.close();
			oo.close();
		} catch (Exception e) {
			throw new SerializationException(e);
		}
		return bytes;
	}

	@Override
	public T deserialize(byte[] bs, Class<T> c) {
		Object obj = null;
		try {
			ByteArrayInputStream bi = new ByteArrayInputStream(bs);
			ObjectInputStream oi = new ObjectInputStream(bi);
			obj = oi.readObject();
			bi.close();
			oi.close();
		} catch (Exception e) {
			throw new SerializationException(e);
		}
		return c.cast(obj);
	}

}
