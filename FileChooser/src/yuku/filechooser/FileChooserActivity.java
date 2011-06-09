package yuku.filechooser;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class FileChooserActivity extends Activity {
	private static final String EXTRA_config = "config"; //$NON-NLS-1$
	private static final String EXTRA_result = null;

	public static Intent createIntent(Context context, FileChooserConfig config) {
		Intent res = new Intent(context, FileChooserActivity.class);
		res.putExtra(EXTRA_config, config);
		return res;
	}
	
	public static FileChooserResult obtainResult(Intent data) {
		if (data == null) return null;
		return data.getParcelableExtra(EXTRA_result);
	}
	
	ListView lsFile;
	
	FileChooserConfig config;
	FileAdapter adapter;
	File cd;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = getIntent().getParcelableExtra(EXTRA_config);
        
        if (config.title == null) {
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else {
        	setTitle(config.title);
        }
        
        setContentView(R.layout.filechooser_activity_filechooser);
        
        lsFile = (ListView) findViewById(R.id.filechooser_lsFile);
        lsFile.setAdapter(adapter = new FileAdapter());
        lsFile.setOnItemClickListener(lsFile_itemClick);
        
        init();
    }

    private OnItemClickListener lsFile_itemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			File file = adapter.getItem(position);
			if (file != null) {
				if (file.isDirectory()) {
					cd = file;
					ls();
				} else {
					FileChooserResult result = new FileChooserResult();
					result.currentDir = cd.getAbsolutePath();
					result.firstFilename = file.getAbsolutePath();
					
					Intent data = new Intent();
					data.putExtra(EXTRA_result, result);
					
					setResult(RESULT_OK, data);
					finish();
				}
			}
		}
	};
	
	private void init() {
		if (config.initialDir != null) {
			cd = new File(config.initialDir);
		} else {
			cd = Environment.getExternalStorageDirectory();
		}
		
		ls();
	}

	private void ls() {
		File[] files = cd.listFiles(new FileFilter() {
			Matcher m;

			@Override
			public boolean accept(File pathname) {
				if (config.pattern == null) {
					return true;
				}
				
				if (pathname.isDirectory()) {
					return true;
				}
				
				if (m == null) {
					m = Pattern.compile(config.pattern).matcher(""); //$NON-NLS-1$
				}
				
				m.reset(pathname.getName());
				return m.matches();
			}
		});
		
		if (files == null) {
			files = new File[0];
		}
		
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File a, File b) {
				if (a.isDirectory() && !b.isDirectory()) {
					return -1;
				} else if (!a.isDirectory() && b.isDirectory()) {
					return +1;
				}
				// both files or both dirs
				
				String aname = a.getName();
				String bname = b.getName();
				
				// dot-files are later
				if (aname.startsWith(".") && !bname.startsWith(".")) { //$NON-NLS-1$ //$NON-NLS-2$
					return +1;
				} else if (!aname.startsWith(".") && bname.startsWith(".")) { //$NON-NLS-1$ //$NON-NLS-2$
					return -1;
				}
				
				return aname.compareToIgnoreCase(bname);
			}
		});
		
		adapter.setNewData(files);
		lsFile.setSelection(0);
	}
    
	class FileAdapter extends BaseAdapter {
		File[] files;
		
		@Override
		public int getCount() {
			return (files == null? 0: files.length) + 1;
		}

		public void setNewData(File[] files) {
			this.files = files;
			notifyDataSetChanged();
		}

		@Override
		public File getItem(int position) {
			if (files == null) return null;
			if (position == 0) return cd.getParentFile();
			return files[position - 1];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView res = (TextView) (convertView != null? convertView: LayoutInflater.from(getApplicationContext()).inflate(android.R.layout.simple_list_item_1, null));
			
			if (position == 0) {
				res.setText(R.string.filechooser_parent_folder);
				res.setCompoundDrawablesWithIntrinsicBounds(R.drawable.filechooser_up, 0, 0, 0);
			} else {
				File file = getItem(position);
				res.setText(file.getName());
				res.setCompoundDrawablesWithIntrinsicBounds(file.isDirectory()? R.drawable.filechooser_folder: R.drawable.filechooser_file, 0, 0, 0);
			}
			
			return res;
		}
	}
}
