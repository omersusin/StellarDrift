package com.stellardrift.game.engine;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.stellardrift.game.util.Constants;

public class Joystick {

    private float baseX, baseY;
    private float knobX, knobY;
    private float baseRadius, knobRadius;
    private boolean active;
    private float dirX, dirY;
    private float magnitude;

    private Paint baseFillPaint, baseBorderPaint;
    private Paint knobPaint, knobGlowPaint;
    private Paint linePaint, crossPaint;

    private static final int CYAN = 0xFF00E5FF;

    public Joystick(int screenW) {
        baseRadius = screenW * Constants.JOY_BASE_RATIO;
        knobRadius = screenW * Constants.JOY_KNOB_RATIO;
        active = false;
        dirX = 0; dirY = 0; magnitude = 0;

        baseFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        baseFillPaint.setStyle(Paint.Style.FILL);

        baseBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        baseBorderPaint.setStyle(Paint.Style.STROKE);
        baseBorderPaint.setStrokeWidth(2.5f);

        knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        knobPaint.setStyle(Paint.Style.FILL);

        knobGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        knobGlowPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        crossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crossPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void onTouchDown(float x, float y) {
        active = true;
        baseX = x; baseY = y;
        knobX = x; knobY = y;
        dirX = 0; dirY = 0;
        magnitude = 0;
    }

    public void onTouchMove(float x, float y) {
        if (!active) return;
        float dx = x - baseX;
        float dy = y - baseY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > baseRadius) {
            knobX = baseX + dx / dist * baseRadius;
            knobY = baseY + dy / dist * baseRadius;
        } else {
            knobX = x;
            knobY = y;
        }

        magnitude = Math.min(1f, dist / baseRadius);

        if (dist > 1f) {
            dirX = dx / dist;
            dirY = dy / dist;
        }
    }

    public void onTouchUp() {
        active = false;
        dirX = 0; dirY = 0;
        magnitude = 0;
    }

    public void render(Canvas c) {
        if (!active) return;

        // Base fill — dark translucent
        baseFillPaint.setColor(Color.argb(20, 0, 30, 40));
        c.drawCircle(baseX, baseY, baseRadius, baseFillPaint);

        // Base inner ring
        baseBorderPaint.setColor(Color.argb(40, 0, 229, 255));
        baseBorderPaint.setStrokeWidth(1.5f);
        c.drawCircle(baseX, baseY, baseRadius * 0.5f, baseBorderPaint);

        // Base outer ring
        baseBorderPaint.setColor(Color.argb(70, 0, 229, 255));
        baseBorderPaint.setStrokeWidth(2.5f);
        c.drawCircle(baseX, baseY, baseRadius, baseBorderPaint);

        // Cross hair
        crossPaint.setColor(Color.argb(20, 0, 229, 255));
        crossPaint.setStrokeWidth(1f);
        c.drawLine(baseX - baseRadius * 0.7f, baseY,
                   baseX + baseRadius * 0.7f, baseY, crossPaint);
        c.drawLine(baseX, baseY - baseRadius * 0.7f,
                   baseX, baseY + baseRadius * 0.7f, crossPaint);

        // Direction line
        if (magnitude > Constants.JOY_DEAD_ZONE) {
            linePaint.setColor(Color.argb(40, 0, 229, 255));
            linePaint.setStrokeWidth(3f);
            c.drawLine(baseX, baseY, knobX, knobY, linePaint);
        }

        // Knob glow
        for (int i = 4; i >= 0; i--) {
            float f = (float) i / 4;
            knobGlowPaint.setColor(Color.argb((int)(12 * (1f - f)), 0, 229, 255));
            c.drawCircle(knobX, knobY, knobRadius * (1.6f + f * 2.5f), knobGlowPaint);
        }

        // Knob body
        knobPaint.setColor(Color.argb(160, 0, 200, 230));
        c.drawCircle(knobX, knobY, knobRadius, knobPaint);

        // Knob bright core
        knobPaint.setColor(Color.argb(200, 100, 230, 255));
        c.drawCircle(knobX, knobY, knobRadius * 0.55f, knobPaint);

        // Knob center dot
        knobPaint.setColor(Color.argb(230, 220, 245, 255));
        c.drawCircle(knobX, knobY, knobRadius * 0.2f, knobPaint);

        // Direction arrows at base edge (4 directions)
        drawDirectionArrows(c);
    }

    private void drawDirectionArrows(Canvas c) {
        crossPaint.setColor(Color.argb(35, 0, 229, 255));
        crossPaint.setStrokeWidth(2f);
        float ar = baseRadius * 0.85f;
        float as = baseRadius * 0.08f;

        // Up arrow
        c.drawLine(baseX, baseY - ar, baseX - as, baseY - ar + as, crossPaint);
        c.drawLine(baseX, baseY - ar, baseX + as, baseY - ar + as, crossPaint);
        // Down arrow
        c.drawLine(baseX, baseY + ar, baseX - as, baseY + ar - as, crossPaint);
        c.drawLine(baseX, baseY + ar, baseX + as, baseY + ar - as, crossPaint);
        // Left arrow
        c.drawLine(baseX - ar, baseY, baseX - ar + as, baseY - as, crossPaint);
        c.drawLine(baseX - ar, baseY, baseX - ar + as, baseY + as, crossPaint);
        // Right arrow
        c.drawLine(baseX + ar, baseY, baseX + ar - as, baseY - as, crossPaint);
        c.drawLine(baseX + ar, baseY, baseX + ar - as, baseY + as, crossPaint);
    }

    public boolean isActive() { return active; }
    public float getDirX() { return active ? dirX : 0; }
    public float getDirY() { return active ? dirY : 0; }
    public float getMagnitude() { return active ? magnitude : 0; }
}
