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
import android.widget.ImageView;

import java.util.Random;
import java.util.Stack;

/**
 * Created by Suzanne on 4/5/2018.
 */

public class PictDraw extends ImageView {
    int currentHeight, currentWidth;        //height and width of our widget container
    Paint backgroundPaint;
    Paint mainPaint;
    Random rand;

    Stack<Rectangle> rectangles;


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
        rand = new Random();
        rectangles = new Stack<>();

        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xffffffff);
        backgroundPaint.setStyle(Paint.Style.FILL);

        mainPaint = new Paint();
        mainPaint.setColor(0xff00ff00);
        mainPaint.setStyle(Paint.Style.FILL);
        //mainPaint.setStrokeWidth(Helpers.dpToPx(20, getContext()));


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

        canvas.drawPaint(backgroundPaint);

        //draw all the rectangles
        for (Rectangle r : rectangles) {
            mainPaint.setColor(r.getColor());
            canvas.drawRect(r.getRect(), mainPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        currentHeight = h;
        currentWidth = w;
    }


    //The rectangle the user is currently manipulating and 'drawing'
    private Rectangle currentlyDrawingRectangle;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int color = rand.nextInt(0x1000000) + 0xff000000;

            Rectangle rect = new Rectangle(color, x, y, x+1, y+1);
            currentlyDrawingRectangle = rect;
            rectangles.push(rect);



            invalidate();

            performClick();         //needed by android studio to handle normal click event stuff
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            currentlyDrawingRectangle = null;
            invalidate();
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE){
            currentlyDrawingRectangle.setRight( (int) event.getX());
            currentlyDrawingRectangle.setBottom( (int) event.getY());
            invalidate();
        }


        return true;
    }

    //not sure why we have to have this but android was yelling about it?
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

    public void setNewImage
}

//Basic wrapper class for Rect that lets it also hold a color

class Rectangle{
    private Rect rect;
    private int color;

    Rectangle(int color, int left, int top, int right, int bottom){
        rect = new Rect(left, top, right, bottom);
        this.color = color;
    }

    public void setRight(int right){
        rect.right = right;
    }

    public void setBottom(int bottom){
        rect.bottom = bottom;
    }

    public void setRightAndBottom(int right, int bottom){
        rect.right = right;
        rect.bottom = bottom;
    }

    public void setColor(int color){
        this.color = color;
    }

    public int getColor(){
        return color;
    }

    public Rect getRect(){
        return rect;
    }
}
