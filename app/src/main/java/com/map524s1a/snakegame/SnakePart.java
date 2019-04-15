package com.map524s1a.snakegame;

import android.graphics.Point;

public class SnakePart {

    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    private Point from, to;
    private Orientation orientation = Orientation.HORIZONTAL;

    public SnakePart(Point from, Point to, Orientation orientation) {
        this.from = from;
        this.to = to;
        this.orientation = orientation;
    }
}
