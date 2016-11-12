package de.e621.rebane.components;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
 */
public abstract class OnSwipeTouchListener implements OnTouchListener {

    private final GestureDetector gestureDetector;

    private final int SWIPE_THRESHOLD;
    private final int SWIPE_VELOCITY_THRESHOLD;


    public OnSwipeTouchListener (Activity activity, double minWidthToSwiperPercent, double minSpeedPerWidthPercent){
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        SWIPE_THRESHOLD = (int)(metrics.widthPixels * minWidthToSwiperPercent);
        SWIPE_VELOCITY_THRESHOLD = (int)(metrics.widthPixels * minSpeedPerWidthPercent);;
        gestureDetector = new GestureDetector(activity.getApplicationContext(), new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight(e1, e2);
                        } else {
                            onSwipeLeft(e1, e2);
                        }
                    }
                    result = true;
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom(e1, e2);
                    } else {
                        onSwipeTop(e1, e2);
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public abstract void onSwipeRight(MotionEvent start, MotionEvent stop);

    public abstract void onSwipeLeft(MotionEvent start, MotionEvent stop);

    public abstract void onSwipeTop(MotionEvent start, MotionEvent stop);

    public abstract void onSwipeBottom(MotionEvent start, MotionEvent stop);
}
