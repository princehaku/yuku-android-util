package yuku.ambilwarna.dicoba;

import yuku.ambilwarna.*;
import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class CobaAmbilWarnaActivity extends Activity {
	int warna = 0xffffff00;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        findViewById(R.id.Button01).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(CobaAmbilWarnaActivity.this, warna, new AmbilWarnaDialog.OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						Toast.makeText(getApplicationContext(), "ok color=0x" + Integer.toHexString(color), Toast.LENGTH_SHORT).show();
						warna = color;
					}
					
					@Override
					public void onCancel(AmbilWarnaDialog dialog) {
						Toast.makeText(getApplicationContext(), "cancel", Toast.LENGTH_SHORT).show();
					}
				});
				dialog.show();
			}
		});
    }
}