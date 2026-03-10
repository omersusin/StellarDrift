package com.stellardrift.game.world;

import android.graphics.Canvas;
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

    // Fade-in
    private int age;
    private float masterAlpha;

    // Sinüs hareketi
    private boolean hasSine;
    private float sinePhase, sineAmp, sineFreq;
    private float baseX;

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
        this(sw, sh, -1);
    }

    public Asteroid(int sw, int sh, float playerX) {
        float range = Constants.ASTEROID_MAX_SIZE - Constants.ASTEROID_MIN_SIZE;
        size = sw * (Constants.ASTEROID_MIN_SIZE + (float)(Math.random() * range));

        // Spawn safety
        if (playerX >= 0) {
            float safeZone = sw * Constants.ASTEROID_SAFE_ZONE;
            float tryX;
            int attempts = 0;
            do {
                tryX = (float)(Math.random() * (sw - size * 2) + size);
                attempts++;
            } while (Math.abs(tryX - playerX) < safeZone && attempts < 10);
            x = tryX;
        } else {
            x = (float)(Math.random() * (sw - size * 2) + size);
        }

        baseX = x;
        y = -size * 2 - (float)(Math.random() * 300);

        float sRange = Constants.ASTEROID_MAX_SPEED - Constants.ASTEROID_MIN_SPEED;
        speed = (Constants.ASTEROID_MIN_SPEED +
            (float)(Math.random() * sRange)) * (sw / 1080f);

        rotation = 0;
        rotSpeed = (float)(Math.random() * 2.5 - 1.25);
        colorIdx = (int)(Math.random() * COLORS.length);

        // Fade-in
        age = 0;
        masterAlpha = 0;

        // Sinüs hareketi
        hasSine = Math.random() < Constants.ASTEROID_SINE_CHANCE;
        sinePhase = (float)(Math.random() * Math.PI * 2);
        sineAmp = sw * Constants.ASTEROID_SINE_AMP * (0.5f + (float)(Math.random() * 0.5));
        sineFreq = Constants.ASTEROID_SINE_FREQ * (0.7f + (float)(Math.random() * 0.6));

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
        craterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        craterPaint.setStyle(Paint.Style.FILL);
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setColor(0xFFFF1744);
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setColor(0xFF90A4AE);
    }

    public void update(float difficulty) {
        y += speed * difficulty;
        rotation += rotSpeed;
        age++;

        // Fade-in
        if (age < Constants.ASTEROID_FADEIN_FRAMES) {
            masterAlpha = (float) age / Constants.ASTEROID_FADEIN_FRAMES;
        } else {
            masterAlpha = 1f;
        }

        // Sinüs hareketi
        if (hasSine) {
            sinePhase += sineFreq;
            x = baseX + (float)(Math.sin(sinePhase) * sineAmp);
        }
    }

    public boolean isOffScreen(int sh) {
        return y > sh + size * 3;
    }

    public void render(Canvas c) {
        int ma = (int)(255 * masterAlpha);
        if (ma < 5) return;

        c.save();
        c.translate(x, y);
        c.rotate(rotation);

        // Danger glow
        for (int i = 3; i >= 0; i--) {
            int a = Math.min(255, (4 + i) * ma / 255);
            glowPaint.setAlpha(a);
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
        bodyPaint.setAlpha(ma);
        c.drawPath(asteroidPath, bodyPaint);
        bodyPaint.setShader(null);

        edgePaint.setAlpha(Math.min(120, 120 * ma / 255));
        c.drawPath(asteroidPath, edgePaint);

        highlightPaint.setAlpha(Math.min(40, 40 * ma / 255));
        c.drawCircle(-size * 0.2f, -size * 0.25f,
            size * 0.35f, highlightPaint);

        craterPaint.setColor(col[2]);
        for (float[] cr : craters) {
            craterPaint.setAlpha(Math.min(140, 140 * ma / 255));
            c.drawCircle(cr[0], cr[1], cr[2], craterPaint);
            craterPaint.setAlpha(Math.min(60, 60 * ma / 255));
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
