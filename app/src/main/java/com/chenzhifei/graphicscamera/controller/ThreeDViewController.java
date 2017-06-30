package com.chenzhifei.graphicscamera.controller;

import android.view.MotionEvent;

import com.chenzhifei.graphicscamera.gesture.TwoFingersGestureDetecter;
import com.chenzhifei.graphicscamera.view.ThreeDView;

/**
 * Created by chenzhifei on 2017/6/30.
 * control the ThreeDView
 */

public class ThreeDViewController {

    private ThreeDView threeDView;

    private TwoFingersGestureDetecter twoFingersGestureDetecter;

    public ThreeDViewController(ThreeDView threeDView) {
        this.threeDView = threeDView;
        this.threeDView.setDistanceVelocityDecrease(1.5f);

        twoFingersGestureDetecter = new TwoFingersGestureDetecter();
        twoFingersGestureDetecter.setTwoFingersGestureListenter(new TwoFingersGestureDetecter.TwoFingersGestureListenter() {
            @Override
            public void down(float downX, float downY, long downTime) {
                ThreeDViewController.this.threeDView.stopAnim();
            }

            @Override
            public void moved(float deltaMovedX, float deltaMovedY, long deltaMilliseconds) {
                ThreeDViewController.this.threeDView.updateXY(deltaMovedX, deltaMovedY);
            }

            @Override
            public void rotated(float deltaRotatedDeg, long deltaMilliseconds) {
                ThreeDViewController.this.threeDView.updateRotateDeg(deltaRotatedDeg);
            }

            @Override
            public void scaled(float deltaScaledDistance, long deltaMilliseconds) {
                ThreeDViewController.this.threeDView.updateCameraZtranslate(deltaScaledDistance);
            }

            @Override
            public void up(float upX, float upY, long upTime, long lastDeltaMilliseconds,
                           float xVelocity, float yVelocity, float rotateDegVelocity, float scaledVelocity) {

                ThreeDViewController.this.threeDView.startAnim(lastDeltaMilliseconds, xVelocity, yVelocity, rotateDegVelocity);
            }
        });
    }

    public void inputTouchEvent(MotionEvent event) {
        twoFingersGestureDetecter.onTouchEvent(event);
    }

}
