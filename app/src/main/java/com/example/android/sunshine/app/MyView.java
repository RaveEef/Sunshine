package com.example.android.sunshine.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by evast on 19-6-2017.
 */



public class MyView extends View implements SensorEventListener
{
    private Canvas mCanvas = new Canvas();
    private Paint mPaint = new Paint(), mTextPaint = new Paint();
    private float cX, cY, mRadiusBig, mRadiusSmall, azimuthValue;
    private RectF circleRect;
    float[] mGravity, mGeomagnetic;

    public MyView(Context context){
        super(context);
    }

    public MyView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public MyView(Context context, AttributeSet attrs, int defaultStyle){
        super(context, attrs, defaultStyle);
        init();
    }

    private void init() {

        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(3);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setTypeface(Typeface.SANS_SERIF);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredHeight = 2000;
        int desiredWidth = 2000;

        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int myWidth = desiredWidth;
        if(wSpecMode == MeasureSpec.EXACTLY)
            myWidth = wSpecSize;
        else if(wSpecMode == MeasureSpec.AT_MOST)
            myWidth = Math.min(wSpecSize, desiredWidth);

        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int myHeight = desiredHeight;
        if(hSpecMode == MeasureSpec.EXACTLY)
            myHeight = hSpecSize;
        else if(hSpecMode == MeasureSpec.AT_MOST)
            myHeight = Math.min(hSpecSize, desiredHeight);

        if(myWidth != myHeight){
            int smallestSize = Math.min(myWidth, myHeight);
            myWidth = smallestSize;
            myHeight = smallestSize;
        }


        setMeasuredDimension(myWidth, myHeight);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        mCanvas = canvas;
        drawCircles();
        drawLines();
        setCircleText();
        drawDegrees();

        super.onDraw(canvas);
    }

    private void drawCircles(){

        cX = (getWidth() - (getPaddingLeft() + getPaddingRight()))/2;
        cY = (getHeight() - (getPaddingTop() + getPaddingBottom()))/2;
        mRadiusBig = Math.min(cX, cY);
        mRadiusSmall = 0.93f*mRadiusBig;
        cX += getPaddingLeft();
        cY += getPaddingTop();
        circleRect = new RectF(cX - mRadiusSmall, cY - mRadiusSmall, cX + mRadiusSmall, cY + mRadiusSmall);

        mPaint.setColor(getResources().getColor(R.color.compass_grey));
        mPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawCircle(cX, cY, mRadiusBig, mPaint);
        fillCircles();
    }

    private void fillCircles(){

        RadialGradient orangeGrad = new RadialGradient(cX, cY, mRadiusSmall,
                new int[]{getResources().getColor(R.color.compass_dark_orange),
                        getResources().getColor(R.color.compass_light_orange)},
                new float[]{0.85f, 1.00f},
                Shader.TileMode.CLAMP);
        mPaint.setShader(orangeGrad);
        mCanvas.drawArc(circleRect, 0, 180, true, mPaint);

        RadialGradient blueGrad = new RadialGradient(cX, cY, mRadiusSmall,
                new int[]{getResources().getColor(R.color.compass_dark_blue),
                        getResources().getColor(R.color.compass_light_blue)},
                new float[]{0.85f, 1.00f},
                Shader.TileMode.CLAMP);
        mPaint.setShader(blueGrad);
        mCanvas.drawArc(circleRect, 180, 180, true, mPaint);

        mPaint.setShader(null);
    }

    private void drawLines(){

        float spaceLength = mRadiusSmall/4.5f;
        float startX = cX - (mRadiusSmall/3);
        float endX = cX + (mRadiusSmall/3);
        float valueY = (cY - mRadiusSmall) + (spaceLength/2);
        int textValue = 80;

        mPaint.setColor(Color.WHITE);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(25);
        while(valueY < (cY + mRadiusSmall)){
            mCanvas.drawLine(startX, valueY, endX, valueY, mPaint);
            mCanvas.drawText(String.valueOf(textValue), cX, valueY, mTextPaint);
            textValue -= 20;
            valueY += spaceLength;
        }
    }

    private void setCircleText(){

        String[] compassPoints =
                {">N<", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};

        //GETTING FROM ANDROID DEVICE
        double valueNorth = 0;
        if(azimuthValue > 0)
            valueNorth = (Math.toDegrees(azimuthValue)+360)%360;

        double degreesPerSegment = 360/((double)compassPoints.length);
        double dNorthStart = valueNorth + 270 - (degreesPerSegment/2);

        Path p = new Path();
        mTextPaint.setColor(getResources().getColor(R.color.compass_green));
        mTextPaint.setTextSize(mRadiusBig*0.06f);

        for(int i = 0; i < compassPoints.length; i++) {
            if(i == 1)
                mTextPaint.setColor(Color.WHITE);
            double startA = dNorthStart + (i * degreesPerSegment);
            p.addArc(circleRect, (float) startA, (float) degreesPerSegment);
            mCanvas.drawTextOnPath(compassPoints[i], p, 0, -5, mTextPaint);
            p.reset();
        }
    }

    private void drawDegrees(){

        float radiusXSmall = 0.98f*mRadiusSmall;
        float outerX, outerY, innerX, innerY;
        double dAngle;
        double nPoints = 18;
        Path p = new Path();
        double degreesPerSegment = 180/nPoints;
        double dStartSegment = 180 - (degreesPerSegment/2);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(0.06f*radiusXSmall);

        int textValue = 90;
        for(int i = 0; i <= nPoints; i++) {
            dAngle = i * (180 / nPoints);
            if(i%3 == 0){
                p.addArc(circleRect,
                        (float)dStartSegment,
                        (float)degreesPerSegment);
                mCanvas.drawTextOnPath(String.valueOf(textValue), p, 0, 24, mTextPaint);
                p.reset();
                textValue -= 30;
                dStartSegment += 30;
            }
            else {
                outerX = (float) (cX - mRadiusSmall * Math.cos(Math.toRadians(dAngle)));
                innerX = (float) (cX - radiusXSmall * Math.cos(Math.toRadians(dAngle)));
                outerY = (float) (cY - mRadiusSmall * Math.sin(Math.toRadians(dAngle)));
                innerY = (float) (cY - radiusXSmall * Math.sin(Math.toRadians(dAngle)));
                mCanvas.drawLine(outerX, outerY, innerX, innerY, mPaint);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = sensorEvent.values;

        if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = sensorEvent.values;

        if(mGravity != null && mGeomagnetic != null){
            float R[] = new float[9];
            float I[] = new float[9];

            if(SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)){
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuthValue = orientation[0];

            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        
    }
}
