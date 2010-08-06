package yuku.kirimfidbek;

import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;

import android.util.*;

public class TangkapSemuaEror {
	private final PengirimFidbek pengirimFidbek_;
	
	TangkapSemuaEror(PengirimFidbek pengirimFidbek) {
		pengirimFidbek_ = pengirimFidbek;
	}
	
	private UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			StringWriter sw = new StringWriter(4000);
			e.printStackTrace(new PrintWriter(sw, true));
			
			String pesanDueh = "[DUEH] thread: " + t.getName() + " (" + t.getId() + ")\n" + sw.toString();
			
			Log.w("KirimFidbek", pesanDueh);
			
			pengirimFidbek_.tambah(pesanDueh);
			pengirimFidbek_.cobaKirim();
			
			// Coba tunggu 4 detik sebelum ancur. Ato ancur aja ya?
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e1) {
			}
			
			Log.w("KirimFidbek", "DUEH selesai.");
			
			System.exit(1);
		}
	};

	public void aktifkan() {
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}
}
