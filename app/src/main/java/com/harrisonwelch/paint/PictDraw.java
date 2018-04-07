package com.harrisonwelch.paint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Suzanne on 4/5/2018.
 */

public class PictDraw extends View {
    int currentHeight, currentWidth;        //height and width of our widget container
    Paint backgroundPaint;
    Paint mainPaint;



    public PictDraw(Context context) {
        super(context);
        setup();
    }

    public PictDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public PictDraw(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    //do initialization of the Picture widget
    private void setup(){
        mainPaint = new Paint();
        mainPaint.setColor(0xff00ff00);
        mainPaint.setStyle(Paint.Style.STROKE);
        mainPaint.setStrokeWidth(Helpers.dpToPx(20, getContext()));


    }

    //Sets the stroke to a passed in dp value
    // after converting it to px
    public void setStrokeThickness(int dpSize){
        mainPaint.setStrokeWidth(Helpers.dpToPx(dpSize, getContext()));
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (drawShape) {/*
            canvas.drawLine(0, 0, currentWidth - 1, currentHeight - 1, mainPaint);
            canvas.drawLine(0, currentHeight - 1, currentWidth - 1, 0, mainPaint);*/

            canvas.drawRect(rect, mainPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        currentHeight = h;
        currentWidth = w;
    }

    private boolean drawShape = false;
    private int startx, starty, endx, endy;
    private Rect rect;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("TouchEvent", "ACTION EVENT = " + event.getAction());

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            rect = new Rect(x, y,x + 1, y + 1);




            drawShape = true;
            invalidate();

            performClick();
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            rect = null;
            drawShape = false;
            invalidate();
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE){
            //setStrokeThickness((int) event.getX());
            rect.right = (int) event.getX();
            rect.bottom = (int) event.getY();
            invalidate();
        }


        return true;
    }

    @Override
    public boolean performClick() {

        return super.performClick();
    }

    //update the size of the widget when measuring the thing
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int dpPixel = 100;
        float actualPixels = Helpers.dpToPx(dpPixel, getContext());


        int desiredHeight = (int) actualPixels;
        int desiredWidth = (int) actualPixels;

        //this is all in pixels
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int width, height;      //the end width and height to be used.

        //the passed in width is the max we can be.
        // if we're given a height to do EXACTLY, use that height.
        // otherwise, use whatever is smaller, our desired height or the
        // passed in height.
        if (widthMode == MeasureSpec.EXACTLY){              //used whenever we set a size with dp
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {      //basically used with stuff when something can be AT MOST this
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }


        setMeasuredDimension(width, height);

    }
}
