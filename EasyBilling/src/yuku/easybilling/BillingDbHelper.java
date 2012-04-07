package yuku.easybilling;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BillingDbHelper extends SQLiteOpenHelper {
	public static final String TAG = BillingDbHelper.class.getSimpleName();

	public BillingDbHelper(Context context) {
		super(context, "BillingDb", null, getVersionCode(context)); //$NON-NLS-1$
	}

	@Override public void onOpen(SQLiteDatabase db) {
		//
	};

	@Override public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "@@onCreate"); //$NON-NLS-1$

		try {
			createTables(db);
			createIndexes(db);
		} catch (SQLException e) {
			Log.e(TAG, "onCreate db failed!", e); //$NON-NLS-1$
			throw e;
		}
	}

	public void createTables(SQLiteDatabase db) {
		db.execSQL("create table if not exists Inventory ( " +
			"_id integer primary key autoincrement, " + //$NON-NLS-1$
			"productId text, " +
			"amount integer" +
		")");
		
		db.execSQL("create table if not exists Orders ( " +
			"_id integer primary key autoincrement, " + //$NON-NLS-1$
			"orderId text, " +
			"purchaseState integer " +
		")");
		
		db.execSQL("create table if not exists Changes ( " +
			"_id integer primary key autoincrement, " + //$NON-NLS-1$
			"orderId text, " +
			"packageName text," +
			"productId text," +
			"purchaseTime integer," +
			"purchaseState integer," +
			"developerPayload text" +
		")");
	}
	
	public void createIndexes(SQLiteDatabase db) {
	}

	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	public static int getVersionCode(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("NameNotFoundException when querying own package. Should not happen", e);
		}
	}
}
