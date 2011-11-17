package yuku.filechooser.demo;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import yuku.filechooser.*;
import yuku.filechooser.FileChooserConfig.Mode;

public class FileChooserDemoActivity extends Activity {
	private static final int REQCODE_showFileChooser = 1;
	private static final int REQCODE_showFolderChooser = 2;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void bShowFileChooser_click(View v) {
		EditText tInitialDir = (EditText) findViewById(R.id.tInitialDir);
		EditText tTitle = (EditText) findViewById(R.id.tTitle);
		EditText tPattern = (EditText) findViewById(R.id.tPattern);

		FileChooserConfig config = new FileChooserConfig();
		config.mode = Mode.Open;
		config.initialDir = tInitialDir.length() == 0 ? null : tInitialDir.getText().toString();
		config.title = tTitle.length() == 0 ? null : tTitle.getText().toString();
		config.pattern = tPattern.length() == 0 ? null : tPattern.getText().toString();

		startActivityForResult(FileChooserActivity.createIntent(getApplicationContext(), config), REQCODE_showFileChooser);
	}

	public void bShowFolderChooser_click(View v) {
		EditText tInitialDir = (EditText) findViewById(R.id.tInitialDir);
		EditText tTitle = (EditText) findViewById(R.id.tTitle);
		CheckBox cShowHidden = (CheckBox) findViewById(R.id.cShowHidden);

		FolderChooserConfig config = new FolderChooserConfig();
		config.roots = Arrays.asList(Environment.getExternalStorageDirectory().toString(), tInitialDir.getText().toString());
		config.showHidden = cShowHidden.isChecked();
		config.title = tTitle.length() == 0 ? null : tTitle.getText().toString();

		startActivityForResult(FolderChooserActivity.createIntent(getApplicationContext(), config), REQCODE_showFolderChooser);
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQCODE_showFileChooser) {

		}
		if (requestCode == REQCODE_showFolderChooser) {
			if (resultCode == RESULT_OK) {
				FolderChooserResult result = FolderChooserActivity.obtainResult(data);
				new AlertDialog.Builder(this)
				.setTitle("Result")
				.setMessage(result.selectedFolder)
				.show();
			}
		}
	}
}