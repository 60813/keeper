package com.keeper.core;
/**
 *@author huangdou
 *@at 2016年12月23日上午8:42:25
 *@version 0.0.1
 */
public class Utils {

	 @SuppressWarnings("unchecked")
	    public static <E> E getObject(String className) {
	        if (className == null) {
	            return (E) null;
	        }
	        try {
	            return (E) Class.forName(className).newInstance();
	        } catch (InstantiationException e) {
	            throw new RuntimeException(e);
	        } catch (IllegalAccessException e) {
	            throw new RuntimeException(e);
	        } catch (ClassNotFoundException e) {
	            throw new RuntimeException(e);
	        }
	    }

}
