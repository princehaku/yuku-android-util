package yuku.afw;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

// This is a temp comment just to make a commit possible
public class App extends Application {
	public static final String TAG = App.class.getSimpleName();

	public static Context context;
	
	@Override public void onCreate() {
		super.onCreate();

		context = getApplicationContext();
	}

	private static PackageInfo packageInfo;
	
	private static void initPackageInfo() {
		if (packageInfo == null) {
			try {
				packageInfo = App.context.getPackageManager().getPackageInfo(App.context.getPackageName(), 0);
			} catch (NameNotFoundException e) {
				throw new RuntimeException("NameNotFoundException when querying own package. Should not happen", e);
			}
		}
	}
	
	public static String getVersionName() {
		initPackageInfo();
		return packageInfo.versionName;
	}
	
	public static int getVersionCode() {
		initPackageInfo();
		return packageInfo.versionCode;
	}
}
