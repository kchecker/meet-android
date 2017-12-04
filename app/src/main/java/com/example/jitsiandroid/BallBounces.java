package com.example.jitsiandroid;

/**
 * Created by supuni on 11/21/17.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import static com.facebook.react.common.ReactConstants.TAG;

/**
 * Created by supuni on 11/21/17.
 */

class BallBounces extends SurfaceView implements SurfaceHolder.Callback,Observer {
    GameThread thread;
    int screenW; //Device's screen width.
    int screenH; //Devices's screen height.
    int ballX; //Ball x position.
    int ballY; //Ball y position.
    int initialY ;
    float dY; //Ball vertical speed.
    int ballW;
    int ballH;
    int bgrW;
    int bgrH;
    int angle;
    int bgrScroll;
    int dBgrY; //Background scroll speed.
    float acc;
    Bitmap ball, bgr, bgrReverse;
    boolean reverseBackroundFirst;
    boolean ballFingerMove;

    //Measure frames per second.
    long now;
    int framesCount=0;
    int framesCountAvg=0;
    long framesTimer=0;
    Paint fpsPaint=new Paint();

    //Frame speed
    long timeNow;
    long timePrev = 0;
    long timePrevFrame = 0;
    long timeDelta;

    //touchevent
    float  touch_initialX, touch_initialY;

    int x=0;
    int y=0;

    public BallBounces(Context context) {
        super(context);
        //-------------------------------------------------------------------------------------------------
        Subject subjectCoordinates = WebSocketEcho.getInstance();
        subjectCoordinates.registerObserver(this);
        ball = BitmapFactory.decodeResource(getResources(), R.drawable.redpoint); //Load a ball image.
        //bgr = BitmapFactory.decodeResource(getResources(),R.drawable.transparent); //Load a background.
        ballW = ball.getWidth();
        ballH = ball.getHeight();

        //Create a flag for the onDraw method to alternate background with its mirror image.
        reverseBackroundFirst = false;

        //Initialise animation variables.
        acc = 0.0f; //Acceleration
        dY = 0; //vertical speed
        initialY = 100; //Initial vertical position
        angle = 0; //Start value for the rotation angle
        bgrScroll = 0;  //Background scroll position
        dBgrY = 1; //Scrolling background speed

        fpsPaint.setTextSize(30);

        //Set thread

        this.setZOrderOnTop(true);
        SurfaceHolder a = getHolder();
        a.setFormat(PixelFormat.TRANSPARENT);
        a.addCallback(this);
        setFocusable(true);
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //This event-method provides the real dimensions of this custom view.
        screenW = w;
        screenH = h;

       // bgr = Bitmap.createScaledBitmap(bgr, w, h, true); //Scale background to fit the screen.
//        bgrW = bgr.getWidth();
//        bgrH = bgr.getHeight();
        bgrW = w;
        bgrH = h;

        //Create a mirror image of the background (horizontal flip) - for a more circular background.
        Matrix matrix = new Matrix();  //Like a frame or mould for an image.
        matrix.setScale(-1, 1); //Horizontal mirror effect.
        //bgrReverse = Bitmap.createBitmap(bgr, 0, 0, bgrW, bgrH, matrix, true); //Create a new mirrored bitmap by applying the matrix.

        ballX = (int) (screenW /2) - (ballW / 2) ; //Centre ball X into the centre of the screen.
        ballY = -50; //Centre ball height above the screen.
    }

    //***************************************
    //*************  TOUCH  *****************
    //***************************************
    @Override
    public synchronized boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN: {
//                ballX = (int) ev.getX() - ballW/2;
//                ballY = (int) ev.getY() - ballH/2;
//                ballFingerMove = true;
//                break;
//            }

            case MotionEvent.ACTION_MOVE: {
//                ballX = (int) ev.getX() - ballW/2;
//                ballY = (int) ev.getY() - ballH/2;

                Log.d("DEVICE: ", String.valueOf((int) ev.getX()));
                Log.d("DEVICE: ", String.valueOf((int) ev.getY()));
                /*int deviceWidth = (int) ev.getX();
                int deviceHeight = (int) ev.getY();
                x = (int)(deviceWidth*x/400);
                y = (int)(deviceHeight*y/1140);*/
                onMoveEvent(x,y);
                getScreenWidth();
                break;
            }

            case MotionEvent.ACTION_UP:
                ballFingerMove = false;
                dY = 0;
                break;
        }
        return true;
    }
//added new function get user inputs
   public boolean onMoveEvent(int x, int y){
        ballX = x;//(int)((screenW*x)/400);

        ballY = y; //(int)((screenH*y)/1140);//0.56
        Log.d("screen", String.valueOf(screenW));

        return true;
   }

        //get screen size
       public static int getScreenWidth() {
          Log.d("size", String.valueOf(Resources.getSystem().getDisplayMetrics().widthPixels));
          return Resources.getSystem().getDisplayMetrics().widthPixels;
       }
//
//    public static int getScreenHeight() {
//        return Resources.getSystem().getDisplayMetrics().heightPixels;
//    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//clear the point
        //canvas.drawColor(Color.argb(0, 0, 0, 0),PorterDuff.Mode.CLEAR);
        //Draw scrolling background.
        Rect fromRect1 = new Rect(0, 0, bgrW - bgrScroll, bgrH);
        Rect toRect1 = new Rect(bgrScroll, 0, bgrW, bgrH);

        Rect fromRect2 = new Rect(bgrW - bgrScroll, 0, bgrW, bgrH);
        Rect toRect2 = new Rect(0, 0, bgrScroll, bgrH);

//        if (!reverseBackroundFirst) {
//            canvas.drawBitmap(bgr, fromRect1, toRect1, null);
//            canvas.drawBitmap(bgrReverse, fromRect2, toRect2, null);
//        }
//        else{
//            canvas.drawBitmap(bgr, fromRect2, toRect2, null);
//            canvas.drawBitmap(bgrReverse, fromRect1, toRect1, null);
//        }
//
//        //Next value for the background's position.
//        if ( (bgrScroll += dBgrY) >= bgrW) {
//            bgrScroll = 0;
//            reverseBackroundFirst = !reverseBackroundFirst;
//        }

        //Compute roughly the ball's speed and location.
        if (!ballFingerMove) {
            ballY += (int) dY; //Increase or decrease vertical position.
            if (ballY > (screenH - ballH)) {
                dY=(-1)*dY; //Reverse speed when bottom hit.
            }
            dY+= acc; //Increase or decrease speed.
        }

        //Increase rotating angle
        if (angle++ >360)
            angle =0;

        //DRAW BALL
        //Rotate method one
        /*
        Matrix matrix = new Matrix();
        matrix.postRotate(angle, (ballW / 2), (ballH / 2)); //Rotate it.
        matrix.postTranslate(ballX, ballY); //Move it into x, y position.
        canvas.drawBitmap(ball, matrix, null); //Draw the ball with applied matrix.

        */// Rotate method two

        canvas.save(); //Save the position of the canvas matrix.
        canvas.rotate(angle, ballX + (ballW / 2), ballY + (ballH / 2)); //Rotate the canvas matrix.
        canvas.drawBitmap(ball, ballX, ballY, null); //Draw the ball by applying the canvas rotated matrix.
        canvas.restore(); //Rotate the canvas matrix back to its saved position - only the ball bitmap was rotated not all canvas.

        //*/

        //Measure frame rate (unit: frames per second).
        now=System.currentTimeMillis();
//        canvas.drawText(framesCountAvg+" fps", 40, 70, fpsPaint);
        framesCount++;
        if(now-framesTimer>1000) {
            framesTimer=now;
            framesCountAvg=framesCount;
            framesCount=0;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //holder.setFormat(PixelFormat.TRANSPARENT);
        thread = new GameThread(getHolder(), this);
        //thread.getSurfaceHolder().setFormat(PixelFormat.TRANSPARENT);
        //thread.setRunning(false);
        //thread.getSurfaceHolder().setFormat(PixelFormat.TRANSLUCENT);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {

            }
        }
    }

    //get coordinates for onMoveEvent
    public void onCoordinatesChanged(int startX, int startY) {
        onMoveEvent(startX,startY);
    }

    class GameThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private BallBounces gameView;
        private boolean run = false;

        public GameThread(SurfaceHolder surfaceHolder, BallBounces gameView) {
            this.surfaceHolder = surfaceHolder;
            //this.surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
            this.gameView = gameView;
        }

        public void setRunning(boolean run) {
            this.run = run;
        }

        public SurfaceHolder getSurfaceHolder() {
            return surfaceHolder;
        }

        @Override
        public void run() {
            Canvas c;
            while (run) {
                c = null;

                //limit frame rate to max 60fps
                timeNow = System.currentTimeMillis();
                timeDelta = timeNow - timePrevFrame;
                if ( timeDelta < 16) {
                    try {
                        Thread.sleep(16 - timeDelta);
                    }
                    catch(InterruptedException e) {

                    }
                }
                timePrevFrame = System.currentTimeMillis();

                try {

                    c = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        //call methods to draw and process next fame

                        gameView.onDraw(c);
                    }
                } finally {
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
}
