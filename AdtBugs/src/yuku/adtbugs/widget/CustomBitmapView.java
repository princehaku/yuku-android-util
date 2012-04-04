package yuku.adtbugs.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import yuku.adtbugs.R;

public class CustomBitmapView extends View {
	public static final String TAG = CustomBitmapView.class.getSimpleName();
	private Bitmap bitmap;

	public CustomBitmapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap640x200px);
	}
	
	@Override protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(bitmap, 0, 0, null);
	}
	
	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(resolveSize(bitmap.getWidth(), widthMeasureSpec), resolveSize(bitmap.getHeight(), heightMeasureSpec));
	}
}
