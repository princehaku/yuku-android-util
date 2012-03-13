package yuku.multigesture;

import yuku.multigesture.MultiGestureDetector.DragEvent;
import yuku.multigesture.MultiGestureDetector.FlickEvent;
import yuku.multigesture.MultiGestureDetector.PinchEvent;
import yuku.multigesture.MultiGestureDetector.RotateEvent;
import yuku.multigesture.MultiGestureDetector.TapEvent;

public class SimpleOnMultiGestureListener implements OnMultiGestureListener {
	public static final String TAG = SimpleOnMultiGestureListener.class.getSimpleName();

	@Override public void onDrag(DragEvent event) {
	}

	@Override public void onRotate(RotateEvent event) {
	}

	@Override public void onPinch(PinchEvent event) {
	}

	@Override public void onTap(TapEvent event) {
	}

	@Override public void onDoubleTap(TapEvent event) {
	}

	@Override public void onFlick(FlickEvent event) {
	}
}
