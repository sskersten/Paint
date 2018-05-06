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
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.Serializable;
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
    public static final int TOOL_STICKER = 4;

    public static final int COLOR_RANDOM = -1;


    private int currentTool = TOOL_BRUSH;           //what tool is being used by the user

    public static final int STICKER_STAR = 1;
    public static final int STICKER_LEAF = 2;
    public static final int STICKER_LEE = 3;
    Bitmap currentBitmap;
    Bitmap stickerStar;
    Bitmap stickerLee;
    Bitmap stickerLeaf;


    Bitmap frame_outside;


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
    public Stack<Integer> shapePositions;

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
        shapePositions = new Stack<>();
        this.bitmap = null;

        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xffffffff);
        backgroundPaint.setStyle(Paint.Style.FILL);



        color = 0xff00f0f0;
        setStrokeThickness(5);
        mainPaint = new Paint();
        mainPaint.setColor(color);
        mainPaint.setStyle(Paint.Style.FILL);
        //mainPaint.setStrokeWidth(Helpers.dpToPx(20, getContext()));

        linePaint = new Paint();
        linePaint.setColor(color);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(thickness);
        linePaint.setStrokeJoin(Paint.Join.ROUND);


        path = new Path();

        setupStickerBitmaps();
    }

    private void setupStickerBitmaps(){
        Drawable androidDrawable = getResources().getDrawable((R.drawable.star));

        int size = (int) Helpers.dpToPx(50, getContext());
        stickerStar = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(stickerStar);
        androidDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        androidDrawable.draw(canvas);


        Drawable leeDrawable = getResources().getDrawable((R.drawable.lee));

        stickerLee = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas2 = new Canvas(stickerLee);
        leeDrawable.setBounds(0, 0, canvas2.getWidth(), canvas2.getHeight());
        leeDrawable.draw(canvas2);



        Drawable leafDrawable = getResources().getDrawable((R.drawable.leaves));

        stickerLeaf = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas3 = new Canvas(stickerLeaf);
        leafDrawable.setBounds(0, 0, canvas3.getWidth(), canvas3.getHeight());
        leafDrawable.draw(canvas3);

        currentBitmap = stickerStar;



    }

    //==============================================================================================
    //=     GETTERS/SETTERS
    //==============================================================================================
    public void setSticker(int stickerId){
        Log.i("Sticker", "Called stickerset with " + stickerId);

        switch(stickerId){
            case STICKER_STAR:
                currentBitmap = stickerStar;
                break;
            case STICKER_LEAF:
                currentBitmap = stickerLeaf;
                break;
            case STICKER_LEE:
                currentBitmap = stickerLee;
                break;
        }
    }

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
        this.linePaint.setColor(color); // sometimes drawing goes in the previous color
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

    public Stack<Shape> getShapes() {
        return shapes;
    }

    public void setShapes(Stack<Shape> shapes) {
        this.shapes = shapes;
        invalidate();
    }

    public Stack<Integer> getShapePositions() {
        return shapePositions;
    }

    public void setShapePositions(Stack<Integer> shapePositions) {
        this.shapePositions = shapePositions;
        invalidate();
    }

    //==============================================================================================
    //=     BASE VIEW FUNCTIONS
    //==============================================================================================
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // give white back
        canvas.drawPaint(backgroundPaint);

        // then the image (if set)
        if (bitmap != null){
//            canvas.drawBitmap(this.bitmap, matrix, mainPaint);
            canvas.drawBitmap(this.bitmap, null, new Rect(0,0,currentWidth,currentHeight), null);
            // float left, float top
//            int cx = (currentWidth - bitmap.getWidth()) >> 1;
//            int cy = (currentHeight - bitmap.getHeight()) >> 1;
//            canvas.drawBitmap(bitmap,cx,cy,null);
        }



        // go thru shaps 1 by 1
        for (Shape s : shapes){
            if (s.getPaintToUse() == Shape.PAINT_FILL) {
                mainPaint.setColor(s.getColor());
                s.draw(canvas, mainPaint);
            } else if (s.getPaintToUse() == Shape.PAINT_STROKE) {
                linePaint.setColor(s.getColor());
                linePaint.setStrokeWidth(s.getThickness());
                s.draw(canvas, linePaint);
            }
        }
        Log.i(TAG_PICT_DRAW,"STUFF IN HERE. shapes.size() = " + shapes.size());

//        linePaint.setStrokeWidth(thickness);
        Log.i(TAG_PICT_DRAW, "HELLO");
//        canvas.drawPath(path, linePaint);

        drawFrame(canvas, mainPaint);

        this.canvas = canvas;

    }

    private boolean doDrawFrame = false;

    public void toggleDoDrawFrame(){
        doDrawFrame = !doDrawFrame;
        invalidate();
    }

    private void drawFrame(Canvas canvas, Paint paint){

        if (doDrawFrame) {
            if (frame_outside == null) {
                Drawable frameDrawable = getResources().getDrawable((R.drawable.frame_outsitde));

                frame_outside = Bitmap.createBitmap(this.getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);

                Canvas canvas4 = new Canvas(frame_outside);
                frameDrawable.setBounds(0, 0, canvas4.getWidth(), canvas4.getHeight());
                frameDrawable.draw(canvas4);
            }


            canvas.drawBitmap(frame_outside, 0, 0, paint);
        }
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

        Log.i(TAG_PICT_DRAW, "onMeasure");
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
        Log.i(TAG_PICT_DRAW, "setMeasuredDimension("+width+", "+height+")");

    }


    //==============================================================================================
    //=     TOUCH EVENTS
    //==============================================================================================
    private boolean isDrawing = false;

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
//                    compressDrawnLines();
                    shapePositions.push(shapes.size());
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
        if (currentTool == TOOL_STICKER){
            onDrawSticker(event);
        }

        return true;
    }


    //Create a line on first touch, and move the line while the user is dragging their finger around
    private void onDrawLine(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            Line line = new Line(x, y, x+1, y+1, color, thickness);
            shapes.push(line);
            isDrawing = true;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            isDrawing = false;
            shapePositions.push(shapes.size());
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (isDrawing) {
                ((Line) shapes.peek()).setEndx(((int) event.getX()));
                ((Line) shapes.peek()).setEndy(((int) event.getY()));
            }
        }
        invalidate();
    }

    //handles all of the drawing of rectangles at different stages of the touch
    private void onDrawRectangle(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            Rectangle rect = new Rectangle(color, x, y, x+1, y+1);
            rect.setColor(color);
            shapes.push(rect);
            isDrawing = true;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            isDrawing = false;
            shapePositions.push(shapes.size());
        }
        //update the last drawn shape if we're still drawing it and moved
        else if (event.getAction() == MotionEvent.ACTION_MOVE){
            ((Rectangle) shapes.peek()).setRight( (int) event.getX());
            ((Rectangle) shapes.peek()).setBottom( (int) event.getY());
        }
        invalidate();
    }

    //Create a line on first touch, and move the line while the user is dragging their finger around
    private void onDrawSticker(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            Sticker sticker = new Sticker(x, y, currentBitmap);
            shapes.push(sticker);
            isDrawing = true;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            isDrawing = false;
            shapePositions.push(shapes.size());
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE){
            if (isDrawing) {
                ((Sticker) shapes.peek()).setX(((int) event.getX()));
                ((Sticker) shapes.peek()).setY(((int) event.getY()));
            }
        }
        invalidate();
    }

    public void setNewImage(Bitmap alteredBitmap, Bitmap bitmap){
        Log.i(TAG_PICT_DRAW, "setNewImage(...)");
        this.bitmap = bitmap;
//        bitmap.setWidth(currentWidth);
//        bitmap.setHeight(currentHeight);
//        canvas = new Canvas(alteredBitmap);
//        matrix = new Matrix();
//        canvas.drawBitmap(bitmap,matrix ,mainPaint);
//        canvas.drawBitmap(bitmap, null, new Rect(0,0,currentWidth/2,currentHeight), null);

        Log.i(TAG_PICT_DRAW, "bitmap.getHeight() = " + bitmap.getHeight());
        Log.i(TAG_PICT_DRAW, "bitmap.getWidth() = " + bitmap.getWidth());
        Log.i(TAG_PICT_DRAW, "currentHeight = " + currentHeight);
        Log.i(TAG_PICT_DRAW, "currentWidth = " + currentWidth);

//        https://stackoverflow.com/questions/11202754/android-how-to-enlarge-a-bitmap#_=_
//        matrix.postScale((float)(currentWidth / bitmap.getWidth()), (float)(currentHeight / bitmap.getHeight()));

//        this.bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        Log.i(TAG_PICT_DRAW, "bitmap.getHeight() = " + bitmap.getHeight());
        Log.i(TAG_PICT_DRAW, "bitmap.getWidth() = " + bitmap.getWidth());
        Log.i(TAG_PICT_DRAW, "currentHeight = " + currentHeight);
        Log.i(TAG_PICT_DRAW, "currentWidth = " + currentWidth);

        Log.i(TAG_PICT_DRAW, " setting image");

        invalidate();
    }


    //==============================================================================================
    //=     PATH
    //==============================================================================================
    public void startPath(float x, float y){
        // make a new tmp path
        MyPath myPathTmp = new MyPath();
        myPathTmp.setColor(color);
        myPathTmp.setThickness(thickness);
        myPathTmp.moveTo(x,y);
        currX = x;
        currY = y;
        shapes.add(myPathTmp);
    }

    public void continuePath(float x, float y){
        MyPath myPathTmp = (MyPath)shapes.get(shapes.size()-1);
        if ( Math.abs(currX - x) >= 4 || Math.abs(currY - y) >= 4 ){
            myPathTmp.quadTo(currX,currY,(x+currX)/2,(y+currY)/2);
//            path.lineTo(x,y);
            currX = x;
            currY = y;
            shapes.add(myPathTmp);
        }
    }

    public void stopPath(float x, float y){
        MyPath myPathTmp = (MyPath)shapes.get(shapes.size()-1);
        myPathTmp.lineTo(x,y);
        currX = x;
        currY = y;
        shapes.add(myPathTmp);
    }

    public void clear(){
        path.reset();
        shapes.clear();
        shapePositions.clear();
        bitmap = null;
//        bitmap = new Bitmap();
        invalidate();
    }

    public void undo(){
        Log.i(TAG_PICT_DRAW,"UNDO!!!");
        MyPath path = null;
        if (shapes.size() >= 1 && shapePositions.size() >= 2){
//            shapes.pop();
            int startPos = shapePositions.pop();
            int stopPos = shapePositions.pop();
            for(int i = startPos; i > stopPos; i--){
                shapes.pop();
            }
            shapePositions.push(stopPos);
            invalidate();
        } else {
            // clear the stuff if less than zero
            clear();
        }
    }

    public void deletePath(){
        Log.i(TAG_PICT_DRAW, "compressDrawnLines");

        // place the good one on the stack
    }
}

//==============================================================================================
//=     SHAPE CLASSES
//==============================================================================================
//Defines a shape, which must know how to draw itself when given a canvas and a paint,
// have a color, and know what type of paint to use.
// using this lets us have a stack of shapes that we can draw in order
class Shape{
    //setup the color, thickness, and paint to use.
    public Shape(int color, int thickness, int paintToUse) {
        this.color = color;
        this.thickness = thickness;
        this.paintToUse = paintToUse;
    }

    //function that MUST be overridden in
    public void draw(Canvas canvas, Paint paint){
        throw new RuntimeException("Error: No draw function overridden in the current class you're trying to use.");
    }

    //gets the color/thickness of the thing to draw
    private int color;
    private int thickness;
    public int getThickness() {
        return thickness;
    }
    public int getColor(){
        return color;
    }
    public void setColor(int color) {this.color = color;}
    public void setThickness(int thickness) {this.thickness = thickness;}

    //Update the type of paint being used
    static final int PAINT_FILL = 1;
    static final int PAINT_STROKE = 0;
    private int paintToUse;
    protected void setPaintToUse(int paintCode){this.paintToUse = paintCode;}
    public int getPaintToUse(){return paintToUse;}
}

//Basic wrapper class for Rect that lets it also hold a color
//==============================================================================================
//=     RECTANGLE
//==============================================================================================
class Rectangle extends Shape{
    private Rect rect;

    Rectangle(int color, int left, int top, int right, int bottom){
        super(color, 0, Shape.PAINT_FILL);
        rect = new Rect(left, top, right, bottom);
    }

    @Override
    public void draw(Canvas canvas, Paint paint){
        canvas.drawRect(getRect(), paint);
    }

    //update the parts of the rectangle
    public void setRight(int right){
        rect.right = right;
    }
    public void setBottom(int bottom){
        rect.bottom = bottom;
    }
    public Rect getRect(){
        return rect;
    }
}

//==============================================================================================
//=     LINE
//==============================================================================================
class Line extends Shape{
    private int startx;
    private int starty;
    private int endx;
    private int endy;

    public Line(int startx, int starty, int endx, int endy, int color, int thickness) {
        super(color, thickness, Shape.PAINT_STROKE);
        this.startx = startx;
        this.starty = starty;
        this.endx = endx;
        this.endy = endy;
    }

    public void draw(Canvas canvas, Paint paint){
        canvas.drawLine(startx, starty, endx, endy, paint);
    }

    //update the endpoints of the line
    public void setEndx(int endx) {
        this.endx = endx;
    }
    public void setEndy(int endy) {
        this.endy = endy;
    }
}

//==============================================================================================
//=     MYPATH
//==============================================================================================
class MyPath extends Shape {
    private Path path;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public MyPath(){
        super(0xff000000, 5, Shape.PAINT_STROKE);
        this.path = new Path();
    }

    public MyPath(Path path, int color, int thickness) {
        super(color, thickness, Shape.PAINT_STROKE);
        this.path = path;
    }

    public void draw(Canvas canvas, Paint paint){
        canvas.drawPath(path, paint);
    }

    public void moveTo(float x, float y){
        path.moveTo(x,y);
    }
    public void quadTo(float x, float y, float x2, float y2){
        path.quadTo(x,y,x2,y2);
    }
    public void lineTo(float x, float y){
        path.lineTo(x,y);
    }
}

//==============================================================================================
//=     STICKER
//==============================================================================================
class Sticker extends Shape {
    private int x, y;           //position of the sticker
    private Bitmap bitmap;      //bitmap to draw

    public Sticker(int x, int y, Bitmap bitmap) {
        super(0xff000000, 0, Shape.PAINT_FILL);
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawBitmap(bitmap, x, y, paint);
    }

    //position getters and setters
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }


}