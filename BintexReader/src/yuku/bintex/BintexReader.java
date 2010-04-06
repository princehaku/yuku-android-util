package yuku.bintex;

import java.io.*;

public class BintexReader {
	private final InputStream is_;
	
	private int pos_ = 0;

	public BintexReader(InputStream is) {
		this.is_ = is;
	}
	
	public String readShortString() throws IOException {
		int len = is_.read();
		pos_++;
		
		if (len < 0) {
			throw new EOFException();
		}
		
		char[] dat = new char[len];
		
		for (int i = 0; i < len; i++) {
			dat[i] = readChar();
		}
		
		return new String(dat);
	}
	
	public String readLongString() throws IOException {
		int len = readInt();
		
		char[] dat = new char[len];
		
		for (int i = 0; i < len; i++) {
			dat[i] = readChar();
		}
		
		return new String(dat);
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

	public int readUint8() throws IOException {
		int res = is_.read();
		pos_++;
		return res;
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
