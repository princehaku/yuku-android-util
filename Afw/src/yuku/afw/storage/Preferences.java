package yuku.afw.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import yuku.afw.App;

public class Preferences {
	private static final String TAG = Preferences.class.getSimpleName();
	
	private static SharedPreferences cache;
	private static boolean dirty = true;
	
	public static void invalidate() {
		dirty = true;
	}
	
	public static int getInt(Enum<?> key, int def) {
		SharedPreferences pref = read(App.context);
		return pref.getInt(key.toString(), def);
	}
	
	public static float getFloat(Enum<?> key, float def) {
		SharedPreferences pref = read(App.context);
		return pref.getFloat(key.toString(), def);
	}
	
	public static long getLong(Enum<?> key, long def) {
		SharedPreferences pref = read(App.context);
		return pref.getLong(key.toString(), def);
	}
	
	public static long getLong(String key, long def) {
		SharedPreferences pref = read(App.context);
		return pref.getLong(key, def);
	}
	
	public static String getString(Enum<?> key, String def) {
		SharedPreferences pref = read(App.context);
		return pref.getString(key.toString(), def);
	}
	
	public static boolean getBoolean(Enum<?> key, boolean def) {
		SharedPreferences pref = read(App.context);
		return pref.getBoolean(key.toString(), def);
	}
	
	public static boolean getBoolean(String key, boolean def) {
		SharedPreferences pref = read(App.context);
		return pref.getBoolean(key, def);
	}
	
	public static int getInt(int keyResId, int defResId) {
		SharedPreferences pref = read(App.context);
		return pref.getInt(App.context.getString(keyResId), App.context.getResources().getInteger(defResId));
	}
	
	public static int getInt(String key, int def) {
		SharedPreferences pref = read(App.context);
		return pref.getInt(key, def);
	}
	
	public static float getFloat(int keyResId, float def) {
		SharedPreferences pref = read(App.context);
		return pref.getFloat(App.context.getString(keyResId), def);
	}
	
	public static String getString(int keyResId, int defResId) {
		SharedPreferences pref = read(App.context);
		return pref.getString(App.context.getString(keyResId), App.context.getString(defResId));
	}
	
	public static String getString(int keyResId, String def) {
		SharedPreferences pref = read(App.context);
		return pref.getString(App.context.getString(keyResId), def);
	}
	
	public static boolean getBoolean(int keyResId, int defResId) {
		SharedPreferences pref = read(App.context);
		return pref.getBoolean(App.context.getString(keyResId), App.context.getResources().getBoolean(defResId));
	}
	
	
	public static void setInt(Enum<?> key, int val) {
		SharedPreferences pref = read(App.context);
		pref.edit().putInt(key.toString(), val).commit();
		Log.d(TAG, key + " = (int) " + val); //$NON-NLS-1$
	}
	
	public static void setInt(String key, int val) {
		SharedPreferences pref = read(App.context);
		pref.edit().putInt(key, val).commit();
		Log.d(TAG, key + " = (int) " + val); //$NON-NLS-1$
	}
	
	public static void setLong(Enum<?> key, long val) {
		SharedPreferences pref = read(App.context);
		pref.edit().putLong(key.toString(), val).commit();
		Log.d(TAG, key + " = (long) " + val); //$NON-NLS-1$
	}
	
	public static void setLong(String key, long val) {
		SharedPreferences pref = read(App.context);
		pref.edit().putLong(key, val).commit();
		Log.d(TAG, key + " = (long) " + val); //$NON-NLS-1$
	}
	
	public static void setString(Enum<?> key, String val) {
		SharedPreferences pref = read(App.context);
		pref.edit().putString(key.toString(), val).commit();
		Log.d(TAG, key + " = (string) " + val); //$NON-NLS-1$
	}
	
	public static void setBoolean(Enum<?> key, boolean val) {
		SharedPreferences pref = read(App.context);
		pref.edit().putBoolean(key.toString(), val).commit();
		Log.d(TAG, key + " = (bool) " + val); //$NON-NLS-1$
	}
	
	public static void setBoolean(String key, boolean val) {
		SharedPreferences pref = read(App.context);
		pref.edit().putBoolean(key, val).commit();
		Log.d(TAG, key + " = (bool) " + val); //$NON-NLS-1$
	}
	
	public static void remove(Enum<?> key) {
		SharedPreferences pref = read(App.context);
		pref.edit().remove(key.toString()).commit();
		Log.d(TAG, key + " removed"); //$NON-NLS-1$
	}
	
	public static void remove(String key) {
		SharedPreferences pref = read(App.context);
		pref.edit().remove(key).commit();
		Log.d(TAG, key + " removed"); //$NON-NLS-1$
	}
	
	private static SharedPreferences read(Context context) {
		SharedPreferences res;
		if (dirty || cache == null) {
			Log.d(TAG, "Preferences are read from disk"); //$NON-NLS-1$
			res = PreferenceManager.getDefaultSharedPreferences(App.context);
			dirty = false;
			cache = res;
		} else {
			res = cache;
		}
		
		return res;
	}

	public static Set<String> getAllKeys() {
		SharedPreferences pref = read(App.context);
		Map<String, ?> all = pref.getAll();
		return new HashSet<String>(all.keySet());
	}
}
