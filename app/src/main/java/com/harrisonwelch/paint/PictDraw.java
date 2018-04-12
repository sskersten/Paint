package com.harrisonwelch.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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

public class PictDraw extends View{
    public static final int TOOL_BRUSH = 1;
    public static final int TOOL_LINE = 2;
    public static final int TOOL_RECTANGLE = 3;


    private int currentTool;

    public int getCurrentTool() {
        return currentTool;
    }

    public void setCurrentTool(int currentTool) {
        this.currentTool = currentTool;
    }


    private static final String TAG_PICT_DRAW = "TAG_PICT_DRAW";
    private int currentHeight, currentWidth;        //height and width of our widget container
    private Paint backgroundPaint;
    private Paint mainPaint;
    private Paint linePaint;
    private Random rand;
    Canvas canvas;
    Matrix matrix;
    Bitmap bitmap;

    Stack<Rectangle> rectangles;
    Stack<Line> lines;
    Stack<Shape> shapes;


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
        lines = new Stack<>();
        shapes = new Stack<>();
        matrix = new Matrix();

        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xffffffff);
        backgroundPaint.setStyle(Paint.Style.FILL);

        mainPaint = new Paint();
        mainPaint.setColor(0xff00ff00);
        mainPaint.setStyle(Paint.Style.FILL);
        //mainPaint.setStrokeWidth(Helpers.dpToPx(20, getContext()));


        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setStrokeWidth(Helpers.dpToPx(5, getContext()));

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

        canvas.drawBitmap(bitmap, matrix, mainPaint);


        for (Shape s : shapes){

            if (s.getPaintToUse() == Shape.PAINT_FILL) {
                mainPaint.setColor(s.getColor());
                s.draw(canvas, mainPaint);
            } else if (s.getPaintToUse() == Shape.PAINT_STROKE) {
                linePaint.setColor(s.getColor());
                s.draw(canvas, linePaint);
            }
        }

        /*
        //draw all the rectangles
        for (Rectangle r : rectangles) {
            mainPaint.setColor(r.getColor());
            r.draw(canvas, mainPaint);
        }

        for (Line l : lines) {
            linePaint.setColor(l.color);
            canvas.drawLine(l.startx, l.starty, l.endx, l.endy, linePaint);
        }*/

        this.canvas = canvas;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        currentHeight = h;
        currentWidth = w;

        bitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }


    //The rectangle the user is currently manipulating and 'drawing'
    private Rectangle currentlyDrawingRectangle;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
       if (currentTool == TOOL_RECTANGLE){
           onDrawRectangle(event);
       }
       if (currentTool == TOOL_LINE){
           onDrawLine(event);
       }


        return true;
    }

    private Line currentlyDrawingLine;
    //handles all of the drawing of rectangles at different stages of the touch
    private void onDrawLine(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int color = rand.nextInt(0x1000000) + 0xff000000;

            Line line = new Line();
            line.startx = x;
            line.starty = y;
            line.endx = x + 1;
            line.endy = y + 1;
            line.color = color;
            currentlyDrawingLine = line;
            lines.push(line);
            shapes.push(line);

            invalidate();

            performClick();         //needed by android studio to handle normal click event stuff
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            currentlyDrawingLine = null;
            invalidate();
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE){
            currentlyDrawingLine.endx = ( (int) event.getX());
            currentlyDrawingLine.endy = ( (int) event.getY());
            invalidate();
        }
    }

    //handles all of the drawing of rectangles at different stages of the touch
    private void onDrawRectangle(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int color = rand.nextInt(0x1000000) + 0xff000000;

            Rectangle rect = new Rectangle(color, x, y, x+1, y+1);
            currentlyDrawingRectangle = rect;
            rectangles.push(rect);
            shapes.push(rect);



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
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
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

    public void setNewImage(Bitmap alteredBitmap, Bitmap bitmap){
        this.bitmap = bitmap;
        canvas = new Canvas(alteredBitmap);
//        mainPaint = new Paint();
//        mainPaint.setColor(Color.GREEN);
//        mainPaint.setStrokeWidth(5);
        matrix = new Matrix();
        matrix.postScale(currentWidth/bitmap.getWidth(), currentHeight / bitmap.getHeight());
//        bitmap.setHeight(currentHeight);
//        bitmap.setWidth(currentWidth);
        canvas.drawBitmap(bitmap,matrix ,mainPaint);

        Log.i(TAG_PICT_DRAW, "bitmap.getHeight() = " + bitmap.getHeight());
        Log.i(TAG_PICT_DRAW, "bitmap.getWidth() = " + bitmap.getWidth());
        Log.i(TAG_PICT_DRAW, "currentHeight = " + currentHeight);
        Log.i(TAG_PICT_DRAW, "currentWidth = " + currentWidth);
//        BitmapDrawable bd = new BitmapDrawable(getContext().getResources(), bitmap);
//        setBackground(bd);

        Log.i(TAG_PICT_DRAW, " setting image");
//        setImageBitmap(alteredBitmap);
        invalidate();
    }

    public Bitmap getBitmap(){
        return this.bitmap;
    }

}

//lets us keep all the shapes in a nice vector to draw in the same order they were placed
interface Shape{
    void draw(Canvas canvas, Paint paint);
    int getColor();

    int PAINT_FILL = 1;
    int PAINT_STROKE = 0;
    int getPaintToUse();
}

//Basic wrapper class for Rect that lets it also hold a color

class Rectangle implements Shape{
    private Rect rect;
    private int color;

    Rectangle(int color, int left, int top, int right, int bottom){
        rect = new Rect(left, top, right, bottom);
        this.color = color;
    }

    public void draw(Canvas canvas, Paint paint){
        canvas.drawRect(getRect(), paint);
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

    @Override
    public int getPaintToUse() {
        return PAINT_FILL;
    }
}

class Line implements Shape{
    public int startx;
    public int starty;
    public int endx;
    public int endy;
    public int color;

    public void draw(Canvas canvas, Paint paint){
        canvas.drawLine(startx, starty, endx, endy, paint);
    }

    public int getColor(){
        return color;
    }

    @Override
    public int getPaintToUse() {
        return PAINT_STROKE;
    }
}
