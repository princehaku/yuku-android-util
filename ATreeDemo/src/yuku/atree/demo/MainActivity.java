package yuku.atree.demo;

import android.app.*;
import android.os.*;
import android.view.*;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.io.*;

import yuku.atree.*;
import yuku.atree.nodes.*;

public class MainActivity extends Activity {
	private ListView tree;
	private TreeAdapter adapter;	
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tree = (ListView) findViewById(R.id.aTree);
		
		TreeNode root = getDemoTree();
		root = new FileTreeNode("Choose a folder", new File[] {
			Environment.getExternalStorageDirectory(),
			new File("/"),
		});
		root.setExpanded(true);
		
		adapter = new TreeAdapter();
		adapter.setRoot(root);
		adapter.setRootVisible(false);
		
		tree.setAdapter(adapter);
		
		adapter.setTreeListener(tree_listener);
		tree.setOnItemClickListener(tree_itemClick);
	}


	private DemoTreeNode getDemoTree() {
		DemoTreeNode root = new DemoTreeNode("root");
		
		root.add(new DemoTreeNode("child 1") {{
			add(new DemoTreeNode("grand child 1"));
			add(new DemoTreeNode("grand child 2"));
			add(new DemoTreeNode("grand child 3"));
			setExpanded(true);
		}});
		root.add(new DemoTreeNode("child 2"));
		root.add(new DemoTreeNode("child 3") {{
			add(new DemoTreeNode("grand child 4"));
		}});
		
		return root;
	}
	
	private TreeListener tree_listener = new BaseTreeListener() {
		
	};

	private OnItemClickListener tree_itemClick = new OnItemClickListener() {
		@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			TreeNode node = adapter.getItem(position);
			node.setExpanded(!node.getExpanded());
			adapter.notifyDataSetChanged();
		};
	};
	
	class DemoTreeNode extends BaseMutableTreeNode {
		private final String text;

		public DemoTreeNode(String text) {
			this.text = text;
		}

		@Override public View getView(int position, View convertView, ViewGroup parent, int level, TreeNodeIconType iconType, int[] lines) {
			View res = convertView != null? convertView: getLayoutInflater().inflate(R.layout.item_node, null);
			
			TextView lText = (TextView) res.findViewById(R.id.lText);
			ImageView imgNodeIcon = (ImageView) res.findViewById(R.id.imgNodeIcon);
			
			lText.setText("" + level + " " + text);
			MarginLayoutParams lp = (MarginLayoutParams) imgNodeIcon.getLayoutParams();
			lp.leftMargin = 20 * level;
			
			return res;
		}
	}
	
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
			View res = convertView != null ? convertView : getLayoutInflater().inflate(R.layout.item_node, null);

			TextView lText = (TextView) res.findViewById(R.id.lText);
			ImageView imgNodeIcon = (ImageView) res.findViewById(R.id.imgNodeIcon);

			lText.setText(label != null? label: file.getName());
			MarginLayoutParams lp = (MarginLayoutParams) imgNodeIcon.getLayoutParams();
			lp.leftMargin = (int) (getResources().getDisplayMetrics().density * 20) * (level - 1);

			imgNodeIcon.setImageResource(file.isDirectory() ? android.R.drawable.presence_online : android.R.drawable.presence_offline);

			return res;
		}

		@Override protected BaseFileTreeNode generateForFile(File file) {
			return new FileTreeNode(file);
		}
		
		@Override protected boolean showHidden() {
			return false;
		}
		
		@Override protected boolean showDirectoriesOnly() {
			return true;
		}
	}
}
