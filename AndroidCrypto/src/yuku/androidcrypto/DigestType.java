package yuku.androidcrypto;

import java.security.*;

import android.util.Log;

public enum DigestType {
	MD5("MD5"),
	SHA128("SHA1"),
	SHA256("SHA256"),
	SHA512("SHA512");
	
	private static final String TAG = MessageDigest.class.getSimpleName();
	private final String algo;
	
	DigestType(String algo) {
		this.algo = algo;
	}
	
	public MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance(algo);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "NoSuchAlgorithmException: " + algo, e);
			return null;
		}
	}
}
