package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.stellardrift.game.util.Constants;

public class ScorePopup {

    private float x, y, startY;
    private String text;
    private int color;
    private int life, maxLife;
    private float scale;

    private static Paint paint;

    static {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public ScorePopup(float x, float y, String text, int color) {
        this.x = x;
        this.y = y;
        this.startY = y;
        this.text = text;
        this.color = color;
        this.life = Constants.POPUP_LIFETIME;
        this.maxLife = Constants.POPUP_LIFETIME;
        this.scale = 1.5f;
    }

    public void update() {
        y -= 2.0f;
        life--;
        // Pop-in effect: starts big, shrinks to normal, then stays
        float progress = 1f - (float) life / maxLife;
        if (progress < 0.2f) {
            scale = 1.5f - (progress / 0.2f) * 0.5f;
        } else {
            scale = 1.0f;
        }
    }

    public boolean isAlive() {
        return life > 0;
    }

    public void render(Canvas c, float baseTextSize) {
        if (!isAlive()) return;
        float alpha = Math.min(1f, (float) life / (maxLife * 0.4f));

        paint.setColor(color);
        paint.setAlpha((int)(255 * alpha));
        paint.setTextSize(baseTextSize * scale);

        // Shadow/glow
        paint.setShadowLayer(8f, 0, 0, color);
        c.drawText(text, x, y, paint);
        paint.setShadowLayer(0, 0, 0, 0);
    }

    public static ScorePopup createCollect(float x, float y, int score, int combo) {
        String txt;
        int col;
        if (combo > 1) {
            txt = "+" + score + " x" + combo;
            col = combo >= 5 ? 0xFFFF6D00 : 0xFFFFD740;
        } else {
            txt = "+" + score;
            col = 0xFFFFD740;
        }
        return new ScorePopup(x, y - 30, txt, col);
    }

    public static ScorePopup createNearMiss(float x, float y) {
        return new ScorePopup(x, y - 50, "NEAR MISS! +" + Constants.NEAR_MISS_BONUS, 0xFF00E5FF);
    }

    public static ScorePopup createPowerUp(float x, float y, int type) {
        String name = PowerUp.getName(type);
        int col = PowerUp.getColor(type);
        return new ScorePopup(x, y - 40, name + "!", col);
    }
}
