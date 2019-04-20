package com.map524s1a.snakegame;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by maddy on 4/13/19.
 */

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CannonView"; // for logging errors

    public static final double TARGET_WIDTH_PERCENT = 1.0 / 50;
    public static final double TARGET_LENGTH_PERCENT = 6.0 / 20;

    public static final double TARGET_MIN_SPEED_PERCENT = 4.0 / 20;
    public static final double TARGET_MAX_SPEED_PERCENT = 5.1 / 30;

    // text size 1/18 of screen width
    public static final double TEXT_SIZE_PERCENT = 1.0 / 18;
    private int score = 0;

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
    private boolean restartNewGame; // is the game over?

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

        textPaint = new Paint();
        textPaint.setColor(Color.BLUE);
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK);

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
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            }
        }
    }

    private void newGame() {
        score = 0;
        Random random = new Random(); // for determining random velocities
        double velocity = (screenWidth > screenHeight ? screenHeight : screenWidth) *
            (random.nextDouble() * (TARGET_MAX_SPEED_PERCENT - TARGET_MIN_SPEED_PERCENT) + TARGET_MIN_SPEED_PERCENT);
        snake = Snake.createSnake(
            (int) (TARGET_WIDTH_PERCENT * getScreenHeight()),
            (int) (TARGET_LENGTH_PERCENT * getScreenWidth()),
            velocity,
            Color.GREEN,
            this);

        foodList = new ArrayList<>();
        createFood();

        if (restartNewGame) { // start a new game after the last game ended
            restartNewGame = false; // the game is not over
            gameThread = new GameThread(getHolder()); // create thread
            gameThread.start(); // start the game loop thread
        }
    }

    private class GameThread extends Thread {

        private final SurfaceHolder surfaceHolder; // for manipulating canvas
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
            double totalElapsedTime = 0; // elapsed seconds before we create food;
            while (threadIsRunning) {
                try {
                    // get Canvas for exclusive drawing from this thread
                    canvas = surfaceHolder.lockCanvas();

                    // lock the surfaceHolder for drawing
                    synchronized (surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        totalElapsedTime += elapsedTimeMS / 1000.0;
                        if (totalElapsedTime > 5) {
                            createFood();
                            totalElapsedTime = 0;
                        }
                        updatePositions(elapsedTimeMS); // update game state
                        testForCollisions(); // test for GameElement collisions
                        drawGameElements(canvas); // draw using the canvas
                        previousFrameTime = currentTime; // update previous time
                    }
                } finally {
                    // display canvas's contents on the CannonView
                    // and enable other threads to use the Canvas
                    if (canvas != null)
                        surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

    }

    private void testForCollisions() {
        boolean isGameUp = snake.testCollisionWithItself() || snake.testCollisionWithScreen();
        gameThread.setRunning(!isGameUp);

        if (isGameUp) {
            showGameOverDialog(R.string.lose);
            restartNewGame = true;
        } else if (testIfCollideWithFood()) {
            snake.increaseSnakeLength();
        }
    }

    private boolean testIfCollideWithFood() {
        int headX = snake.getHead().getFrom().x;
        int headY = snake.getHead().getFrom().y;

        for (Food food: foodList) {
            if (food.isEaten(headX, headY)) {
                foodList.remove(food);
                score += 10;
                return true;
            }
        }
        return false;
    }

    private void updatePositions(double elapsedTimeMS) {
        double interval = elapsedTimeMS / 1000.0; // convert to seconds
        snake.updateSnakePosition(interval);
    }

    // draws the game to the given Canvas
    public void drawGameElements(Canvas canvas) {
        // clear the background
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

        snake.draw(canvas); // draw the cannon

        // draw all of the Food
        for (Food food : foodList) {
            food.draw(canvas);
        }

        canvas.drawText(getResources().getString(R.string.score, score), 50, 100, textPaint);
    }

    // called when the user touches the screen in this activity
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get int representing the type of action which caused this event
        int action = event.getAction();

        // the user touched the screen or dragged along the screen
        if (action == MotionEvent.ACTION_DOWN) {
            // move the snake in apt direction
            snake.addNewHead(event);
        }
        return true;
    }

    // display an AlertDialog when the game ends
    private void showGameOverDialog(final int messageId) {
        @SuppressLint("ValidFragment")
        // DialogFragment to display game stats and start new game
        final DialogFragment gameResult =
            new DialogFragment() {
                // create an AlertDialog and return it
                @Override
                public Dialog onCreateDialog(Bundle bundle) {
                    // create dialog displaying String resource for messageId
                    AlertDialog.Builder builder =
                        new AlertDialog.Builder(getActivity());
                    builder.setTitle(getResources().getString(messageId));

                    // display number of shots fired and total time elapsed
                    builder.setPositiveButton(R.string.reset_game,
                        new DialogInterface.OnClickListener() {
                            // called when "Reset Game" Button is pressed
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialogIsDisplayed = false;
                                newGame(); // set up and start a new game
                            }
                        }
                    );

                    return builder.create(); // return the AlertDialog
                }
            };

        // in GUI thread, use FragmentManager to display the DialogFragment
        activity.runOnUiThread(
            new Runnable() {
                public void run() {
//                        showSystemBars();
                    dialogIsDisplayed = true;
                    gameResult.setCancelable(false); // modal dialog
                    gameResult.show(activity.getFragmentManager(), "results");
                }
            }
        );
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

    private void createFood() {
        int radius = (int) (screenWidth * TARGET_WIDTH_PERCENT);
        int maxSpaceAvailableForFoodX = screenWidth - 2 * radius;
        int maxSpaceAvailableForFoodY = screenHeight - 2 * radius;

        int fromX = (int) (Math.random() * (maxSpaceAvailableForFoodX - 2 * radius) + radius);
        int fromY = (int) (Math.random() * (maxSpaceAvailableForFoodY - 2 * radius) + radius);

        if (foodList.size() < 5) {
            foodList.add(new Food(fromX, fromY, radius));
        }
    }
}
