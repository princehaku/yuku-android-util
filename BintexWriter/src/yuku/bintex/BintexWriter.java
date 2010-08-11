package yuku.bintex;

import java.io.*;

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
			throw new IllegalArgumentException("string must not more than 255 chars. String is: " + s);
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
	
	public void writeInt(int a) throws IOException {
		os_.write((a & 0xff000000) >> 24);
		os_.write((a & 0x00ff0000) >> 16);
		os_.write((a & 0x0000ff00) >> 8);
		os_.write((a & 0x000000ff) >> 0);
		
		pos += 4;
	}
	
	public void writeChar(char c) throws IOException {
		os_.write((c & 0xff00) >> 8);
		os_.write(c & 0x00ff);
		
		pos += 2;
	}
	
	public void writeUint8(int a) throws IOException {
		if (a < 0 || a > 255) {
			throw new IllegalArgumentException("uint8 must be 0 to 255");
		}
		
		os_.write(a);
		
		pos += 1;
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
	
	public void close() throws IOException {
		os_.close();
	}
	
	public int getPos() {
		return pos;
	}

}
