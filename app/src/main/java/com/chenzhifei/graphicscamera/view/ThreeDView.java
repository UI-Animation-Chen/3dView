package com.chenzhifei.graphicscamera.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.chenzhifei.graphicscamera.R;

/**
 * Created by chenzhifei on 2017/6/25.
 * 使用Graphics.Camera来实现3D效果。
 */

public class ThreeDView extends View {

    private int THREE_D_VIEW_WIDTH;
    private int THREE_D_VIEW_HEIGHT;
    private static final int BIT_MAP_WIDTH = 200;
    private static final int BIT_MAP_HEIGHT = 200;
    private static final float CENTER_CIRCLE_R = 60f;
    private static final float CENTER_CIRCLE_SHADOW_R = 200f;

    private Camera camera = new Camera(); //default location: (0f, 0f, -8.0f), in pixels: -8.0f * 72 = -576f

    private Matrix matrix = new Matrix();
    private Matrix matrix2 = new Matrix();
    private Matrix matrix3 = new Matrix();
    private Matrix matrix4 = new Matrix();
    private Matrix matrix5 = new Matrix();
    private Matrix matrix6 = new Matrix();
    private Paint paint = new Paint();
    private Bitmap bitmap;

    private float distanceX = 0;
    private float distanceY = 0;
    private float rotateDeg = 0f;
    private float cameraZtranslate; // 3D rotate radius

    private float distanceToDegree; // cameraZtranslate --> 90度

    private boolean isInfinity = false;
    private float distanceVelocityDecrease = 1f; //decrease 1 pixels/second when a message is handled in the loop
                    //loop frequency is 60hz or 120hz when handleMessage(msg) includes UI update code

    private float xVelocity = 0f;
    private float yVelocity = 0f;
    private float rotateDegVelocity = 0f;
    private long lastDeltaMilliseconds = 0;

    private Handler animHandler;
    private Handler touchHandler;

    public ThreeDView(Context context) {
        this(context, null);
    }

    public ThreeDView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThreeDView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        bitmap = Bitmap.createScaledBitmap(bitmap, BIT_MAP_WIDTH, BIT_MAP_HEIGHT, false);

        animHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                distanceX += (xVelocity * lastDeltaMilliseconds / 1000);
                distanceY += (yVelocity * lastDeltaMilliseconds / 1000);
                rotateDeg += (rotateDegVelocity * lastDeltaMilliseconds / 1000);

                ThreeDView.this.invalidate();

                if (ThreeDView.this.stateValueListener != null) {
                    ThreeDView.this.stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
                }

                if (xVelocity == 0f && yVelocity == 0f && rotateDegVelocity == 0f) { // anim will stop
                    return true;
                }

                if (ThreeDView.this.isInfinity) {
                    ThreeDView.this.sendMsgForAnim();

                } else {
                    // decrease the velocities.
                    // 'Math.abs(xVelocity) <= distanceVelocityDecrease' make sure the xVelocity will be 0 finally.
                    xVelocity =  Math.abs(xVelocity) <= distanceVelocityDecrease ? 0f :
                            (xVelocity > 0 ? xVelocity - distanceVelocityDecrease : xVelocity + distanceVelocityDecrease);

                    yVelocity = Math.abs(yVelocity) <= distanceVelocityDecrease ? 0f :
                            (yVelocity > 0 ? yVelocity - distanceVelocityDecrease : yVelocity + distanceVelocityDecrease);

                    float degVelocityDecrease = distanceVelocityDecrease * distanceToDegree;
                    rotateDegVelocity = Math.abs(rotateDegVelocity) <= degVelocityDecrease ? 0f :
                            (rotateDegVelocity > 0 ? rotateDegVelocity - degVelocityDecrease :
                                    rotateDegVelocity + degVelocityDecrease);

                    ThreeDView.this.sendMsgForAnim();
                }

                return true;
            }
        });

        touchHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (ThreeDView.this.stateValueListener != null) {
                    ThreeDView.this.stateValueListener.stateValue(distanceX, -distanceY, rotateDeg, cameraZtranslate);
                }
                return true;
            }
        });
    }

    public void setDistanceVelocityDecrease(float distanceVelocityDecrease) {
        if (distanceVelocityDecrease <= 0f) {
            this.isInfinity = true;
            this.distanceVelocityDecrease = 0f;
        } else {
            this.isInfinity = false;
            this.distanceVelocityDecrease = distanceVelocityDecrease;
        }
    }

    public void updateXY(float movedX, float movedY) {
        this.distanceX += movedX;
        this.distanceY += movedY;
        invalidate();
        touchHandler.sendEmptyMessage(0);
    }

    public void updateRotateDeg(float deltaRotateDeg) {
        this.rotateDeg += deltaRotateDeg;
        invalidate();
        touchHandler.sendEmptyMessage(0);
    }

    public void updateCameraZtranslate(float cameraZtranslate) {
        this.cameraZtranslate += cameraZtranslate;
        invalidate();
        touchHandler.sendEmptyMessage(0);
    }

    private void sendMsgForAnim() {
        animHandler.sendEmptyMessage(0);
    }

    public void stopAnim() {
        animHandler.removeCallbacksAndMessages(null);
    }

    public void startAnim(long lastDeltaMilliseconds, float xVelocity, float yVelocity, float rotatedVelocity) {
        this.lastDeltaMilliseconds = lastDeltaMilliseconds;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        this.rotateDegVelocity = rotatedVelocity;

        sendMsgForAnim();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        THREE_D_VIEW_WIDTH = w; //params value is in pixels not dp
        THREE_D_VIEW_HEIGHT = h;
        cameraZtranslate = Math.min(w, h) / 2;
        distanceToDegree = 90f / cameraZtranslate;//NOT changed when cameraZtranslate changed in the future
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // convert distances in pixels into degrees
        float xDeg = -distanceY * distanceToDegree;
        float yDeg = distanceX * distanceToDegree;

//        setMatrix_test(xDeg, yDeg);
        setMatrix(xDeg, yDeg);
        setMatrix2(xDeg, yDeg);
        setMatrix3(xDeg, yDeg);
        setMatrix4(xDeg, yDeg);
        setMatrix5(xDeg, yDeg);
        setMatrix6(xDeg, yDeg);

        // translate canvas to locate the bitmap in center of the ThreeDViwe
        canvas.translate((THREE_D_VIEW_WIDTH - BIT_MAP_WIDTH) / 2f, (THREE_D_VIEW_HEIGHT - BIT_MAP_HEIGHT) / 2f);

        drawCanvas(canvas, xDeg, yDeg);
//        drawCanvas_test(canvas, xDeg, yDeg);
    }

    private void setMatrix_test(float xDeg, float yDeg) {
        matrix.reset();

        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrix);
        camera.restore();

        matrix.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void drawCanvas_test(Canvas canvas, float xDeg, float yDeg) {
        if (Math.cos(Math.toRadians(xDeg)) <= 0 || Math.cos(Math.toRadians(yDeg)) <= 0) {
            canvas.drawBitmap(bitmap, matrix, paint);
            drawCenter(canvas);
        }  else {
            drawCenter(canvas);
            canvas.drawBitmap(bitmap, matrix, paint);
        }
    }

    private void setMatrix(float xDeg, float yDeg) {
        matrix.reset();

        camera.save(); // save the original state(no any transformation) so you can restore it after any changes
        camera.rotateX(xDeg); // it will lead to rotate Y and Z axis
        camera.rotateY(yDeg); // it will just lead to rotate Z axis, NOT X axis. BUT rotateZ(deg) will lead to nothing
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrix);
        camera.restore(); // restore to the original state after uses for next use

        // translate coordinate origin the camera's transformation depends on to center of the bitmap
        matrix.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrix2(float xDeg, float yDeg) {
        matrix2.reset();

        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, cameraZtranslate);
        camera.getMatrix(matrix2);
        camera.restore();

        matrix2.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix2.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrix3(float xDeg, float yDeg) {
        matrix3.reset();

        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg - 90f);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrix3);
        camera.restore();

        matrix3.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix3.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrix4(float xDeg, float yDeg) {
        matrix4.reset();

        camera.save();
        camera.rotateX(xDeg);
        camera.rotateY(yDeg - 90f);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, cameraZtranslate);
        camera.getMatrix(matrix4);
        camera.restore();

        matrix4.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix4.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrix5(float xDeg, float yDeg) {
        matrix5.reset();

        camera.save();
        camera.rotateX(xDeg - 90f);
        camera.rotateY(yDeg);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, -cameraZtranslate);
        camera.getMatrix(matrix5);
        camera.restore();

        matrix5.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix5.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void setMatrix6(float xDeg, float yDeg) {
        matrix6.reset();

        camera.save();
        camera.rotateX(xDeg - 90f);
        camera.rotateY(yDeg);
        camera.rotateZ(-rotateDeg);
        camera.translate(0f, 0f, cameraZtranslate);
        camera.getMatrix(matrix6);
        camera.restore();

        matrix6.preTranslate(-(BIT_MAP_WIDTH / 2), -(BIT_MAP_HEIGHT / 2));
        matrix6.postTranslate(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2);
    }

    private void drawCenter(Canvas canvas) {
        // draw center circle shadow first
        paint.setColor(Color.parseColor("#550000ff"));
        canvas.drawCircle(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2, CENTER_CIRCLE_SHADOW_R, paint);

        // draw center circle second, it's above the shadow
        paint.setColor(Color.parseColor("#0000ff"));
        canvas.drawCircle(BIT_MAP_WIDTH / 2, BIT_MAP_HEIGHT / 2, CENTER_CIRCLE_R, paint);
    }

    private void drawCanvas(Canvas canvas, float xDeg, float yDeg) {
        if (Math.cos(Math.toRadians(xDeg)) <= 0 || Math.cos(Math.toRadians(yDeg)) <= 0) {
            canvas.drawBitmap(bitmap, matrix, paint);
            if (Math.cos(Math.toRadians(xDeg)) <= 0 || Math.cos(Math.toRadians(yDeg - 90f)) <= 0) {
                canvas.drawBitmap(bitmap, matrix3, paint);
                if (Math.cos(Math.toRadians(xDeg - 90f)) <= 0 || Math.cos(Math.toRadians(yDeg)) <= 0) {
                    canvas.drawBitmap(bitmap, matrix5, paint);
                    drawCenter(canvas);
                    canvas.drawBitmap(bitmap, matrix6, paint);
                } else {
                    canvas.drawBitmap(bitmap, matrix6, paint);
                    drawCenter(canvas);
                    canvas.drawBitmap(bitmap, matrix5, paint);
                }
                canvas.drawBitmap(bitmap, matrix4, paint);
            } else {
                canvas.drawBitmap(bitmap, matrix4, paint);
                if (Math.cos(Math.toRadians(xDeg - 90f)) <= 0 || Math.cos(Math.toRadians(yDeg)) <= 0) {
                    canvas.drawBitmap(bitmap, matrix5, paint);
                    drawCenter(canvas);
                    canvas.drawBitmap(bitmap, matrix6, paint);
                } else {
                    canvas.drawBitmap(bitmap, matrix6, paint);
                    drawCenter(canvas);
                    canvas.drawBitmap(bitmap, matrix5, paint);
                }
                canvas.drawBitmap(bitmap, matrix3, paint);
            }
            canvas.drawBitmap(bitmap, matrix2, paint);
        }  else {
            canvas.drawBitmap(bitmap, matrix2, paint);
            if (Math.cos(Math.toRadians(xDeg)) <= 0 || Math.cos(Math.toRadians(yDeg - 90f)) <= 0) {
                canvas.drawBitmap(bitmap, matrix3, paint);
                if (Math.cos(Math.toRadians(xDeg - 90f)) <= 0 || Math.cos(Math.toRadians(yDeg)) <= 0) {
                    canvas.drawBitmap(bitmap, matrix5, paint);
                    drawCenter(canvas);
                    canvas.drawBitmap(bitmap, matrix6, paint);
                } else {
                    canvas.drawBitmap(bitmap, matrix6, paint);
                    drawCenter(canvas);
                    canvas.drawBitmap(bitmap, matrix5, paint);
                }
                canvas.drawBitmap(bitmap, matrix4, paint);
            } else {
                canvas.drawBitmap(bitmap, matrix4, paint);
                if (Math.cos(Math.toRadians(xDeg - 90f)) <= 0 || Math.cos(Math.toRadians(yDeg)) <= 0) {
                    canvas.drawBitmap(bitmap, matrix5, paint);
                    drawCenter(canvas);
                    canvas.drawBitmap(bitmap, matrix6, paint);
                } else {
                    canvas.drawBitmap(bitmap, matrix6, paint);
                    drawCenter(canvas);
                    canvas.drawBitmap(bitmap, matrix5, paint);
                }
                canvas.drawBitmap(bitmap, matrix3, paint);
            }
            canvas.drawBitmap(bitmap, matrix, paint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animHandler != null) {
            animHandler.removeCallbacksAndMessages(null);
        }
    }

    public interface StateValueListener {
        void stateValue(float distanceX, float distanceY, float rotateDegree, float cameraZtranslate);
    }

    private StateValueListener stateValueListener;

    public void setStateValueListener(StateValueListener stateValueListener) {
        this.stateValueListener = stateValueListener;
    }
}
