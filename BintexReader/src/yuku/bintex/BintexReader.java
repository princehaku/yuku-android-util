package yuku.bintex;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class BintexReader {
	private static final int[] SUPPORTED_TYPE_MAP = { // 1 = int; 2 = string; 3 = int[]; 4 = simple map
		//.1 .2 .3 .4 .5 .6 .7 .8 .9 .a .b .c .d .e .f
		0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 2, 2, 1, 1, // 0. 
		1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 1. 
		1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 2. 
		1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 3. 
		1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 4. 
		0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, // 5. 
		0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, // 6. 
		2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 7. 
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 8. 
		4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 9. 
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // a. 
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // b. 
		3, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 0, // c. 
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // d. 
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // e. 
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // f. 
	};
	
	private final InputStream is_;
	
	private int pos_ = 0;
	private byte[] buf_byte = new byte[2048]; 
	private char[] buf_char = new char[1024]; 
	
	public BintexReader(InputStream is) {
		this.is_ = is;
	}
	
	public String readShortString() throws IOException {
		int len = is_.read();
		pos_++;
		
		if (len < 0) {
			throw new EOFException();
		} else if (len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		// max len = 255, maka buf pasti cukup
		char[] _buf = this.buf_char;
		for (int i = 0; i < len; i++) {
			_buf[i] = readCharWithoutIncreasingPos();
		}
		pos_ += len + len;
		
		return new String(_buf, 0, len);
	}
	
	public String readLongString() throws IOException {
		int len = readInt();
		if (len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		if (len > buf_char.length) {
			this.buf_char = new char[len + 1024];
		}
		char[] _buf = this.buf_char;
		for (int i = 0; i < len; i++) {
			_buf[i] = readCharWithoutIncreasingPos();
		}
		pos_ += len + len;
		
		return new String(_buf, 0, len);
	}
	
	/**
	 * Baca pake 8-bit atau 16-bit
	 * 
	 * byte pertama menentukan
	 * 0x01 = 8 bit short
	 * 0x02 = 16 bit short
	 * 0x11 = 8 bit long
	 * 0x12 = 16 bit long
	 */
	public String readAutoString() throws IOException {
		int jenis = readUint8();
		int len = 0;
		if (jenis == 0x01 || jenis == 0x02) {
			len = readUint8();
		} else if (jenis == 0x11 || jenis == 0x12) {
			len = readInt();
		}
		
		if (len > buf_char.length) {
			this.buf_char = new char[len + 1024];
		}
		
		if (jenis == 0x01 || jenis == 0x11) {
			char[] _buf = this.buf_char;
			for (int i = 0; i < len; i++) {
				_buf[i] = (char) is_.read();
			}
			pos_ += len;
			
			return new String(_buf, 0, len);
		} else if (jenis == 0x02 || jenis == 0x12) {
			char[] _buf = this.buf_char;
			for (int i = 0; i < len; i++) {
				_buf[i] = readCharWithoutIncreasingPos();
			}
			pos_ += len + len;
			
			return new String(_buf, 0, len);
		} else {
			return null;
		}
	}
	
	public int readInt() throws IOException {
		int res = (is_.read() << 24) | (is_.read() << 16) | (is_.read() << 8) | (is_.read());
		pos_ += 4;
		return res;
	}

	public char readChar() throws IOException {
		char res = (char) ((is_.read() << 8) | (is_.read()));
		pos_ += 2;
		return res;
	}
	
	private char readCharWithoutIncreasingPos() throws IOException {
		return (char) ((is_.read() << 8) | (is_.read()));
	}

	public int readUint8() throws IOException {
		int res = is_.read();
		pos_++;
		return res;
	}
	
	public int readUint16() throws IOException {
		int res = (is_.read() << 8) | (is_.read());
		pos_ += 2;
		return res;
	}
	
	public float readFloat() throws IOException {
		int a = (is_.read() << 24) | (is_.read() << 16) | (is_.read() << 8) | (is_.read());
		pos_ += 4;
		return Float.intBitsToFloat(a);
	}

	public int readRaw(byte[] buf) throws IOException {
		return readRaw(buf, 0, buf.length);
	}
	
	public int readRaw(byte[] buf, int off, int len) throws IOException {
		int total = 0;
		int _off = off;
		int _len = len;
		
		while (true) {
			int read = is_.read(buf, _off, _len);
			if (read < 0) {
				if (total == 0) total = -1;
				break;
			}
			total += read;
			if (total >= len) {
				break;
			}
			_off += read;
			_len -= read;
		}
		pos_ += total;

		return total;
	}

	public int readValueInt() throws IOException {
		int t = is_.read();
		pos_++;
		return _readValueInt(t);
	}
	
	private int _readValueInt(int t) throws IOException {
		switch (t) {
		case 0x0e: // special value 0
			return 0;
		case 0x01:
		case 0x02:
		case 0x03:
		case 0x04:
		case 0x05:
		case 0x06:
		case 0x07: // immediate 1-7
			return t;
		case 0x0f: // special value -1
			return -1;
		case 0x10: 
		case 0x11: { 
			int a = is_.read();
			pos_++;
			return t == 0x11? ~a: a;
		}
		case 0x20: 
		case 0x21: { 
			int a = (is_.read() << 8) | (is_.read());
			pos_ += 2;
			return t == 0x21? ~a: a;
		}
		case 0x30: 
		case 0x31: { 
			int a = (is_.read() << 16) | (is_.read() << 8) | (is_.read());
			pos_ += 3;
			return t == 0x31? ~a: a;
		}
		case 0x40: 
		case 0x41: { 
			int a = (is_.read() << 24) | (is_.read() << 16) | (is_.read() << 8) | (is_.read());
			pos_ += 4;
			return t == 0x41? ~a: a;
		}
		default: {
			throw new IOException(String.format("value is not int: type=%02x", t));
		}
		}
	}
	
	public String readValueString() throws IOException {
		int t = is_.read();
		pos_++;
		return _readValueString(t);
	}
	
	private String _readValueString(int t) throws IOException {
		switch (t) {
		case 0x0c: // null
			return null;
		case 0x0d:
			return "";
		case 0x51:
		case 0x52:
		case 0x53:
		case 0x54:
		case 0x55:
		case 0x56:
		case 0x57:
		case 0x58:
		case 0x59:
		case 0x5a:
		case 0x5b:
		case 0x5c:
		case 0x5d:
		case 0x5e:
		case 0x5f: { // 8-bit string with len 1-15
			int len = t & 0x0f;
			return _read8BitString(len);
		}
		case 0x61:
		case 0x62:
		case 0x63:
		case 0x64:
		case 0x65:
		case 0x66:
		case 0x67:
		case 0x68:
		case 0x69:
		case 0x6a:
		case 0x6b:
		case 0x6c:
		case 0x6d:
		case 0x6e:
		case 0x6f: { // 16-bit string with len 1-15
			int len = t & 0x0f;
			return _read16BitString(len);
		}
		case 0x70: { // 8-bit string with len < 256
			int len = is_.read();
			pos_++;
			return _read8BitString(len);
		}
		case 0x71: { // 16-bit string with len < 256
			int len = is_.read();
			pos_++;
			return _read16BitString(len);
		}
		case 0x72: { // long 8-bit string
			int len = readInt();
			return _read8BitString(len);
		}
		case 0x73: { // long 16-bit string
			int len = readInt();
			return _read16BitString(len);
		}
		default: 
			throw new IOException(String.format("value is not string: type=%02x", t));
		}
	}

	private String _read8BitString(int len) throws IOException {
		byte[] buf1 = len <= this.buf_byte.length? this.buf_byte: new byte[len];
		is_.read(buf1, 0, len);
		pos_ += len;
		return new String(buf1, 0x00, 0, len);
	}
	
	private String _read16BitString(int len) throws IOException {
		int bytes = len << 1;
		char[] buf2 = len <= this.buf_char.length? this.buf_char: new char[len];
		for (int i = 0; i < len; i++) {
			buf2[i] = readCharWithoutIncreasingPos();
		}
		pos_ += bytes;
		return new String(buf2, 0, len);
	}

	public int[] readValueUint8Array() throws IOException {
		int t = is_.read();
		pos_++;
		return _readValueUint8Array(t);
	}
	
	private int[] _readValueUint8Array(int t) throws IOException {
		int len;
		if (t == 0xc0) { // len < 256
			len = is_.read();
			pos_++;
		} else if (t == 0xc8) {
			len = readInt();
		} else {
			throw new IOException(String.format("value is not uint8 array: type=%02x", t));
		}
		
		byte[] buf1 = len <= this.buf_byte.length? this.buf_byte: new byte[len];
		is_.read(buf1, 0, len);
		pos_ += len;
		
		int[] res = new int[len];
		for (int i = 0; i < len; i++) {
			res[i] = buf1[i] & 0xff;
		}
		return res;
	}

	/** also returns correctly if the data is of type uint8 array */ 
	public int[] readValueIntArray() throws IOException {
		int t = is_.read();
		pos_++;
		return _readValueIntArray(t);
	}
	
	private int[] _readValueIntArray(int t) throws IOException {
		int len;
		if (t == 0xc0) {
			return _readValueUint8Array(t);
		} else if (t == 0xc8) {
			return _readValueUint8Array(t);
		} else if (t == 0xc4) { // len < 256
			len = is_.read();
			pos_++;
		} else if (t == 0xcc) {
			len = readInt();
		} else {
			throw new IOException(String.format("value is not int array: type=%02x", t));
		}
		
		int[] res = new int[len];
		byte[] buf = new byte[4];
		for (int i = 0; i < len; i++) {
			is_.read(buf, 0, 4);
			res[i] = ((buf[0] & 0xff) << 24) | ((buf[1] & 0xff) << 16) | ((buf[2] & 0xff) << 8) | (buf[3] & 0xff);
		}
		pos_ += len << 2;
		
		return res;
	}
	
	public ValueMap readValueSimpleMap() throws IOException {
		int t = is_.read();
		pos_++;
		return _readValueSimpleMap(t);
	}

	private ValueMap _readValueSimpleMap(int t) throws IOException {
		if (t != 0x90) {
			throw new IOException(String.format("value is not simple map: type=%02x", t));
		}
		
		int size = is_.read();
		pos_++;
		
		ValueMap res = new ValueMap();
		
		for (int i = 0; i < size; i++) {
			int key_len = is_.read();
			pos_++;
			
			String k = _read8BitString(key_len);
			Object v = readValue();
			
			res.put(k, v);
		}
		
		return res;
	}

	public Object readValue() throws IOException {
		int t = is_.read();
		pos_++;
		
		// ints
		int type = SUPPORTED_TYPE_MAP[t];
		if (type == 1) {
			return _readValueInt(t);
		} else if (type == 2) {
			return _readValueString(t);
		} else if (type == 3) {
			return _readValueIntArray(t);
		} else if (type == 4) {
			return _readValueSimpleMap(t);
		} else {
			throw new IOException(String.format("value has unknown type: type=%02x", t));
		}
	}

	public long skip(long n) throws IOException {
		long res = is_.skip(n);
		pos_ += (int) res;
		return res;
	}
	
	public int getPos() {
		return pos_;
	}
	
	public void close() {
		try {
			is_.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
