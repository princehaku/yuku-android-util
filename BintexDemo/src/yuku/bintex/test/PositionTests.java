package yuku.bintex.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.TreeMap;

import org.junit.Test;

import yuku.bintex.BintexReader;
import yuku.bintex.BintexWriter;
import yuku.bintex.ValueMap;

public class PositionTests {
	public static final String TAG = PositionTests.class.getSimpleName();

	byte[] bytes = new byte[100];
	
	@Test public void testPositionForNonValues() throws Exception {
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

	@Test public void testPositionForValues() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		int p = 0;
		assertEquals(p, bw.getPos());
		
		bw.writeValueInt(0);
		p += 1;
		assertEquals(p, bw.getPos());
		
		bw.writeValueInt(-1);
		p += 1;
		assertEquals(p, bw.getPos());
		
		bw.writeValueInt(7);
		p += 1;
		assertEquals(p, bw.getPos());
		
		bw.writeValueInt(100);
		p += 2;
		assertEquals(p, bw.getPos());
		
		bw.writeValueInt(1000);
		p += 3;
		assertEquals(p, bw.getPos());
		
		bw.writeValueInt(11222333);
		p += 4;
		assertEquals(p, bw.getPos());
		
		bw.writeValueInt(Integer.MIN_VALUE);
		p += 5;
		assertEquals(p, bw.getPos());
		
		bw.writeValueIntArray(new int[100]);
		p += 100 + 1 + 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeValueIntArray(new int[] {-1, -2, -3});
		p += 3*4 + 1 + 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeValueIntArray(new int[1000]);
		p += 1000 + 1 + 4;
		assertEquals(p, bw.getPos()); 
		
		int[] a = new int[1000]; 
		Arrays.fill(a, 1000);
		bw.writeValueIntArray(a);
		p += 1000*2 + 1 + 4;
		assertEquals(p, bw.getPos());
		
		Arrays.fill(a, 65536);
		bw.writeValueIntArray(a);
		p += 1000*4 + 1 + 4;
		assertEquals(p, bw.getPos());
		
		bw.writeValueString(null);
		p += 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeValueString("");
		p += 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeValueString("hi");
		p += 2 + 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeValueString("1234567890123456");
		p += 16 + 1 + 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeValueString("\u9999");
		p += 1*2 + 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeValueString("\u9999234567890123456");
		p += 16*2 + 1 + 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeValueString(new String(new char[1000]));
		p += 1000 + 1 + 4;
		assertEquals(p, bw.getPos()); 

		char[] b = new char[1000];
		Arrays.fill(b, '\u9999');
		bw.writeValueString(new String(b));
		p += 1000*2 + 1 + 4;
		assertEquals(p, bw.getPos()); 
		
		bw.writeValueSimpleMap(new ValueMap());
		p += 1;
		assertEquals(p, bw.getPos()); 
		
		bw.writeValueSimpleMap(new TreeMap<String, Object>());
		p += 1;
		assertEquals(p, bw.getPos()); 
		
		ValueMap c = new ValueMap();
		c.put("int", 1);
		c.put("string", "string");
		c.put("array", new int[100]);
		c.put("map", new ValueMap());
		
		bw.writeValueSimpleMap(c);
		p += 1 /*header*/ + 1 /* size of map */ 
		+ 4 /* key "int" */ + 1 /* value */
		+ 7 /* key "string" */ + 7 /* value */
		+ 6 /* key "array" */ + 102 /* value */
		+ 4 /* key "map" */ + 1 /* value */;
		assertEquals(p, bw.getPos()); 
		
		
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		BintexReader br = new BintexReader(is);
		p = 0;
		assertEquals(p, br.getPos()); 
		
		br.readValueInt();
		p += 1;
		assertEquals(p, br.getPos());
		
		br.readValueInt();
		p += 1;
		assertEquals(p, br.getPos());
		
		br.readValueInt();
		p += 1;
		assertEquals(p, br.getPos());
		
		br.readValueInt();
		p += 2;
		assertEquals(p, br.getPos());
		
		br.readValueInt();
		p += 3;
		assertEquals(p, br.getPos());
		
		br.readValueInt();
		p += 4;
		assertEquals(p, br.getPos());
		
		br.readValueInt();
		p += 5;
		assertEquals(p, br.getPos());
		
		br.readValueIntArray();
		p += 100 + 1 + 1;
		assertEquals(p, br.getPos());
		
		br.readValueIntArray();
		p += 3*4 + 1 + 1;
		assertEquals(p, br.getPos());
		
		br.readValueIntArray();
		p += 1000 + 1 + 4;
		assertEquals(p, br.getPos());
		
		br.readValueIntArray();
		p += 1000*2 + 1 + 4;
		assertEquals(p, br.getPos());
		
		br.readValueIntArray();
		p += 1000*4 + 1 + 4;
		assertEquals(p, br.getPos());
		
		br.readValueString();
		p += 1;
		assertEquals(p, br.getPos());
		
		br.readValueString();
		p += 1;
		assertEquals(p, br.getPos());
		
		br.readValueString();
		p += 2 + 1;
		assertEquals(p, br.getPos());
		
		br.readValueString();
		p += 16 + 1 + 1;
		assertEquals(p, br.getPos());
		
		br.readValueString();
		p += 1*2 + 1;
		assertEquals(p, br.getPos());
		
		br.readValueString();
		p += 16*2 + 1 + 1;
		assertEquals(p, br.getPos());
		
		br.readValueString();
		p += 1000 + 1 + 4;
		assertEquals(p, br.getPos());
		
		br.readValueString();
		p += 1000*2 + 1 + 4;
		assertEquals(p, br.getPos());
		
		br.readValueSimpleMap();
		p += 1;
		assertEquals(p, br.getPos());
		
		br.readValueSimpleMap();
		p += 1;
		assertEquals(p, br.getPos());
		
		br.readValueSimpleMap();
		p += 1 /*header*/ + 1 /* size of map */ 
		+ 4 /* key "int" */ + 1 /* value */
		+ 7 /* key "string" */ + 7 /* value */
		+ 6 /* key "array" */ + 102 /* value */
		+ 4 /* key "map" */ + 1 /* value */;
		assertEquals(p, br.getPos());
	}
}
