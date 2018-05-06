package com.harrisonwelch.paint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import java.util.Stack;

/**
 * Created by Suzanne on 5/6/2018.
 * This file holds a Shape class and all the different
 * shapes that can be drawn in the PictDraw object.
 */

public class Shape{
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

    private boolean isDrawing = false;
    //handles how to update the current shape between onTouchEvents
    public void onTouchEventHandler(MotionEvent event, View view){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                view.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDrawing) {
                    onTouchMove(event);
                    view.invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                break;
        }
    }

    public void onTouchMove(MotionEvent event){
        throw new RuntimeException("Error: No onTouchMove function overridden in the current class you're trying to use.");
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

    @Override
    public void onTouchMove(MotionEvent event) {
        this.setRight( (int) event.getX());
        this.setBottom( (int) event.getY());
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
    private float startx;
    private float starty;
    private float endx;
    private float endy;

    public Line(float startx, float starty, float endx, float endy, int color, int thickness) {
        super(color, thickness, Shape.PAINT_STROKE);
        this.startx = startx;
        this.starty = starty;
        this.endx = endx;
        this.endy = endy;
    }

    public void draw(Canvas canvas, Paint paint){
        canvas.drawLine(startx, starty, endx, endy, paint);
    }

    @Override
    public void onTouchMove(MotionEvent event) {
        setEndx(( event.getX()));
        setEndy(( event.getY()));
    }

    //update the endpoints of the line
    public void setEndx(float endx) {
        this.endx = endx;
    }
    public void setEndy(float endy) {
        this.endy = endy;
    }
}

//==============================================================================================
//=     MYPATH
//==============================================================================================
class MyPath extends Shape {
    private Path path;

    private float currX, currY;
    public MyPath(float startX, float startY, int color, int thickness) {
        super(color, thickness, Shape.PAINT_STROKE);
        this.path = new Path();
        moveTo(startX, startY);
        currX = startX;
        currY = startY;
    }

    public void continuePath(float x, float y){
        if ( Math.abs(currX - x) >= 4 || Math.abs(currY - y) >= 4 ){
            //myPathTmp.lineTo(x, y);
            this.quadTo(currX,currY,(x+currX)/2,(y+currY)/2);
//            path.lineTo(x,y);
            currX = x;
            currY = y;
        }
    }

    public void draw(Canvas canvas, Paint paint){
        canvas.drawPath(path, paint);
    }

    @Override
    public void onTouchMove(MotionEvent event) {
        continuePath(event.getX(), event.getY());
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

    @Override
    public void onTouchMove(MotionEvent event) {
        setX(((int) event.getX()));
        setY(((int) event.getY()));
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