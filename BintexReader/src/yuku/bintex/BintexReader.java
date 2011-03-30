package yuku.bintex;

import java.io.*;

public class BintexReader {
	private final InputStream is_;
	
	private int pos_ = 0;
	private char[] buf = new char[1024]; // paling dikit 255 biar bisa shortstring
	
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
		char[] _buf = this.buf;
		for (int i = 0; i < len; i++) {
			_buf[i] = readCharTanpaNaikPos();
		}
		pos_ += len + len;
		
		return new String(_buf, 0, len);
	}
	
	public String readLongString() throws IOException {
		int len = readInt();
		if (len == 0) {
			return ""; //$NON-NLS-1$
		}
		
		if (len > buf.length) {
			this.buf = new char[len + 1024];
		}
		char[] _buf = this.buf;
		for (int i = 0; i < len; i++) {
			_buf[i] = readCharTanpaNaikPos();
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
		
		if (len > buf.length) {
			this.buf = new char[len + 1024];
		}
		
		if (jenis == 0x01 || jenis == 0x11) {
			char[] _buf = this.buf;
			for (int i = 0; i < len; i++) {
				_buf[i] = (char) is_.read();
			}
			pos_ += len;
			
			return new String(_buf, 0, len);
		} else if (jenis == 0x02 || jenis == 0x12) {
			char[] _buf = this.buf;
			for (int i = 0; i < len; i++) {
				_buf[i] = readCharTanpaNaikPos();
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
	
	private char readCharTanpaNaikPos() throws IOException {
		return (char) ((is_.read() << 8) | (is_.read()));
	}

	public int readUint8() throws IOException {
		int res = is_.read();
		pos_++;
		return res;
	}
	
	public float readFloat() throws IOException {
		int a = (is_.read() << 24) | (is_.read() << 16) | (is_.read() << 8) | (is_.read());
		return Float.intBitsToFloat(a);
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
