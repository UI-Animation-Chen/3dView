package com.chenzhifei.graphicscamera.controller;

import android.view.MotionEvent;

import com.chenzhifei.graphicscamera.gesture.TwoFingersGestureDetector;
import com.chenzhifei.graphicscamera.view.ThreeDView;

/**
 * Created by chenzhifei on 2017/6/30.
 * control the ThreeDView
 */

public class ThreeDViewController {

    private ThreeDView threeDView;

    private TwoFingersGestureDetector twoFingersGestureDetector;

    public ThreeDViewController(ThreeDView threeDView) {
        this.threeDView = threeDView;
        this.threeDView.setDistanceVelocityDecrease(1.5f);

        twoFingersGestureDetector = new TwoFingersGestureDetector();
        twoFingersGestureDetector.setTwoFingersGestureListener(new TwoFingersGestureDetector.TwoFingersGestureListener() {
            @Override
            public void onDown(float downX, float downY, long downTime) {
                ThreeDViewController.this.threeDView.stopAnim();
            }

            @Override
            public void onMoved(float deltaMovedX, float deltaMovedY, long deltaMilliseconds) {
                ThreeDViewController.this.threeDView.updateXY(deltaMovedX, deltaMovedY);
            }

            @Override
            public void onRotated(float deltaRotatedDeg, long deltaMilliseconds) {
                ThreeDViewController.this.threeDView.updateRotateDeg(deltaRotatedDeg);
            }

            @Override
            public void onScaled(float deltaScaledX, float deltaScaledY, float deltaScaledDistance, long deltaMilliseconds) {
                ThreeDViewController.this.threeDView.updateCameraZtranslate(deltaScaledDistance);
            }

            @Override
            public void onUp(float upX, float upY, long upTime, long lastDeltaMilliseconds,
                             float xVelocity, float yVelocity, float rotateDegVelocity, float scaledVelocity) {

                ThreeDViewController.this.threeDView.startAnim(lastDeltaMilliseconds, xVelocity, yVelocity, rotateDegVelocity);
            }

            @Override
            public void onCancel() {}
        });
    }

    public void inputTouchEvent(MotionEvent event) {
        twoFingersGestureDetector.onTouchEvent(event);
    }

}
