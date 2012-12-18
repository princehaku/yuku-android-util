package yuku.salsa20demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.Salsa20Engine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import yuku.salsa20.cipher.Salsa20;
import yuku.salsa20.cipher.Salsa20InputStream;
import yuku.salsa20.cipher.Salsa20OutputStream;


public class MainActivity extends Activity {
	static final String TAG = MainActivity.class.getSimpleName();
	
	Charset utf8 = Charset.forName("utf-8");
	
	byte[] key1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, };
	byte[] key2 = "key1key2key3key4".getBytes();
	byte[] nonce1 = {0, 0, 0, 0, 0, 0, 0, 0, };
	byte[] nonce2 = {1, 2, 3, 4, 5, (byte) 0xdd, (byte) 0xee, (byte) 0xff };
	String m1 = "________________________________________________________________________________________________________";
	String m2 = "Salsa20 is a family of 256-bit stream ciphers designed in 2005 " + 
			"and submitted to eSTREAM, the ECRYPT Stream Cipher Project. " + 
			"Salsa20 has progressed to the third round of eSTREAM without any " + 
			"changes. The 20-round stream cipher Salsa20/20 is consistently faster " + 
			"than AES and is recommended by the designer for typical cryptographic " + 
			"applications. The reduced-round ciphers Salsa20/12 and Salsa20/8 are " + 
			"among the fastest 256-bit stream ciphers available and are recommended " + 
			"for applications where speed is more important than confidence. The " + 
			"fastest known attacks use ~ 2^153 " + 
			"simple operations against Salsa20/7, ~ 2^249 " + 
			"simple operations against Salsa20/8, and ~ 2^255 " + 
			"simple operations " + 
			"against Salsa20/9, Salsa20/10, etc. In this paper, the Salsa20 designer " + 
			"presents Salsa20 and discusses the decisions made in the Salsa20 design.";
	
	void test1(byte[] key, byte[] nonce, String m_) {
		Log.d(TAG, "##################  test  ###################");
		
		Salsa20 s = new Salsa20(key, nonce);
		byte[] m1 = m_.getBytes();
		byte[] c = new byte[m1.length];
		s.crypt(m1, 0, c, 0, m1.length);
		s.setPosition(0);
		byte[] m2 = new byte[m1.length];
		s.crypt(c, 0, m2, 0, c.length);
		
		// seek to 2/3 of stream
		int trySeek = m1.length * 2/3;
		s.setPosition(trySeek);
		byte[] m_seek = new byte[m1.length - trySeek];
		s.crypt(c, trySeek, m_seek, 0, m1.length - trySeek);
		
		StringBuilder sb = new StringBuilder();
		for (byte b: c) {
			sb.append(String.format("%02x", b));
		}
		
		Log.d(TAG, "m1 = " + m_);
		Log.d(TAG, "c  = " + sb);
		Log.d(TAG, "m2 = " + new String(m2));
		Log.d(TAG, "m_seek = " + new String(m_seek));
	}
	
	void test1bc(byte[] key, byte[] nonce, String m_) {
		Log.d(TAG, "##################  test with BC ###################");
		
		KeyParameter keyparam = new KeyParameter(key);
		ParametersWithIV params = new ParametersWithIV(keyparam, nonce);

        StreamCipher s = new Salsa20Engine();
        s.init(true, params);

		byte[] m1 = m_.getBytes();
		byte[] c = new byte[m1.length];
		s.processBytes(m1, 0, m1.length, c, 0);
		s.reset();
		byte[] m2 = new byte[m1.length];
		s.processBytes(c, 0, c.length, m2, 0);
		
		StringBuilder sb = new StringBuilder();
		for (byte b: c) {
			sb.append(String.format("%02x", b));
		}
		
		Log.d(TAG, "m1 = " + m_);
		Log.d(TAG, "c  = " + sb);
		Log.d(TAG, "m2 = " + new String(m2));
	}
	
	void test2() {
		Salsa20 s = new Salsa20(key2, nonce2);
		
		byte[] m = {0};
		byte[] c = {0};
		
		for (int i = 0; i < 200; i++) {
			Log.d(TAG, "position " + i + " == " + s.getPosition());
			s.crypt(m, 0, c, 0, 1);
		}
		
		s.setPosition(63);

		for (int i = 0; i < 200; i++) {
			Log.d(TAG, "position " + (i+63) + " == " + s.getPosition());
			s.crypt(m, 0, c, 0, 1);
		}
	}
	
	void test3(int n) {
		test3(n, 20);
	}
	
	void test3(int n, int rounds) {
		Log.d(TAG, "##################  performance test  ###################");

		Salsa20 s = new Salsa20(key2, nonce2, rounds);
		
		byte[] m = new byte[3999 * n];
		for (int i = 0; i < m.length; i++) {
			m[i] = (byte) ((i+1) & 0xff);
		}
		byte[] c = new byte[m.length];
		
		long time_start = System.currentTimeMillis();
		for (int i = 0; i < m.length; i += 3999) {
			s.crypt(m, i, c, i, 3999);
		}
		Log.d(TAG, "Time taken to crypt " + m.length + " bytes: " + (System.currentTimeMillis() - time_start) + " ms");
		
		Arrays.fill(m, (byte) 0);
		s.setPosition(0);
		
		time_start = System.currentTimeMillis();
		for (int i = 0; i < m.length; i += 3999) {
			s.crypt(c, i, m, i, 3999);
		}
		Log.d(TAG, "Time taken to crypt again " + m.length + " bytes: " + (System.currentTimeMillis() - time_start) + " ms");

		for (int i = 0; i < m.length; i++) {
			if (m[i] != (byte) ((i+1) & 0xff)) {
				throw new RuntimeException("verify fail!");
			}
		}
	}
	
	void test3bc(int n) {
		Log.d(TAG, "##################  performance test with BC ###################");
		
		KeyParameter keyparam = new KeyParameter(key2);
		ParametersWithIV params = new ParametersWithIV(keyparam, nonce2);

        StreamCipher s = new Salsa20Engine();
        s.init(true, params);

		byte[] m = new byte[3999 * n];
		for (int i = 0; i < m.length; i++) {
			m[i] = (byte) ((i+1) & 0xff);
		}
		byte[] c = new byte[m.length];
		
		long time_start = System.currentTimeMillis();
		for (int i = 0; i < m.length; i += 3999) {
			s.processBytes(m, i, 3999, c, i);
		}
		Log.d(TAG, "Time taken to crypt " + m.length + " bytes: " + (System.currentTimeMillis() - time_start) + " ms");
		
		Arrays.fill(m, (byte) 0);
		s.reset();
		
		time_start = System.currentTimeMillis();
		for (int i = 0; i < m.length; i += 3999) {
			s.processBytes(c, i, 3999, m, i);
		}
		Log.d(TAG, "Time taken to crypt again " + m.length + " bytes: " + (System.currentTimeMillis() - time_start) + " ms");
		
		for (int i = 0; i < m.length; i++) {
			if (m[i] != (byte) ((i+1) & 0xff)) {
				throw new RuntimeException("verify fail!");
			}
		}
	}
	
	void test4(int n) throws IOException {
		Log.d(TAG, "##################  performance with stream test  ###################");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Salsa20OutputStream sos = new Salsa20OutputStream(baos, key1, nonce2);
		
		byte[] m = new byte[3999 * n];
		for (int i = 0; i < m.length; i++) {
			m[i] = (byte) ((i+1) & 0xff);
		}
		
		long time_start = System.currentTimeMillis();
		for (int i = 0; i < m.length; i += 3999) {
			sos.write(m, i, 3999);
		}
		Log.d(TAG, "Time taken to crypt " + m.length + " bytes: " + (System.currentTimeMillis() - time_start) + " ms");
		
		byte[] c = baos.toByteArray();
		
		Arrays.fill(m, (byte) 0);
		Salsa20InputStream sis = new Salsa20InputStream(new ByteArrayInputStream(c), key1, nonce2);
		
		time_start = System.currentTimeMillis();
		int m_offset = 0;
		while (true) {
			int read = sis.read(m, m_offset, 78000);
			if (read < 0) break;
			m_offset += read;
		}
		Log.d(TAG, "Time taken to crypt again " + baos.size() + " bytes: " + (System.currentTimeMillis() - time_start) + " ms");

		for (int i = 0; i < m.length; i++) {
			if (m[i] != (byte) ((i+1) & 0xff)) {
				throw new RuntimeException("verify fail!");
			}
		}
	}
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		test1(key1, nonce1, m1);
		test1(key1, nonce2, m1);
		test1(key2, nonce1, m1);
		test1(key2, nonce2, m1);
		
		test1(key1, nonce1, m2);
		test1(key1, nonce2, m2);
		test1(key2, nonce1, m2);
		test1(key2, nonce2, m2);

		//test1bc(key2, nonce2, m2);
		
		test2();
		
		test3(1251);
		test3(1251);
		test3(125);
//		test3bc(1251);
//		test3bc(1251);

		Log.d(TAG, "################# ROUNDS TEST: 20 #################");
		test3(1251, 20);
		Log.d(TAG, "################# ROUNDS TEST: 12 #################");
		test3(1251, 12);
		Log.d(TAG, "################# ROUNDS TEST: 8 #################");
		test3(1251, 8);
		Log.d(TAG, "################# ROUNDS TEST: 8 AGAIN #################");
		test3(1251, 8);
		
		try {
			test4(1251);
			test4(444);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		Log.d(TAG, "SLEEEPPPPPPPPPPPP");
		SystemClock.sleep(2000);
		Log.d(TAG, "not SLEEEPPPPPPPPPPPP");
		test3(125);
		Log.d(TAG, "SLEEEPPPPPPPPPPPP");
		SystemClock.sleep(2000);
		Log.d(TAG, "not SLEEEPPPPPPPPPPPP");
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
