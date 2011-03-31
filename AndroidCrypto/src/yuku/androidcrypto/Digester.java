
package yuku.androidcrypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class Digester {
	public static byte[] digest(DigestType type, byte[] data) {
		MessageDigest md = type.getMessageDigest();
		md.update(data);
		return md.digest();
	}
	
	/**
	 * String encoded in utf8 first
	 */
	public static byte[] digest(DigestType type, String data) {
		return digest(type, utf8Encode(data));
	}
	
	public static byte[] utf8Encode(String s) {
		try {
			return s.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public static String toHex(byte[] a) {
		char[] d = new char[a.length * 2];
		int pos = 0;
		
		for (byte b: a) {
			int h = (b & 0xf0) >> 4;
			int l = b & 0x0f;
			d[pos++] = (char) (h < 10? ('0' + h): ('a' + h - 10)); 
			d[pos++] = (char) (l < 10? ('0' + l): ('a' + l - 10)); 
		}
		
		return new String(d);
	}
}
