package yuku.bintex.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import yuku.bintex.BintexReader;
import yuku.bintex.BintexWriter;

public class NumberTests {
	public static final String TAG = NumberTests.class.getSimpleName();

	@Test public void testUint8() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		bw.writeUint8(0x00);
		bw.writeUint8(0xff);
		bw.writeUint8(0x80);
		bw.writeUint8(0x7f);
		
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		
		BintexReader br = new BintexReader(is);
		assertEquals(0x00, br.readUint8());
		assertEquals(0xff, br.readUint8());
		assertEquals(0x80, br.readUint8());
		assertEquals(0x7f, br.readUint8());
	}
	
	@Test public void testUint16() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		bw.writeUint16(0x0000);
		bw.writeUint16(0xffff);
		bw.writeUint16(0x8000);
		bw.writeUint16(0x7fff);
		
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		
		BintexReader br = new BintexReader(is);
		assertEquals(0x0000, br.readUint16());
		assertEquals(0xffff, br.readUint16());
		assertEquals(0x8000, br.readUint16());
		assertEquals(0x7fff, br.readUint16());
	}
	
	@Test public void testChar() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		bw.writeChar('\u0000');
		bw.writeChar('\uffff');
		bw.writeChar('a');
		bw.writeChar('\ue800');
		
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		
		BintexReader br = new BintexReader(is);
		assertEquals('\u0000', br.readChar());
		assertEquals('\uffff', br.readChar());
		assertEquals('a'     , br.readChar());
		assertEquals('\ue800', br.readChar());
	}
	
	@Test public void testInt() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		bw.writeInt(0);
		bw.writeInt(-1);
		bw.writeInt(Integer.MAX_VALUE);
		bw.writeInt(Integer.MIN_VALUE);
		
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		
		BintexReader br = new BintexReader(is);
		assertEquals(0, br.readInt());
		assertEquals(-1, br.readInt());
		assertEquals(Integer.MAX_VALUE, br.readInt());
		assertEquals(Integer.MIN_VALUE, br.readInt());
	}
	
	@Test public void testFloat() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		bw.writeFloat(0.f);
		bw.writeFloat(+1.f);
		bw.writeFloat(-1.f);
		bw.writeFloat(0.1f);
		bw.writeFloat(Float.MAX_VALUE);
		bw.writeFloat(Float.MIN_VALUE);
		
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		
		BintexReader br = new BintexReader(is);
		assertEquals(0.f, br.readFloat(), 0);
		assertEquals(+1.f, br.readFloat(), 0);
		assertEquals(-1.f, br.readFloat(), 0);
		assertEquals(0.1f, br.readFloat(), 0);
		assertEquals(Float.MAX_VALUE, br.readFloat(), 0);
		assertEquals(Float.MIN_VALUE, br.readFloat(), 0);
	}
}
