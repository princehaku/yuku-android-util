package yuku.bintex;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * VALUE types:
 * A, B etc are unsigned byte
 * 
 * [value takes 1 byte]
 * 01..07 = int 1..7
 * 0c = string null
 * 0d = string with zero length
 * 0e = int 0
 * 0f = int -1
 * 
 * [value takes 2 bytes]
 * 10 A = int A
 * 11 A = int ~A (negate of A, not minus A) 
 * 
 * [value takes 3 bytes]
 * 20 A[2] = positive 16-bit int
 * 21 A[2] = negate of 16-bit int
 * 
 * [value takes 4 bytes]
 * 30 A[3] = positive 24-bit int
 * 31 A[3] = negate of 24-bit int
 * 
 * [value takes 5 bytes]
 * 40 A[4] = positive 32-bit int
 * 41 A[4] = negate of 32-bit int
 * 
 * [variable length]
 * 51..5f A[n] = 8-bit (all the characters are in range 0x0000-0x00ff) string with length n=1..15 
 * 61..6f A[n*2] = 16-bit string with length n=1..15 
 * 70 A B[A] = 8-bit string with length A
 * 71 A B[A] = 16-bit string with length A
 * 72 A[4] B[A] = 8-bit string with length A
 * 73 A[4] B[A] = 16-bit string with length A
 * 
 * [maps]
 * 90 empty map
 * 91 A {B C[B], value}[A] = simple map (no more than 255 entries with 8-bit string keys and values of type: int/string/int[]/simplemap)
 * (not-yet-implemented) value A {B C[B], value}[A] = same as above with total size of map specified
 * 
 * [arrays]
 * c0 A B[A] = uint8 array with max 255 elements  
 * c4 A B[A*4] = int array with max 255 elements  
 * c8 A[4] B[A] = uint8 array
 * cc A[4] B[A*4] = int array
 */
public class BintexWriter {
	private final OutputStream os_;

	/** 
	 * Tambah hanya kalau manggil os_.write(*) Jangan tambah kalo ga.
	 */
	private int pos = 0;

	public BintexWriter(OutputStream os) {
		this.os_ = os;
	}
	
	public void writeShortString(String s) throws IOException {
		int len = s.length();
		
		if (len > 255) {
			throw new IllegalArgumentException("string must not more than 255 chars. String is: " + s); //$NON-NLS-1$
		}
		
		os_.write(len);
		pos += 1;
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			
			writeChar(c);
		}
	}
	
	public void writeLongString(String s) throws IOException {
		writeInt(s.length());
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			
			writeChar(c);
		}
	}
	
	/**
	 * Tulis pake 8-bit atau 16-bit
	 * 
	 * byte pertama menentukan
	 * 0x01 = 8 bit short
	 * 0x02 = 16 bit short
	 * 0x11 = 8 bit long
	 * 0x12 = 16 bit long
	 */
	public void writeAutoString(String s) throws IOException {
		// cek dulu apa semuanya 8 bit
		boolean semua8bit = true;
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c > 0xff) {
				semua8bit = false;
				break;
			}
		}
		
		if (len <= 255 && semua8bit) writeUint8(0x01);
		if (len >  255 && semua8bit) writeUint8(0x11);
		if (len <= 255 && !semua8bit) writeUint8(0x02);
		if (len >  255 && !semua8bit) writeUint8(0x12);
		
		if (len <= 255) {
			writeUint8(len);
		} else {
			writeInt(len);
		}
		
		if (semua8bit) {
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				writeUint8(c);
			}
		} else {
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				writeChar(c);
			}
		}
	}
	
	public void writeInt(int a) throws IOException {
		os_.write((a & 0xff000000) >> 24);
		os_.write((a & 0x00ff0000) >> 16);
		os_.write((a & 0x0000ff00) >> 8);
		os_.write((a & 0x000000ff));
		
		pos += 4;
	}
	
	public void writeChar(char c) throws IOException {
		os_.write((c & 0xff00) >> 8);
		os_.write(c & 0x00ff);
		
		pos += 2;
	}
	
	public void writeUint8(int a) throws IOException {
		if (a < 0 || a > 255) {
			throw new IllegalArgumentException("uint8 must be 0 to 255"); //$NON-NLS-1$
		}
		
		os_.write(a);
		
		pos += 1;
	}
	
	public void writeUint16(int a) throws IOException {
		if (a < 0 || a > 0xffff) {
			throw new IllegalArgumentException("uint16 must be 0 to 65535"); //$NON-NLS-1$
		}
		
		os_.write((a & 0x0000ff00) >> 8);
		os_.write((a & 0x000000ff) >> 0);
		
		pos += 2;
	}
	
	public void writeFloat(float f) throws IOException {
		int a = Float.floatToIntBits(f);
		writeInt(a);
	}
	
	public void writeRaw(byte[] buf) throws IOException {
		writeRaw(buf, 0, buf.length);
	}
	
	public void writeRaw(byte[] buf, int off, int len) throws IOException {
		os_.write(buf, off, len);
		
		pos += len;
	}
	
	public void writeValueInt(int a) throws IOException {
		if (a == 0) {
			os_.write(0x0e);
			pos += 1;
		} else if (a >= 1 && a <= 7) {
			os_.write(a);
			pos += 1;
		} else if (a == -1) {
			os_.write(0x0f);
			pos += 1;
		} else if (a > 0) {
			if (a < 256) { 
				os_.write(0x10);
				os_.write(a);
				pos += 2;
			} else if (a < 65536) {
				os_.write(0x20);
				os_.write((a & 0xff00) >> 8);
				os_.write(a & 0x00ff);
				pos += 3;
			} else if (a < 16777216) {
				os_.write(0x30);
				os_.write((a & 0xff0000) >> 16);
				os_.write((a & 0x00ff00) >> 8);
				os_.write(a & 0x0000ff);
				pos += 4;
			} else {
				os_.write(0x40);
				os_.write((a & 0xff000000) >> 24);
				os_.write((a & 0x00ff0000) >> 16);
				os_.write((a & 0x0000ff00) >> 8);
				os_.write((a & 0x000000ff));
				pos += 5;
			}
		} else {
			a = ~a;
			if (a < 256) { 
				os_.write(0x11);
				os_.write(a);
				pos += 2;
			} else if (a < 65536) {
				os_.write(0x21);
				os_.write((a & 0xff00) >> 8);
				os_.write(a & 0x00ff);
				pos += 3;
			} else if (a < 16777216) {
				os_.write(0x31);
				os_.write((a & 0xff0000) >> 16);
				os_.write((a & 0x00ff00) >> 8);
				os_.write(a & 0x0000ff);
				pos += 4;
			} else {
				os_.write(0x41);
				os_.write((a & 0xff000000) >> 24);
				os_.write((a & 0x00ff0000) >> 16);
				os_.write((a & 0x0000ff00) >> 8);
				os_.write((a & 0x000000ff));
				pos += 5;
			}
		}
	}
	
	public void writeValueString(String s) throws IOException {
		// special case: null and length-0 strings
		if (s == null) {
			os_.write(0x0c);
			pos += 1;
		} else if (s.length() == 0) {
			os_.write(0x0d);
			pos += 1;
		} else {
			// check if all characters are 0x0000-0x00ff
			boolean all8bits = true;
			int len = s.length();
			for (int i = 0; i < len; i++) {
				char c = s.charAt(i);
				if (c > 0xff) {
					all8bits = false;
					break;
				}
			}
			
			// write header and length
			if (len < 16) {
				if (all8bits) {
					os_.write(0x50 | len);
				} else {
					os_.write(0x60 | len);
				}
				pos += 1;
			} else if (len < 256) {
				if (all8bits) {
					os_.write(0x70);
					os_.write(len);
				} else {
					os_.write(0x71);
					os_.write(len);
				}
				pos += 2;
			} else {
				if (all8bits) {
					os_.write(0x72);
				} else {
					os_.write(0x73);
				}
				pos += 1;
				
				writeInt(len);
			}
			
			// write characters
			if (all8bits) {
				for (int i = 0; i < s.length(); i++) {
					char c = s.charAt(i);
					os_.write(c);
				}
				pos += len;
			} else {
				for (int i = 0; i < s.length(); i++) {
					char c = s.charAt(i);
					os_.write((c & 0xff00) >> 8);
					os_.write(c & 0x00ff);
				}
				pos += len << 1;
			}
		}
	}

	public void writeValueUint8Array(int[] a) throws IOException {
		int len = a.length;
		if (len < 256) {
			writeUint8(0xc0);
			writeUint8(len);
		} else {
			writeUint8(0xc8);
			writeInt(len);
		}
		
		for (int e: a) {
			if (e > 255) {
				throw new RuntimeException("element is larger than 255");
			}
			os_.write(e);
		}
		pos += len;
	}
	
	/** will use {@link #writeValueUint8Array(int[])} if possible */ 
	public void writeValueIntArray(int[] a) throws IOException {
		{ // check optimization 
			boolean allUint8 = true;
			for (int e: a) {
				if (e > 255 || e < 0) {
					allUint8 = false;
					break;
				}
			}
			if (allUint8) {
				writeValueUint8Array(a);
				return;
			}
		}

		int len = a.length;
		if (len < 256) {
			writeUint8(0xc4);
			writeUint8(len);
		} else {
			writeUint8(0xcc);
			writeInt(len);
		}
		
		for (int e: a) {
			os_.write((e & 0xff000000) >> 24);
			os_.write((e & 0x00ff0000) >> 16);
			os_.write((e & 0x0000ff00) >> 8);
			os_.write((e & 0x000000ff));
		}
		pos += len << 2;
	}
	
	public void writeValueSimpleMap(Map<String, Object> map) throws IOException {
		int size = map.size();
		
		if (size == 0) { // empty map
			os_.write(0x90);
			pos += 1;
			return;
		}
		
		if (size > 255) {
			throw new RuntimeException("entries of map max 255");
		}
		
		for (Map.Entry<String, Object> e: map.entrySet()) {
			Object v = e.getValue();
			if (v instanceof String) {
			} else if (v instanceof Number) {
			} else if (v instanceof int[]) {
			} else {
				throw new RuntimeException("map entry values must be string or int or int array");
			}
			String k = e.getKey();
			if (k == null || k.length() > 255) {
				throw new RuntimeException("map entry keys must be string with length max 255");
			}
			for (int i = 0; i < k.length(); i++) {
				char c = k.charAt(i);
				if (c > 0xff) throw new RuntimeException("map entry keys must be string with 8-bit characters");
			}
		}
		
		os_.write(0x91);
		os_.write(size);
		pos += 2;
		
		for (Map.Entry<String, Object> e: map.entrySet()) {
			String k = e.getKey();
			os_.write(k.length());
			pos += 1;
			for (int i = 0; i < k.length(); i++) {
				os_.write(k.charAt(i));
			}
			pos += k.length();
			Object v = e.getValue();
			if (v instanceof String) {
				writeValueString((String) v);
			} else if (v instanceof Number) {
				writeValueInt(((Number) v).intValue());
			} else if (v instanceof int[]) {
				writeValueIntArray((int[]) v);
			}
		}
	}
	
	public void close() throws IOException {
		os_.close();
	}
	
	public int getPos() {
		return pos;
	}

	public OutputStream getOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int oneByte) throws IOException {
				writeUint8(oneByte);
			}
			
			@Override
			public void write(byte[] buffer, int offset, int count) throws IOException {
				writeRaw(buffer, offset, count);
			}
		};
	}
}
