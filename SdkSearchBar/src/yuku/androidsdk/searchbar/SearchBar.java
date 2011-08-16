package yuku.androidsdk.searchbar;

import android.content.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import android.widget.TextView.BufferType;
import android.widget.TextView.OnEditorActionListener;

public class SearchBar extends LinearLayout {
	public static final String TAG = SearchBar.class.getSimpleName();
	
	public interface OnSearchListener {
		void onSearch(SearchBar searchBar, Editable text);
	}
	
	TextView lBadge;
	EditText tSearch;
	Button bSearch;
	Button bExtra1;
	OnSearchListener onSearchListener;
	
	public SearchBar(Context context) {
		super(context);
		init();
	}

	public SearchBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.search_bar, this);
		
        lBadge = (TextView) findViewById(R.id.search_badge);
        tSearch = (EditText) findViewById(R.id.search_src_text);
        bSearch = (Button) findViewById(R.id.search_go_btn);
        bExtra1 = (Button) findViewById(R.id.search_extra1_btn);
        
        tSearch.setOnEditorActionListener(new OnEditorActionListener() {
			@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					if (onSearchListener != null) {
						onSearchListener.onSearch(SearchBar.this, tSearch.getText());
					}
					return true;
				}
				return false;
			}
		});
        bSearch.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if (onSearchListener != null) {
					onSearchListener.onSearch(SearchBar.this, tSearch.getText());
				}
			}
		});
        
        lBadge.setVisibility(View.GONE);
	}

	public Editable getText() {
		return tSearch.getText();
	}

	public void setText(CharSequence text, BufferType type) {
		tSearch.setText(text, type);
	}

	public final void setText(CharSequence text) {
		tSearch.setText(text);
	}
	
	public void setOnSearchListener(OnSearchListener l) {
		this.onSearchListener = l;
	}
	
	public EditText getSearchField() {
		return tSearch;
	}
	
	public Button getSearchButton() {
		return bSearch;
	}
	
	public Button getSearchExtra1() {
		return bExtra1;
	}
}
