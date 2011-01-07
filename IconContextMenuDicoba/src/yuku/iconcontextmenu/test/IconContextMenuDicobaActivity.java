package yuku.iconcontextmenu.test;

import yuku.iconcontextmenu.*;
import yuku.iconcontextmenu.IconContextMenu.IconContextItemSelectedListener;
import android.app.Activity;
import android.content.*;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

public class IconContextMenuDicobaActivity extends Activity implements IconContextItemSelectedListener, OnCancelListener, OnDismissListener {
	private IconContextMenu iconContextMenu = null;
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    }
    
    public void bDemo1_click(View v) {
    	initContextMenu();
    	iconContextMenu.setInfo(v);
    	iconContextMenu.show();
    }

	private void initContextMenu() {
    	if (iconContextMenu == null) {
    		iconContextMenu = new IconContextMenu(this, R.menu.demo);
    		iconContextMenu.setTitle("See the icons?! Nice?");
    		iconContextMenu.setOnIconContextItemSelectedListener(this);
    		iconContextMenu.setOnCancelListener(this);
    		iconContextMenu.setOnDismissListener(this);
    	}
	}
	
	@Override
	public void onIconContextItemSelected(MenuItem item, Object info) {
		Toast.makeText(this, "menuItem: " + item + " info: " + info, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		Toast.makeText(this, "onDismiss", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		Toast.makeText(this, "onCancel", Toast.LENGTH_SHORT).show();
	}
}