package yuku.afw.rpc;

import org.json.JSONObject;

public class ResponseData {
	public static final String TAG = ResponseData.class.getSimpleName();

	public enum Type {
		object,
		raw,
	}
	
	public final Type type;
	public final JSONObject object;
	public byte[] raw;
	
	public ResponseData(JSONObject object) {
		this.type = Type.object;
		this.object = object;
		this.raw = null;
	}
	
	public ResponseData(byte[] raw) {
		this.type = Type.raw;
		this.object = null;
		this.raw = raw;
	}

	public Object get() {
		if (type == Type.object) return object;
		if (type == Type.raw) return raw;
		return null;
	}
}
