package yuku.kirimfidbek;

import android.util.*;

import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;

public class TangkapSemuaEror {
	public static final String TAG = "KirimFidbek"; //$NON-NLS-1$
	
	final PengirimFidbek pengirimFidbek_;
	
	TangkapSemuaEror(PengirimFidbek pengirimFidbek) {
		pengirimFidbek_ = pengirimFidbek;
	}
	
	private UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			StringWriter sw = new StringWriter(4000);
			e.printStackTrace(new PrintWriter(sw, true));
			
			String pesanDueh = "[DUEH2] thread: " + t.getName() + " (" + t.getId() + ") " + e.getClass().getName() + ": " + e.getMessage() + "\n" + sw.toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
			Log.w(TAG, pesanDueh);
			
			pengirimFidbek_.tambah(pesanDueh);
			pengirimFidbek_.cobaKirim();
			
			// Coba tunggu 4 detik sebelum ancur. Ato ancur aja ya?
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e1) {
			}
			
			Log.w(TAG, "DUEH selesai."); //$NON-NLS-1$
			
			System.exit(1);
		}
	};

	public void aktifkan() {
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}
}
