package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import com.stellardrift.game.util.EconomyManager;
import java.util.List;

public class ProjectileSystem {

    static class Projectile {
        boolean active = false;
        float x, y, velX, velY;
        int damage, color, shipType;
        float width, height;        

        static final int TRAIL_LENGTH = 6;
        final float[] trailX = new float[TRAIL_LENGTH];
        final float[] trailY = new float[TRAIL_LENGTH];
        int trailCursor = 0, trailFrameSkip = 0;

        void init(float x, float y, float velX, float velY, int damage, int color, float w, float h, int type) {
            this.active = true; this.x = x; this.y = y; this.velX = velX; this.velY = velY;
            this.damage = damage; this.color = color; this.width = w; this.height = h; this.shipType = type;
            this.trailCursor = 0; this.trailFrameSkip = 0;
            for (int i = 0; i < TRAIL_LENGTH; i++) { trailX[i] = x; trailY[i] = y; }
        }

        void update(float dt, float screenHeight, float screenWidth) {
            if (!active) return;
            x += velX * dt; y += velY * dt;
            trailFrameSkip++;
            if (trailFrameSkip % 2 == 0) { trailX[trailCursor] = x; trailY[trailCursor] = y; trailCursor = (trailCursor + 1) % TRAIL_LENGTH; }
            if (y < -50 || y > screenHeight + 50 || x < -50 || x > screenWidth + 50) active = false;
        }
    }

    private static final int POOL_SIZE = 80;  
    private final Projectile[] pool = new Projectile[POOL_SIZE];
    private int cursor = 0;
    private float fireTimer = 0f;
    
    // EKSIK OLAN FIELD VE METHOD BURADA
    private int damageOverride = -1;

    public void setDamageOverride(int damage) {
        this.damageOverride = damage;
    }

    static final int MAX_FLASHES = 8;
    final float[] flashX = new float[MAX_FLASHES], flashY = new float[MAX_FLASHES], flashLife = new float[MAX_FLASHES]; 
    final int[] flashColor = new int[MAX_FLASHES];
    int flashCursor = 0;

    private float screenWidth, screenHeight;
    private final Paint projPaint = new Paint(Paint.ANTI_ALIAS_FLAG), trailPaint = new Paint(), flashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path projPath = new Path();

    public ProjectileSystem(float screenW, float screenH) {
        this.screenWidth = screenW; this.screenHeight = screenH;
        for (int i = 0; i < POOL_SIZE; i++) pool[i] = new Projectile();
        projPaint.setStyle(Paint.Style.FILL); trailPaint.setStyle(Paint.Style.STROKE); trailPaint.setStrokeCap(Paint.Cap.ROUND); flashPaint.setStyle(Paint.Style.FILL);
    }

    public float autoFire(float dt, float shipX, float shipY, float bankAngle, ShipData ship, float fireRateMultiplier) {
        fireTimer += dt;
        
        float effectiveFireRate = ship.baseFireRate * fireRateMultiplier;
        float fireInterval = 1f / effectiveFireRate;
        float recoilAmount = 0f;

        if (fireTimer >= fireInterval) {
            fireTimer -= fireInterval;
            
            float speedBoost = (fireRateMultiplier > 1.5f) ? 1.3f : 1.0f;
            float baseSpeed = -800f * (screenHeight / 1920f) * ship.baseProjectileSpeed * speedBoost; 

            float scale = (screenWidth / 1080f) * 2.5f;

            // Damage Override (Upgrade edildiyse kullanir)
            int effectiveDamage = (damageOverride > 0) ? damageOverride : ship.baseDamage;

            for (int w = 0; w < ship.projectileCount; w++) {
                float rad = (float) Math.toRadians(bankAngle);
                float cos = (float) Math.cos(rad); float sin = (float) Math.sin(rad);

                float localX = ship.weaponX[w] * scale;
                float localY = ship.weaponY[w] * scale;
                float worldX = shipX + localX * cos - localY * sin;
                float worldY = shipY + localX * sin + localY * cos;
                
                float spreadX = 0f;
                if (fireRateMultiplier > 1.5f) {
                    spreadX = (float)((Math.random() - 0.5) * 40);
                }

                float pw, ph;
                switch (ship.id) {
                    case ShipRegistry.STRIKER: pw = 2.5f * scale; ph = 8f * scale; break;
                    case ShipRegistry.JUGGERNAUT: pw = 4f * scale; ph = 10f * scale; break;
                    case ShipRegistry.PHANTOM: pw = 2f * scale; ph = 14f * scale; break;
                    case ShipRegistry.SWARM: pw = 3.5f * scale; ph = 6f * scale; break; 
                    case ShipRegistry.ECLIPSE: pw = 2f * scale; ph = 16f * scale; break; 
                    case ShipRegistry.ZENITH: pw = 4f * scale; ph = 8f * scale; break; 
                    default: pw = 2.5f * scale; ph = 8f * scale; break;
                }

                Projectile p = pool[cursor]; cursor = (cursor + 1) % POOL_SIZE;
                p.init(worldX, worldY, spreadX, baseSpeed, effectiveDamage, ship.projectileColor, pw, ph, ship.id);

                flashX[flashCursor] = worldX; flashY[flashCursor] = worldY; flashLife[flashCursor] = 1f; 
                flashColor[flashCursor] = (fireRateMultiplier > 1.5f) ? Color.rgb(60, 180, 255) : ship.projectileColor; 
                flashCursor = (flashCursor + 1) % MAX_FLASHES;
            }
            recoilAmount = 1.0f; 
        }
        return recoilAmount;
    }

    public void update(float dt) {
        for (int i = 0; i < POOL_SIZE; i++) pool[i].update(dt, screenHeight, screenWidth);
        for (int i = 0; i < MAX_FLASHES; i++) if (flashLife[i] > 0) flashLife[i] -= dt * 12f; 
    }

    public void draw(Canvas canvas) {
        for (int i = 0; i < POOL_SIZE; i++) { Projectile p = pool[i]; if (!p.active) continue; drawTrail(canvas, p); drawProjectile(canvas, p); }
        drawMuzzleFlashes(canvas);
    }

    private void drawProjectile(Canvas canvas, Projectile p) {
        projPaint.setColor(p.color);
        switch (p.shipType) {
            case ShipRegistry.STRIKER:
                projPath.reset(); projPath.moveTo(p.x, p.y - p.height / 2); projPath.lineTo(p.x + p.width / 2, p.y); projPath.lineTo(p.x, p.y + p.height / 2); projPath.lineTo(p.x - p.width / 2, p.y); projPath.close();
                canvas.drawPath(projPath, projPaint); projPaint.setColor(Color.argb(200, 255, 255, 255)); canvas.drawCircle(p.x, p.y, 1.5f, projPaint);
                break;
            case ShipRegistry.JUGGERNAUT:
                canvas.drawRoundRect(p.x - p.width / 2, p.y - p.height / 2, p.x + p.width / 2, p.y + p.height / 2, 3f, 3f, projPaint);
                projPaint.setColor(Color.argb(220, 255, 230, 180)); canvas.drawRect(p.x - p.width / 2 + 1f, p.y - p.height / 2, p.x + p.width / 2 - 1f, p.y - p.height / 2 + 6f, projPaint);
                break;
            case ShipRegistry.PHANTOM:
                canvas.drawRoundRect(p.x - p.width / 2, p.y - p.height / 2, p.x + p.width / 2, p.y + p.height / 2, p.width / 2, p.width / 2, projPaint);
                projPaint.setColor(Color.argb(40, Color.red(p.color), Color.green(p.color), Color.blue(p.color))); canvas.drawCircle(p.x, p.y, p.height * 0.4f, projPaint);
                break;
            case ShipRegistry.SWARM:
                canvas.drawCircle(p.x, p.y, p.width * 0.7f, projPaint);
                projPaint.setColor(Color.argb(230, 220, 255, 200)); canvas.drawCircle(p.x, p.y, p.width * 0.3f, projPaint);
                break;
            case ShipRegistry.ECLIPSE:
                projPath.reset(); projPath.moveTo(p.x, p.y - p.height / 2); projPath.lineTo(p.x + p.width * 0.4f, p.y - p.height * 0.15f); projPath.lineTo(p.x + p.width * 0.3f, p.y + p.height * 0.3f); projPath.lineTo(p.x, p.y + p.height / 2); projPath.lineTo(p.x - p.width * 0.3f, p.y + p.height * 0.3f); projPath.lineTo(p.x - p.width * 0.4f, p.y - p.height * 0.15f); projPath.close();
                canvas.drawPath(projPath, projPaint); projPaint.setColor(Color.argb(30, 255, 40, 20)); canvas.drawCircle(p.x, p.y, p.height * 0.5f, projPaint);
                break;
            case ShipRegistry.ZENITH:
                projPaint.setColor(Color.argb(35, Color.red(p.color), Color.green(p.color), Color.blue(p.color))); canvas.drawCircle(p.x, p.y, p.width * 1.8f, projPaint);
                projPaint.setColor(Color.argb(80, Color.red(p.color), Color.green(p.color), Color.blue(p.color))); canvas.drawCircle(p.x, p.y, p.width, projPaint);
                projPaint.setColor(Color.argb(230, 255, 250, 220)); canvas.drawCircle(p.x, p.y, p.width * 0.5f, projPaint);
                break;
        }
    }

    private void drawTrail(Canvas canvas, Projectile p) {
        trailPaint.setStrokeWidth(p.width * 0.6f);
        for (int t = 0; t < Projectile.TRAIL_LENGTH - 1; t++) {
            int idx0 = (p.trailCursor + t) % Projectile.TRAIL_LENGTH, idx1 = (p.trailCursor + t + 1) % Projectile.TRAIL_LENGTH;
            float age = (float) t / Projectile.TRAIL_LENGTH; int alpha = (int)(80 * (1f - age)); if (alpha < 5) continue;
            trailPaint.setColor(Color.argb(alpha, Color.red(p.color), Color.green(p.color), Color.blue(p.color)));
            canvas.drawLine(p.trailX[idx0], p.trailY[idx0], p.trailX[idx1], p.trailY[idx1], trailPaint);
        }
    }

    private void drawMuzzleFlashes(Canvas canvas) {
        for (int i = 0; i < MAX_FLASHES; i++) {
            if (flashLife[i] <= 0) continue;
            float life = flashLife[i], radius = 10f + (1f - life) * 8f; 
            flashPaint.setColor(Color.argb((int)(40 * life), Color.red(flashColor[i]), Color.green(flashColor[i]), Color.blue(flashColor[i])));
            canvas.drawCircle(flashX[i], flashY[i], radius * 2, flashPaint);
            flashPaint.setColor(Color.argb((int)(200 * life), 255, 255, 255)); canvas.drawCircle(flashX[i], flashY[i], radius * 0.6f, flashPaint);
        }
    }

    public interface HitCallback { void onAsteroidHit(Asteroid asteroid); void onAsteroidDestroyed(Asteroid asteroid); }
    public boolean checkHits(List<Asteroid> asteroids, EconomyManager economy, HitCallback callback) {
        boolean anyHit = false;
        for (int pi = 0; pi < POOL_SIZE; pi++) {
            Projectile p = pool[pi]; if (!p.active) continue;
            for (Asteroid a : asteroids) {
                if (a.isOffScreen(10000)) continue; 
                float dx = p.x - a.getX(), dy = p.y - a.getY(), distSq = dx * dx + dy * dy;
                float hitDist = a.getSize() + Math.max(p.width, p.height);
                if (distSq < hitDist * hitDist) {
                    p.active = false; boolean destroyed = a.takeDamage(p.damage, economy); anyHit = true;
                    if (destroyed) callback.onAsteroidDestroyed(a); else callback.onAsteroidHit(a); break;
                }
            }
        } return anyHit;
    }
}
