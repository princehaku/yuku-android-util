package yuku.capjempol;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;

import java.io.UnsupportedEncodingException;

public class HitungCapJempol {
	public static final String TAG = HitungCapJempol.class.getSimpleName();
	
	public static String hitung(Context context) {
		String packageName = context.getPackageName();
		PackageManager pm = context.getPackageManager();
		PackageInfo packageInfo;
		try {
			packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		Signature[] signatures = packageInfo.signatures;
		if (signatures == null) return null;
		
		try {
			Signature s = signatures[0];
			
			int yrc_s = yrc1(s.toByteArray(), 0);
			int yrc_p;
			yrc_p = yrc1(packageName.getBytes("utf-8"), yrc_s);
			
			return String.format("c1:y1:%08x:%08x", yrc_s, yrc_p);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	private static int yrc1(byte[] bytes, int initial) {
		int res = initial;
		
		for (byte b: bytes) {
			int n = b;
			if (n < 0) {
				n += 256;
			}
			
			res <<= 3;
			res &= 0x0fffffff;
			res += n;
		}
		
		return res;
	}
}
