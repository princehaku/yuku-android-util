package yuku.bintex.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Semaphore;

import org.junit.Test;

import yuku.bintex.BintexReader;
import yuku.bintex.BintexWriter;

public class ConcurrentTests {
	public static final String TAG = ConcurrentTests.class.getSimpleName();

	@Test public void testReadWriteInt() throws Exception {
		final Semaphore sema = new Semaphore(0);
		
		class ReadWriteInt extends Thread {
			public ReadWriteInt(int id) {
				super("" + id);
			}
			
			@Override public void run() {
				System.out.println("Thread " + getName() + " start");
				try {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					BintexWriter bw = new BintexWriter(os);
					
					for (int i = 0; i < 10000; i++) {
						bw.writeUint16(i);
						bw.writeValueString("" + i);
					}
					
					BintexWriter bw2 = new BintexWriter(os);
					
					for (int i = 0; i < 10000; i++) {
						bw2.writeValueString("\u9988" + i);
					}
					
					sleep((long) (Math.random() * 100));
					
					ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
					
					BintexReader br = new BintexReader(is);
					for (int i = 0; i < 10000; i++) {
						assertEquals(i, br.readUint16());
						assertEquals("" + i, br.readValueString());
					}
					
					BintexReader br2 = new BintexReader(is);
					for (int i = 0; i < 10000; i++) {
						assertEquals("\u9988" + i, br2.readValueString());
					}
					
					sema.release();
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("Thread " + getName() + " finish");
			}
		}
		
		ReadWriteInt[] threads = new ReadWriteInt[100];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ReadWriteInt(i);
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		for (int i = 0; i < threads.length; i++) {
			sema.acquire();
		}
	}
}
