package com.map524s1a.snakegame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.Collections;
import java.util.List;

/**
 * Created by maddy on 4/13/19.
 */

class Snake {

    private Paint paint = new Paint(); // Paint used to draw the Snake
    private GameView gameView;
    private int screenWidth, screenHeight;
    private int velocity;

    private Snake(int snakeWidth, int snakeHeight, GameView gameView) {
        screenWidth = gameView.getScreenWidth();
        screenHeight = gameView.getScreenHeight();
        Point from = new Point(screenWidth/2, gameView.getScreenHeight()/2);
        Point to = new Point(screenWidth/2 - snakeWidth, screenHeight/2-snakeHeight);
        SnakePart firstSnakePart = new SnakePart(from, to, SnakePart.Orientation.HORIZONTAL);

        this.gameView = gameView;
        this.snakeBody = Collections.singletonList(firstSnakePart);
    }

    private List<SnakePart> snakeBody;

    public SnakePart getHead() {
        return snakeBody.get(0);
    }

    public SnakePart getTail() {
        return snakeBody.get(snakeBody.size() - 1);
    }

    public void moveSnake() {

    }

    // implement singleton
    static Snake createSnake(int snakeWidth, int snakeHeight, GameView gameView) {
        return new Snake(snakeWidth, snakeHeight, gameView);
    }

    public void draw(Canvas canvas) {
//        canvas.drawLine(0, view.getScreenHeight() / 2, barrelEnd.x,
//            barrelEnd.y, paint);
    }
}
