package yuku.kirimfidbek;

import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.*;
import android.util.Log;

public class TangkapSemuaEror {
	private final PengirimFidbek pengirimFidbek_;
	private final Activity activity_;
	private final String pesanEror_;
	
	TangkapSemuaEror(PengirimFidbek pengirimFidbek, Activity activity, String pesanEror) {
		pengirimFidbek_ = pengirimFidbek;
		activity_ = activity;
		pesanEror_ = pesanEror;
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
				new AlertDialog.Builder(activity_)
				.setMessage(pesanEror_)
				.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
				.setPositiveButton("OK", null)
				.show();
				
				Thread.sleep(4000);
			} catch (InterruptedException e1) {
			}
			
			Log.w("KirimFidbek", "DUEH selesai.");
			
			throw new RuntimeException("dari DUEH", e);
		}
	};

	public void aktifkan() {
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}
}
