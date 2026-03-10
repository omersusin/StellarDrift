package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import com.stellardrift.game.util.Constants;

public class PowerUp {

    private float x, y, speed, size;
    private int type;
    private float pulse, rotation;

    private Paint glowPaint, bodyPaint, iconPaint, ringPaint;
    private RectF boundsRect;

    private static final int[] COLORS = {
        0xFFFFD740,  // MAGNET - gold
        0xFF448AFF,  // SLOWMO - blue
        0xFF00E676,  // DOUBLE - green
        0xFF00E5FF   // SHIELD - cyan
    };
    private static final String[] ICONS = {"M", "S", "2", "+"};

    public PowerUp(int sw, int sh, int type) {
        this.type = type;
        x = (float)(Math.random() * (sw * 0.7) + sw * 0.15);
        y = -(float)(Math.random() * 100 + 50);
        speed = 3f * (sw / 1080f);
        size = sw * 0.03f;
        pulse = (float)(Math.random() * Math.PI * 2);
        rotation = 0;

        int col = COLORS[type];

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(col); glowPaint.setStyle(Paint.Style.FILL);

        bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setColor(col); bodyPaint.setStyle(Paint.Style.FILL);

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setColor(col); ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(2f);

        iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconPaint.setColor(0xFF000000); iconPaint.setTextAlign(Paint.Align.CENTER);
        iconPaint.setTextSize(size * 1.1f);
        iconPaint.setTypeface(Typeface.DEFAULT_BOLD);

        boundsRect = new RectF();
    }

    public void update(float difficulty) {
        y += speed * difficulty;
        pulse += 0.1f;
        rotation += 2f;
    }

    public boolean isOffScreen(int sh) {
        return y > sh + size * 4;
    }

    public void render(Canvas c) {
        float p = (float)(Math.sin(pulse) * 0.2 + 0.8);
        float ds = size * p;

        // Outer glow
        for (int i = 4; i >= 0; i--) {
            float f = (float) i / 4;
            glowPaint.setAlpha((int)(18 * (1f - f)));
            c.drawCircle(x, y, ds * (2f + f * 3f), glowPaint);
        }

        // Rotating ring
        c.save();
        c.rotate(rotation, x, y);
        ringPaint.setAlpha((int)(120 * p));
        float rr = ds * 2.2f;
        c.drawCircle(x, y, rr, ringPaint);
        // Corner ticks
        for (int i = 0; i < 4; i++) {
            float angle = (float)(i * Math.PI / 2);
            float tx = x + (float)(Math.cos(angle) * rr);
            float ty = y + (float)(Math.sin(angle) * rr);
            ringPaint.setAlpha(200);
            c.drawCircle(tx, ty, size * 0.15f, bodyPaint);
        }
        c.restore();

        // Body circle
        bodyPaint.setAlpha(230);
        c.drawCircle(x, y, ds * 1.3f, bodyPaint);

        // Inner bright core
        bodyPaint.setAlpha(255);
        c.drawCircle(x, y, ds, bodyPaint);

        // Icon letter
        iconPaint.setAlpha(255);
        float textY = y + size * 0.38f;
        c.drawText(ICONS[type], x, textY, iconPaint);
    }

    public int getType() { return type; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getSize() { return size; }

    public RectF getBounds() {
        float s = size * 1.5f;
        boundsRect.set(x - s, y - s, x + s, y + s);
        return boundsRect;
    }

    public static int getColor(int type) {
        return (type >= 0 && type < COLORS.length) ? COLORS[type] : 0xFFFFFFFF;
    }

    public static String getName(int type) {
        switch (type) {
            case Constants.POWERUP_MAGNET: return "MAGNET";
            case Constants.POWERUP_SLOWMO: return "SLOW-MO";
            case Constants.POWERUP_DOUBLE: return "DOUBLE";
            case Constants.POWERUP_SHIELD: return "SHIELD";
            default: return "";
        }
    }
}
