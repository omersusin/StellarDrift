package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class PlasmaCore {

    private static final int POOL_SIZE = 6;

    static class Core {
        boolean active = false;
        float x, y;
        float velY;
        float radius;
        float rotation;
        float orbitAngle;
        float pulsePhase;
        float spawnScale;
        float spawnAge;

        void init(float x, float y, float velY) {
            this.active = true;
            this.x = x;
            this.y = y;
            this.velY = velY;
            this.radius = 12f;
            this.rotation = 0f;
            this.orbitAngle = 0f;
            this.pulsePhase = (float)(Math.random() * Math.PI * 2);
            this.spawnScale = 0f;
            this.spawnAge = 0f;
        }
    }

    private final Core[] pool = new Core[POOL_SIZE];
    private int cursor = 0;

    private boolean overchargeActive = false;
    private float overchargeTimer = 0f;
    private float overchargeMaxDuration = 4.0f;
    private float overchargeFlash = 0f;

    private final Paint corePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint auraBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path diamondPath = new Path();
    private final RectF tempRect = new RectF();

    private float screenWidth, screenHeight;

    private static final int PLASMA_BLUE = Color.rgb(40, 140, 255);
    private static final int PLASMA_CYAN = Color.rgb(60, 220, 255);
    private static final int PLASMA_WHITE = Color.rgb(200, 235, 255);

    public PlasmaCore(float screenW, float screenH) {
        this.screenWidth = screenW;
        this.screenHeight = screenH;
        for (int i = 0; i < POOL_SIZE; i++) {
            pool[i] = new Core();
        }
        corePaint.setStyle(Paint.Style.FILL);
        glowPaint.setStyle(Paint.Style.FILL);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);
        auraBarPaint.setStyle(Paint.Style.FILL);
    }

    public void trySpawn(float x, float y, float velY) {
        Core c = pool[cursor];
        cursor = (cursor + 1) % POOL_SIZE;
        c.init(x, y, velY);
    }

    public void update(float dt) {
        for (int i = 0; i < POOL_SIZE; i++) {
            Core c = pool[i];
            if (!c.active) continue;

            c.y += c.velY * dt * 60f; 
            c.rotation += 120f * dt;      
            c.orbitAngle += 200f * dt;     
            c.pulsePhase += dt * 5f;       
            c.spawnAge += dt;
            c.spawnScale = Math.min(1f, c.spawnAge / 0.4f); 

            if (c.y > screenHeight + 50 || c.y < -50) {
                c.active = false;
            }
        }

        if (overchargeActive) {
            overchargeTimer -= dt;
            overchargeFlash = (float)(0.5 + 0.5 * Math.sin(overchargeTimer * 12));
            if (overchargeTimer <= 0) {
                overchargeActive = false;
                overchargeTimer = 0;
                overchargeFlash = 0;
            }
        }
    }

    public boolean checkCollection(float playerX, float playerY, float playerRadius) {
        for (int i = 0; i < POOL_SIZE; i++) {
            Core c = pool[i];
            if (!c.active) continue;

            float dx = playerX - c.x;
            float dy = playerY - c.y;
            float distSq = dx * dx + dy * dy;
            float collectDist = playerRadius + c.radius;

            if (distSq < collectDist * collectDist) {
                c.active = false;
                activateOvercharge();
                return true;
            }

            float magnetDist = screenWidth * 0.15f; 
            if (distSq < magnetDist * magnetDist && distSq > 0) {
                float dist = (float) Math.sqrt(distSq);
                float pull = (1f - dist / magnetDist);
                pull = pull * pull * 0.2f;
                c.x += dx / dist * pull * magnetDist * 0.3f;
                c.y += dy / dist * pull * magnetDist * 0.3f;
            }
        }
        return false;
    }

    private void activateOvercharge() {
        overchargeActive = true;
        overchargeTimer = overchargeMaxDuration;
        overchargeFlash = 1f;
    }

    public void draw(Canvas canvas) {
        for (int i = 0; i < POOL_SIZE; i++) {
            Core c = pool[i];
            if (!c.active) continue;
            drawSingleCore(canvas, c);
        }
    }

    private void drawSingleCore(Canvas canvas, Core c) {
        float scale = c.spawnScale; 
        float pulse = (float)(0.5 + 0.5 * Math.sin(c.pulsePhase));

        canvas.save();
        canvas.translate(c.x, c.y);
        canvas.scale(scale, scale);

        float auraR = c.radius * (2.8f + pulse * 0.5f);
        glowPaint.setColor(Color.argb((int)(18 + 12 * pulse), 40, 140, 255));
        canvas.drawCircle(0, 0, auraR, glowPaint);

        float glowR = c.radius * (1.6f + pulse * 0.2f);
        glowPaint.setColor(Color.argb((int)(40 + 20 * pulse), 60, 180, 255));
        canvas.drawCircle(0, 0, glowR, glowPaint);

        drawOrbitRing(canvas, c, c.orbitAngle, c.radius * 1.8f, 1.2f);
        drawOrbitRing(canvas, c, c.orbitAngle * 0.7f + 90, c.radius * 2.2f, 0.8f);

        canvas.save();
        canvas.rotate(c.rotation);

        float dr = c.radius * 0.85f;
        diamondPath.reset();
        diamondPath.moveTo(0, -dr);           
        diamondPath.lineTo(dr * 0.6f, 0);    
        diamondPath.lineTo(0, dr);            
        diamondPath.lineTo(-dr * 0.6f, 0);   
        diamondPath.close();

        corePaint.setColor(PLASMA_BLUE);
        canvas.drawPath(diamondPath, corePaint);

        canvas.scale(0.55f, 0.55f);
        corePaint.setColor(Color.argb((int)(180 + 60 * pulse), 180, 235, 255));
        canvas.drawPath(diamondPath, corePaint);

        canvas.restore(); 

        float coreR = c.radius * (0.25f + 0.08f * pulse);
        corePaint.setColor(PLASMA_WHITE);
        canvas.drawCircle(0, 0, coreR, corePaint);

        canvas.restore(); 
    }

    private void drawOrbitRing(Canvas canvas, Core c, float angle, float orbitR, float strokeW) {
        canvas.save();
        canvas.rotate(angle);
        ringPaint.setColor(Color.argb(100, 80, 200, 255));
        ringPaint.setStrokeWidth(strokeW);
        tempRect.set(-orbitR, -orbitR * 0.35f, orbitR, orbitR * 0.35f);
        canvas.drawOval(tempRect, ringPaint);
        canvas.restore();
    }

    public void drawOverchargeHUD(Canvas canvas, float screenW) {
        if (!overchargeActive) return;

        float ratio = overchargeTimer / overchargeMaxDuration;
        float barW = screenW * 0.5f;
        float barH = 8f;
        float barX = (screenW - barW) / 2f;
        float barY = 30f;

        auraBarPaint.setColor(Color.argb(80, 20, 40, 80));
        tempRect.set(barX, barY, barX + barW, barY + barH);
        canvas.drawRoundRect(tempRect, barH/2, barH/2, auraBarPaint);

        int blue = (int)(140 + 115 * overchargeFlash);
        auraBarPaint.setColor(Color.argb(220, 40, blue, 255));
        tempRect.set(barX, barY, barX + barW * ratio, barY + barH);
        canvas.drawRoundRect(tempRect, barH/2, barH/2, auraBarPaint);

        float tipX = barX + barW * ratio;
        glowPaint.setColor(Color.argb((int)(60 * overchargeFlash), 100, 200, 255));
        canvas.drawCircle(tipX, barY + barH/2, 12, glowPaint);

        auraBarPaint.setColor(Color.argb((int)(180 + 75 * overchargeFlash), 100, 210, 255));
        auraBarPaint.setTextSize(screenW * 0.035f);
        auraBarPaint.setTextAlign(Paint.Align.CENTER);
        auraBarPaint.setFakeBoldText(true);
        canvas.drawText("⚡ OVERCHARGE ⚡", screenW / 2, barY + barH + screenW * 0.04f, auraBarPaint);
    }

    public void drawOverchargeScreenEffect(Canvas canvas, float screenW, float screenH) {
        if (!overchargeActive) return;

        int alpha = (int)(20 + 15 * overchargeFlash);
        glowPaint.setColor(Color.argb(alpha, 30, 120, 255));

        tempRect.set(0, 0, screenW, screenH * 0.06f);
        canvas.drawRect(tempRect, glowPaint);

        tempRect.set(0, screenH * 0.94f, screenW, screenH);
        canvas.drawRect(tempRect, glowPaint);
    }

    public boolean isOverchargeActive()  { return overchargeActive; }
    public float getFireRateMultiplier() { return overchargeActive ? 3.0f : 1.0f; }
    public float getSpeedMultiplier() { return overchargeActive ? 2.0f : 1.0f; }

    public void reset() {
        for (Core c : pool) c.active = false;
        overchargeActive = false;
        overchargeTimer = 0;
        cursor = 0;
    }
}
