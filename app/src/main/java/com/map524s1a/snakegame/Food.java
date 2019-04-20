package com.map524s1a.snakegame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Calendar;

/**
 * Created by maddy on 4/13/19.
 */

class Food {

    private Calendar availableFor = Calendar.getInstance();
    private int centerX, centerY, radius;
    private Paint paint = new Paint();

    Food(int fromX, int fromY, int radius) {
        this.centerX = fromX + radius;
        this.centerY = fromY + radius;
        this.radius = radius;
        availableFor.add(Calendar.SECOND, 10);
        paint.setColor(Color.YELLOW);
    }

    void draw(Canvas canvas) {
        Calendar now = Calendar.getInstance();
        if (this.availableFor.getTimeInMillis() > now.getTimeInMillis()) {
//            float timeLeft = (this.availableFor.getTimeInMillis() - now.getTimeInMillis())/1000;
//            paint.setAlpha((int) (255 * (1/timeLeft)));
            canvas.drawCircle(centerX, centerY, radius, paint);
        }
    }

    boolean isEaten(int x, int y) {
        double distanceFromCenter = Math.sqrt(Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2));
        return !(distanceFromCenter > radius);
    }
}
