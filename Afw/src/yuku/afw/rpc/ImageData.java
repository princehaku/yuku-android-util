package yuku.afw.rpc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

public abstract class ImageData extends BaseData {
	public static final String TAG = ImageData.class.getSimpleName();

	public Bitmap bitmap;
	public Options opts;
	
	class ImageProcessor implements ResponseProcessor {
		@Override public void process(byte[] raw) throws Exception {
			opts = new Options();
			bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.length, opts);
		}
	}
		
	@Override public ResponseProcessor getResponseProcessor(Response response) {
		return new ImageProcessor();
	}
}
