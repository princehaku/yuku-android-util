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
	
	@Test public void testVarUint() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		

		/** Write a non-negative int using variable length encoding. 
		 * 0-127 is 1 byte: 0xxxxxxx
		 * 128-16383 (0x3fff) is 2 bytes: 10xxxxxx + 1byte
		 * 16383-2097151 (0x1fffff) is 3 bytes: 110xxxxx + 2byte
		 * 2097152-268435455 (0x0fffffff) is 4 bytes: 1110xxxx + 3byte
		 * 268435456-2147483647 (0x7fffffff) is 5 bytes: 11110000 + 0xxxxxxx + 3byte
		 **/
		
		bw.writeVarUint(0);
		bw.writeVarUint(127);
		bw.writeVarUint(128);
		bw.writeVarUint(4001);
		bw.writeVarUint(16383);
		bw.writeVarUint(16384);
		bw.writeVarUint(500001);
		bw.writeVarUint(2097151);
		bw.writeVarUint(2097152);
		bw.writeVarUint(100000001);
		bw.writeVarUint(268435455);
		bw.writeVarUint(268435456);
		bw.writeVarUint(1000000001);
		bw.writeVarUint(Integer.MAX_VALUE);

		byte[] bytes = os.toByteArray();
		for (byte b: bytes) {
			System.out.printf("%02x ", b);
		}
		System.out.println();

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		
		BintexReader br = new BintexReader(is);
		assertEquals(0          , br.readVarUint());
		assertEquals(127        , br.readVarUint());
		assertEquals(128        , br.readVarUint());
		assertEquals(4001       , br.readVarUint());
		assertEquals(16383      , br.readVarUint());
		assertEquals(16384      , br.readVarUint());
		assertEquals(500001     , br.readVarUint());
		assertEquals(2097151    , br.readVarUint());
		assertEquals(2097152    , br.readVarUint());
		assertEquals(100000001  , br.readVarUint());
		assertEquals(268435455  , br.readVarUint());
		assertEquals(268435456  , br.readVarUint());
		assertEquals(1000000001 , br.readVarUint());
		assertEquals(Integer.MAX_VALUE , br.readVarUint());
	}
}
