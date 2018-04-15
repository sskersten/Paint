package com.harrisonwelch.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

/**
 * Created by Suzanne on 4/5/2018.
 */

public class PictDraw extends View{
    public static final int TOOL_BRUSH = 1;
    public static final int TOOL_LINE = 2;
    public static final int TOOL_RECTANGLE = 3;

    public static final int COLOR_RANDOM = -1;


    private int currentTool = TOOL_BRUSH;           //what tool is being used by the user




    private static final String TAG_PICT_DRAW = "TAG_PICT_DRAW";
    private int currentHeight, currentWidth;        //height and width of our widget container
    private Paint backgroundPaint;                  //draws the background
    private Paint mainPaint;                        //draws any fill-based things
    private Paint linePaint;                        //draws any stroke-based things
    private Random rand;                            //random number generator
    private int color;                              //what color to draw new shapes with
    private int thickness;                          //how thick to make lines
    Canvas canvas;
    Matrix matrix;
    Bitmap bitmap;
    Path path;

    Stack<Shape> shapes;                            //all of the shapes that have been drawn on the canvas
    ArrayList<Path> paths;

    float currX; // current path position
    float currY; // current path position

    //==============================================================================================
    //=     SETUP
    //==============================================================================================
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
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        rand = new Random();
        shapes = new Stack<>();
        matrix = new Matrix();
        paths = new ArrayList<>();

        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xffffffff);
        backgroundPaint.setStyle(Paint.Style.FILL);

        color = 0xff00ff00;
        mainPaint = new Paint();
        mainPaint.setColor(color);
        mainPaint.setStyle(Paint.Style.FILL);
        //mainPaint.setStrokeWidth(Helpers.dpToPx(20, getContext()));

        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5f);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        path = new Path();

    }

    //==============================================================================================
    //=     GETTERS/SETTERS
    //==============================================================================================
    //Sets the stroke to a passed in dp value
    // after converting it to px
    public void setStrokeThickness(int dpSize){
        thickness = (int) Helpers.dpToPx(dpSize, getContext());
    }

    public int getStrokeThickness(){
        return thickness;
    }

    public Bitmap getBitmap(){
        return this.bitmap;
    }

    //return a random color if set to that, else return the color
    public int getColor() {
        if (color == -1){
            return rand.nextInt(0x1000000) + 0xff000000;
        } else {
            return color;
        }
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public int getCurrentTool() {
        return currentTool;
    }

    public void setCurrentTool(int currentTool) {
        this.currentTool = currentTool;
    }

    //==============================================================================================
    //=     BASE VIEW FUNCTIONS
    //==============================================================================================
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

        canvas.drawPath(path, linePaint);

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


    //==============================================================================================
    //=     TOUCH EVENTS
    //==============================================================================================
    //The rectangle the user is currently manipulating and 'drawing'
    private Rectangle currentlyDrawingRectangle;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            performClick();         //needed by android studio to handle normal click event stuff
        }

        float x = event.getX();
        float y = event.getY();

        Log.i(TAG_PICT_DRAW, "x = " + x + ", y = " + y);

        if (currentTool == TOOL_BRUSH) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startPath(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    continuePath(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    stopPath(x, y);
                    invalidate();
                    break;
            }
        }

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
            //int color = rand.nextInt(0x1000000) + 0xff000000;

            Line line = new Line(x, y, x+1, y+1, color, thickness);
            currentlyDrawingLine = line;
            shapes.push(line);

            invalidate();


        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            currentlyDrawingLine = null;
            invalidate();
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE){
            currentlyDrawingLine.setEndx(( (int) event.getX()));
            currentlyDrawingLine.setEndy(( (int) event.getY()));
            invalidate();
        }
    }

    //handles all of the drawing of rectangles at different stages of the touch
    private void onDrawRectangle(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            //int color = rand.nextInt(0x1000000) + 0xff000000;

            Rectangle rect = new Rectangle(color, x, y, x+1, y+1);
            rect.setColor(color);
            currentlyDrawingRectangle = rect;
            shapes.push(rect);



            invalidate();
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


    //==============================================================================================
    //=     PATH
    //==============================================================================================
    public void startPath(float x, float y){
//        path.reset();
        path.moveTo(x,y);
        currX = x;
        currY = y;
    }

    public void continuePath(float x, float y){
        if ( Math.abs(currX - x) >= 4 || Math.abs(currY - y) >= 4 ){
            path.quadTo(currX,currY,(x+currX)/2,(y+currY)/2);
//            path.lineTo(x,y);
            currX = x;
            currY = y;
        }
    }

    public void stopPath(float x, float y){
        path.lineTo(x,y);
        currX = x;
        currY = y;
//        this.canvas.drawPath(path, linePaint);
        MyPath newPath = new MyPath(path, color);
        shapes.add(newPath);
        path = new Path();
    }

    public void clear(){
        path.reset();
        invalidate();
    }
}

//==============================================================================================
//=     SHAPE CLASSES
//==============================================================================================
//lets us keep all the shapes in a nice vector to draw in the same order they were placed
interface Shape{
    void draw(Canvas canvas, Paint paint);
    int getColor();

    int PAINT_FILL = 1;
    int PAINT_STROKE = 0;
    int getPaintToUse();
}

//Basic wrapper class for Rect that lets it also hold a color
//==============================================================================================
//=     RECTANGLE
//==============================================================================================
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

//==============================================================================================
//=     LINE
//==============================================================================================
class Line implements Shape{
    private int startx;
    private int starty;
    private int endx;
    private int endy;
    private int color;
    private int thickness;



    public Line(int startx, int starty, int endx, int endy, int color, int thickness) {
        this.startx = startx;
        this.starty = starty;
        this.endx = endx;

        this.endy = endy;
        this.color = color;
        this.thickness = thickness;
    }

    public void draw(Canvas canvas, Paint paint){
        canvas.drawLine(startx, starty, endx, endy, paint);
    }

    public void setEndx(int endx) {
        this.endx = endx;
    }

    public void setEndy(int endy) {
        this.endy = endy;
    }

    public int getThickness() {
        return thickness;
    }

    public int getColor(){
        return color;
    }

    @Override
    public int getPaintToUse() {
        return PAINT_STROKE;
    }
}

//==============================================================================================
//=     MYPATH
//==============================================================================================
class MyPath implements Shape {
    public Path path;
    public int color;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public MyPath(Path path, int color) {
        this.path = path;
        this.color = color;
    }

    public void draw(Canvas canvas, Paint paint){
        canvas.drawPath(path, paint);
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public int getPaintToUse() {
        return PAINT_STROKE;
    }
}