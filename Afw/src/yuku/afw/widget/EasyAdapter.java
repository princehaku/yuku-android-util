package yuku.afw.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class EasyAdapter extends BaseAdapter {
	public static final String TAG = EasyAdapter.class.getSimpleName();

	@Override public Object getItem(int position) {
		return null;
	}

	@Override public long getItemId(int position) {
		return position;
	}
	
	@Override public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = newView(position, parent);
		}
		bindView(convertView, position, parent);
		return convertView;
	}
	
	public abstract View newView(int position, ViewGroup parent);
	public abstract void bindView(View view, int position, ViewGroup parent);
}
