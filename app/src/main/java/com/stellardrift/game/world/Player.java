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

    private float x, y, size, prevX;
    private float bankAngle, targetBank;
    private int screenW, screenH;
    private float glowPulse, engineFlicker;
    private int comboTier, comboCount;
    private float comboProgress;

    private Path shipPath, wingL, wingR, flamePath, flameCore;
    private RectF cockpitRect, boundsRect, comboArcRect;

    private static final int TRAIL_LEN = 15;
    private float[] trailX, trailY;
    private int trailIdx;

    private static final int AFTERIMAGE_COUNT = 4;
    private float[] afterX = new float[AFTERIMAGE_COUNT];
    private float[] afterY = new float[AFTERIMAGE_COUNT];
    private float[] afterAngle = new float[AFTERIMAGE_COUNT];
    private int afterIndex = 0;
    private int afterFrameSkip = 0;

    private Paint shipPaint, outlinePaint, glowPaint;
    private Paint enginePaint, engineCorePaint, trailPaint;
    private Paint cockpitPaint, wingPaint, stripePaint, shieldPaint;
    private Paint comboArcPaint;

    private boolean shielded;
    private int shieldTimer;
    private float shieldPulse;
    private boolean overdrive;
    private int overdriveTimer;
    private float overdrivePulse;

    private static final int CYAN = Color.parseColor("#00E5FF");
    private static final int DEEP_BLUE = Color.parseColor("#1A237E");
    private static final int PURPLE = Color.parseColor("#7C4DFF");
    private static final int ORANGE = Color.parseColor("#FF6D00");
    private static final int YELLOW = Color.parseColor("#FFF9C4");

    public Player(int sw, int sh) {
        screenW = sw; screenH = sh;
        size = sw * Constants.PLAYER_SIZE_RATIO;
        x = sw / 2f; y = sh * Constants.PLAYER_START_Y_RATIO;
        prevX = x; bankAngle = 0; targetBank = 0; comboTier = 0;

        trailX = new float[TRAIL_LEN]; trailY = new float[TRAIL_LEN];
        for (int i = 0; i < TRAIL_LEN; i++) { trailX[i] = x; trailY[i] = y; }
        trailIdx = 0;

        for (int i = 0; i < AFTERIMAGE_COUNT; i++) { afterX[i] = x; afterY[i] = y; afterAngle[i] = 0; }

        shipPath = new Path(); wingL = new Path(); wingR = new Path();
        flamePath = new Path(); flameCore = new Path();
        cockpitRect = new RectF(); boundsRect = new RectF(); comboArcRect = new RectF();
        initPaints();
    }

    private void initPaints() {
        shipPaint = new Paint(Paint.ANTI_ALIAS_FLAG); shipPaint.setStyle(Paint.Style.FILL);
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG); outlinePaint.setColor(CYAN);
        outlinePaint.setStyle(Paint.Style.STROKE); outlinePaint.setStrokeWidth(1.5f); outlinePaint.setAlpha(180);
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG); glowPaint.setColor(CYAN); glowPaint.setStyle(Paint.Style.FILL);
        enginePaint = new Paint(Paint.ANTI_ALIAS_FLAG); enginePaint.setStyle(Paint.Style.FILL);
        engineCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG); engineCorePaint.setColor(YELLOW); engineCorePaint.setStyle(Paint.Style.FILL);
        trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG); trailPaint.setColor(CYAN); trailPaint.setStyle(Paint.Style.FILL);
        cockpitPaint = new Paint(Paint.ANTI_ALIAS_FLAG); cockpitPaint.setColor(Color.WHITE); cockpitPaint.setStyle(Paint.Style.FILL);
        wingPaint = new Paint(Paint.ANTI_ALIAS_FLAG); wingPaint.setColor(PURPLE); wingPaint.setStyle(Paint.Style.FILL); wingPaint.setAlpha(140);
        stripePaint = new Paint(Paint.ANTI_ALIAS_FLAG); stripePaint.setColor(CYAN); stripePaint.setStrokeCap(Paint.Cap.ROUND); stripePaint.setStyle(Paint.Style.STROKE);
        shieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG); shieldPaint.setColor(CYAN); shieldPaint.setStyle(Paint.Style.STROKE); shieldPaint.setStrokeWidth(2.5f);
        comboArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG); comboArcPaint.setStyle(Paint.Style.STROKE);
        comboArcPaint.setStrokeWidth(size * 0.15f); comboArcPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setComboInfo(int count, float progress) {
        this.comboCount = count; this.comboProgress = progress;
        if (count >= 10) comboTier = 4; else if (count >= 6) comboTier = 3;
        else if (count >= 3) comboTier = 2; else if (count >= 1) comboTier = 1; else comboTier = 0;
    }

    private int getTrailColor() {
        if (overdrive) return 0xFFFF6D00;
        return Constants.COMBO_TRAIL_COLORS[Math.min(comboTier, Constants.COMBO_TRAIL_COLORS.length - 1)];
    }

    public void update(float dirX, float dirY, float magnitude) {
        prevX = x;
        if (magnitude > Constants.JOY_DEAD_ZONE) {
            float adjMag = (magnitude - Constants.JOY_DEAD_ZONE) / (1f - Constants.JOY_DEAD_ZONE);
            float speed = screenW * Constants.PLAYER_MOVE_SPEED * adjMag;
            x += dirX * speed; y += dirY * speed;
        }
        x = Math.max(size, Math.min(screenW - size, x));
        y = Math.max(screenH * Constants.PLAYER_Y_MIN_RATIO, Math.min(screenH * Constants.PLAYER_Y_MAX_RATIO, y));

        float dx = x - prevX;
        targetBank = Math.max(-Constants.PLAYER_MAX_BANK_ANGLE, Math.min(Constants.PLAYER_MAX_BANK_ANGLE, dx * 2.5f));
        bankAngle += (targetBank - bankAngle) * Constants.PLAYER_BANK_SPEED;

        trailIdx = (trailIdx + 1) % TRAIL_LEN; trailX[trailIdx] = x; trailY[trailIdx] = y;

        afterFrameSkip++;
        if (afterFrameSkip % 2 == 0) {
            afterX[afterIndex] = x; afterY[afterIndex] = y; afterAngle[afterIndex] = bankAngle;
            afterIndex = (afterIndex + 1) % AFTERIMAGE_COUNT;
        }

        glowPulse += 0.06f; engineFlicker = 0.7f + (float)(Math.random() * 0.3);
        if (shielded) { shieldTimer--; shieldPulse += 0.15f; if (shieldTimer <= 0) shielded = false; }
        if (overdrive) { overdriveTimer--; overdrivePulse += 0.12f; if (overdriveTimer <= 0) overdrive = false; }
    }

    public void render(Canvas c) {
        renderTrail(c); renderGlow(c); drawAfterimages(c);
        c.save(); c.rotate(bankAngle, x, y);
        renderEngine(c, 255); buildShip(); renderShip(c, 255); renderDetails(c, 255);
        c.restore();
        if (comboCount > 1) drawComboArc(c);
        if (overdrive) renderOverdrive(c);
        if (shielded) renderShield(c);
    }

    private void drawAfterimages(Canvas c) {
        for (int i = 0; i < AFTERIMAGE_COUNT; i++) {
            int idx = (afterIndex + i) % AFTERIMAGE_COUNT;
            float age = (float)(AFTERIMAGE_COUNT - i) / AFTERIMAGE_COUNT;
            int alpha = (int)(60 * (1f - age));
            if (alpha < 5) continue;
            float scale = 1f - age * 0.2f;
            c.save();
            c.translate(afterX[idx] - x, afterY[idx] - y);
            c.translate(x, y); c.rotate(afterAngle[idx]); c.scale(scale, scale); c.translate(-x, -y);
            buildShipAt(afterX[idx], afterY[idx]); renderShipAt(c, afterX[idx], afterY[idx], alpha);
            c.restore();
        }
        buildShip();
    }

    private void drawComboArc(Canvas c) {
        float sweepAngle = comboProgress * 360f;
        int arcColor;
        if (comboProgress > 0.5f) arcColor = Color.rgb(100, 255, 100);
        else if (comboProgress > 0.3f) arcColor = Color.rgb(255, 255, 80);
        else {
            float pulse = (float)(0.6 + 0.4 * Math.sin(System.currentTimeMillis() * 0.015));
            arcColor = Color.rgb(255, (int)(60 * pulse), (int)(60 * pulse));
        }
        comboArcPaint.setColor(arcColor);
        float r = size * 1.8f;
        comboArcRect.set(x - r, y - r, x + r, y + r);
        c.drawArc(comboArcRect, -90, sweepAngle, false, comboArcPaint);
    }

    private void renderTrail(Canvas c) {
        int trailColor = getTrailColor();
        float baseSize = size * 0.2f + comboTier * size * 0.03f + (overdrive ? size * 0.1f : 0);
        int baseAlpha = overdrive ? 55 : 25 + comboTier * 6;
        for (int i = 0; i < TRAIL_LEN; i++) {
            int idx = (trailIdx - i + TRAIL_LEN) % TRAIL_LEN;
            float a = 1f - (i / (float) TRAIL_LEN);
            trailPaint.setColor(trailColor); trailPaint.setAlpha((int)(baseAlpha * a));
            c.drawCircle(trailX[idx], trailY[idx] + size * 0.5f, baseSize * a, trailPaint);
        }
    }

    private void renderGlow(Canvas c) {
        float p = (float)(Math.sin(glowPulse) * 0.15 + 0.85);
        int gc = overdrive ? 0xFFFF6D00 : CYAN;
        glowPaint.setColor(gc);
        for (int i = 6; i >= 0; i--) {
            float f = (float) i / 6;
            glowPaint.setAlpha((int)((overdrive ? 15 : 10) * (1f - f)));
            c.drawCircle(x, y, size * (1.3f + f * 1.8f) * p, glowPaint);
        }
    }

    private void renderEngine(Canvas c, int alphaMult) {
        float s = size, len = s * (0.7f + engineFlicker * 0.4f);
        if (overdrive) len *= 1.4f;
        drawFlame(c, x - s*0.18f, y + s*0.4f, s*0.09f, len*0.75f, alphaMult);
        drawFlame(c, x + s*0.18f, y + s*0.4f, s*0.09f, len*0.75f, alphaMult);
        drawFlame(c, x, y + s*0.35f, s*0.13f, len, alphaMult);
    }

    private void drawFlame(Canvas c, float fx, float fy, float w, float l, int alphaMult) {
        flamePath.reset(); flamePath.moveTo(fx-w, fy); flamePath.lineTo(fx, fy+l); flamePath.lineTo(fx+w, fy); flamePath.close();
        enginePaint.setColor(ORANGE); enginePaint.setAlpha((int)(200 * engineFlicker * alphaMult / 255f)); c.drawPath(flamePath, enginePaint);
        flameCore.reset(); flameCore.moveTo(fx-w*0.45f, fy); flameCore.lineTo(fx, fy+l*0.55f); flameCore.lineTo(fx+w*0.45f, fy); flameCore.close();
        engineCorePaint.setAlpha((int)(240 * engineFlicker * alphaMult / 255f)); c.drawPath(flameCore, engineCorePaint);
    }

    private void buildShip() { buildShipAt(x, y); }
    private void buildShipAt(float cx, float cy) {
        shipPath.reset(); float s = size;
        shipPath.moveTo(cx, cy-s*1.3f); shipPath.lineTo(cx+s*0.15f, cy-s*0.65f);
        shipPath.lineTo(cx+s*0.35f, cy-s*0.25f); shipPath.lineTo(cx+s*0.85f, cy+s*0.45f);
        shipPath.lineTo(cx+s*0.45f, cy+s*0.28f); shipPath.lineTo(cx+s*0.25f, cy+s*0.42f);
        shipPath.lineTo(cx, cy+s*0.35f); shipPath.lineTo(cx-s*0.25f, cy+s*0.42f);
        shipPath.lineTo(cx-s*0.45f, cy+s*0.28f); shipPath.lineTo(cx-s*0.85f, cy+s*0.45f);
        shipPath.lineTo(cx-s*0.35f, cy-s*0.25f); shipPath.lineTo(cx-s*0.15f, cy-s*0.65f);
        shipPath.close();
    }

    private void renderShip(Canvas c, int alpha) { renderShipAt(c, x, y, alpha); }
    private void renderShipAt(Canvas c, float cx, float cy, int alpha) {
        int tc = overdrive ? 0xFF4A148C : DEEP_BLUE; int bc = overdrive ? 0xFFFF6D00 : PURPLE;
        shipPaint.setShader(new LinearGradient(cx, cy-size*1.3f, cx, cy+size*0.5f, tc, bc, Shader.TileMode.CLAMP));
        shipPaint.setAlpha(alpha); c.drawPath(shipPath, shipPaint); shipPaint.setShader(null);
        int oc = overdrive ? 0xFFFF6D00 : CYAN; outlinePaint.setColor(oc); outlinePaint.setAlpha((int)(180 * alpha / 255f));
        c.drawPath(shipPath, outlinePaint);
    }

    private void renderDetails(Canvas c, int alpha) {
        float s = size; stripePaint.setAlpha((int)(120 * alpha / 255f)); stripePaint.setStrokeWidth(s*0.05f);
        c.drawLine(x, y-s*0.9f, x, y+s*0.15f, stripePaint);
        cockpitPaint.setAlpha((int)(210 * alpha / 255f)); cockpitRect.set(x-s*0.07f, y-s*0.75f, x+s*0.07f, y-s*0.3f); c.drawOval(cockpitRect, cockpitPaint);
        wingL.reset(); wingL.moveTo(x-s*0.32f, y-s*0.05f); wingL.lineTo(x-s*0.7f, y+s*0.35f); wingL.lineTo(x-s*0.38f, y+s*0.18f); wingL.close();
        wingPaint.setAlpha((int)(140 * alpha / 255f)); c.drawPath(wingL, wingPaint);
        wingR.reset(); wingR.moveTo(x+s*0.32f, y-s*0.05f); wingR.lineTo(x+s*0.7f, y+s*0.35f); wingR.lineTo(x+s*0.38f, y+s*0.18f); wingR.close();
        c.drawPath(wingR, wingPaint);
    }

    private void renderOverdrive(Canvas c) {
        float p = (float)(Math.sin(overdrivePulse) * 0.15 + 0.85); float r = size * 1.6f * p;
        for (int i = 3; i >= 0; i--) { glowPaint.setColor(0xFFFF6D00); glowPaint.setAlpha(30-i*7); c.drawCircle(x, y, r+i*4, glowPaint); }
        glowPaint.setColor(CYAN);
    }

    private void renderShield(Canvas c) {
        float p = (float)(Math.sin(shieldPulse) * 0.1 + 0.9); float r = size * 1.5f * p;
        for (int i = 3; i >= 0; i--) { shieldPaint.setAlpha(70-i*16); c.drawCircle(x, y, r+i*3, shieldPaint); }
    }

    public float getX() { return x; } public float getY() { return y; }
    public float getSize() { return size; } public boolean isShielded() { return shielded; }
    public boolean isOverdrive() { return overdrive; }
    public void activateShield(int d) { shielded = true; shieldTimer = d; shieldPulse = 0; }
    public void activateOverdrive() { overdrive = true; overdriveTimer = Constants.OVERDRIVE_DURATION; overdrivePulse = 0; }
    public RectF getBounds() { float s = size*0.4f; boundsRect.set(x-s, y-s*1.5f, x+s, y+s); return boundsRect; }
    public void reset() {
        x = screenW/2f; y = screenH * Constants.PLAYER_START_Y_RATIO; prevX = x;
        bankAngle = 0; targetBank = 0; comboTier = 0; comboCount = 0;
        shielded = false; shieldTimer = 0; overdrive = false; overdriveTimer = 0;
        for (int i = 0; i < TRAIL_LEN; i++) { trailX[i] = x; trailY[i] = y; }
        for (int i = 0; i < AFTERIMAGE_COUNT; i++) { afterX[i] = x; afterY[i] = y; afterAngle[i] = 0; }
    }
}
