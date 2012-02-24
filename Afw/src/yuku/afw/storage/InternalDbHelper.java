package yuku.afw.storage;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import yuku.afw.App;

public abstract class InternalDbHelper extends SQLiteOpenHelper {
	public static final String TAG = InternalDbHelper.class.getSimpleName();

	public InternalDbHelper() {
		super(App.context, "InternalDb", null, App.getVersionCode()); //$NON-NLS-1$
	}

	@Override public void onOpen(SQLiteDatabase db) {
		//
	};

	@Override public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate called"); //$NON-NLS-1$

		try {
			createTables(db);
			createIndexes(db);
		} catch (SQLException e) {
			Log.e(TAG, "onCreate db failed!", e); //$NON-NLS-1$
			throw e;
		}
	}

	public abstract void createTables(SQLiteDatabase db);
	public abstract void createIndexes(SQLiteDatabase db);

	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
}
