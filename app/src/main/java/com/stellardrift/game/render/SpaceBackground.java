package com.stellardrift.game.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import com.stellardrift.game.util.Constants;

public class SpaceBackground {

    private int screenW, screenH;

    private float[][] starsL1, starsL2, starsL3;
    private float[][] nebulas;
    private int[] nebulaColors;

    private Paint starPaint, nebulaPaint;
    private float twinklePhase;

    private static final int[] STAR_COLORS = {
        0xFFFFFFFF, 0xFFCCE5FF, 0xFFFFE0B2, 0xFFD1C4E9
    };
    private static final int[] NEBULA_BASE = {
        0xFF7C4DFF, 0xFF00BCD4, 0xFFE91E63, 0xFF1A237E
    };

    public SpaceBackground(int sw, int sh) {
        screenW = sw; screenH = sh;
        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setStyle(Paint.Style.FILL);
        nebulaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nebulaPaint.setStyle(Paint.Style.FILL);

        starsL1 = genStars(Constants.BG_STARS_L1, 1.2f);
        starsL2 = genStars(Constants.BG_STARS_L2, 2.0f);
        starsL3 = genStars(Constants.BG_STARS_L3, 3.0f);

        int nc = Constants.BG_NEBULA_COUNT;
        nebulas = new float[nc][3];
        nebulaColors = new int[nc];
        for (int i = 0; i < nc; i++) {
            nebulas[i][0] = (float)(Math.random() * sw);
            nebulas[i][1] = (float)(Math.random() * sh);
            nebulas[i][2] = sw * (0.3f + (float)(Math.random() * 0.5));
            nebulaColors[i] = NEBULA_BASE[(int)(Math.random() * NEBULA_BASE.length)];
        }

        twinklePhase = 0;
    }

    private float[][] genStars(int count, float maxSize) {
        float[][] stars = new float[count][4];
        for (int i = 0; i < count; i++) {
            stars[i][0] = (float)(Math.random() * screenW);
            stars[i][1] = (float)(Math.random() * screenH);
            stars[i][2] = 0.5f + (float)(Math.random() * maxSize);
            stars[i][3] = (float)(Math.random() * Math.PI * 2);
        }
        return stars;
    }

    public void update(float difficulty) {
        twinklePhase += 0.03f;
        moveLayer(starsL1, Constants.BG_SPEED_L1 * difficulty);
        moveLayer(starsL2, Constants.BG_SPEED_L2 * difficulty);
        moveLayer(starsL3, Constants.BG_SPEED_L3 * difficulty);
        for (float[] n : nebulas) {
            n[1] += 0.15f * difficulty;
            if (n[1] > screenH + n[2]) {
                n[1] = -n[2];
                n[0] = (float)(Math.random() * screenW);
            }
        }
    }

    private void moveLayer(float[][] layer, float speed) {
        for (float[] s : layer) {
            s[1] += speed;
            if (s[1] > screenH + 5) {
                s[1] = -3;
                s[0] = (float)(Math.random() * screenW);
            }
        }
    }

    public void render(Canvas c) {
        for (float[] n : nebulas) renderNebula(c, n);
        renderStarLayer(c, starsL1, 0.4f);
        renderStarLayer(c, starsL2, 0.7f);
        renderStarLayer(c, starsL3, 1.0f);
    }

    private void renderNebula(Canvas c, float[] n) {
        int idx = 0;
        for (int i = 0; i < nebulas.length; i++)
            if (nebulas[i] == n) { idx = i; break; }

        int col = nebulaColors[idx];
        int r = Color.red(col), g = Color.green(col), b = Color.blue(col);

        nebulaPaint.setShader(new RadialGradient(
            n[0], n[1], n[2],
            Color.argb(18, r, g, b),
            Color.argb(0, r, g, b),
            Shader.TileMode.CLAMP));
        c.drawCircle(n[0], n[1], n[2], nebulaPaint);

        nebulaPaint.setShader(new RadialGradient(
            n[0], n[1], n[2] * 0.4f,
            Color.argb(25, r, g, b),
            Color.argb(0, r, g, b),
            Shader.TileMode.CLAMP));
        c.drawCircle(n[0], n[1], n[2] * 0.4f, nebulaPaint);
    }

    private void renderStarLayer(Canvas c, float[][] layer, float brightness) {
        for (float[] s : layer) {
            float tw = (float)(Math.sin(twinklePhase + s[3]) * 0.3 + 0.7);
            int alpha = (int)(255 * brightness * tw);
            int col = STAR_COLORS[(int)(s[3] * 10) % STAR_COLORS.length];
            starPaint.setColor(col);

            starPaint.setAlpha(Math.max(15, alpha / 4));
            c.drawCircle(s[0], s[1], s[2] * 2.5f, starPaint);

            starPaint.setAlpha(Math.max(30, alpha));
            c.drawCircle(s[0], s[1], s[2], starPaint);

            if (s[2] > 1.8f) {
                starPaint.setAlpha(Math.max(10, alpha / 2));
                c.drawCircle(s[0], s[1], s[2] * 0.4f, starPaint);
            }
        }
    }
}
