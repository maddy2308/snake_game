package com.map524s1a.snakegame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

import java.util.LinkedList;
import java.util.List;

import static com.map524s1a.snakegame.SnakePart.Orientation.HORIZONTAL;
import static com.map524s1a.snakegame.SnakePart.Orientation.VERTICAL;

/**
 * Created by maddy on 4/13/19.
 */

class Snake {

    private Paint paint; // Paint used to draw the Snake
    private GameView gameView;
    private int snakeWidth, snakeLength, screenWidth, screenHeight;
    private int velocity;

    private Snake(int snakeWidth, int snakeLength, double velocity, int color, GameView gameView) {
        screenWidth = gameView.getScreenWidth();
        screenHeight = gameView.getScreenHeight();

        this.snakeLength = snakeLength;
        this.snakeWidth = snakeWidth;

//        Point from = new Point(screenWidth / 2, screenHeight / 2);
//        Point to = new Point(screenWidth / 2 - snakeLength, screenHeight / 2);

        SnakePart firstSnakePart = createNewSnakePart(screenWidth / 2, screenHeight / 2, HORIZONTAL, 1);

        this.snakeBody = new LinkedList<>();
        this.snakeBody.add(firstSnakePart);
        this.velocity = (int) velocity;
        this.gameView = gameView;
        paint = new Paint();
        paint.setStrokeWidth(snakeWidth - 10);
        paint.setColor(color);

    }

    private List<SnakePart> snakeBody;

    public SnakePart getHead() {
        return snakeBody.get(0);
    }

    public SnakePart getTail() {
        return snakeBody.get(snakeBody.size() - 1);
    }

    // implement singleton
    static Snake createSnake(int snakeWidth, int snakeLength, double velocity, int color, GameView gameView) {
        return new Snake(snakeWidth, snakeLength, velocity, color, gameView);
    }

    void updateSnakePosition(double interval) {
        int distance = (int) (interval * velocity);
        Log.d("update_position", String.valueOf(distance));

        if (snakeBody.size() == 1) {
            SnakePart snakePart = getHead();
            distance *= snakePart.direction;
            switch (snakePart.getOrientation()) {
                case HORIZONTAL:
                    snakePart.getFrom().x += distance;
                    snakePart.getTo().x += distance;
                    break;
                case VERTICAL:
                    snakePart.getFrom().y += distance;
                    snakePart.getTo().y += distance;
                    break;
            }
        } else {
            updateSnakePositionHelper(getHead(), distance, true);
            updateSnakePositionHelper(getTail(), distance, false);
            // make new Tail if necessary
            removeTail();
        }

    }

    private void updateSnakePositionHelper(SnakePart snakePart, int distance, boolean isHead) {
        distance *= snakePart.direction;
        switch (snakePart.getOrientation()) {
            case HORIZONTAL:
                if (isHead) {
                    snakePart.getFrom().x += distance;
                } else {
                    snakePart.getTo().x += distance;
                }
                break;
            case VERTICAL:
                if (isHead) {
                    snakePart.getFrom().y += distance;
                } else {
                    snakePart.getTo().y += distance;
                }
                break;
        }
    }

    synchronized void draw(Canvas canvas) {
        for (SnakePart snakePart : snakeBody) {
            canvas.drawLine(snakePart.getFrom().x, snakePart.getFrom().y, snakePart.getTo().x, snakePart.getTo().y, paint);
        }
    }

    void addNewHead(MotionEvent event) {
        int touchPositionX = (int) event.getX();
        int touchPositionY = (int) event.getY();
        SnakePart head = snakeBody.get(0);

        int headPositionX = head.getFrom().x;
        int headPositionY = head.getFrom().y;

        switch (head.getOrientation()) {
            case VERTICAL:
                if (touchPositionX < headPositionX) {
                    snakeBody.add(0, createNewHead(
                        new Point(headPositionX, headPositionY),
                        new Point(headPositionX, headPositionY),
                        HORIZONTAL, -1));
                } else {
                    snakeBody.add(0, createNewHead(
                        new Point(headPositionX, headPositionY),
                        new Point(headPositionX, headPositionY),
                        HORIZONTAL, 1));
                }
                break;
            case HORIZONTAL:
                if (touchPositionY < headPositionY) {
                    snakeBody.add(0, createNewHead(
                        new Point(headPositionX, headPositionY),
                        new Point(headPositionX, headPositionY),
                        VERTICAL, -1));

                } else {
                    snakeBody.add(0, createNewHead(
                        new Point(headPositionX, headPositionY),
                        new Point(headPositionX, headPositionY),
                        VERTICAL, 1));
                }
                break;
        }
    }

    void increaseSnakeLength() {
        switch (getTail().getOrientation()) {
            case HORIZONTAL:
                snakeBody.add(createNewSnakePart(getTail().getTo().x, getTail().getTo().y,
                    SnakePart.Orientation.VERTICAL, getTail().direction * -1));
                break;
            case VERTICAL:
                snakeBody.add(createNewSnakePart(getTail().getTo().x, getTail().getTo().y,
                    SnakePart.Orientation.HORIZONTAL, getTail().direction * -1));
                break;
        }
    }

    private SnakePart createNewHead(Point from, Point to, SnakePart.Orientation orientation, int direction) {
        return new SnakePart(from, to, orientation, direction);
    }

    private SnakePart createNewSnakePart(int fromX, int fromY, SnakePart.Orientation orientation, int direction) {
        Point from = new Point(fromX, fromY);
        Point to = new Point(fromX - (snakeLength * direction), fromY);
        return new SnakePart(from, to, orientation, direction);
    }

    private void removeTail() {
        // make new Tail if necessary
        SnakePart tail = getTail();
        switch (tail.getOrientation()) {
            case VERTICAL:
                if (tail.direction == 1 && tail.getTo().y >= tail.getFrom().y) {
                    snakeBody.remove(snakeBody.size() - 1);
                } else if (tail.direction == -1 && tail.getTo().y <= tail.getFrom().y) {
                    snakeBody.remove(snakeBody.size() - 1);
                }
                break;
            case HORIZONTAL:
                if (tail.direction == 1 && tail.getTo().x >= tail.getFrom().x) {
                    snakeBody.remove(snakeBody.size() - 1);
                } else if (tail.direction == -1 && tail.getTo().x <= tail.getFrom().x) {
                    snakeBody.remove(snakeBody.size() - 1);
                }
                break;
        }
    }

    boolean testCollisionWithItself() {
        int x1 = getHead().getFrom().x;
        int y1 = getHead().getFrom().y;

        int x2 = getHead().getTo().x;
        int y2 = getHead().getTo().y;

        for (int i = 3; i < snakeBody.size(); i++) {
            int x3 = snakeBody.get(i).getFrom().x;
            int y3 = snakeBody.get(i).getFrom().y;

            int x4 = snakeBody.get(i).getTo().x;
            int y4 = snakeBody.get(i).getTo().y;

            if (intersects(x1, y1, x2, y2, x3, y3, x4, y4)) {
                return true;
            }
        }
        return false;
    }

    boolean testCollisionWithScreen() {
        int x1 = getHead().getFrom().x;
        int y1 = getHead().getFrom().y;

        int x2 = getHead().getTo().x;
        int y2 = getHead().getTo().y;

        return intersects(x1, y1, x2, y2, 0, 0, screenWidth, 0) ||
            intersects(x1, y1, x2, y2, screenWidth, 0, screenWidth, screenHeight) ||
            intersects(x1, y1, x2, y2, 0, screenHeight, screenWidth, screenHeight) ||
            intersects(x1, y1, x2, y2, 0, 0, 0, screenHeight);

    }

    // Taken from https://gist.github.com/coleww/9403691

    /**
     * @param x1 line 1 from x coordinate
     * @param y1 line 1 from y coordinate
     * @param x2 line 1 to x coordinate
     * @param y2 line 1 to y coordinate
     * @param x3 line 2 from x coordinate
     * @param y3 line 2 from y coordinate
     * @param x4 line 2 to x coordinate
     * @param y4 line 2 to y coordinate
     * @return true if intersects else false
     */
    private boolean intersects(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        float bx = x2 - x1;
        float by = y2 - y1;
        float dx = x4 - x3;
        float dy = y4 - y3;
        float b_dot_d_perp = bx * dy - by * dx;
        if (b_dot_d_perp == 0) {
            return false;
        }
        float cx = x3 - x1;
        float cy = y3 - y1;
        float t = (cx * dy - cy * dx) / b_dot_d_perp;
        if (t < 0 || t > 1) {
            return false;
        }
        float u = (cx * by - cy * bx) / b_dot_d_perp;
        if (u < 0 || u > 1) {
            return false;
        }
        return true;
    }
}
