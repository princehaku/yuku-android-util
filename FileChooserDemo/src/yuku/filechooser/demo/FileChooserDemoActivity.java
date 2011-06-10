package yuku.filechooser.demo;

import yuku.filechooser.*;
import yuku.filechooser.FileChooserConfig.Mode;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class FileChooserDemoActivity extends Activity {
	private static final int REQCODE_show = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void bShow_click(View v) {
		EditText tInitialDir = (EditText) findViewById(R.id.tInitialDir);
		EditText tTitle = (EditText) findViewById(R.id.tTitle);
		EditText tPattern = (EditText) findViewById(R.id.tPattern);
		
		FileChooserConfig config = new FileChooserConfig();
		config.mode = Mode.Open;
		config.initialDir = tInitialDir.length() == 0? null: tInitialDir.getText().toString();
		config.title = tTitle.length() == 0? null: tTitle.getText().toString();
		config.pattern = tPattern.length() == 0? null: tPattern.getText().toString();
		
		startActivityForResult(FileChooserActivity.createIntent(getApplicationContext(), config), REQCODE_show);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQCODE_show) {
			
		}
	}
}