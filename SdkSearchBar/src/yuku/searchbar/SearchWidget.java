package yuku.searchbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.SearchView;

import yuku.androidsdk.searchbar.SearchBar;

public class SearchWidget extends FrameLayout {
	public static final String TAG = SearchWidget.class.getSimpleName();

	public interface OnQueryTextListener {
        /**
         * Called when the query text is changed by the user.
         *
         * @param newText the new content of the query text field.
         *
         * @return false if the SearchView should perform the default action of showing any
         * suggestions if available, true if the action was handled by the listener.
         */
		boolean onQueryTextChange(SearchWidget searchWidget, String newText);
		
        /**
         * Called when the user submits the query. This could be due to a key press on the
         * keyboard or due to pressing a submit button.
         * The listener can override the standard behavior by returning true
         * to indicate that it has handled the submit request. Otherwise return false to
         * let the SearchView handle the submission by launching any associated intent.
         *
         * @param query the query text that is to be submitted
         *
         * @return true if the query has been handled by the listener, false to let the
         * SearchView perform the default action.
         */
		boolean onQueryTextSubmit(SearchWidget searchWidget, String query);
	}
	
	public static abstract class SimpleOnQueryTextListener implements OnQueryTextListener {
		@Override public boolean onQueryTextChange(SearchWidget searchWidget, String newText) {
			return false;
		};
		
		@Override public boolean onQueryTextSubmit(SearchWidget searchWidget, String query) {
			return false;
		};
	}

	SearchBar searchBar;
	SearchView searchView;
	
	private OnQueryTextListener listener;
	
	public SearchWidget(Context context) {
		super(context);
		init();
	}
	
	public SearchWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	@TargetApi(11) private void init() {
		LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		if (Build.VERSION.SDK_INT >= 11) {
			searchView = new SearchView(getContext());
			searchView.setIconifiedByDefault(false);
			searchView.setSubmitButtonEnabled(true);
			searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override public boolean onQueryTextSubmit(String query) {
					if (listener != null) {
						return listener.onQueryTextSubmit(SearchWidget.this, query);
					}
					return false;
				}
				
				@Override public boolean onQueryTextChange(String newText) {
					if (listener != null) {
						return listener.onQueryTextChange(SearchWidget.this, newText);
					}
					return false;
				}
			});
			this.addView(searchView, lp);
		} else {
			searchBar = new SearchBar(getContext());
			searchBar.setOnSearchListener(new SearchBar.OnSearchListener() {
				@Override public void onSearch(SearchBar searchBar, Editable text) {
					if (listener != null) {
						listener.onQueryTextSubmit(SearchWidget.this, text.toString());
					}
				}
			});
			searchBar.getSearchField().addTextChangedListener(new TextWatcher() {
				@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
				@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override public void afterTextChanged(Editable s) {
					if (listener != null) {
						listener.onQueryTextChange(SearchWidget.this, s.toString());
					}
				}
			});
			this.addView(searchBar, lp);
		}
	}

	public void setOnQueryTextListener(OnQueryTextListener listener) {
		this.listener = listener;
	}
}
