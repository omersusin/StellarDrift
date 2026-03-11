package com.stellardrift.game.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import com.stellardrift.game.util.Constants;
import com.stellardrift.game.world.Asteroid;
import com.stellardrift.game.world.GameWorld;
import com.stellardrift.game.world.Particle;
import com.stellardrift.game.world.PowerUp;
import com.stellardrift.game.world.ScorePopup;
import com.stellardrift.game.world.StarDust;
import java.util.List;

public class Renderer {
    private Paint particlePaint, warningPaint, shockPaint, killHighlight, flashPaint, flashGlowPaint;
    private Paint burstPaint;
    private float baseTextSize;
    private Paint chromaticRed, chromaticBlue;

    public Renderer() {
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG); particlePaint.setStyle(Paint.Style.FILL);
        warningPaint = new Paint(Paint.ANTI_ALIAS_FLAG); warningPaint.setColor(0xFFFF1744); warningPaint.setStyle(Paint.Style.FILL);
        shockPaint = new Paint(Paint.ANTI_ALIAS_FLAG); shockPaint.setStyle(Paint.Style.STROKE);
        killHighlight = new Paint(Paint.ANTI_ALIAS_FLAG); killHighlight.setStyle(Paint.Style.STROKE); killHighlight.setStrokeWidth(4f); killHighlight.setColor(Color.WHITE);
        flashPaint = new Paint(Paint.ANTI_ALIAS_FLAG); flashPaint.setStrokeCap(Paint.Cap.ROUND);
        flashGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG); flashGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        burstPaint = new Paint(Paint.ANTI_ALIAS_FLAG); burstPaint.setStyle(Paint.Style.FILL);
        chromaticRed = new Paint(); chromaticRed.setColorFilter(new PorterDuffColorFilter(Color.argb(120, 255, 0, 0), PorterDuff.Mode.LIGHTEN));
        chromaticBlue = new Paint(); chromaticBlue.setColorFilter(new PorterDuffColorFilter(Color.argb(120, 0, 100, 255), PorterDuff.Mode.LIGHTEN));
    }

    public void render(Canvas canvas, GameWorld world) {
        int state = world.getState();
        if (baseTextSize == 0) baseTextSize = canvas.getWidth() * 0.032f;
        
        if (state == Constants.STATE_PLAYING || state == Constants.STATE_GAME_OVER || state == Constants.STATE_PAUSED) {
            if (world.isFreezing()) {
                float intensity = (float)(Math.random() * 8f);
                canvas.saveLayer(null, chromaticRed, Canvas.ALL_SAVE_FLAG); canvas.translate(-intensity, 0); drawMainScene(canvas, world); canvas.restore();
                canvas.saveLayer(null, chromaticBlue, Canvas.ALL_SAVE_FLAG); canvas.translate(intensity, 0); drawMainScene(canvas, world); canvas.restore();
                drawMainScene(canvas, world);
            } else {
                drawMainScene(canvas, world);
            }
        }
    }

    private void drawMainScene(Canvas canvas, GameWorld world) {
        renderSpawnWarnings(canvas, world);
        for (Asteroid a : world.getAsteroids()) a.render(canvas);
        if (world.isFreezing() && world.getKillerAsteroid() != null) {
            Asteroid ka = world.getKillerAsteroid();
            float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.05) * 0.3 + 0.7);
            killHighlight.setAlpha((int)(255 * pulse)); canvas.drawCircle(ka.getX(), ka.getY(), ka.getSize() * 0.75f, killHighlight);
        }
        for (StarDust s : world.getStarDusts()) s.render(canvas);
        for (PowerUp p : world.getPowerUps()) p.render(canvas);
        for (Particle p : world.getParticles()) p.render(canvas, particlePaint);
        renderRingBursts(canvas, world);
        renderNearMissFlashes(canvas, world);
        world.getPlayer().render(canvas);
        for (ScorePopup sp : world.getPopups()) sp.render(canvas, baseTextSize);
        if (world.isShockwaveActive()) renderShockwave(canvas, world);
    }

    private void renderSpawnWarnings(Canvas canvas, GameWorld world) {
        for (float[] w : world.getSpawnWarnings()) {
            float alpha = w[1] / 20f; float blink = (float)(Math.sin(w[1] * 0.8) * 0.4 + 0.6);
            warningPaint.setAlpha((int)(80 * alpha * blink)); canvas.drawCircle(w[0], 8, 5, warningPaint);
            warningPaint.setAlpha((int)(30 * alpha * blink)); canvas.drawCircle(w[0], 8, 12, warningPaint);
        }
    }

    private void renderNearMissFlashes(Canvas canvas, GameWorld world) {
        for (float[] f : world.getNearMissFlashes()) {
            float alpha = f[4] / Constants.NEAR_MISS_FLASH_LIFE;
            flashGlowPaint.setStrokeWidth(12f * alpha); flashGlowPaint.setColor(Color.argb((int)(alpha * 80), 200, 220, 255));
            canvas.drawLine(f[0], f[1], f[2], f[3], flashGlowPaint);
            flashPaint.setStrokeWidth(4f * alpha); flashPaint.setColor(Color.argb((int)(alpha * 255), 255, 255, 255));
            canvas.drawLine(f[0], f[1], f[2], f[3], flashPaint);
        }
    }

    private void renderShockwave(Canvas canvas, GameWorld world) {
        float r = world.getShockwaveRadius(), a = world.getShockwaveAlpha(), cx = world.getShockwaveX(), cy = world.getShockwaveY();
        shockPaint.setStrokeWidth(8f); shockPaint.setColor(Color.argb((int)(a * 100), 255, 255, 255)); canvas.drawCircle(cx, cy, r, shockPaint);
        shockPaint.setStrokeWidth(3f); shockPaint.setColor(Color.argb((int)(a * 200), 255, 200, 100)); canvas.drawCircle(cx, cy, r * 0.85f, shockPaint);
        if (r < 100) { float fa = 1f - r / 100f; shockPaint.setStyle(Paint.Style.FILL); shockPaint.setColor(Color.argb((int)(fa * 150), 255, 255, 255)); canvas.drawCircle(cx, cy, r * 0.3f, shockPaint); shockPaint.setStyle(Paint.Style.STROKE); }
    }

    private void renderRingBursts(Canvas canvas, GameWorld world) {
        for (float[] b : world.getRingBursts()) {
            int alpha = (int)(255 * b[5]);
            burstPaint.setColor(Color.argb(alpha, Color.red((int)b[6]), Color.green((int)b[6]), Color.blue((int)b[6])));
            canvas.drawCircle(b[0], b[1], 3f * b[5], burstPaint);
        }
    }
}
