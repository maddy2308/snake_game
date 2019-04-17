package com.map524s1a.snakegame;

import android.graphics.Point;

public class SnakePart {

    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    int direction = 1;

    private Point from, to;
    private Orientation orientation = Orientation.HORIZONTAL;

    public SnakePart(Point from, Point to, Orientation orientation, int direction) {
        this.from = from;
        this.to = to;
        this.orientation = orientation;
        this.direction = direction;
    }

    public Point getFrom() {
        return from;
    }

    public void setFrom(Point from) {
        this.from = from;
    }

    public Point getTo() {
        return to;
    }

    public void setTo(Point to) {
        this.to = to;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }
}
