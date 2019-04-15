package com.map524s1a.snakegame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * Created by maddy on 4/13/19.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback{

    private static final String TAG = "CannonView"; // for logging errors

    public static final int HIT_REWARD = 3; // seconds added on a hit

    public static final double SNAKE_SPEED_PERCENT = 3.0 / 2;
    // constants for the Targets
    public static final double TARGET_WIDTH_PERCENT = 1.0 / 40;
    public static final double TARGET_LENGTH_PERCENT = 3.0 / 20;

    // text size 1/18 of screen width
    public static final double TEXT_SIZE_PERCENT = 1.0 / 18;

    private GameThread gameThread; // controls the game loop
    private Activity activity = null; // to display Game Over dialog in GUI thread
    private boolean dialogIsDisplayed = false;

    // game objects
    private Snake snake;
    private List<Food> foodList;

    // dimension variables
    private int screenWidth;
    private int screenHeight;

    // variables for the game loop and tracking statistics
    private boolean isGameOver; // is the game over?
    private double timeLeft; // time remaining in seconds
    private double totalElapsedTime; // elapsed seconds

    // constants and variables for managing sounds
    public static final int EATING_SOUND_ID = 0;
    public static final int DEAD_SOUND_ID = 1;
    private SoundPool soundPool; // plays sound effects
    private SparseIntArray soundMap; // maps IDs to SoundPool

    // Paint variables used when drawing each item on the screen
    private Paint textPaint; // Paint used to draw text
    private Paint backgroundPaint; // Paint used to clear the drawing area

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs); // call superclass constructor
        activity = (Activity) context; // store reference to MainActivity

        // register SurfaceHolder.Callback listener
        getHolder().addCallback(this);

        // configure audio attributes for game audio
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);
        AudioAttributes audioAttributes = attrBuilder.build();

        // initialize SoundPool to play the app's two sound effects
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        // (1) SoundPool.Builder setAudioAttributes (AudioAttributes attributes)
        // Sets the AudioAttributes.
        // For examples, game applications will use attributes built with usage information set to USAGE_GAME.
        // (2) AudioAttributes build ()
        // Combines all of the attributes that have been set and return a new AudioAttributes object.
        builder.setAudioAttributes(audioAttributes);
        // (1) SoundPool build ()
        soundPool = builder.build();

        soundMap = new SparseIntArray(3); // create new SparseIntArray
        soundMap.put(EATING_SOUND_ID,
            soundPool.load(context, R.raw.eating, 1));
        soundMap.put(DEAD_SOUND_ID,
            soundPool.load(context, R.raw.dead, 1));

        textPaint = new Paint();
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        screenWidth = w; // store CannonView's width
        screenHeight = h; // store CannonView's height

        // configure text properties
        textPaint.setTextSize((int) (TEXT_SIZE_PERCENT * screenHeight));
//        textPaint.setAntiAlias(true); // smoothes the text
    }

    // get width of the game screen
    public int getScreenWidth() {
        return screenWidth;
    }

    // get height of the game screen
    public int getScreenHeight() {
        return screenHeight;
    }

    // plays a sound with the given soundId in soundMap
    public void playSound(int soundId) {
        soundPool.play(soundMap.get(soundId), 1, 1, 1, 0, 1f);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!dialogIsDisplayed) {
            newGame(); // set up and start a new game
            gameThread = new GameThread(holder); // create thread
            gameThread.setRunning(true); // start game running
            gameThread.start(); // start the game loop thread
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameThread.setRunning(false); // terminate cannonThread

        while (retry) {
            try {
                gameThread.join(); // wait for cannonThread to finish
                retry = false;
            }
            catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    }

    private void newGame() {
        snake = Snake.createSnake(
            (int)TARGET_WIDTH_PERCENT * getScreenHeight(),
            (int)TARGET_LENGTH_PERCENT * getScreenWidth(),
            this);

        if (isGameOver) { // start a new game after the last game ended
            isGameOver = false; // the game is not over
            gameThread = new GameThread(getHolder()); // create thread
            gameThread.start(); // start the game loop thread
        }
    }

    private class GameThread extends Thread{

        private SurfaceHolder surfaceHolder; // for manipulating canvas
        private boolean threadIsRunning = true; // running by default

        // initializes the surface holder
        public GameThread(SurfaceHolder holder) {
            surfaceHolder = holder;
            setName("GameThread");
        }

        // changes running state
        public void setRunning(boolean running) {
            threadIsRunning = running;
        }

        // controls the game loop
        @Override
        public void run() {
            Canvas canvas = null; // used for drawing
            long previousFrameTime = System.currentTimeMillis();

            while (threadIsRunning) {
                try {
                    // get Canvas for exclusive drawing from this thread
                    canvas = surfaceHolder.lockCanvas();

                    // lock the surfaceHolder for drawing
                    synchronized(surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        totalElapsedTime += elapsedTimeMS / 1000.0;
                        updatePositions(elapsedTimeMS); // update game state
                        // testForCollisions(); // test for GameElement collisions
                        drawGameElements(canvas); // draw using the canvas
                        previousFrameTime = currentTime; // update previous time
                    }
                }
                finally {
                    // display canvas's contents on the CannonView
                    // and enable other threads to use the Canvas
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

    }

    private void updatePositions(double elapsedTimeMS) {
        double interval = elapsedTimeMS / 1000.0; // convert to seconds
    }

    // draws the game to the given Canvas
    public void drawGameElements(Canvas canvas) {
        // clear the background
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

        snake.draw(canvas); // draw the cannon

        // draw all of the Targets
//        for (GameElement target : targets)
//            target.draw(canvas);
    }

    // stops the game: called by CannonGameFragment's onPause method
    public void stopGame() {
        if (gameThread != null)
            gameThread.setRunning(false); // tell thread to terminate
    }

    // release resources: called by CannonGame's onDestroy method
    public void releaseResources() {
        soundPool.release(); // release all resources used by the SoundPool
        soundPool = null;
    }
}
