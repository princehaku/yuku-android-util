package yuku.multigesture;

import yuku.multigesture.MultiGestureDetector.DragEvent;
import yuku.multigesture.MultiGestureDetector.FlickEvent;
import yuku.multigesture.MultiGestureDetector.PinchEvent;
import yuku.multigesture.MultiGestureDetector.RotateEvent;
import yuku.multigesture.MultiGestureDetector.TapEvent;

public interface OnMultiGestureListener {
	void onDrag(DragEvent event);
	void onRotate(RotateEvent event);
	void onPinch(PinchEvent event);
	void onTap(TapEvent event);
	void onDoubleTap(TapEvent event);
	void onFlick(FlickEvent event);
}
