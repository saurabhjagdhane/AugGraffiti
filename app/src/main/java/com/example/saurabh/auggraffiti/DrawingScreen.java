/**
 * This class is a custom View class. This is helpful in drawing (placing) a tag on camera view.
 * DrawingScreen class uses Canvas class which has various methods for drawing lines, bitmaps and other graphics.
 * These methods are used in onDraw override method to create our own custom view class.
*/

package com.example.saurabh.auggraffiti;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

//DrawingScreen is custom View class
public class DrawingScreen extends View {

        public  int height;

        private Bitmap mBitmap; // Bitmap holds the pixels

        private Canvas mCanvas;  // Canvas is used to write into Bitmaps
        private Path mPath;  // Drawing primitive chosen for random tag drawing instead of specified shapes
        private Paint   mBitmapPaint;  // Colors and styles for drawing
        Context context;
        private Paint customPaint;
        private Path customPath;
        private static Paint mPaint;

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

    // Constructor setting up the color, style, stroke width, etc. for use with a Paint class
        public DrawingScreen(Context c, Paint mPaint) {
            super(c);
            context=c;
            DrawingScreen.mPaint = mPaint;
            mPath = new Path();  //Instantiate class
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            customPaint = new Paint();
            customPath = new Path();
            customPaint.setAntiAlias(true);
            customPaint.setColor(Color.BLUE);
            customPaint.setStyle(Paint.Style.STROKE);
            customPaint.setStrokeJoin(Paint.Join.MITER);
            customPaint.setStrokeWidth(4f);
        }

    // Bitmap corresponding to tag drawn while placing a tag
        public Bitmap getmBitmap(){
            return this.mBitmap;
        }

    //  Called when size of bitmap is changed and setting bitmap with new width and height
        @Override
        protected void onSizeChanged(int wid, int ht, int old_width, int old_height) {
            super.onSizeChanged(wid, ht, old_width, old_height);

            mBitmap = Bitmap.createBitmap(wid, ht, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);

        }

    // Parameter to onDraw overridden method is Canvas object that View will use to draw.
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( customPath,  customPaint);  // Drawing path using specified object of Paint
        }

    // Touch event to handle down action
        private void touch_down(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

    // Touch event to handle move action
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                customPath.reset();
                customPath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

    // Touch event to handle up action
        private void touch_up() {
            mPath.lineTo(mX, mY);
            customPath.reset();
            // Drawing path on a screen
            mCanvas.drawPath(mPath,  mPaint);
            mPath.reset();
        }

    // onTouchEvent handles touch screen motion events.
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            float x = motionEvent.getX();   // X position relative in pixel
            float y = motionEvent.getY();   // Y position relative in pixel

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_down(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
}

