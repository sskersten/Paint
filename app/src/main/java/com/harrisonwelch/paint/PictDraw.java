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

    Stack<Shape> shapes;                            //all of the shapes that have been drawn on the canvas

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
    public int getStrokeThickness(){
        return thickness;
    }
    public void setStrokeThickness(int dpSize){
        thickness = (int) Helpers.dpToPx(dpSize, getContext());
    }

    public Bitmap getBitmap(){
        return this.bitmap;
    }

    //return a random color if set to -1, else return the color
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

    public Stack<Shape> getShapes() {
        return shapes;
    }
    public void setShapes(Stack<Shape> shapes) {
        this.shapes = shapes;
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



        // go thru shapes 1 by 1
        for (Shape s : shapes){
            Log.i("Shapes", "Found a shape.");
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
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        //create the needed shape
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            performClick();         //needed by android studio to handle normal click event stuff

            //create a new shape based on which shape is currently selected
            switch (currentTool){
                case TOOL_BRUSH:    shapes.push(new MyPath(x, y, color, thickness));                                       break;
                case TOOL_LINE:     shapes.push(new Line(x, y, x+1, y+1, color, thickness));                    break;
                case TOOL_RECTANGLE:shapes.push(new Rectangle(color, (int) x, (int) y, (int) x+1, (int) y+1)); break;
                case TOOL_STICKER:  shapes.push( new Sticker((int)x,(int) y, currentBitmap));                             break;
            }
        }

        //handles the moving of your finger and when you up your finger
        shapes.peek().onTouchEventHandler(event, this);
        return true;
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

    public void clear(){
        shapes.clear();
        bitmap = null;
        invalidate();
    }

    public void undo(){
        Log.i(TAG_PICT_DRAW,"UNDO!!!");
        if (shapes.size() >= 1){
            shapes.pop();
            invalidate();
        }
    }

}