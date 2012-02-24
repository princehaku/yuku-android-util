package yuku.afw.rpc;

import org.json.JSONTokener;

public abstract class ResponseProcessor {
	public static final String TAG = ResponseProcessor.class.getSimpleName();
	
	public static class Kind {
		public static class Json extends ResponseProcessor {
			@Override public Object process(byte[] raw) throws Exception {
				return new JSONTokener(new String(raw, "utf-8")).nextValue();
			}
		}
	}
	
	public abstract Object process(byte[] raw) throws Exception;
}
