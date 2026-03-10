package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import com.stellardrift.game.util.Constants;

public class Player {

    private float x, y, size;
    private int screenW, screenH;
    private float glowPulse, engineFlicker;

    private Path shipPath, wingL, wingR, flamePath, flameCore;
    private RectF cockpitRect, boundsRect;

    private static final int TRAIL_LEN = 15;
    private float[] trailX, trailY;
    private int trailIdx;

    private Paint shipPaint, outlinePaint, glowPaint;
    private Paint enginePaint, engineCorePaint, trailPaint;
    private Paint cockpitPaint, wingPaint, stripePaint, shieldPaint;

    private boolean shielded;
    private int shieldTimer;
    private float shieldPulse;

    private static final int CYAN = Color.parseColor("#00E5FF");
    private static final int DEEP_BLUE = Color.parseColor("#1A237E");
    private static final int PURPLE = Color.parseColor("#7C4DFF");
    private static final int ORANGE = Color.parseColor("#FF6D00");
    private static final int YELLOW = Color.parseColor("#FFF9C4");

    public Player(int sw, int sh) {
        screenW = sw; screenH = sh;
        size = sw * Constants.PLAYER_SIZE_RATIO;
        x = sw / 2f; y = sh * 0.82f;

        trailX = new float[TRAIL_LEN];
        trailY = new float[TRAIL_LEN];
        for (int i = 0; i < TRAIL_LEN; i++) { trailX[i] = x; trailY[i] = y; }
        trailIdx = 0;

        shipPath = new Path(); wingL = new Path(); wingR = new Path();
        flamePath = new Path(); flameCore = new Path();
        cockpitRect = new RectF(); boundsRect = new RectF();
        initPaints();
    }

    private void initPaints() {
        shipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shipPaint.setStyle(Paint.Style.FILL);
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(CYAN); outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(1.5f); outlinePaint.setAlpha(180);
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(CYAN); glowPaint.setStyle(Paint.Style.FILL);
        enginePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        enginePaint.setStyle(Paint.Style.FILL);
        engineCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        engineCorePaint.setColor(YELLOW); engineCorePaint.setStyle(Paint.Style.FILL);
        trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trailPaint.setColor(CYAN); trailPaint.setStyle(Paint.Style.FILL);
        cockpitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cockpitPaint.setColor(Color.WHITE); cockpitPaint.setStyle(Paint.Style.FILL);
        wingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wingPaint.setColor(PURPLE); wingPaint.setStyle(Paint.Style.FILL);
        wingPaint.setAlpha(140);
        stripePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stripePaint.setColor(CYAN); stripePaint.setStrokeCap(Paint.Cap.ROUND);
        stripePaint.setStyle(Paint.Style.STROKE);
        shieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shieldPaint.setColor(CYAN); shieldPaint.setStyle(Paint.Style.STROKE);
        shieldPaint.setStrokeWidth(2.5f);
    }

    public void update(float touchX, boolean touching) {
        // Parmağı doğrudan takip et — çok daha kolay kontrol
        if (touching && touchX >= 0) {
            float target = Math.max(size, Math.min(screenW - size, touchX));
            x += (target - x) * Constants.PLAYER_FOLLOW_SPEED;
        }

        trailIdx = (trailIdx + 1) % TRAIL_LEN;
        trailX[trailIdx] = x; trailY[trailIdx] = y;
        glowPulse += 0.06f;
        engineFlicker = 0.7f + (float)(Math.random() * 0.3);

        if (shielded) {
            shieldTimer--; shieldPulse += 0.15f;
            if (shieldTimer <= 0) shielded = false;
        }
    }

    public void render(Canvas c) {
        renderTrail(c); renderGlow(c); renderEngine(c);
        buildShip(); renderShip(c); renderDetails(c);
        if (shielded) renderShield(c);
    }

    private void renderTrail(Canvas c) {
        for (int i = 0; i < TRAIL_LEN; i++) {
            int idx = (trailIdx - i + TRAIL_LEN) % TRAIL_LEN;
            float a = 1f - (i / (float) TRAIL_LEN);
            trailPaint.setAlpha((int)(35 * a));
            c.drawCircle(trailX[idx], trailY[idx] + size * 0.5f, size * 0.2f * a, trailPaint);
        }
    }

    private void renderGlow(Canvas c) {
        float p = (float)(Math.sin(glowPulse) * 0.15 + 0.85);
        for (int i = 6; i >= 0; i--) {
            float f = (float) i / 6;
            glowPaint.setAlpha((int)(10 * (1f - f)));
            c.drawCircle(x, y, size * (1.3f + f * 1.8f) * p, glowPaint);
        }
    }

    private void renderEngine(Canvas c) {
        float s = size, len = s * (0.7f + engineFlicker * 0.4f);
        drawFlame(c, x - s*0.18f, y + s*0.4f, s*0.09f, len*0.75f);
        drawFlame(c, x + s*0.18f, y + s*0.4f, s*0.09f, len*0.75f);
        drawFlame(c, x, y + s*0.35f, s*0.13f, len);
    }

    private void drawFlame(Canvas c, float fx, float fy, float w, float l) {
        flamePath.reset();
        flamePath.moveTo(fx - w, fy); flamePath.lineTo(fx, fy + l);
        flamePath.lineTo(fx + w, fy); flamePath.close();
        enginePaint.setColor(ORANGE); enginePaint.setAlpha((int)(200 * engineFlicker));
        c.drawPath(flamePath, enginePaint);
        flameCore.reset();
        flameCore.moveTo(fx - w*0.45f, fy); flameCore.lineTo(fx, fy + l*0.55f);
        flameCore.lineTo(fx + w*0.45f, fy); flameCore.close();
        engineCorePaint.setAlpha((int)(240 * engineFlicker));
        c.drawPath(flameCore, engineCorePaint);
    }

    private void buildShip() {
        shipPath.reset(); float s = size;
        shipPath.moveTo(x, y - s*1.3f);
        shipPath.lineTo(x + s*0.15f, y - s*0.65f);
        shipPath.lineTo(x + s*0.35f, y - s*0.25f);
        shipPath.lineTo(x + s*0.85f, y + s*0.45f);
        shipPath.lineTo(x + s*0.45f, y + s*0.28f);
        shipPath.lineTo(x + s*0.25f, y + s*0.42f);
        shipPath.lineTo(x, y + s*0.35f);
        shipPath.lineTo(x - s*0.25f, y + s*0.42f);
        shipPath.lineTo(x - s*0.45f, y + s*0.28f);
        shipPath.lineTo(x - s*0.85f, y + s*0.45f);
        shipPath.lineTo(x - s*0.35f, y - s*0.25f);
        shipPath.lineTo(x - s*0.15f, y - s*0.65f);
        shipPath.close();
    }

    private void renderShip(Canvas c) {
        shipPaint.setShader(new LinearGradient(
            x, y - size*1.3f, x, y + size*0.5f,
            DEEP_BLUE, PURPLE, Shader.TileMode.CLAMP));
        c.drawPath(shipPath, shipPaint);
        shipPaint.setShader(null);
        c.drawPath(shipPath, outlinePaint);
    }

    private void renderDetails(Canvas c) {
        float s = size;
        stripePaint.setAlpha(120); stripePaint.setStrokeWidth(s * 0.05f);
        c.drawLine(x, y - s*0.9f, x, y + s*0.15f, stripePaint);
        cockpitPaint.setAlpha(210);
        cockpitRect.set(x - s*0.07f, y - s*0.75f, x + s*0.07f, y - s*0.3f);
        c.drawOval(cockpitRect, cockpitPaint);
        wingL.reset();
        wingL.moveTo(x - s*0.32f, y - s*0.05f);
        wingL.lineTo(x - s*0.7f, y + s*0.35f);
        wingL.lineTo(x - s*0.38f, y + s*0.18f); wingL.close();
        c.drawPath(wingL, wingPaint);
        wingR.reset();
        wingR.moveTo(x + s*0.32f, y - s*0.05f);
        wingR.lineTo(x + s*0.7f, y + s*0.35f);
        wingR.lineTo(x + s*0.38f, y + s*0.18f); wingR.close();
        c.drawPath(wingR, wingPaint);
    }

    private void renderShield(Canvas c) {
        float p = (float)(Math.sin(shieldPulse) * 0.1 + 0.9);
        float r = size * 1.5f * p;
        for (int i = 3; i >= 0; i--) {
            shieldPaint.setAlpha(70 - i * 16);
            c.drawCircle(x, y, r + i * 3, shieldPaint);
        }
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getSize() { return size; }
    public boolean isShielded() { return shielded; }

    public void activateShield(int dur) {
        shielded = true; shieldTimer = dur; shieldPulse = 0;
    }

    public RectF getBounds() {
        float s = size * 0.4f;
        boundsRect.set(x - s, y - s*1.5f, x + s, y + s);
        return boundsRect;
    }

    public void reset() {
        x = screenW / 2f;
        shielded = false; shieldTimer = 0;
        for (int i = 0; i < TRAIL_LEN; i++) { trailX[i] = x; trailY[i] = y; }
    }
}
