package yuku.bintex;

import java.io.*;

public class BintexReader {
	private final InputStream is_;

	public BintexReader(InputStream is) {
		this.is_ = is;
	}
	
	public String readShortString() throws IOException {
		int len = is_.read();
		
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
		return (is_.read() << 24) | (is_.read() << 16) | (is_.read() << 8) | (is_.read());
	}

	public char readChar() throws IOException {
		return (char) ((is_.read() << 8) | (is_.read()));
	}

	public int readUint8() throws IOException {
		return is_.read();
	}
	
	public void close() throws IOException {
		is_.close();
	}
}
