package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import com.stellardrift.game.util.Constants;

public class Asteroid {

    private float x, y, speed, size, rotation, rotSpeed;
    private float[] shapeX, shapeY;
    private int vertices;

    private Path asteroidPath;
    private RectF boundsRect;

    private Paint bodyPaint, edgePaint, craterPaint, glowPaint, highlightPaint;

    private static final int[][] COLORS = {
        {0xFF546E7A, 0xFF37474F, 0xFF263238},
        {0xFF5D4037, 0xFF3E2723, 0xFF212121},
        {0xFF455A64, 0xFF1B5E20, 0xFF1A237E},
        {0xFF616161, 0xFF424242, 0xFF212121},
    };

    private int colorIdx;
    private float[][] craters;

    public Asteroid(int sw, int sh) {
        float range = Constants.ASTEROID_MAX_SIZE - Constants.ASTEROID_MIN_SIZE;
        size = sw * (Constants.ASTEROID_MIN_SIZE + (float)(Math.random() * range));

        x = (float)(Math.random() * (sw - size * 2) + size);
        y = -size * 2 - (float)(Math.random() * 300);

        float sRange = Constants.ASTEROID_MAX_SPEED - Constants.ASTEROID_MIN_SPEED;
        speed = (Constants.ASTEROID_MIN_SPEED +
            (float)(Math.random() * sRange)) * (sw / 1080f);

        rotation = 0;
        rotSpeed = (float)(Math.random() * 2.5 - 1.25);
        colorIdx = (int)(Math.random() * COLORS.length);

        vertices = 8 + (int)(Math.random() * 5);
        shapeX = new float[vertices];
        shapeY = new float[vertices];
        for (int i = 0; i < vertices; i++) {
            double angle = 2 * Math.PI * i / vertices;
            float r = size * (0.7f + (float)(Math.random() * 0.35));
            shapeX[i] = (float)(Math.cos(angle) * r);
            shapeY[i] = (float)(Math.sin(angle) * r);
        }

        int nc = 2 + (int)(Math.random() * 3);
        craters = new float[nc][3];
        for (int i = 0; i < nc; i++) {
            craters[i][0] = (float)(Math.random() * size * 0.6 - size * 0.3);
            craters[i][1] = (float)(Math.random() * size * 0.6 - size * 0.3);
            craters[i][2] = size * (0.08f + (float)(Math.random() * 0.15));
        }

        asteroidPath = new Path();
        boundsRect = new RectF();

        bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setStyle(Paint.Style.FILL);

        edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(1.5f);
        edgePaint.setColor(0xFF78909C);
        edgePaint.setAlpha(120);

        craterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        craterPaint.setStyle(Paint.Style.FILL);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setColor(0xFFFF1744);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setColor(0xFF90A4AE);
        highlightPaint.setAlpha(40);
    }

    public void update(float difficulty) {
        y += speed * difficulty;
        rotation += rotSpeed;
    }

    public boolean isOffScreen(int sh) {
        return y > sh + size * 3;
    }

    public void render(Canvas c) {
        c.save();
        c.translate(x, y);
        c.rotate(rotation);

        for (int i = 3; i >= 0; i--) {
            glowPaint.setAlpha(4 + i);
            c.drawCircle(0, 0, size * (1.1f + i * 0.2f), glowPaint);
        }

        asteroidPath.reset();
        asteroidPath.moveTo(shapeX[0], shapeY[0]);
        for (int i = 1; i < vertices; i++)
            asteroidPath.lineTo(shapeX[i], shapeY[i]);
        asteroidPath.close();

        int[] col = COLORS[colorIdx];
        bodyPaint.setShader(new LinearGradient(
            -size, -size, size, size,
            col[0], col[1], Shader.TileMode.CLAMP));
        c.drawPath(asteroidPath, bodyPaint);
        bodyPaint.setShader(null);

        c.drawPath(asteroidPath, edgePaint);

        c.drawCircle(-size * 0.2f, -size * 0.25f,
            size * 0.35f, highlightPaint);

        craterPaint.setColor(col[2]);
        for (float[] cr : craters) {
            craterPaint.setAlpha(140);
            c.drawCircle(cr[0], cr[1], cr[2], craterPaint);
            craterPaint.setAlpha(60);
            c.drawCircle(cr[0] + cr[2] * 0.15f,
                cr[1] + cr[2] * 0.15f, cr[2] * 0.65f, craterPaint);
        }

        c.restore();
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getSize() { return size; }

    public RectF getBounds() {
        float s = size * 0.65f;
        boundsRect.set(x - s, y - s, x + s, y + s);
        return boundsRect;
    }
}
