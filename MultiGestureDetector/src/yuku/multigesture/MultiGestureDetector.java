package yuku.multigesture;

import android.graphics.PointF;
import android.util.FloatMath;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Iterator;

// Android Multi-Touch event demo
// David Bouchard
// http://www.deadpixel.ca
public class MultiGestureDetector {
	// util methods impl by yuku
	public static float dist(float x1, float y1, float x2, float y2) {
		float dx = x1 - x2;
		float dy = y1 - y2;
		return FloatMath.sqrt(dx*dx + dy*dy);
	}
	
	// The main detector object
	final TouchProcessor touch;

	// listener
	private OnMultiGestureListener listener;
	
	
	// -------------------------------------------------------------------------------------
	public MultiGestureDetector(float screenDensity) {
		touch = new TouchProcessor(screenDensity);
	}

	public OnMultiGestureListener getListener() {
		return listener;
	}

	public void setListener(OnMultiGestureListener listener) {
		this.listener = listener;
	}

	// -------------------------------------------------------------------------------------
	public void draw() {
		// I do the analysis and event processing inside draw, since I found that on Android
		// trying to draw from outside the main thread can cause pretty serious screen flickering
		// devices
		// TODO
		touch.analyse();
		touch.sendEvents();
	}

	// -------------------------------------------------------------------------------------
	// MULTI TOUCH EVENTS!

	void onTap(TapEvent event) {
		if (event.isSingleTap()) {
			if (listener != null) listener.onTap(event);
		}
		if (event.isDoubleTap()) {
			if (listener != null) listener.onDoubleTap(event);
		}
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void onFlick(FlickEvent event) {
		if (listener != null) listener.onFlick(event);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void onDrag(DragEvent event) {
		if (listener != null) listener.onDrag(event);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void onRotate(RotateEvent event) {
		if (listener != null) listener.onRotate(event);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void onPinch(PinchEvent event) {
		if (listener != null) listener.onPinch(event);
	}

	// -------------------------------------------------------------------------------------
	// This is the stock Android touch event
	// modified by yuku
	public boolean onTouchEvent(MotionEvent event) {

		// extract the action code & the pointer ID
		int action = event.getAction();
		int code = action & MotionEvent.ACTION_MASK;
		int index = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;

		float x = event.getX(index);
		float y = event.getY(index);
		int id = event.getPointerId(index);

		// pass the events to the TouchProcessor
		if (code == MotionEvent.ACTION_DOWN || code == MotionEvent.ACTION_POINTER_DOWN) {
			touch.pointDown(x, y, id);
		} else if (code == MotionEvent.ACTION_UP || code == MotionEvent.ACTION_POINTER_UP) {
			touch.pointUp(event.getPointerId(index));
		} else if (code == MotionEvent.ACTION_MOVE) {
			int numPointers = event.getPointerCount();
			for (int i = 0; i < numPointers; i++) {
				id = event.getPointerId(i);
				x = event.getX(i);
				y = event.getY(i);
				touch.pointMoved(x, y, id);
			}
		}
		
		// immediately analyze and sendEvents
		touch.analyse();
		touch.sendEvents();
		
		return true;
	}

	// Event classes
	// /////////////////////////////////////////////////////////////////////////////////
	public class TouchEvent {
		// empty base class to make event handling easier
	}

	// /////////////////////////////////////////////////////////////////////////////////
	public class DragEvent extends TouchEvent {

		public float x; // position
		public float y;
		public float dx; // movement
		public float dy;
		public int numberOfPoints;

		DragEvent(float x, float y, float dx, float dy, int n) {
			this.x = x;
			this.y = y;
			this.dx = dx;
			this.dy = dy;
			numberOfPoints = n;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////
	public class PinchEvent extends TouchEvent {

		public float centerX;
		public float centerY;
		public float amount; // in pixels
		public float scale;
		public int numberOfPoints;

		PinchEvent(float centerX, float centerY, float amount, float scale, int n) {
			this.centerX = centerX;
			this.centerY = centerY;
			this.amount = amount;
			this.scale = scale;
			this.numberOfPoints = n;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////
	public class RotateEvent extends TouchEvent {

		public float centerX;
		public float centerY;
		public float angle; // delta, in radians
		public int numberOfPoints;

		RotateEvent(float centerX, float centerY, float angle, int n) {
			this.centerX = centerX;
			this.centerY = centerY;
			this.angle = angle;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////
	public class TapEvent extends TouchEvent {

		public static final int SINGLE = 0;
		public static final int DOUBLE = 1;

		public float x;
		public float y;
		public int type;

		TapEvent(float x, float y, int type) {
			this.x = x;
			this.y = y;
			this.type = type;
		}

		public boolean isSingleTap() {
			return (type == SINGLE) ? true : false;
		}

		public boolean isDoubleTap() {
			return (type == DOUBLE) ? true : false;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////
	public class FlickEvent extends TouchEvent {

		public float x;
		public float y;
		public PointF velocity;

		FlickEvent(float x, float y, PointF velocity) {
			this.x = x;
			this.y = y;
			this.velocity = velocity;
		}
	}

	class TouchPoint {

		public float x;
		public float y;
		public float px;
		public float py;
		public int id;

		// used for gesture detection
		float angle;
		float oldAngle;
		float pinch;
		float oldPinch;

		// -------------------------------------------------------------------------------------
		TouchPoint(float x, float y, int id) {
			this.x = x;
			this.y = y;
			this.px = x;
			this.py = y;
			this.id = id;
		}

		// -------------------------------------------------------------------------------------
		public void update(float x, float y) {
			px = this.x;
			py = this.y;
			this.x = x;
			this.y = y;
		}

		// -------------------------------------------------------------------------------------
		public void initGestureData(float cx, float cy) {
			pinch = oldPinch = dist(x, y, cx, cy);
			angle = oldAngle = (float) Math.atan2((y - cy), (x - cx));
		}

		// -------------------------------------------------------------------------------------
		// delta x -- int to get rid of some noise
		public int dx() {
			return (int) (x - px);
		}

		// -------------------------------------------------------------------------------------
		// delta y -- int to get rid of some noise
		public int dy() {
			return (int) (y - py);
		}

		// -------------------------------------------------------------------------------------
		public void setAngle(float angle) {
			oldAngle = this.angle;
			this.angle = angle;
		}

		// -------------------------------------------------------------------------------------
		public void setPinch(float pinch) {
			oldPinch = this.pinch;
			this.pinch = pinch;
		}

	}

	// TODO: make distance thershold based on pixel density information!

	class TouchProcessor {

		// heuristic constants
		int DOUBLE_TAP_DIST_THRESHOLD = 30;
		int FLICK_VELOCITY_THRESHOLD = 20;
		float MAX_MULTI_DRAG_DISTANCE = 100; // from the centroid

		// A list of currently active touch points
		ArrayList<TouchPoint> touchPoints;

		// Used for tap/doubletaps
		TouchPoint firstTap;
		TouchPoint secondTap;
		long tap;
		int tapCount = 0;

		// Events to be broadcast to the sketch
		ArrayList<TouchEvent> events;

		// centroid information
		float cx, cy;
		float old_cx, old_cy;

		boolean pointsChanged = false;

		// -------------------------------------------------------------------------------------
		public TouchProcessor(float screenDensity) {
			touchPoints = new ArrayList<TouchPoint>();
			events = new ArrayList<TouchEvent>();
			
			DOUBLE_TAP_DIST_THRESHOLD *= screenDensity;
			FLICK_VELOCITY_THRESHOLD *= screenDensity;
			MAX_MULTI_DRAG_DISTANCE *= screenDensity;
		}

		// -------------------------------------------------------------------------------------
		// Point Update functions
		public synchronized void pointDown(float x, float y, int id) {
			TouchPoint p = new TouchPoint(x, y, id);
			touchPoints.add(p);

			updateCentroid();
			if (touchPoints.size() >= 2) {
				p.initGestureData(cx, cy);
				if (touchPoints.size() == 2) {
					// if this is the second point, we now have a valid centroid to update the first point
					TouchPoint frst = (TouchPoint) touchPoints.get(0);
					frst.initGestureData(cx, cy);
				}
			}

			// tap detection
			if (tapCount == 0) {
				firstTap = p;
			}
			if (tapCount == 1) {
				secondTap = p;
			}
			tap = System.currentTimeMillis();
			pointsChanged = true;
		}

		// -------------------------------------------------------------------------------------
		public synchronized void pointUp(int id) {
			TouchPoint p = getPoint(id);
			touchPoints.remove(p);

			// tap detection
			// TODO: handle a long press event here?
			if (p == firstTap || p == secondTap) {
				// this could be either a Tap or a Flick gesture, based on movement
				float d = dist(p.x, p.y, p.px, p.py);
				if (d > FLICK_VELOCITY_THRESHOLD) {
					FlickEvent event = new FlickEvent(p.px, p.py, new PointF(p.x - p.px, p.y - p.py));
					events.add(event);
				} else {
					// long interval = System.currentTimeMillis() - tap;
					tapCount++;
				}
			}
			pointsChanged = true;
		}

		// -------------------------------------------------------------------------------------
		public synchronized void pointMoved(float x, float y, int id) {
			TouchPoint p = getPoint(id);
			p.update(x, y);
			// since the events will be in sync with draw(), we just wait until analyse() to
			// look for gestures
			pointsChanged = true;
		}

		// -------------------------------------------------------------------------------------
		// Calculate the centroid of all active points
		public void updateCentroid() {
			old_cx = cx;
			old_cy = cy;
			cx = 0;
			cy = 0;
			for (int i = 0; i < touchPoints.size(); i++) {
				TouchPoint p = (TouchPoint) touchPoints.get(i);
				cx += p.x;
				cy += p.y;
			}
			cx /= touchPoints.size();
			cy /= touchPoints.size();
		}

		// -------------------------------------------------------------------------------------
		public synchronized void analyse() {
			handleTaps();
			// simple event priority rule: do not try to rotate or pinch while dragging
			// this gets rid of a lot of jittery events
			if (pointsChanged) {
				updateCentroid();
				if (handleDrag()) {
					// we have handled drag, reset tap/doubletap
					firstTap = secondTap = null;
					tapCount = 0;
				} else {
					boolean ret1 = handleRotation();
					boolean ret2 = handlePinch();
					if (ret1 || ret2) {
						// we have handled rotation or pinch, reset tap/doubletap
						firstTap = secondTap = null;
						tapCount = 0;
					}
				}
				pointsChanged = false;
			}
		}

		// -------------------------------------------------------------------------------------
		// send events to the sketch
		public void sendEvents() {
			for (int i = 0; i < events.size(); i++) {
				TouchEvent e = (TouchEvent) events.get(i);
				if (e instanceof TapEvent)
					onTap((TapEvent) e);
				else if (e instanceof FlickEvent)
					onFlick((FlickEvent) e);
				else if (e instanceof DragEvent)
					onDrag((DragEvent) e);
				else if (e instanceof PinchEvent)
					onPinch((PinchEvent) e);
				else if (e instanceof RotateEvent) onRotate((RotateEvent) e);
			}
			events.clear();
		}

		// -------------------------------------------------------------------------------------
		public void handleTaps() {
			if (tapCount == 2) {
				// check if the tap point has moved
				float d = dist(firstTap.x, firstTap.y, secondTap.x, secondTap.y);
				if (d > DOUBLE_TAP_DIST_THRESHOLD) {
					// if the two taps are apart, count them as two single taps
//					TapEvent event1 = new TapEvent(firstTap.x, firstTap.y, TapEvent.SINGLE);
//					onTap(event1);
					TapEvent event2 = new TapEvent(secondTap.x, secondTap.y, TapEvent.SINGLE);
					onTap(event2);
				} else {
					events.add(new TapEvent(firstTap.x, firstTap.y, TapEvent.DOUBLE));
				}
				tapCount = 0;
			} else if (tapCount == 1) {
//				long interval = System.currentTimeMillis() - tap;
//				if (interval > TAP_TIMEOUT) {
					events.add(new TapEvent(firstTap.x, firstTap.y, TapEvent.SINGLE));
//					tapCount = 0;
//				}
			}
		}

		// -------------------------------------------------------------------------------------
		// rotation is the average angle change between each point and the centroid
		public boolean handleRotation() {
			if (touchPoints.size() < 2) return false;
			// look for rotation events
			float rotation = 0;
			for (int i = 0; i < touchPoints.size(); i++) {
				TouchPoint p = (TouchPoint) touchPoints.get(i);
				float angle = (float) Math.atan2(p.y - cy, p.x - cx);
				p.setAngle(angle);
				float delta = p.angle - p.oldAngle;
				if (delta > Math.PI) delta -= (float) Math.PI * 2.f;
				if (delta < -Math.PI) delta += (float) Math.PI * 2.f;
				rotation += delta;
			}
			rotation /= touchPoints.size();
			if (rotation != 0) {
				events.add(new RotateEvent(cx, cy, rotation, touchPoints.size()));
				return true;
			}
			return false;
		}

		// -------------------------------------------------------------------------------------
		// pinch is simply the average distance change from each points to the centroid
		public boolean handlePinch() {
			int ntouches = touchPoints.size();
			if (ntouches < 2) return false;
			// look for pinch events
			float pinch = 0;
			float scalediff = 0.f;
			for (int i = 0; i < ntouches; i++) {
				TouchPoint p = touchPoints.get(i);
				float distance = dist(p.x, p.y, cx, cy);
				p.setPinch(distance);
				float delta = p.pinch - p.oldPinch;
				pinch += delta;
				scalediff += delta / distance;
			}
			pinch /= ntouches;
			scalediff /= ntouches;
			if (pinch != 0) {
				events.add(new PinchEvent(cx, cy, pinch, 1.f + scalediff, ntouches));
				return true;
			}
			return false;
		}

		// -------------------------------------------------------------------------------------
		public boolean handleDrag() {
			// look for multi-finger drag events
			// multi-drag is defined as all the fingers moving close-ish together in the same direction
			boolean x_drag = true;
			boolean y_drag = true;
			boolean clustered = false;
			int first_x_dir = 0;
			int first_y_dir = 0;

			for (int i = 0; i < touchPoints.size(); i++) {
				TouchPoint p = (TouchPoint) touchPoints.get(i);
				int x_dir = 0;
				int y_dir = 0;
				if (p.dx() > 0) x_dir = 1;
				if (p.dx() < 0) x_dir = -1;
				if (p.dy() > 0) y_dir = 1;
				if (p.dy() < 0) y_dir = -1;

				if (i == 0) {
					first_x_dir = x_dir;
					first_y_dir = y_dir;
				} else {
					if (first_x_dir != x_dir) x_drag = false;
					if (first_y_dir != y_dir) y_drag = false;
				}

				// if the point is stationary
				if (x_dir == 0) x_drag = false;
				if (y_dir == 0) y_drag = false;

				if (touchPoints.size() == 1)
					clustered = true;
				else {
					float distance = dist(p.x, p.y, cx, cy);
					if (distance < MAX_MULTI_DRAG_DISTANCE) {
						clustered = true;
					}
				}
			}

			if ((x_drag || y_drag) && clustered) {
				if (touchPoints.size() == 1) {
					TouchPoint p = (TouchPoint) touchPoints.get(0);
					// use the centroid to calculate the position and delta of this drag event
					events.add(new DragEvent(p.x, p.y, p.dx(), p.dy(), 1));
				} else {
					// use the centroid to calculate the position and delta of this drag event
					events.add(new DragEvent(cx, cy, cx - old_cx, cy - old_cy, touchPoints.size()));
				}
				return true;
			}
			return false;
		}

		// -------------------------------------------------------------------------------------
		@SuppressWarnings("unchecked") public synchronized ArrayList<TouchPoint> getPoints() {
			return (ArrayList<TouchPoint>) touchPoints.clone();
		}

		// -------------------------------------------------------------------------------------
		public synchronized TouchPoint getPoint(int pid) {
			Iterator<TouchPoint> i = touchPoints.iterator();
			while (i.hasNext()) {
				TouchPoint tp = (TouchPoint) i.next();
				if (tp.id == pid) return tp;
			}
			return null;
		}

	}

}
