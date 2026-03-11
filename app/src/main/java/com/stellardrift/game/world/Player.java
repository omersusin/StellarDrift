package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.stellardrift.game.util.Constants;
import com.stellardrift.game.render.ShipRenderer;

public class Player {

    private float x, y, prevX;
    
    private float velX = 0f, velY = 0f;
    private static final float ACCELERATION = 18f;
    private static final float FRICTION = 12f;
    private static final float VELOCITY_DEADZONE = 2f;
    private static final float MIN_SPEED_FLOOR = 0.45f;

    private float bankAngle, targetBank;
    private int screenW, screenH;
    private float glowPulse;
    private int comboTier, comboCount;
    private float comboProgress;

    private float idleHoverTimer = 0f;
    private RectF boundsRect, comboArcRect;

    private static final int TRAIL_LEN = 15;
    private float[] trailX, trailY;
    private int trailIdx;

    private static final int AFTERIMAGE_COUNT = 4;
    private float[] afterX = new float[AFTERIMAGE_COUNT];
    private float[] afterY = new float[AFTERIMAGE_COUNT];
    private float[] afterAngle = new float[AFTERIMAGE_COUNT];
    private int afterIndex = 0;
    private int afterFrameSkip = 0;

    private Paint trailPaint, glowPaint, shieldPaint, comboArcPaint;

    private boolean shielded;
    private int shieldTimer;
    private float shieldPulse;
    private boolean overdrive;
    private int overdriveTimer;
    private float overdrivePulse;

    private float overchargeSpeedBoost = 1.0f;

    private ShipData currentShip;
    private ShipRenderer renderer;
    private float scaleMultiplier; 
    
    private FuelSystem fuelSystem;

    public Player(int sw, int sh, ShipRegistry registry, FuelSystem fuel) {
        screenW = sw; screenH = sh;
        x = sw / 2f; y = sh * Constants.PLAYER_START_Y_RATIO;
        prevX = x; bankAngle = 0; targetBank = 0; comboTier = 0;
        
        currentShip = registry.getSelectedShip();
        renderer = new ShipRenderer();
        fuelSystem = fuel;
        
        scaleMultiplier = (sw / 1080f) * 2.5f;

        trailX = new float[TRAIL_LEN]; trailY = new float[TRAIL_LEN];
        for (int i = 0; i < TRAIL_LEN; i++) { trailX[i] = x; trailY[i] = y; }
        for (int i = 0; i < AFTERIMAGE_COUNT; i++) { afterX[i] = x; afterY[i] = y; afterAngle[i] = 0; }

        boundsRect = new RectF(); comboArcRect = new RectF();
        initPaints();
    }

    private void initPaints() {
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG); glowPaint.setStyle(Paint.Style.FILL);
        trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG); trailPaint.setStyle(Paint.Style.FILL);
        shieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG); shieldPaint.setStyle(Paint.Style.STROKE); shieldPaint.setStrokeWidth(4.0f);
        comboArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG); comboArcPaint.setStyle(Paint.Style.STROKE); comboArcPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setShip(ShipData ship) { this.currentShip = ship; }
    public ShipData getShipData() { return currentShip; }

    public void setComboInfo(int count, float progress) {
        this.comboCount = count; this.comboProgress = progress;
        if (count >= 10) comboTier = 4; else if (count >= 6) comboTier = 3;
        else if (count >= 3) comboTier = 2; else if (count >= 1) comboTier = 1; else comboTier = 0;
    }

    public void setOverchargeSpeedBoost(float boost) {
        this.overchargeSpeedBoost = boost;
    }

    private int getTrailColor() {
        if (overdrive || overchargeSpeedBoost > 1.5f) return 0xFFFF6D00; 
        return Constants.COMBO_TRAIL_COLORS[Math.min(comboTier, Constants.COMBO_TRAIL_COLORS.length - 1)];
    }

    public void update(float joystickX, float joystickY, float magnitude, float dt) {
        prevX = x;
        
        float fuelSpeedMult = (fuelSystem != null) ? fuelSystem.getSpeedMultiplier() : 1.0f;
        float shipSpeedMult = (currentShip != null) ? currentShip.speedMultiplier : 1.0f;
        
        float rawMaxSpeed = (screenW * Constants.PLAYER_MOVE_SPEED * 60f) * shipSpeedMult * fuelSpeedMult * overchargeSpeedBoost;
        float maxSpeed = Math.max((screenW * Constants.PLAYER_MOVE_SPEED * 60f) * MIN_SPEED_FLOOR, rawMaxSpeed);

        float targetVelX = 0;
        float targetVelY = 0;

        boolean joystickActive = magnitude > Constants.JOY_DEAD_ZONE;

        if (joystickActive) {
            float adjMag = (magnitude - Constants.JOY_DEAD_ZONE) / (1f - Constants.JOY_DEAD_ZONE);
            targetVelX = joystickX * maxSpeed * adjMag;
            targetVelY = joystickY * maxSpeed * adjMag;
        } else {
            idleHoverTimer += dt;
            targetVelX = (float) Math.sin(idleHoverTimer * 1.1f) * 1.5f * 60f * fuelSpeedMult;
            targetVelY = (float) Math.sin(idleHoverTimer * 0.8f + 0.7f) * 2.0f * 60f * fuelSpeedMult;
        }

        if (joystickActive) {
            velX += (targetVelX - velX) * ACCELERATION * dt;
            velY += (targetVelY - velY) * ACCELERATION * dt;
        } else {
            velX -= velX * FRICTION * dt;
            velY -= velY * FRICTION * dt;
            
            velX += (targetVelX - velX) * (ACCELERATION * 0.5f) * dt;
            velY += (targetVelY - velY) * (ACCELERATION * 0.5f) * dt;

            if (Math.abs(velX) < VELOCITY_DEADZONE) velX = 0;
            if (Math.abs(velY) < VELOCITY_DEADZONE) velY = 0;
        }

        float currentVelMag = (float) Math.sqrt(velX * velX + velY * velY);
        if (currentVelMag > maxSpeed && joystickActive) {
            float scale = maxSpeed / currentVelMag;
            velX *= scale;
            velY *= scale;
        }

        x += velX * dt;
        y += velY * dt;

        float margin = getSize();
        if (x < margin) { x = margin; velX = Math.max(0, velX); }
        if (x > screenW - margin) { x = screenW - margin; velX = Math.min(0, velX); }
        if (y < screenH * Constants.PLAYER_Y_MIN_RATIO) { y = screenH * Constants.PLAYER_Y_MIN_RATIO; velY = Math.max(0, velY); }
        if (y > screenH * Constants.PLAYER_Y_MAX_RATIO) { y = screenH * Constants.PLAYER_Y_MAX_RATIO; velY = Math.min(0, velY); }

        float normalizedVelX = velX / Math.max(maxSpeed, 1f);
        targetBank = -normalizedVelX * Constants.PLAYER_MAX_BANK_ANGLE;
        bankAngle += (targetBank - bankAngle) * Constants.PLAYER_BANK_SPEED * (dt * 60f);

        trailIdx = (trailIdx + 1) % TRAIL_LEN; trailX[trailIdx] = x; trailY[trailIdx] = y;

        afterFrameSkip++;
        if (afterFrameSkip % 2 == 0) {
            afterX[afterIndex] = x; afterY[afterIndex] = y; afterAngle[afterIndex] = bankAngle;
            afterIndex = (afterIndex + 1) % AFTERIMAGE_COUNT;
        }

        glowPulse += 0.06f;
        if (shielded) { shieldTimer--; shieldPulse += 0.15f; if (shieldTimer <= 0) shielded = false; }
        if (overdrive) { overdriveTimer--; overdrivePulse += 0.12f; if (overdriveTimer <= 0) overdrive = false; }
    }

    public void render(Canvas c, float cosmicBreath) {
        renderTrail(c); renderGlow(c, cosmicBreath); drawAfterimages(c);
        renderer.drawShip(c, currentShip, x, y, bankAngle, 255, scaleMultiplier, overdrive || overchargeSpeedBoost > 1.5f);
        if (comboCount > 1) drawComboArc(c);
        if (overdrive) renderOverdrive(c, scaleMultiplier);
        if (shielded) renderShield(c, scaleMultiplier);
    }

    private void drawAfterimages(Canvas c) {
        for (int i = 0; i < AFTERIMAGE_COUNT; i++) {
            int idx = (afterIndex + i) % AFTERIMAGE_COUNT;
            float age = (float)(AFTERIMAGE_COUNT - i) / AFTERIMAGE_COUNT;
            int alpha = (int)(60 * (1f - age));
            if (alpha < 5) continue;
            float scale = (1f - age * 0.2f) * scaleMultiplier;
            renderer.drawShip(c, currentShip, afterX[idx], afterY[idx], afterAngle[idx], alpha, scale, overdrive || overchargeSpeedBoost > 1.5f);
        }
    }

    private void drawComboArc(Canvas c) {
        float sweepAngle = comboProgress * 360f;
        int arcColor = comboProgress > 0.5f ? Color.rgb(100, 255, 100) : comboProgress > 0.3f ? Color.rgb(255, 255, 80) : Color.rgb(255, (int)(60 * (0.6 + 0.4 * Math.sin(System.currentTimeMillis() * 0.015))), (int)(60 * (0.6 + 0.4 * Math.sin(System.currentTimeMillis() * 0.015))));
        comboArcPaint.setColor(arcColor); comboArcPaint.setStrokeWidth(currentShip.collisionRadius * scaleMultiplier * 0.2f);
        float r = currentShip.collisionRadius * scaleMultiplier * 1.8f;
        comboArcRect.set(x - r, y - r, x + r, y + r);
        c.drawArc(comboArcRect, -90, sweepAngle, false, comboArcPaint);
    }

    private void renderTrail(Canvas c) {
        float s = currentShip.collisionRadius * scaleMultiplier;
        int trailColor = getTrailColor();
        float baseSize = s * 0.4f + comboTier * s * 0.06f + (overdrive ? s * 0.2f : 0);
        int baseAlpha = overdrive ? 55 : 25 + comboTier * 6;
        for (int i = 0; i < TRAIL_LEN; i++) {
            int idx = (trailIdx - i + TRAIL_LEN) % TRAIL_LEN;
            float a = 1f - (i / (float) TRAIL_LEN);
            trailPaint.setColor(trailColor); trailPaint.setAlpha((int)(baseAlpha * a));
            c.drawCircle(trailX[idx], trailY[idx] + s * 1.2f, baseSize * a, trailPaint);
        }
    }

    private void renderGlow(Canvas c, float cosmicBreath) {
        float s = currentShip.collisionRadius * scaleMultiplier;
        float p = 0.85f + 0.15f * cosmicBreath;
        glowPaint.setColor(overdrive || overchargeSpeedBoost > 1.5f ? 0xFFFF6D00 : currentShip.cockpitGlowColor);
        for (int i = 6; i >= 0; i--) {
            float f = (float) i / 6; glowPaint.setAlpha((int)((overdrive ? 15 : 10) * (1f - f)));
            c.drawCircle(x, y, s * (1.8f + f * 2.5f) * p, glowPaint);
        }
    }

    private void renderOverdrive(Canvas c, float scaleMultiplier) {
        float p = (float)(Math.sin(overdrivePulse) * 0.15 + 0.85); float r = currentShip.collisionRadius * scaleMultiplier * 2.2f * p;
        for (int i = 3; i >= 0; i--) { glowPaint.setColor(0xFFFF6D00); glowPaint.setAlpha(30-i*7); c.drawCircle(x, y, r+i*5, glowPaint); }
    }

    private void renderShield(Canvas c, float scaleMultiplier) {
        float p = (float)(Math.sin(shieldPulse) * 0.1 + 0.9); float r = currentShip.collisionRadius * scaleMultiplier * 2.0f * p;
        shieldPaint.setColor(currentShip.cockpitGlowColor);
        for (int i = 3; i >= 0; i--) { shieldPaint.setAlpha(70-i*16); c.drawCircle(x, y, r+i*3, shieldPaint); }
    }

    public float getX() { return x; } public float getY() { return y; }
    public float getSize() { return currentShip.collisionRadius * scaleMultiplier; } 
    
    // EKSIK OLAN METOD:
    public float getCollisionRadius() { return getSize(); }

    public float getBankAngle() { return bankAngle; }
    public boolean isShielded() { return shielded; } public boolean isOverdrive() { return overdrive; }
    public void activateShield(int d) { shielded = true; shieldTimer = d; shieldPulse = 0; }
    public void activateOverdrive() { overdrive = true; overdriveTimer = Constants.OVERDRIVE_DURATION; overdrivePulse = 0; }
    public RectF getBounds() { float s = getSize() * 0.8f; boundsRect.set(x-s, y-s*1.5f, x+s, y+s); return boundsRect; }
    
    public void reset() {
        x = screenW/2f; y = screenH * Constants.PLAYER_START_Y_RATIO; prevX = x;
        velX = 0; velY = 0;
        bankAngle = 0; targetBank = 0; comboTier = 0; comboCount = 0; idleHoverTimer = 0f;
        shielded = false; shieldTimer = 0; overdrive = false; overdriveTimer = 0; overchargeSpeedBoost = 1.0f;
        for (int i = 0; i < TRAIL_LEN; i++) { trailX[i] = x; trailY[i] = y; }
        for (int i = 0; i < AFTERIMAGE_COUNT; i++) { afterX[i] = x; afterY[i] = y; afterAngle[i] = 0; }
    }
}
