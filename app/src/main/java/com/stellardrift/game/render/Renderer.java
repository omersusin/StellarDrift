package com.stellardrift.game.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.stellardrift.game.util.Constants;
import com.stellardrift.game.world.Asteroid;
import com.stellardrift.game.world.GameWorld;
import com.stellardrift.game.world.Particle;
import com.stellardrift.game.world.PowerUp;
import com.stellardrift.game.world.ScorePopup;
import com.stellardrift.game.world.StarDust;
import java.util.List;

public class Renderer {

    private Paint particlePaint, warningPaint, shockPaint, killHighlight;
    private float baseTextSize;

    public Renderer() {
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setStyle(Paint.Style.FILL);

        warningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        warningPaint.setColor(0xFFFF1744);
        warningPaint.setStyle(Paint.Style.FILL);

        shockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shockPaint.setStyle(Paint.Style.STROKE);

        killHighlight = new Paint(Paint.ANTI_ALIAS_FLAG);
        killHighlight.setStyle(Paint.Style.STROKE);
        killHighlight.setStrokeWidth(4f);
        killHighlight.setColor(Color.WHITE);
    }

    public void render(Canvas canvas, GameWorld world) {
        int state = world.getState();
        if (baseTextSize == 0) baseTextSize = canvas.getWidth() * 0.032f;

        if (state == Constants.STATE_PLAYING || state == Constants.STATE_GAME_OVER) {

            // Spawn warnings
            renderSpawnWarnings(canvas, world);

            for (Asteroid a : world.getAsteroids()) {
                a.render(canvas);
            }

            // Killer asteroid highlight (freeze frame)
            if (world.isFreezing() && world.getKillerAsteroid() != null) {
                Asteroid ka = world.getKillerAsteroid();
                float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.02) * 0.3 + 0.7);
                killHighlight.setAlpha((int)(255 * pulse));
                canvas.drawCircle(ka.getX(), ka.getY(),
                    ka.getSize() * 0.75f, killHighlight);
            }

            for (StarDust s : world.getStarDusts()) s.render(canvas);
            for (PowerUp p : world.getPowerUps()) p.render(canvas);
            for (Particle p : world.getParticles()) p.render(canvas, particlePaint);

            world.getPlayer().render(canvas);

            for (ScorePopup sp : world.getPopups()) sp.render(canvas, baseTextSize);

            // Shockwave
            if (world.isShockwaveActive()) {
                renderShockwave(canvas, world);
            }
        }
    }

    private void renderSpawnWarnings(Canvas canvas, GameWorld world) {
        List<float[]> warnings = world.getSpawnWarnings();
        for (float[] w : warnings) {
            float alpha = w[1] / 20f;
            float blink = (float)(Math.sin(w[1] * 0.8) * 0.4 + 0.6);
            warningPaint.setAlpha((int)(80 * alpha * blink));
            canvas.drawCircle(w[0], 8, 5, warningPaint);

            warningPaint.setAlpha((int)(30 * alpha * blink));
            canvas.drawCircle(w[0], 8, 12, warningPaint);
        }
    }

    private void renderShockwave(Canvas canvas, GameWorld world) {
        float r = world.getShockwaveRadius();
        float a = world.getShockwaveAlpha();
        float cx = world.getShockwaveX(), cy = world.getShockwaveY();

        // Outer ring
        shockPaint.setStrokeWidth(8f);
        shockPaint.setColor(Color.argb((int)(a * 100), 255, 255, 255));
        canvas.drawCircle(cx, cy, r, shockPaint);

        // Inner ring
        shockPaint.setStrokeWidth(3f);
        shockPaint.setColor(Color.argb((int)(a * 200), 255, 200, 100));
        canvas.drawCircle(cx, cy, r * 0.85f, shockPaint);

        // Center flash
        if (r < 100) {
            float fa = 1f - r / 100f;
            shockPaint.setStyle(Paint.Style.FILL);
            shockPaint.setColor(Color.argb((int)(fa * 150), 255, 255, 255));
            canvas.drawCircle(cx, cy, r * 0.3f, shockPaint);
            shockPaint.setStyle(Paint.Style.STROKE);
        }
    }
}
