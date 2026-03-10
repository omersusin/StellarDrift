package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Particle {

    private float x, y, vx, vy;
    private float size, originalSize;
    private int color;
    private int life, maxLife;

    public Particle(float x, float y, float vx, float vy,
                    float size, int color, int life) {
        this.x = x; this.y = y;
        this.vx = vx; this.vy = vy;
        this.size = size; this.originalSize = size;
        this.color = color;
        this.life = life; this.maxLife = life;
    }

    public void update() {
        x += vx; y += vy;
        vx *= 0.97f; vy *= 0.97f;
        life--;
        size = originalSize * ((float) life / maxLife);
    }

    public boolean isAlive() {
        return life > 0 && size > 0.3f;
    }

    public void render(Canvas canvas, Paint paint) {
        if (!isAlive()) return;
        float alpha = (float) life / maxLife;

        paint.setColor(color);
        paint.setAlpha((int)(60 * alpha));
        canvas.drawCircle(x, y, size * 2.5f, paint);

        paint.setAlpha((int)(255 * alpha));
        canvas.drawCircle(x, y, Math.max(size, 0.5f), paint);
    }
}
