package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.stellardrift.game.util.Constants;

public class StarDust {
    private float x, y, speed, size;
    private float pulsePhase, rotation, rotSpeed, pullVX, pullVY;
    private Paint glowPaint, corePaint, centerPaint, rayPaint;
    private RectF boundsRect;
    private static final int GOLD = Color.parseColor("#FFD740");
    private static final int GOLD_LIGHT = Color.parseColor("#FFECB3");

    public StarDust(int sw, int sh) {
        x = (float)(Math.random() * sw); y = -(float)(Math.random() * 200 + 50);
        speed = Constants.STARDUST_SPEED * (sw / 1080f); size = sw * Constants.STARDUST_SIZE_RATIO;
        pulsePhase = (float)(Math.random() * Math.PI * 2); rotation = 0;
        rotSpeed = (float)(Math.random() * 3 - 1.5); pullVX = 0; pullVY = 0;
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG); glowPaint.setColor(GOLD); glowPaint.setStyle(Paint.Style.FILL);
        corePaint = new Paint(Paint.ANTI_ALIAS_FLAG); corePaint.setColor(GOLD); corePaint.setStyle(Paint.Style.FILL);
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG); centerPaint.setStyle(Paint.Style.FILL);
        rayPaint = new Paint(Paint.ANTI_ALIAS_FLAG); rayPaint.setColor(GOLD); rayPaint.setStrokeCap(Paint.Cap.ROUND);
        boundsRect = new RectF();
    }

    public void setPosition(float nx, float ny) { x = nx; y = ny; }

    public void update(float difficulty) {
        y += speed * difficulty + pullVY; x += pullVX;
        pullVX *= 0.85f; pullVY *= 0.85f;
        pulsePhase += 0.08f; rotation += rotSpeed;
    }

    public void pull(float vx, float vy) { pullVX += vx; pullVY += vy; }
    public boolean isOffScreen(int sh) { return y > sh + size * 3; }

    public void render(Canvas c) {
        float pulse = (float)(Math.sin(pulsePhase) * 0.25 + 0.75); float ds = size * pulse;
        for (int i = 4; i >= 0; i--) { float f = (float) i / 4; glowPaint.setAlpha((int)(22 * (1f - f))); c.drawCircle(x, y, ds * (1.5f + f * 3f), glowPaint); }
        c.save(); c.rotate(rotation, x, y);
        rayPaint.setAlpha(100); rayPaint.setStrokeWidth(size * 0.12f);
        for (int i = 0; i < 4; i++) { float angle = (float)(i * Math.PI / 2); float rx = (float)(Math.cos(angle) * ds * 2.2); float ry = (float)(Math.sin(angle) * ds * 2.2); c.drawLine(x, y, x + rx, y + ry, rayPaint); }
        c.restore();
        corePaint.setAlpha(255); c.drawCircle(x, y, ds, corePaint);
        centerPaint.setColor(GOLD_LIGHT); centerPaint.setAlpha(220); c.drawCircle(x, y, ds * 0.5f, centerPaint);
        centerPaint.setColor(Color.WHITE); centerPaint.setAlpha(200); c.drawCircle(x, y, ds * 0.22f, centerPaint);
    }

    public float getX() { return x; } public float getY() { return y; }
    public float getSize() { return size; }
    public RectF getBounds() { boundsRect.set(x - size, y - size, x + size, y + size); return boundsRect; }
}
