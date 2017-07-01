package com.chenzhifei.graphicscamera.gesture;

import android.view.MotionEvent;

/**
 * Created by chenzhifei on 2017/6/30.
 * two fingers: move(one or two), rotate, scale
 */

public class TwoFingersGestureDetector {

    private boolean moreThan2Fingers = false;

    private float oldX = 0f;
    private float oldY = 0f;

    // 用户在连续两次MOVE事件中，评估最快旋转的角度不会超过100度。
    private static final float MAX_DEGREES_IN_TWO_MOVE_EVENTS = 100f;
    private static final float REFERENCE_DEGREES = 360f - MAX_DEGREES_IN_TWO_MOVE_EVENTS;
    private static final float RADIAN_TO_DEGREE = (float) (180.0 / Math.PI);
    private float oldTanDeg = 0f;

    private float oldScaledX = 0f;
    private float oldScaledY = 0f;
    private float old2FingersDistance = 0f;

    private long oldTimestamp = 0, currDeltaMilliseconds = 0;

    private float currDeltaRotatedDeg, currDeltaScaledDistance, currDeltaMovedX, currDeltaMovedY;

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 2) {
            moreThan2Fingers = true;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 如果不清理上一次双指产生的旋转和距离的增量，本次单指触摸up时还可以获得上次的速度值。
                // 这里选择清理。
                currDeltaRotatedDeg = 0f;
                currDeltaScaledDistance = 0f;

                oldX = event.getX(0);
                oldY = event.getY(0);
                oldTimestamp = event.getDownTime();
                if (twoFingersGestureListener != null) {
                    twoFingersGestureListener.onDown(oldX, oldY, oldTimestamp);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (moreThan2Fingers) {
                    return true;
                }

                // 第二个触点一出现就清空。当然上次up清理也行。
                oldTanDeg = 0f;
                oldScaledX = 0f;
                oldScaledY = 0f;
                old2FingersDistance = 0f;

                oldX = (event.getX(0) + event.getX(1)) / 2f;
                oldY = (event.getY(0) + event.getY(1)) / 2f;
                oldTimestamp = event.getEventTime();
                break;
            case MotionEvent.ACTION_MOVE:
                if (moreThan2Fingers) {
                    return true;
                }

                long newTimestamp = event.getEventTime();
                currDeltaMilliseconds = newTimestamp - oldTimestamp;
                oldTimestamp = newTimestamp;

                float newX, newY;
                // handle 2 fingers touch
                if (event.getPointerCount() == 2) {
                    // handle rotate
                    currDeltaRotatedDeg = getRotatedDegBetween2Events(event);
                    // handle scale
                    float deltaScaledX = getDeltaScaledXBetween2Events(event);
                    float deltaScaledY = getDeltaScaledYBetween2Events(event);
                    currDeltaScaledDistance = getScaledDistanceBetween2Events(event);

                    if (this.twoFingersGestureListener != null) {
                        twoFingersGestureListener.onScaled(deltaScaledX, deltaScaledY, currDeltaScaledDistance, currDeltaMilliseconds);
                        twoFingersGestureListener.onRotated(currDeltaRotatedDeg, currDeltaMilliseconds);
                    }

                    // handle move
                    newX = (event.getX(0) + event.getX(1)) / 2f;
                    newY = (event.getY(0) + event.getY(1)) / 2f;
                } else {
                    newX = event.getX(0);
                    newY = event.getY(0);
                }

                currDeltaMovedX = newX - oldX;
                currDeltaMovedY = newY - oldY;
                oldX = newX;
                oldY = newY;

                if (this.twoFingersGestureListener != null) {
                    twoFingersGestureListener.onMoved(currDeltaMovedX, currDeltaMovedY, currDeltaMilliseconds);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (moreThan2Fingers) {
                    return true;
                }

                if (event.getActionIndex() == 0) {
                    oldX = event.getX(1);
                    oldY = event.getY(1);
                } else if (event.getActionIndex() == 1) {
                    oldX = event.getX(0);
                    oldY = event.getY(0);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (moreThan2Fingers) {
                    moreThan2Fingers = false;
                    return true;
                }

                if (twoFingersGestureListener != null) {
                    twoFingersGestureListener.onUp(
                            oldX, oldY, oldTimestamp, currDeltaMilliseconds,
                            currDeltaMilliseconds == 0f ? 0f : 1000 * currDeltaMovedX / currDeltaMilliseconds,
                            currDeltaMilliseconds == 0f ? 0f : 1000 * currDeltaMovedY / currDeltaMilliseconds,
                            currDeltaMilliseconds == 0f ? 0f : 1000 * currDeltaRotatedDeg / currDeltaMilliseconds,
                            currDeltaMilliseconds == 0f ? 0f : 1000 * currDeltaScaledDistance / currDeltaMilliseconds);
                }
                break;
        }
        return true;
    }

    private float getRotatedDegBetween2Events(MotionEvent event) {
        float spanX = event.getX(1) - event.getX(0);
        float spanY = event.getY(1) - event.getY(0);
        float tanDeg = (float) Math.atan2(spanY, spanX) * RADIAN_TO_DEGREE;
        if (oldTanDeg == 0f
                || (tanDeg - oldTanDeg > REFERENCE_DEGREES && tanDeg >= 0f && oldTanDeg <= 0f)
                || (oldTanDeg - tanDeg > REFERENCE_DEGREES && oldTanDeg >= 0f && tanDeg <= 0f)) {

            oldTanDeg = tanDeg;
            return 0f;
        } else {
            float deltaDeg = tanDeg - oldTanDeg;
            oldTanDeg = tanDeg;
            return deltaDeg;
        }
    }

    private float getDeltaScaledXBetween2Events(MotionEvent event) {
        float newScaledX = event.getX(1) - event.getX(0);
        if (oldScaledX == 0f) {
            oldScaledX = newScaledX;
            return 0f;
        } else {
            float deltaScaledX = newScaledX - oldScaledX;
            oldScaledX = newScaledX;
            return deltaScaledX;
        }
    }

    private float getDeltaScaledYBetween2Events(MotionEvent event) {
        float newScaledY = event.getY(1) - event.getY(0);
        if (oldScaledY == 0f) {
            oldScaledY = newScaledY;
            return 0f;
        } else {
            float deltaScaledY = newScaledY - oldScaledY;
            oldScaledY = newScaledY;
            return deltaScaledY;
        }
    }

    private float getScaledDistanceBetween2Events(MotionEvent event) {
        float newScaledX = event.getX(1) - event.getX(0), newScaledY = event.getY(1) - event.getY(0);
        float new2FingerDistance = (float) Math.sqrt((newScaledX * newScaledX) + (newScaledY * newScaledY));
        if (old2FingersDistance == 0f) {
            old2FingersDistance = new2FingerDistance;
            return 0f;
        } else {
            float deltaDistance = new2FingerDistance - old2FingersDistance;
            old2FingersDistance = new2FingerDistance;
            return deltaDistance;
        }
    }

    public interface TwoFingersGestureListener {
        void onDown(float downX, float downY, long downTime);

        void onMoved(float deltaMovedX, float deltaMovedY, long deltaMilliseconds);

        void onRotated(float deltaRotatedDeg, long deltaMilliseconds);

        void onScaled(float deltaScaledX, float deltaScaledY, float deltaScaledDistance, long deltaMilliseconds);

        // velocity: pixels/second   degrees/second
        void onUp(float upX, float upY, long upTime, long lastDeltaMilliseconds, float xVelocity, float yVelocity, float rotatedVelocity, float scaledVelocity);
    }

    private TwoFingersGestureListener twoFingersGestureListener;

    public void setTwoFingersGestureListener(TwoFingersGestureListener l) {
        this.twoFingersGestureListener = l;
    }
}
