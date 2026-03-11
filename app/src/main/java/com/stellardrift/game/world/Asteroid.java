package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import com.stellardrift.game.util.Constants;
import com.stellardrift.game.util.EconomyManager;

public class Asteroid {

    boolean active = false;

    float x, y;
    float velX, velY;
    float rotation, rotSpeed;
    float radius;

    int maxHP;
    int currentHP;
    float hitFlashTimer = 0f;
    static final float HIT_FLASH_DURATION = 0.08f; 

    Path cachedPath;
    float[] localVX, localVY;
    int vertexCount;

    int baseColor;
    int creditValue; // Dinamik Kredi

    private int age;
    private float masterAlpha;
    private boolean hasSine;
    private float sinePhase, sineAmp, sineFreq;
    private float baseX;

    private RectF boundsRect;
    private Paint bodyPaint, edgePaint, craterPaint, glowPaint, highlightPaint, flashOverlayPaint;

    public Asteroid(int sw, int sh, float playerX) {
        float range = Constants.ASTEROID_MAX_SIZE - Constants.ASTEROID_MIN_SIZE;
        radius = sw * (Constants.ASTEROID_MIN_SIZE + (float)(Math.random() * range));

        if (playerX >= 0) {
            float safeZone = sw * Constants.ASTEROID_SAFE_ZONE;
            float tryX; int attempts = 0;
            do { tryX = (float)(Math.random() * (sw - radius * 2) + radius); attempts++; } 
            while (Math.abs(tryX - playerX) < safeZone && attempts < 10);
            x = tryX;
        } else { x = (float)(Math.random() * (sw - radius * 2) + radius); }

        baseX = x;
        y = -radius * 2 - (float)(Math.random() * 300);

        float sRange = Constants.ASTEROID_MAX_SPEED - Constants.ASTEROID_MIN_SPEED;
        float speed = (Constants.ASTEROID_MIN_SPEED + (float)(Math.random() * sRange)) * (sw / 1080f);
        
        velX = 0;
        velY = speed;

        rotation = 0; rotSpeed = (float)(Math.random() * 2.5 - 1.25);

        age = 0; masterAlpha = 0;
        hasSine = Math.random() < Constants.ASTEROID_SINE_CHANCE;
        sinePhase = (float)(Math.random() * Math.PI * 2);
        sineAmp = sw * Constants.ASTEROID_SINE_AMP * (0.5f + (float)(Math.random() * 0.5));
        sineFreq = Constants.ASTEROID_SINE_FREQ * (0.7f + (float)(Math.random() * 0.6));

        // YENİ: DİNAMİK HP VE KREDİ HESAPLAMA
        this.maxHP = Math.max(1, (int)(radius * 0.05f)); // Boyuta orantılı HP
        this.currentHP = maxHP;

        // Küçük taş (az kredi), Büyük taş (çok kredi)
        this.creditValue = Math.max(3, (int)(radius * 0.15f)); 

        // Renk de boyuta göre grinin tonları
        float sizeRatio = Math.min(radius / (sw * 0.08f), 1f); 
        int gray = (int)(145 - sizeRatio * 50);
        baseColor = Color.rgb(gray + 10, gray, gray - 5);

        buildPolygon();

        int nc = 2 + (int)(Math.random() * 3);
        craters = new float[nc][3];
        for (int i = 0; i < nc; i++) {
            craters[i][0] = (float)(Math.random() * radius * 0.6 - radius * 0.3);
            craters[i][1] = (float)(Math.random() * radius * 0.6 - radius * 0.3);
            craters[i][2] = radius * (0.08f + (float)(Math.random() * 0.15));
        }

        active = true;
        boundsRect = new RectF();
        bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG); bodyPaint.setStyle(Paint.Style.FILL);
        edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG); edgePaint.setStyle(Paint.Style.STROKE); edgePaint.setStrokeWidth(1.5f); edgePaint.setColor(0xFF78909C);
        craterPaint = new Paint(Paint.ANTI_ALIAS_FLAG); craterPaint.setStyle(Paint.Style.FILL);
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG); glowPaint.setStyle(Paint.Style.FILL); glowPaint.setColor(0xFFFF1744);
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG); highlightPaint.setStyle(Paint.Style.FILL); highlightPaint.setColor(0xFF90A4AE);
        flashOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG); flashOverlayPaint.setStyle(Paint.Style.FILL);
    }

    private void buildPolygon() {
        int verts = 7 + (int)(Math.random() * 4); 
        vertexCount = verts;
        if (localVX == null || localVX.length < verts) {
            localVX = new float[verts];
            localVY = new float[verts];
        }
        if (cachedPath == null) cachedPath = new Path();

        float angleStep = (float)(2 * Math.PI / verts);
        for (int i = 0; i < verts; i++) {
            float angle = angleStep * i + (float)(Math.random() * angleStep * 0.4);
            float r = radius * (0.7f + (float)(Math.random() * 0.3f));
            localVX[i] = (float) Math.cos(angle) * r;
            localVY[i] = (float) Math.sin(angle) * r;
        }

        cachedPath.reset();
        cachedPath.moveTo(localVX[0], localVY[0]);
        for (int i = 1; i < verts; i++) cachedPath.lineTo(localVX[i], localVY[i]);
        cachedPath.close();
    }

    public boolean takeDamage(int damage, EconomyManager economy) {
        currentHP -= damage;
        hitFlashTimer = HIT_FLASH_DURATION;
        if (currentHP <= 0) {
            economy.addCredits(creditValue);
            return true; // Patladı (Destroyed)
        }
        return false;
    }

    public void update(float difficulty) {
        if (!active) return;
        float dt = 0.016f;
        y += velY * difficulty;
        rotation += rotSpeed * dt;
        age++;
        if (age < Constants.ASTEROID_FADEIN_FRAMES) masterAlpha = (float) age / Constants.ASTEROID_FADEIN_FRAMES; else masterAlpha = 1f;
        if (hasSine) { sinePhase += sineFreq; x = baseX + (float)(Math.sin(sinePhase) * sineAmp); }
        if (hitFlashTimer > 0) hitFlashTimer -= dt;
    }

    public boolean isOffScreen(int sh) { return y > sh + radius * 3; }

    public void render(Canvas c) {
        if (!active) return;
        int ma = (int)(255 * masterAlpha);
        if (ma < 5) return;

        c.save(); c.translate(x, y); c.rotate(rotation);

        for (int i = 3; i >= 0; i--) { int a = Math.min(255, (4 + i) * ma / 255); glowPaint.setAlpha(a); c.drawCircle(0, 0, radius * (1.1f + i * 0.2f), glowPaint); }

        float hpRatio = (float) currentHP / maxHP;
        
        int rTop = Color.red(baseColor), gTop = (int)(Color.green(baseColor) * hpRatio), bTop = (int)(Color.blue(baseColor) * hpRatio);
        int rBot = Color.red(baseColor)-20, gBot = (int)((Color.green(baseColor)-20) * hpRatio), bBot = (int)((Color.blue(baseColor)-20) * hpRatio);
        int dynamicCol0 = Color.rgb(Math.min(255, rTop + (int)((1f-hpRatio)*80)), Math.max(0, gTop), Math.max(0, bTop));
        int dynamicCol1 = Color.rgb(Math.min(255, rBot + (int)((1f-hpRatio)*80)), Math.max(0, gBot), Math.max(0, bBot));

        bodyPaint.setShader(new LinearGradient(-radius, -radius, radius, radius, dynamicCol0, dynamicCol1, Shader.TileMode.CLAMP));
        bodyPaint.setAlpha(ma); c.drawPath(cachedPath, bodyPaint); bodyPaint.setShader(null);
        edgePaint.setAlpha(Math.min(120, 120 * ma / 255)); c.drawPath(cachedPath, edgePaint);
        highlightPaint.setAlpha(Math.min(40, 40 * ma / 255)); c.drawCircle(-radius * 0.2f, -radius * 0.25f, radius * 0.35f, highlightPaint);

        craterPaint.setColor(Color.argb(140, 30, 30, 30));
        for (float[] cr : craters) {
            craterPaint.setAlpha(Math.min(140, 140 * ma / 255)); c.drawCircle(cr[0], cr[1], cr[2], craterPaint);
            craterPaint.setAlpha(Math.min(60, 60 * ma / 255)); c.drawCircle(cr[0] + cr[2] * 0.15f, cr[1] + cr[2] * 0.15f, cr[2] * 0.65f, craterPaint);
        }

        if (hitFlashTimer > 0) {
            float flashIntensity = hitFlashTimer / HIT_FLASH_DURATION;
            flashOverlayPaint.setColor(Color.argb((int)(180 * flashIntensity), 255, 255, 255));
            c.drawPath(cachedPath, flashOverlayPaint);
        }

        if (currentHP < maxHP && currentHP > 0) drawHPBar(c, hpRatio);

        c.restore();
    }

    private void drawHPBar(Canvas c, float ratio) {
        float barW = radius * 1.4f, barH = 3f, barY = -radius - 8;
        bodyPaint.setColor(Color.argb(120, 0, 0, 0)); c.drawRect(-barW / 2, barY, barW / 2, barY + barH, bodyPaint);
        int hpColor = ratio > 0.6f ? Color.rgb(80, 220, 80) : ratio > 0.3f ? Color.rgb(240, 200, 40) : Color.rgb(240, 60, 40);
        bodyPaint.setColor(hpColor); c.drawRect(-barW / 2, barY, -barW / 2 + barW * ratio, barY + barH, bodyPaint);
    }

    public float getX() { return x; } 
    public float getY() { return y; }
    public float getSize() { return radius; }
    public boolean isDead() { return currentHP <= 0; }
    public int getCreditValue() { return creditValue; }
    
    public RectF getBounds() { float s = radius * 0.65f; boundsRect.set(x - s, y - s, x + s, y + s); return boundsRect; }
}
