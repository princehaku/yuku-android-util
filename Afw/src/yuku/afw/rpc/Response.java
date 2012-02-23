package yuku.afw.rpc;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import yuku.afw.D;
import yuku.afw.rpc.Request.Method;
import yuku.afw.rpc.ResponseData.Type;

public class Response {
	public static final String TAG = Response.class.getSimpleName();

	public enum Validity {
		Ok,
		Cancelled,
		JsonError,
		IoError,
	}
	
	public final Validity validity;
	public final int code;
	public final String message;
	public final ResponseData data;
	public final Object jsonRoot;
	
	public Response(String json) {
		final JSONObject root;
		try {
			Object nextValue = new JSONTokener(json).nextValue();
			if (nextValue instanceof JSONObject) {
				root = (JSONObject) nextValue;
				
				if (D.EBUG) {
					new Thread() {
						@Override public void run() {
							try {
								String s = root.toString(2);
								if (s.length() < 4000) {
									Log.d(TAG, s);
								} else {
									for (int i = 0; i < s.length(); i += 4000) {
										Log.d(TAG, "[response " + i + " to " + Math.min(i + 4000, s.length()) + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
										Log.d(TAG, s.substring(i, Math.min(i + 4000, s.length())));
									}
								}
							} catch (Exception e) {
							}
						};
					}.start();
				}
			} else {
				throw new JSONException("not an JsonObject: " + json); //$NON-NLS-1$
			}
		} catch (JSONException e) {
			Log.e(TAG, "json exception from string: " + json, e); //$NON-NLS-1$
			this.validity = Validity.JsonError;
			this.code = 0;
			this.message = null;
			this.data = null;
			this.jsonRoot = null;
			return;
		}
		
		this.validity = Validity.Ok;
		this.code = root.has("code")? root.optInt("code"): 200; //$NON-NLS-1$ //$NON-NLS-2$
		this.message = root.optString("message"); //$NON-NLS-1$
		this.jsonRoot = root;
		
		Object data = root.opt("data"); //$NON-NLS-1$
		if (data instanceof JSONObject) {
			this.data = new ResponseData((JSONObject) data);
		} else {
			this.data = null;
		}
	}

	public Response(Validity validity, String message) {
		this.validity = validity;
		this.code = 0;
		this.message = message;
		this.data = null;
		this.jsonRoot = null;
	}
	
	/** for {@link Method#GET_RAW} */
	public Response(byte[] raw, int code) {
		this.validity = Validity.Ok;
		this.code = code; 
		this.message = null;
		this.data = new ResponseData(raw);
		this.jsonRoot = null;
	}

	@Override public String toString() {
		return "Response{" + validity + " " + code + " message=" + message + " data=" + ((data == null || data.type == null)? "null": data.type == Type.object? "(object)": data.type == Type.raw? (data.raw == null? "raw null": "raw len " + data.raw.length): "others") + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
