package yuku.snappydemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import yuku.snappy.codec.Snappy;

public class MainActivity extends Activity {
	static final String TAG = MainActivity.class.getSimpleName();
	
	Charset utf8 = Charset.forName("utf-8");

	String m0 = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";
	String m1 = "___HELLO___HELLO___HELLO___HELLO___HELLO___HELLO___HELLO___HELLO___HELLO___HELLO___HELLO___HELLO___HELLO___HELLO___HELLO";
	String m2 = "Snappy is a compression/decompression library. " +
			"It does not aim for maximum compression, or compatibility with any other compression library; " +
			"instead, it aims for very high speeds and reasonable compression. For instance, compared to " +
			"the fastest mode of zlib, Snappy is an order of magnitude faster for most inputs, but the " +
			"resulting compressed files are anywhere from 20% to 100% bigger. On a single core of a Core " +
			"i7 processor in 64-bit mode, Snappy compresses at about 250 MB/sec or more and decompresses " +
			"at about 500 MB/sec or more.\n" + 
			"\n" + 
			"Snappy is widely used inside Google, in everything from BigTable and MapReduce to our internal " +
			"RPC systems. (Snappy has previously been referred to as “Zippy” in some presentations and the " +
			"likes.)\n" + 
			"\n" + 
			"For more information, please see the README. Benchmarks against a few other compression " +
			"libraries (zlib, LZO, LZF, FastLZ, and QuickLZ) are included in the source code distribution. " +
			"The source code also contains a formal format specification, as well as a specification for " +
			"a framing format useful for higher-level framing and encapsulation of Snappy data, e.g. for " +
			"transporting Snappy-compressed data across HTTP in a streaming fashion. Note that there is " +
			"currently no known code implementing the latter.\n" + 
			"\n";
	
	void test1(String m_) {
		Log.d(TAG, "##################  test1  ###################");
		
		Snappy s = new Snappy.Factory().newInstance();
		
		byte[] m1 = m_.getBytes();	
		Log.d(TAG, "Message: " + m1.length + " bytes. ");

		int max_comp = s.maxCompressedLength(m1.length);
		Log.d(TAG, "- Max compressed size: " + max_comp);
		
		byte[] c = new byte[max_comp];
		int c_len = s.compress(m1, 0, c, 0, m1.length);
		Log.d(TAG, "- Compressed size: " + c_len);
		
		int decomp_size = s.uncompressedLength(c, 0, c_len);
		Log.d(TAG, "- Uncompressed preview size: " + decomp_size);
		
		byte[] m2 = new byte[decomp_size];
		int m2_len = s.decompress(c, 0, m2, 0, c_len);
		Log.d(TAG, "- Uncompressed real size: " + m2_len);
		
		if (m2_len != decomp_size) {
			throw new RuntimeException("error: previewed size and real size differs!");
		}
		
		for (int i = 0; i < m2_len; i++) {
			if (m1[i] != m2[i]) {
				throw new RuntimeException("m1 and m2 differs at offset " + i);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < c_len; i++) {
			if (c[i] >= 0x20 && c[i] < 0x80) {
				sb.append((char) (c[i] & 0xff));
			} else {
				sb.append(String.format("<%02x>", c[i]));
			}
		}
		
		Log.d(TAG, "m1 = " + m_);
		Log.d(TAG, "c  = " + sb);
		Log.d(TAG, "m2 = " + new String(m2));
	}


	void test3(int n) {
		Log.d(TAG, "##################  test3: performance n=" + n + " ###################");

		Snappy s = new Snappy.Factory().newInstance();
		
		byte[] m = new byte[3999 * n];
		for (int i = 0; i < m.length; i++) {
			m[i] = (byte) ((i+1) & 0xff);
		}
		
		Log.d(TAG, "Message is " + m.length + " bytes");
		
		int c_max = s.maxCompressedLength(m.length);
		Log.d(TAG, "- Max compressed size: " + c_max + " bytes");
		
		byte[] c = new byte[c_max];
		
		long time_start = System.currentTimeMillis();
		int c_len = s.compress(m, 0, c, 0, m.length);
		Log.d(TAG, "Time taken to compress " + m.length + " bytes: " + (System.currentTimeMillis() - time_start) + " ms");
		Log.d(TAG, "- Compressed length: " + c_len);
		
		time_start = System.currentTimeMillis();
		int m_len = s.decompress(c, 0, m, 0, c_len);
		Log.d(TAG, "Time taken to decompress again: " + (System.currentTimeMillis() - time_start) + " ms");

		if (m_len != m.length) {
			throw new RuntimeException("verify fail m_len!");
		}
		
		for (int i = 0; i < m.length; i++) {
			if (m[i] != (byte) ((i+1) & 0xff)) {
				throw new RuntimeException("verify fail m contents!");
			}
		}
	}
	
	void test5(int n) {
		Log.d(TAG, "##################  implementation comparison test  ###################");

		Snappy s1 = new Snappy.Factory().newInstanceJava();
		Snappy s2 = new Snappy.Factory().newInstanceNative();
		
        MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
        
        byte[] m = new byte[4999 * n];
        
		for (int i = 0; i < m.length; i++) {
			m[i] = (byte) ((i+3 + i/2) & 0xff);
		}
		
		
		/// benchmark start
		{
			byte[] c = new byte[s1.maxCompressedLength(m.length)];
			long time_start = System.currentTimeMillis();
			int c_len = s1.compress(m, 0, c, 0, m.length);
			md.update(c);
			Log.d(TAG, "Time taken to compress " + m.length + " bytes (Java): " + (System.currentTimeMillis() - time_start) + " ms. Compressed length: " + c_len + " Digest: " + Arrays.toString(md.digest()));
		}
		
		{
			byte[] c = new byte[s2.maxCompressedLength(m.length)];
			long time_start = System.currentTimeMillis();
			int c_len = s2.compress(m, 0, c, 0, m.length);
			md.update(c);
			Log.d(TAG, "Time taken to compress " + m.length + " bytes (Native): " + (System.currentTimeMillis() - time_start) + " ms. Compressed length: " + c_len + " Digest: " + Arrays.toString(md.digest()));
		}
	}

	void test7(int n) {
		Log.d(TAG, "##################  random data test " + n + " bytes ###################");
		
		Snappy s1 = new Snappy.Factory().newInstanceJava();
		Snappy s2 = new Snappy.Factory().newInstanceNative();
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		
		Random r = new Random();
		r.setSeed(0x92678549abcdef02L);
		
		byte[] m = new byte[n];
		r.nextBytes(m);
		
		// copy data in random so that some repetitions are there 
		for (int i = 0; i < n; i++) {
			int forward = r.nextInt(1000);
			i += forward;
			int len = r.nextInt(1000);
			int to = r.nextInt(2000);
			if (i + to + len < n) {
				System.arraycopy(m, i, m, i + to, len);
			}
		}
		
		md.update(m);
		byte[] digest = md.digest();
		
		{
			byte[] c = new byte[s1.maxCompressedLength(m.length)];
			long time_start = System.currentTimeMillis();
			int c_len = s1.compress(m, 0, c, 0, m.length);
			Log.d(TAG, "Time taken to compress " + m.length + " bytes (Java): " + (System.currentTimeMillis() - time_start) + " ms. Compressed length: " + c_len);
			Arrays.fill(m, (byte) 0);
			time_start = System.currentTimeMillis();
			s1.decompress(c, 0, m, 0, c_len);
			Log.d(TAG, "Time taken to decompress (Java): " + (System.currentTimeMillis() - time_start) + " ms.");
			md.reset();
			md.update(m);
			byte[] digest_too = md.digest();
			if (!Arrays.equals(digest, digest_too)) throw new RuntimeException("data corrupt on java");
		}
		
		{
			byte[] c = new byte[s2.maxCompressedLength(m.length)];
			long time_start = System.currentTimeMillis();
			int c_len = s2.compress(m, 0, c, 0, m.length);
			Log.d(TAG, "Time taken to compress " + m.length + " bytes (Native): " + (System.currentTimeMillis() - time_start) + " ms. Compressed length: " + c_len);
			Arrays.fill(m, (byte) 0);
			time_start = System.currentTimeMillis();
			s2.decompress(c, 0, m, 0, c_len);
			Log.d(TAG, "Time taken to decompress (Native): " + (System.currentTimeMillis() - time_start) + " ms.");
			md.reset();
			md.update(m);
			byte[] digest_too = md.digest();
			if (!Arrays.equals(digest, digest_too)) throw new RuntimeException("data corrupt on native");
		}
		
		try { // comparison gzip and snappy
			ByteArrayOutputStream gzip_out = new ByteArrayOutputStream(m.length);
			ByteArrayInputStream gzip_in;
			byte[] c = new byte[m.length];
			int c_len;
			byte[] m2 = new byte[m.length];
			Snappy s = new Snappy.Factory().newInstance();
			for (int i = 0; i < 10; i++) {
				gzip_out.reset();
				
				{ // compress gzip
					long start = System.currentTimeMillis();
					GZIPOutputStream g = new GZIPOutputStream(gzip_out);
					g.write(m, 0, m.length);
					g.close();
					long now = System.currentTimeMillis();
					Log.d(TAG, "gzip    original=" + m.length + " compressed=" + gzip_out.size() + " time=" + (now - start));
				}
				
				{ // compress snappy
					long start = System.currentTimeMillis();
					c_len = s.compress(m, 0, c, 0, m.length);
					long now = System.currentTimeMillis();
					Log.d(TAG, "snappy  original=" + m.length + " compressed=" + c_len + " time=" + (now - start));
				}
				
				gzip_in = new ByteArrayInputStream(gzip_out.toByteArray());
				
				{ // uncompress gzip
					long start = System.currentTimeMillis();
					GZIPInputStream g = new GZIPInputStream(gzip_in);
					int m2_len = 0;
					while (m2_len < m.length) {
						int read = g.read(m2, m2_len, m2.length - m2_len);
						if (read < 0) break;
						m2_len += read;
					}
					g.close();
					long now = System.currentTimeMillis();
					Log.d(TAG, "ungzip    original=" + gzip_out.size() + " uncompressed=" + m2_len + " time=" + (now - start));
				}
				
				{ // uncompress snappy
					long start = System.currentTimeMillis();
					int m2_len = s.decompress(c, 0, m2, 0, c_len);
					long now = System.currentTimeMillis();
					Log.d(TAG, "unsnappy  original=" + c_len + " uncompressed=" + m2_len + " time=" + (now - start));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void test8() { // mis-alignment test
		for (int sft = 0; sft < 8; sft++) {
			Snappy s = new Snappy.Factory().newInstance();
			byte[] m1 = "987979876876qwertyuiopasdfghjklzxcvbnm,..;'/[]=-QWERTYUIOPLKJHGFDSAZXCVBNM<>?}}7654uytr7654uytr7564uytriuyiuyiuy876876876".getBytes();
	
			int c_max = s.maxCompressedLength(m1.length - sft);
			byte[] c = new byte[c_max + 24];
			int c_len = s.compress(m1, sft, c, sft * 3, m1.length - sft);
			Log.d(TAG, "- Uncompressed size: " + (m1.length - sft));
			Log.d(TAG, "- Compressed size: " + c_len);
			
			int m2_max = s.uncompressedLength(c, sft * 3, c_len);
			byte[] m2 = new byte[m2_max + 8];
			int m2_len = s.decompress(c, sft * 3, m2, sft, c_len);
			Log.d(TAG, "- Uncompressed real size: " + m2_len);
			
			for (int i = 0; i < m2_len; i++) {
				if (m1[i + sft] != m2[i + sft]) {
					throw new RuntimeException("m1 and m2 differs at offset " + i);
				}
			}
		}
	}
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		test1(m0);
		test1(m1);
		test1(m2);

		test3(1251);
		test3(1251);
		test3(125);
		
		test5(101);
		test5(11);
		
		test7(10000000);
		test7(1000000);
		test7(100000);
		test7(10000);
		test7(1000);
		
		test8();
	}
}
