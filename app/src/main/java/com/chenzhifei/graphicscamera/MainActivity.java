package com.chenzhifei.graphicscamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import com.chenzhifei.graphicscamera.controller.ThreeDViewController;
import com.chenzhifei.graphicscamera.view.ThreeDView;

public class MainActivity extends AppCompatActivity {

    private TextView xValue;
    private TextView yValue;
    private TextView rotateValue;
    private TextView cameraZvalue;

    private ThreeDViewController threeDViewController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xValue = (TextView) findViewById(R.id.tv_x_value);
        yValue = (TextView) findViewById(R.id.tv_y_value);
        rotateValue = (TextView) findViewById(R.id.tv_rotate_value);
        cameraZvalue = (TextView) findViewById(R.id.tv_cameraZ_value);

        ThreeDView threeDView = (ThreeDView)findViewById(R.id.three_d_view);
        threeDView.setStateValueListener(new ThreeDView.StateValueListener() {
            @Override
            public void stateValue(float distanceX, float distanceY, float rotateDeg, float cameraZtranslate) {
                String xvalue = "" + distanceX, yvalue = "" + distanceY, rotateDegStr = "" + rotateDeg,
                        cameraZtranslateStr = "" + cameraZtranslate;
                xValue.setText(xvalue);
                yValue.setText(yvalue);
                rotateValue.setText(rotateDegStr);
                cameraZvalue.setText(cameraZtranslateStr);
            }
        });

        threeDViewController = new ThreeDViewController(threeDView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        threeDViewController.inputTouchEvent(event);
        return super.onTouchEvent(event);
    }
}