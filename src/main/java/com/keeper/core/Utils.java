package com.keeper.core;

/**
 * @author huangdou
 * @at 2016年12月23日上午8:42:25
 * @version 0.0.1
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

	public static String validatePath(String path)
			throws IllegalArgumentException {
		if (path == null) {
			throw new IllegalArgumentException("Path cannot be null");
		}
		if (path.length() == 0) {
			throw new IllegalArgumentException("Path length must be > 0");
		}
		if (path.charAt(0) != '/') {
			throw new IllegalArgumentException(
					"Path must start with / character");
		}
		if (path.length() == 1) { // done checking - it's the root
			return path;
		}
		if (path.charAt(path.length() - 1) == '/') {
			throw new IllegalArgumentException(
					"Path must not end with / character");
		}

		String reason = null;
		char lastc = '/';
		char chars[] = path.toCharArray();
		char c;
		for (int i = 1; i < chars.length; lastc = chars[i], i++) {
			c = chars[i];

			if (c == 0) {
				reason = "null character not allowed @" + i;
				break;
			} else if (c == '/' && lastc == '/') {
				reason = "empty node name specified @" + i;
				break;
			} else if (c == '.' && lastc == '.') {
				if (chars[i - 2] == '/'
						&& ((i + 1 == chars.length) || chars[i + 1] == '/')) {
					reason = "relative paths not allowed @" + i;
					break;
				}
			} else if (c == '.') {
				if (chars[i - 1] == '/'
						&& ((i + 1 == chars.length) || chars[i + 1] == '/')) {
					reason = "relative paths not allowed @" + i;
					break;
				}
			} else if (c > '\u0000' && c < '\u001f' || c > '\u007f'
					&& c < '\u009F' || c > '\ud800' && c < '\uf8ff'
					|| c > '\ufff0' && c < '\uffff') {
				reason = "invalid charater @" + i;
				break;
			}
		}

		if (reason != null) {
			throw new IllegalArgumentException("Invalid path string \"" + path
					+ "\" caused by " + reason);
		}

		return path;
	}

}
