package yuku.easybilling;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import yuku.easybilling.BillingSecurity.SignedData;

public class BillingDb {
	public static final String TAG = BillingDb.class.getSimpleName();

	private static BillingDb instance = null;
	
	public static BillingDb get() {
		if (instance == null) {
			throw new RuntimeException("Pass in context for first time");
		}
		return instance;
	}
	
	public static synchronized BillingDb get(Context context) {
		if (instance == null) {
			instance = new BillingDb(new BillingDbHelper(context));
		}
		return instance;
	}
	
	protected final BillingDbHelper helper;

	private BillingDb(BillingDbHelper helper) {
		this.helper = helper;
	}

	public int getInventoryAmount(String productId) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor c = db.query("Inventory", new String[] {"amount"}, "productId=?", new String[] {productId}, null, null, null);
		try {
			if (c.moveToNext()) {
				return c.getInt(0);
			}
			return 0;
		} finally {
			c.close();
		}
	}

	public Map<String, Integer> getAllInventory() {
		Map<String, Integer> res = new LinkedHashMap<String, Integer>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor c = db.query("Inventory", new String[] {"productId", "amount"}, null, null, null, null, null);
		try {
			while (c.moveToNext()) {
				String productId = c.getString(0);
				int amount = c.getInt(1);
				res.put(productId, amount);
			}
		} finally {
			c.close();
		}
		return res;
	}

	public void updateWithChange(SignedData.Order change, Set<EasyBillingListener> listenerSet) {
		SQLiteDatabase db = helper.getWritableDatabase();
		String orderId = change.orderId;
		String productId = change.productId;
		
		Log.d(TAG, "@@updateWithOrder orderId: " + orderId);
		
		db.beginTransaction();
		try {
			{ // put to permanent log
				ContentValues cv = new ContentValues();
				cv.put("orderId", change.orderId);
				cv.put("packageName", change.packageName);
				cv.put("productId", change.productId);
				cv.put("purchaseTime", change.purchaseTime);
				cv.put("purchaseState", change.purchaseState.ordinal());
				cv.put("developerPayload", change.developerPayload);
				db.insert("Changes", null, cv);
			}
			
			boolean orderInDb = count(db, "Orders", "orderId=?", orderId) > 0;
			Log.d(TAG, "This order is in db?: " + orderInDb);
			
			if (orderInDb) {
				PurchaseState oldPurchaseState = PurchaseState.valueOf(getInt(db, "Orders", "purchaseState", -1, "orderId=?", orderId));
				PurchaseState newPurchaseState = change.purchaseState;
				boolean oldHave = oldPurchaseState == PurchaseState.PURCHASED;
				boolean newHave = newPurchaseState == PurchaseState.PURCHASED;
				
				{ // update db
					ContentValues cv = new ContentValues();
					cv.put("orderId", change.orderId);
					cv.put("purchaseState", change.purchaseState.ordinal());
					db.update("Orders", cv, "orderId=?", new String[] {orderId});
				}
				
				if (oldHave == newHave) {
					Log.d(TAG, "productId " + change.productId + " inventory not updated, purchase state old=" + oldPurchaseState + " new=" + newPurchaseState);
				} else {
					int[] amounts = modifyAmount(db, productId, newHave? +1: -1);
					for (EasyBillingListener listener: listenerSet) {
						listener.onInventoryAmountChange(productId, newPurchaseState, amounts[0], amounts[1]); 
					}
				}
			} else {
				PurchaseState newPurchaseState = change.purchaseState;
				boolean newHave = newPurchaseState == PurchaseState.PURCHASED;
				
				{ // insert to db
					ContentValues cv = new ContentValues();
					cv.put("orderId", change.orderId);
					cv.put("purchaseState", change.purchaseState.ordinal());
					db.insert("Orders", null, cv);
				}
				
				if (newHave) {
					int[] amounts = modifyAmount(db, productId, +1);
					for (EasyBillingListener listener: listenerSet) {
						listener.onInventoryAmountChange(productId, newPurchaseState, amounts[0], amounts[1]); 
					}
				} else {
					Log.d(TAG, "new purchase state: " + newPurchaseState + " for non-existing product in inventory, not modifying amount");
				}
			}
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	private int[] modifyAmount(SQLiteDatabase db, String productId, int delta) {
		int oldAmount = getInt(db, "Inventory", "amount", 0, "productId=?", productId);
		int newAmount = oldAmount + delta;
		
		if (newAmount < 0) {
			Log.w(TAG, "New inventory amount of " + productId + " is negative: " + newAmount + " was: " + oldAmount + ", using 0 instead");
			newAmount = 0;
		}
		
		ContentValues cv = new ContentValues();
		cv.put("productId", productId);
		cv.put("amount", newAmount);
		
		if (count(db, "Inventory", "productId=?", productId) > 0) { // already on the table
			db.update("Inventory", cv, "productId=?", new String[] {productId});
		} else {
			db.insert("Inventory", null, cv);
		}

		Log.w(TAG, "Inventory amount of " + productId + " was: " + oldAmount + ", now: " + newAmount);
		return new int[] {oldAmount, newAmount};
	}

	private int count(SQLiteDatabase db, String table, String whereClause, String... whereArgs) {
		Cursor c = db.query(table, new String[] {"count(*)"}, whereClause, whereArgs, null, null, null);
		try {
			c.moveToNext();
			return c.getInt(0);
		} finally {
			c.close();
		}
	}

	private int getInt(SQLiteDatabase db, String table, String column, int def, String whereClause, String... whereArgs) {
		Cursor c = db.query(table, new String[] {column}, whereClause, whereArgs, null, null, null);
		try {
			if (c.moveToNext()) {
				return c.getInt(0);
			} else {
				return def;
			}
		} finally {
			c.close();
		}
	}
}
