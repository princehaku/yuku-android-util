package yuku.bintex.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import yuku.bintex.BintexReader;
import yuku.bintex.BintexWriter;

public class PositionTests {
	public static final String TAG = PositionTests.class.getSimpleName();

	byte[] bytes = new byte[100];
	
	@Test public void testPosition() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		int p = 0;
		assertEquals(p, bw.getPos());
		
		bw.writeUint8(0x00);
		p += 1;
		assertEquals(p, bw.getPos());
		
		bw.writeUint16(0xff7f);
		p += 2;
		assertEquals(p, bw.getPos());
		
		bw.writeChar('\uffee');
		p += 2;
		assertEquals(p, bw.getPos());
		
		bw.writeInt(999999);
		p += 4;
		assertEquals(p, bw.getPos());
		
		bw.writeFloat(0.3f);
		p += 4;
		assertEquals(p, bw.getPos());
		
		bw.writeRaw(bytes);
		p += 100;
		assertEquals(p, bw.getPos());
		
		bw.writeRaw(bytes, 10, 87);
		p += 87;
		assertEquals(p, bw.getPos());
		
		bw.writeShortString("6chars");
		p += 6*2 + 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeLongString("6chars");
		p += 6*2 + 4;
		assertEquals(p, bw.getPos()); 
		
		bw.writeAutoString("short8bit");
		p += 9 + 1 + 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeAutoString("short16bit\u900a");
		p += 11*2 + 1 + 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeAutoString("long8bit" + new String(new char[500]));
		p += 508 + 1 + 4;
		assertEquals(p, bw.getPos()); 
		
		bw.writeAutoString("long16bit\u900a" + new String(new char[500]));
		p += 510*2 + 1 + 4;
		assertEquals(p, bw.getPos()); 

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		BintexReader br = new BintexReader(is);
		p = 0;
		assertEquals(p, br.getPos()); 
		
		br.readUint8();
		p += 1;
		assertEquals(p, br.getPos());
		
		br.readUint16();
		p += 2;
		assertEquals(p, br.getPos());
		
		br.readChar();
		p += 2;
		assertEquals(p, br.getPos());
		
		br.readInt();
		p += 4;
		assertEquals(p, br.getPos());
		
		br.readFloat();
		p += 4;
		assertEquals(p, br.getPos());
		
		br.readRaw(new byte[100]);
		p += 100;
		assertEquals(p, br.getPos());
		
		br.readRaw(new byte[100], 10, 87);
		p += 87;
		assertEquals(p, br.getPos());
		
		br.readShortString();
		p += 6*2 + 1;
		assertEquals(p, br.getPos());
		
		br.readLongString();
		p += 6*2 + 4;
		assertEquals(p, br.getPos());
		
		br.readAutoString();
		p += 9 + 1 + 1;
		assertEquals(p, br.getPos());
		
		br.readAutoString();
		p += 11*2 + 1 + 1;
		assertEquals(p, br.getPos());
		
		br.readAutoString();
		p += 508 + 1 + 4;
		assertEquals(p, br.getPos());
		
		br.readAutoString();
		p += 510*2 + 1 + 4;
		assertEquals(p, br.getPos());
	}
}
