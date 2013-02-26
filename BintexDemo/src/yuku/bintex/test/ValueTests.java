package yuku.bintex.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import yuku.bintex.BintexReader;
import yuku.bintex.BintexWriter;
import yuku.bintex.ValueMap;

public class ValueTests {
	public static final String TAG = ValueTests.class.getSimpleName();

	@Test public void testIntValues() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		bw.writeValueInt(0);
		bw.writeValueInt(1);
		bw.writeValueInt(7);
		bw.writeValueInt(8);
		bw.writeValueInt(-1);
		bw.writeValueInt(-2);
		bw.writeValueInt(255);
		bw.writeValueInt(256);
		bw.writeValueInt(65535);
		bw.writeValueInt(65536);
		bw.writeValueInt(16777215);
		bw.writeValueInt(16777216);
		bw.writeValueInt(5555);
		bw.writeValueInt(-7777);
		bw.writeValueInt(-255);
		bw.writeValueInt(-256);
		bw.writeValueInt(-257);
		bw.writeValueInt(-65535);
		bw.writeValueInt(-65536);
		bw.writeValueInt(-16777215);
		bw.writeValueInt(-16777216);
		bw.writeValueInt(Integer.MAX_VALUE);
		bw.writeValueInt(Integer.MIN_VALUE);
		
		byte[] bytes = os.toByteArray();
		for (byte b: bytes) {
			System.out.printf("%02x ", b);
		}
		System.out.println();
		
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		
		BintexReader br = new BintexReader(is);
		assertEquals(0                , br.readValueInt());
		assertEquals(1                , br.readValueInt());
		assertEquals(7                , br.readValueInt());
		assertEquals(8                , br.readValueInt());
		assertEquals(-1               , br.readValueInt());
		assertEquals(-2               , br.readValueInt());
		assertEquals(255              , br.readValueInt());
		assertEquals(256              , br.readValueInt());
		assertEquals(65535            , br.readValueInt());
		assertEquals(65536            , br.readValueInt());
		assertEquals(16777215         , br.readValueInt());
		assertEquals(16777216         , br.readValueInt());
		assertEquals(5555             , br.readValueInt());
		assertEquals(-7777            , br.readValueInt());
		assertEquals(-255             , br.readValueInt());
		assertEquals(-256             , br.readValueInt());
		assertEquals(-257             , br.readValueInt());
		assertEquals(-65535           , br.readValueInt());
		assertEquals(-65536           , br.readValueInt());
		assertEquals(-16777215        , br.readValueInt());
		assertEquals(-16777216        , br.readValueInt());
		assertEquals(Integer.MAX_VALUE, br.readValueInt());
		assertEquals(Integer.MIN_VALUE, br.readValueInt());
	}
	
	@Test public void testStringValues() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		String len255 = "12345678901234567890123456789012345678901234567890"
		+ "12345678901234567890123456789012345678901234567890"
		+ "12345678901234567890123456789012345678901234567890"
		+ "12345678901234567890123456789012345678901234567890"
		+ "12345678901234567890123456789012345678901234567890"
		+ "12345";
		
		String len255uni = "12345678901234567890123456789012345678901234567890"
		+ "12345678901234567890123456789012345678901234567890"
		+ "12345678901234567890123456789012345678901234567890"
		+ "12345678901234567890123456789012345678901234567890"
		+ "12345678901234567890123456789012345678901234567890"
		+ "1234\uf9f0";
		
		bw.writeValueString(null);
		bw.writeValueString("");
		bw.writeValueString("hi");
		bw.writeValueString("hi\u8888\u9999");
		bw.writeValueString("hellohellohello");
		bw.writeValueString("hellohellohello!");
		bw.writeValueString("hellohellohell\u0100");
		bw.writeValueString("hellohellohell\u0101!");
		bw.writeValueString(len255);
		bw.writeValueString(len255 + 'x');
		bw.writeValueString(len255uni);
		bw.writeValueString(len255uni + '\u1234');
		
		byte[] bytes = os.toByteArray();
		for (byte b: bytes) {
			System.out.printf("%02x ", b);
		}
		System.out.println();
		
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		
		BintexReader br = new BintexReader(is);
		assertEquals(null                , br.readValueString());
		assertEquals(""                , br.readValueString());
		assertEquals("hi"                , br.readValueString());
		assertEquals("hi\u8888\u9999"                , br.readValueString());
		assertEquals("hellohellohello"               , br.readValueString());
		assertEquals("hellohellohello!"               , br.readValueString());
		assertEquals("hellohellohell\u0100"              , br.readValueString());
		assertEquals("hellohellohell\u0101!"              , br.readValueString());
		assertEquals(len255               , br.readValueString());
		assertEquals(len255 + 'x'         , br.readValueString());
		assertEquals(len255uni            , br.readValueString());
		assertEquals(len255uni + '\u1234' , br.readValueString());
	}
	
	@Test public void testIntArrayValues() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		int[] smallInts = {0,1,255,80}; 
		int[] mediumInts1 = {0, 65535};
		int[] mediumInts2 = {255, 256};
		int[] bigInts1 = {0, 1, 1000, -1}; 
		int[] bigInts2 = {-1000, Integer.MAX_VALUE, Integer.MIN_VALUE, 0}; 
		int[] longSmallInts = new int[1000];
		int[] longMediumInts = new int[1000];
		int[] longBigInts = new int[1000];
		Arrays.fill(longMediumInts, 256);
		Arrays.fill(longBigInts, -100800900);
		
		bw.writeValueIntArray(smallInts);
		bw.writeValueUint8Array(smallInts);
		bw.writeValueUint8Array(smallInts);
		bw.writeValueUint16Array(mediumInts1);
		bw.writeValueUint16Array(mediumInts2);
		bw.writeValueIntArray(mediumInts1);
		bw.writeValueIntArray(mediumInts2);
		bw.writeValueIntArray(bigInts1);
		bw.writeValueIntArray(bigInts2);
		bw.writeValueIntArray(longSmallInts);
		bw.writeValueIntArray(longMediumInts);
		bw.writeValueIntArray(longBigInts);
		
		byte[] bytes = os.toByteArray();
		for (byte b: bytes) {
			System.out.printf("%02x ", b);
		}
		System.out.println();
		
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		
		BintexReader br = new BintexReader(is);
		assertArrayEquals(smallInts, br.readValueIntArray());
		assertArrayEquals(smallInts, br.readValueUint8Array());
		assertArrayEquals(smallInts, br.readValueIntArray());
		assertArrayEquals(mediumInts1, br.readValueUint16Array());
		assertArrayEquals(mediumInts2, br.readValueIntArray());
		assertArrayEquals(mediumInts1, br.readValueUint16Array());
		assertArrayEquals(mediumInts2, br.readValueIntArray());
		assertArrayEquals(bigInts1, br.readValueIntArray());
		assertArrayEquals(bigInts2, br.readValueIntArray());
		assertArrayEquals(longSmallInts, br.readValueIntArray());
		assertArrayEquals(longMediumInts, br.readValueIntArray());
		assertArrayEquals(longBigInts, br.readValueIntArray());
	}
	
	@Test public void testSimpleMapValues() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		// empty map
		bw.writeValueSimpleMap(new LinkedHashMap<String, Object>());
		
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("", 0);
		map.put("int", 1);
		map.put("string", "Hello!!");
		map.put("array", new int[] {4,4,899});
		map.put("map", new LinkedHashMap<String, Object>());
		bw.writeValueSimpleMap(map);
		
		byte[] bytes = os.toByteArray();
		for (byte b: bytes) {
			System.out.printf("%02x ", b);
		}
		System.out.println();
		
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		
		BintexReader br = new BintexReader(is);
		
		// empty map
		br.readValueSimpleMap();
		
		ValueMap map2 = br.readValueSimpleMap();
		assertEquals(0, map2.get(""));
		assertEquals(0, map2.getInt(""));
		assertEquals(1, map2.getInt("int"));
		assertEquals("Hello!!", map2.getString("string"));
		assertEquals(0, map2.getSimpleMap("map").size());
		assertArrayEquals(new int[] {4,4,899}, map2.getIntArray("array"));
	}
	
	@Test public void testMixedValues() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BintexWriter bw = new BintexWriter(os);
		
		bw.writeValueInt(-1);
		bw.writeValueInt(5000);
		bw.writeValueInt(Integer.MIN_VALUE);
		
		bw.writeValueString("");
		bw.writeValueString("xyz");
		bw.writeValueString("quitelooooooooooooong");
		
		bw.writeValueIntArray(new int[50]);
		
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("xyz", 0x55443322);
		bw.writeValueSimpleMap(map);
		
		assertEquals(os.size(), bw.getPos());
		
		byte[] bytes = os.toByteArray();
		for (byte b: bytes) {
			System.out.printf("%02x ", b);
		}
		System.out.println();
		
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		
		BintexReader br = new BintexReader(is);
		assertEquals(-1, br.readValue());
		assertEquals(5000, br.readValue());
		assertEquals(Integer.MIN_VALUE, br.readValue());
		assertEquals("", br.readValue());
		assertEquals("xyz", br.readValue());
		assertEquals("quitelooooooooooooong", br.readValue());
		assertArrayEquals(new int[50], (int[]) br.readValue());
		
		@SuppressWarnings("unchecked") Map<String, Object> map2 = (Map<String, Object>) br.readValue();
		assertEquals(0x55443322, map2.get("xyz"));
		
		assertEquals(os.size(), br.getPos());
	}
}
