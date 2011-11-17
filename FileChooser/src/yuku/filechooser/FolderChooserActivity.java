package yuku.filechooser;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.BufferType;

import java.io.*;
import java.util.*;

import yuku.atree.*;
import yuku.atree.nodes.*;

public class FolderChooserActivity extends Activity {
	static final String EXTRA_config = "config"; //$NON-NLS-1$
	static final String EXTRA_result = null;

	public static Intent createIntent(Context context, FolderChooserConfig config) {
		Intent res = new Intent(context, FolderChooserActivity.class);
		res.putExtra(EXTRA_config, config);
		return res;
	}

	public static FolderChooserResult obtainResult(Intent data) {
		if (data == null) return null;
		return data.getParcelableExtra(EXTRA_result);
	}

	ListView tree;
	Button bOk;
	TextView lPath;
	
	FolderChooserConfig config;
	TreeAdapter adapter;
	File selectedDir;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		config = getIntent().getParcelableExtra(EXTRA_config);

		if (config.title == null) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		} else {
			setTitle(config.title);
		}

		setContentView(R.layout.filechooser_activity_folderchooser);

		tree = (ListView) findViewById(R.id.filechooser_tree);
		bOk = (Button) findViewById(R.id.filechooser_bOk);
		lPath = (TextView) findViewById(R.id.filechooser_lPath);
		
		adapter = new TreeAdapter();
		tree.setAdapter(adapter);
		tree.setOnItemClickListener(tree_itemClick);
		
		bOk.setOnClickListener(bOk_click);

		setSelectedDir(null);
		
		if (config.roots == null || config.roots.size() == 0) {
			config.roots = Arrays.asList(Environment.getExternalStorageDirectory().getAbsolutePath());
		}
		
		FileTreeNode root;
		
		if (config.roots.size() == 1) {
			root = new FileTreeNode(new File(config.roots.get(0)));
			adapter.setRootVisible(true);
		} else {
			File[] children = new File[config.roots.size()];
			for (int i = 0; i < config.roots.size(); i++) {
				children[i] = new File(config.roots.get(i));
			}
			root = new FileTreeNode("root", children);
			adapter.setRootVisible(false);
			root.setExpanded(true);
		}
		
		adapter.setRoot(root);
	}

	private OnItemClickListener tree_itemClick = new OnItemClickListener() {
		@Override public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			FileTreeNode node = (FileTreeNode) adapter.getItem(position);
			node.setExpanded(!node.getExpanded());
			adapter.notifyDataSetChanged();

			setSelectedDir(node.getFile());
		}
	};
	
	OnClickListener bOk_click = new OnClickListener() {
		@Override public void onClick(View v) {
			if (selectedDir == null) return;
			
			FolderChooserResult result = new FolderChooserResult();
			result.selectedFolder = selectedDir.getAbsolutePath(); 
			
			Intent data = new Intent();
			data.putExtra(EXTRA_result, result);
			
			setResult(RESULT_OK, data);
			finish();
		}
	};

	class FileTreeNode extends BaseFileTreeNode {
		private String label;

		public FileTreeNode(File file) {
			super(file);
		}
		
		public FileTreeNode(String label, File[] virtualChildren) {
			super(virtualChildren);
			this.label = label;
		}

		@Override public View getView(int position, View convertView, ViewGroup parent, int level, TreeNodeIconType iconType, int[] lines) {
			TextView res = (TextView) (convertView != null? convertView: LayoutInflater.from(getApplicationContext()).inflate(android.R.layout.simple_list_item_1, null));
			
			res.setPadding((int) ((getResources().getDisplayMetrics().density) * (20 * (level - 1) + 6)), 0, 0, 0);
			res.setText(label != null? label: file.getName());
			res.setCompoundDrawablesWithIntrinsicBounds(file.isDirectory()? R.drawable.filechooser_folder: R.drawable.filechooser_file, 0, 0, 0);
			
			return res;
		}

		@Override protected BaseFileTreeNode generateForFile(File file) {
			return new FileTreeNode(file);
		}
		
		@Override protected boolean showDirectoriesOnly() {
			return true;
		}
		
		@Override protected boolean showHidden() {
			return config.showHidden;
		}
	}

	protected void setSelectedDir(File dir) {
		this.selectedDir = dir;
		bOk.setEnabled(dir != null);
		
		if (dir != null) {
			SpannableStringBuilder sb = new SpannableStringBuilder();
			String parent = dir.getParent();
			sb.append(parent == null? "": parent + "/");
			int sb_len = sb.length();
			sb.append(dir.getName());
			sb.setSpan(new StyleSpan(Typeface.BOLD), sb_len, sb.length(), 0);
			lPath.setText(sb, BufferType.SPANNABLE);
		} else {
			lPath.setText("");
		}
	}
}
