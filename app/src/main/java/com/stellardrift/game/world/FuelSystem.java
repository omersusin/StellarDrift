package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class FuelSystem {

    private static final float MAX_FUEL = 100f;
    private static final float FUEL_DRAIN_PER_SECOND = 3.5f;   
    private static final float FUEL_PER_STARDUST = 8f;          
    private static final float FUEL_BONUS_CHAIN = 3f;           

    private static final float SPEED_AT_FULL = 1.3f;            
    private static final float SPEED_AT_EMPTY = 0.35f;          
    private static final float SPEED_CRITICAL_BOOST = 0.05f;    

    private float currentFuel;
    private float displayedFuel;        
    private float speedMultiplier;
    private float pulseTimer = 0f;      

    private float barFlashTimer = 0f;   
    private float warningPulse = 0f;    
    private boolean isCritical = false; 
    
    private boolean drainPaused = false; 

    private final Paint barBgPaint = new Paint();
    private final Paint barFillPaint = new Paint();
    private final Paint barBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint warningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private final RectF barRect = new RectF();
    private final RectF fillRect = new RectF();
    private final RectF glowRect = new RectF();

    private float barX, barY, barWidth, barHeight;
    private float screenWidth, screenHeight;

    public FuelSystem(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.currentFuel = MAX_FUEL;
        this.displayedFuel = MAX_FUEL;
        this.speedMultiplier = SPEED_AT_FULL;

        barWidth = screenWidth * 0.015f;
        barHeight = screenHeight * 0.3f;
        barX = screenWidth * 0.025f;
        barY = screenHeight * 0.35f;

        barBgPaint.setColor(Color.argb(100, 10, 10, 20));
        barBgPaint.setStyle(Paint.Style.FILL);
        barFillPaint.setStyle(Paint.Style.FILL);
        
        barBorderPaint.setStyle(Paint.Style.STROKE);
        barBorderPaint.setStrokeWidth(1.5f);
        barBorderPaint.setColor(Color.argb(120, 150, 160, 180));

        barGlowPaint.setStyle(Paint.Style.FILL);

        labelPaint.setTextSize(screenWidth * 0.025f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);
        labelPaint.setColor(Color.argb(180, 200, 200, 210));

        warningPaint.setStyle(Paint.Style.FILL);
    }
    
    public void setDrainPaused(boolean paused) { this.drainPaused = paused; }

    public void update(float dt) {
        if (!drainPaused) {
            currentFuel -= FUEL_DRAIN_PER_SECOND * dt;
            currentFuel = Math.max(0f, currentFuel);
        } else {
            currentFuel = MAX_FUEL;
        }

        float diff = currentFuel - displayedFuel;
        float step = Math.abs(diff) * 5f * dt;
        if (step < 0.1f) step = 0.1f;
        
        if (Math.abs(diff) < 0.1f) {
            displayedFuel = currentFuel;
        } else {
            displayedFuel += Math.signum(diff) * step;
        }

        float fuelRatio = currentFuel / MAX_FUEL;
        speedMultiplier = SPEED_AT_EMPTY + (SPEED_AT_FULL - SPEED_AT_EMPTY) * fuelRatio;

        isCritical = (fuelRatio < 0.20f);

        if (isCritical && !drainPaused) {
            pulseTimer += dt;
            float pulse = (float) Math.sin(pulseTimer * 8.0) * SPEED_CRITICAL_BOOST;
            speedMultiplier += pulse;
            warningPulse = (float) (0.5 + 0.5 * Math.sin(pulseTimer * 6.0));
        } else {
            pulseTimer = 0f;
            warningPulse = 0f;
        }

        speedMultiplier = Math.max(0.2f, speedMultiplier);
        if (barFlashTimer > 0) barFlashTimer -= dt;
    }

    public void addFuel(float amount) {
        currentFuel = Math.min(MAX_FUEL, currentFuel + amount);
        barFlashTimer = 0.3f;
    }

    // YENİ: Double Power-Up Destekli Yakıt Alma
    public void onStarDustCollected(float multiplier) { addFuel(FUEL_PER_STARDUST * multiplier); }
    public void onStarDustCollected() { addFuel(FUEL_PER_STARDUST); } // Fallback
    public void onChainCompleted() { addFuel(FUEL_BONUS_CHAIN); }

    public float getSpeedMultiplier() { return speedMultiplier; }
    public boolean isCritical() { return isCritical; }

    public void reset() {
        currentFuel = MAX_FUEL; displayedFuel = MAX_FUEL; speedMultiplier = SPEED_AT_FULL;
        barFlashTimer = 0f; pulseTimer = 0f; isCritical = false; drainPaused = false;
    }

    public void draw(Canvas canvas) {
        float ratio = displayedFuel / MAX_FUEL;
        barRect.set(barX, barY, barX + barWidth, barY + barHeight);
        canvas.drawRoundRect(barRect, barWidth / 2, barWidth / 2, barBgPaint);

        float fillHeight = barHeight * ratio;
        float fillTop = barY + barHeight - fillHeight;
        int fillColor = getFuelColor(ratio);

        if (barFlashTimer > 0) {
            float flash = barFlashTimer / 0.3f;
            int r = Math.min(255, Color.red(fillColor) + (int)((255 - Color.red(fillColor)) * flash * 0.5f));
            int g = Math.min(255, Color.green(fillColor) + (int)((255 - Color.green(fillColor)) * flash * 0.5f));
            int b = Math.min(255, Color.blue(fillColor) + (int)((255 - Color.blue(fillColor)) * flash * 0.5f));
            fillColor = Color.rgb(r, g, b);
        }

        barFillPaint.setColor(fillColor);
        fillRect.set(barX + 1.5f, fillTop, barX + barWidth - 1.5f, barY + barHeight - 1);
        canvas.drawRoundRect(fillRect, (barWidth - 3) / 2, (barWidth - 3) / 2, barFillPaint);

        if (ratio > 0.05f) {
            int glowAlpha = (int)(40 + 30 * ratio);
            if (barFlashTimer > 0) glowAlpha += (int)(80 * (barFlashTimer / 0.3f));
            barGlowPaint.setColor(Color.argb(glowAlpha, Color.red(fillColor), Color.green(fillColor), Color.blue(fillColor)));
            float glowH = barWidth * 0.8f;
            glowRect.set(barX - 3, fillTop - glowH / 2, barX + barWidth + 3, fillTop + glowH / 2);
            canvas.drawOval(glowRect, barGlowPaint);
        }

        canvas.drawRoundRect(barRect, barWidth / 2, barWidth / 2, barBorderPaint);

        barBorderPaint.setColor(Color.argb(50, 200, 200, 220));
        for (int i = 1; i <= 3; i++) {
            float tickY = barY + barHeight * (1f - i * 0.25f);
            canvas.drawLine(barX + 3, tickY, barX + barWidth - 3, tickY, barBorderPaint);
        }
        barBorderPaint.setColor(Color.argb(120, 150, 160, 180));

        labelPaint.setColor(Color.argb(160, 180, 190, 200));
        float charGap = labelPaint.getTextSize() * 1.1f;
        float startTextY = barY + barHeight + charGap;
        
        canvas.drawText("F", barX + barWidth / 2, startTextY, labelPaint);
        canvas.drawText("U", barX + barWidth / 2, startTextY + charGap, labelPaint);
        canvas.drawText("E", barX + barWidth / 2, startTextY + charGap * 2, labelPaint);
        canvas.drawText("L", barX + barWidth / 2, startTextY + charGap * 3, labelPaint);

        int percent = (int)(ratio * 100);
        labelPaint.setColor(fillColor);
        labelPaint.setTextSize(screenWidth * 0.025f);
        canvas.drawText(percent + "%", barX + barWidth / 2, barY - 10, labelPaint);

        if (isCritical && !drainPaused) drawCriticalWarning(canvas);
    }

    private void drawCriticalWarning(Canvas canvas) {
        int warnAlpha = (int)(warningPulse * 35);
        warningPaint.setColor(Color.argb(warnAlpha, 255, 30, 20));
        canvas.drawRect(0, barY, barX + barWidth + 20, barY + barHeight, warningPaint);
        if (warningPulse > 0.5f) {
            labelPaint.setColor(Color.argb((int)(warningPulse * 220), 255, 60, 40));
            labelPaint.setTextSize(screenWidth * 0.022f);
            labelPaint.setFakeBoldText(true);
            canvas.drawText("LOW", barX + barWidth / 2, barY - 30, labelPaint);
            labelPaint.setTextSize(screenWidth * 0.025f);
        }
    }

    private int getFuelColor(float ratio) {
        if (ratio > 0.6f) {
            float t = (ratio - 0.6f) / 0.4f;
            return Color.rgb((int)(80 + (1 - t) * 100), (int)(200 + t * 55), (int)(60 + t * 20));
        } else if (ratio > 0.3f) {
            float t = (ratio - 0.3f) / 0.3f;
            return Color.rgb((int)(255 - t * 75), (int)(140 + t * 80), 40);
        } else {
            float t = ratio / 0.3f;
            return Color.rgb(255, (int)(40 + t * 100), (int)(30 + t * 10));
        }
    }
}
