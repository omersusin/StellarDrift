package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.stellardrift.game.util.Constants;
import com.stellardrift.game.util.Ease;

public class ScorePopup {

    private float x, y;
    private float velY;
    private String text;
    private int color;
    private float life;
    private float maxLife;
    private float scale;
    private float comboBonus;

    private static Paint paint;

    static {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public ScorePopup(float x, float y, String text, int color, float comboBonus) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.life = 0f; 
        this.maxLife = 0.6f; // 600ms ömür (daha yavaş ve pürüzsüz)
        this.velY = -80f; // Yukarı doğru hız
        this.comboBonus = comboBonus;
        this.scale = 0f;
    }

    public void update(float dt) {
        life += dt;
        
        // Yukarı doğru yavaşlayarak süzülme
        y += velY * dt;
        velY *= 0.94f; // Sürtünme
        
        // Elastic Out scale animasyonu (0'dan fırlar, yaylanır)
        float scaleT = Math.min(life / 0.3f, 1f); 
        scale = Ease.outElastic(scaleT);
        
        // Combo yüksekse daha da titrer
        if (comboBonus > 1f) {
            scale *= 1f + 0.15f * Ease.punch(scaleT);
        }
    }

    public boolean isAlive() {
        return life < maxLife;
    }

    public void render(Canvas c, float baseTextSize) {
        if (!isAlive()) return;
        
        float lifeFraction = life / maxLife;
        float alpha = 1f;
        
        // Son %35'te SmoothStep ile fade out
        if (lifeFraction > 0.65f) {
            float fadeT = (lifeFraction - 0.65f) / 0.35f;
            alpha = 1f - Ease.smoothStep(fadeT);
        }

        if (alpha <= 0.01f) return;

        // Parlaklık atışı (İlk 200ms'de beyaza doğru kayar)
        float brightnessBoost = life < 0.2f ? 1f - Ease.outQuad(life / 0.2f) : 0f;
        
        int baseR = Color.red(color);
        int baseG = Color.green(color);
        int baseB = Color.blue(color);
        
        int r = (int) Math.min(255, baseR + (255 - baseR) * brightnessBoost);
        int g = (int) Math.min(255, baseG + (255 - baseG) * brightnessBoost);
        int b = (int) Math.min(255, baseB + (255 - baseB) * brightnessBoost);

        paint.setColor(Color.argb((int)(255 * alpha), r, g, b));
        paint.setTextSize(baseTextSize * scale * comboBonus);

        // Koyu bir gölge verelim ki her arka planda okunsun
        paint.setShadowLayer(4f, 0, 2f, Color.argb((int)(150 * alpha), 0, 0, 0));
        c.drawText(text, x, y, paint);
        paint.setShadowLayer(0, 0, 0, 0); // Kapat
    }

    public static ScorePopup createCollect(float x, float y, int score, int combo) {
        String txt;
        int col;
        float cBonus = 1f;
        if (combo > 1) {
            txt = "+" + score + " x" + combo;
            col = combo >= 5 ? 0xFFFF6D00 : 0xFFFFD740;
            cBonus = 1f + (Math.min(combo, 10) * 0.05f); // Her comboda %5 daha büyük
        } else {
            txt = "+" + score;
            col = 0xFFFFD740;
        }
        return new ScorePopup(x, y - 30, txt, col, cBonus);
    }

    public static ScorePopup createNearMiss(float x, float y) {
        return new ScorePopup(x, y - 50, "NEAR MISS! +" + Constants.NEAR_MISS_BONUS, 0xFF00E5FF, 1.2f);
    }

    public static ScorePopup createPowerUp(float x, float y, int type) {
        String name = PowerUp.getName(type);
        int col = PowerUp.getColor(type);
        return new ScorePopup(x, y - 40, name + "!", col, 1.4f);
    }
}
