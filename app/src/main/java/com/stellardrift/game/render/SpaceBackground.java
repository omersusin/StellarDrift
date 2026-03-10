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

    // Speed lines
    private float[][] speedLines;
    private int speedLineCount;
    private Paint speedLinePaint;

    // Tempo
    private float tempoGlow;
    private int tempoPhase;

    private static final int MAX_SPEED_LINES = 30;
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
        speedLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        speedLinePaint.setStrokeCap(Paint.Cap.ROUND);

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

        speedLines = new float[MAX_SPEED_LINES][5];
        speedLineCount = 0;
        twinklePhase = 0;
        tempoGlow = 0;
        tempoPhase = Constants.TEMPO_CALM;
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
        update(difficulty, Constants.TEMPO_CALM);
    }

    public void update(float difficulty, int tempo) {
        twinklePhase += 0.03f;
        tempoPhase = tempo;

        float speedFactor = tempo == Constants.TEMPO_PRESSURE ? 1.3f : 1.0f;
        moveLayer(starsL1, Constants.BG_SPEED_L1 * difficulty * speedFactor);
        moveLayer(starsL2, Constants.BG_SPEED_L2 * difficulty * speedFactor);
        moveLayer(starsL3, Constants.BG_SPEED_L3 * difficulty * speedFactor);

        for (float[] n : nebulas) {
            n[1] += 0.15f * difficulty;
            if (n[1] > screenH + n[2]) {
                n[1] = -n[2];
                n[0] = (float)(Math.random() * screenW);
            }
        }

        // Tempo glow
        if (tempo == Constants.TEMPO_REWARD) {
            tempoGlow = Math.min(1f, tempoGlow + 0.02f);
        } else if (tempo == Constants.TEMPO_PRESSURE) {
            tempoGlow = Math.min(0.5f, tempoGlow + 0.01f);
        } else {
            tempoGlow = Math.max(0, tempoGlow - 0.02f);
        }

        updateSpeedLines(difficulty);
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

    // ===== SPEED LINES =====
    private void updateSpeedLines(float difficulty) {
        float intensity = Math.max(0, (difficulty - 1.3f) / (Constants.MAX_DIFFICULTY - 1.3f));
        if (tempoPhase == Constants.TEMPO_PRESSURE) intensity = Math.max(intensity, 0.5f);

        // Spawn
        if (intensity > 0 && Math.random() < intensity * 0.35 && speedLineCount < MAX_SPEED_LINES) {
            float lx;
            if (Math.random() < 0.5) {
                lx = (float)(Math.random() * screenW * 0.2f);
            } else {
                lx = screenW * 0.8f + (float)(Math.random() * screenW * 0.2f);
            }
            float len = 30 + (float)(Math.random() * 80 * intensity);
            float alpha = 0.15f + (float)(Math.random() * 0.25f * intensity);
            float spd = 12 + (float)(Math.random() * 18 * intensity);

            speedLines[speedLineCount][0] = lx;
            speedLines[speedLineCount][1] = -len;
            speedLines[speedLineCount][2] = len;
            speedLines[speedLineCount][3] = alpha;
            speedLines[speedLineCount][4] = spd;
            speedLineCount++;
        }

        // Move + remove
        int write = 0;
        for (int i = 0; i < speedLineCount; i++) {
            speedLines[i][1] += speedLines[i][4];
            if (speedLines[i][1] < screenH + speedLines[i][2]) {
                if (write != i) {
                    System.arraycopy(speedLines[i], 0, speedLines[write], 0, 5);
                }
                write++;
            }
        }
        speedLineCount = write;
    }

    public void render(Canvas c) {
        for (float[] n : nebulas) renderNebula(c, n);
        renderStarLayer(c, starsL1, 0.4f);
        renderStarLayer(c, starsL2, 0.7f);
        renderStarLayer(c, starsL3, 1.0f);
        renderSpeedLines(c);
    }

    private void renderNebula(Canvas c, float[] n) {
        int idx = 0;
        for (int i = 0; i < nebulas.length; i++)
            if (nebulas[i] == n) { idx = i; break; }

        int col = nebulaColors[idx];
        int r = Color.red(col), g = Color.green(col), b = Color.blue(col);

        int baseAlpha = 18;
        if (tempoPhase == Constants.TEMPO_REWARD)
            baseAlpha = (int)(18 + tempoGlow * 12);

        nebulaPaint.setShader(new RadialGradient(
            n[0], n[1], n[2],
            Color.argb(baseAlpha, r, g, b),
            Color.argb(0, r, g, b),
            Shader.TileMode.CLAMP));
        c.drawCircle(n[0], n[1], n[2], nebulaPaint);

        nebulaPaint.setShader(new RadialGradient(
            n[0], n[1], n[2] * 0.4f,
            Color.argb(baseAlpha + 7, r, g, b),
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

    private void renderSpeedLines(Canvas c) {
        for (int i = 0; i < speedLineCount; i++) {
            float lx = speedLines[i][0];
            float ly = speedLines[i][1];
            float len = speedLines[i][2];
            float al = speedLines[i][3];

            speedLinePaint.setColor(Color.argb((int)(al * 255), 200, 220, 255));
            speedLinePaint.setStrokeWidth(1.5f);
            c.drawLine(lx, ly, lx, ly + len, speedLinePaint);
        }
    }
}
