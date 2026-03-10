package com.stellardrift.game.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import com.stellardrift.game.util.Constants;
import com.stellardrift.game.util.SettingsManager;

public class UIOverlay {

    private int sw, sh;
    private Paint titlePaint, subtitlePaint, scorePaint, smallPaint;
    private Paint btnPaint, btnTextPaint, btnOutlinePaint;
    private Paint dimPaint, accentPaint, labelPaint, valuePaint;
    private Paint hudScorePaint, hudLabelPaint, diffBarBg, diffBarFill;

    private RectF playBtn, settingsBtn, backBtn;
    private RectF diffBtn, soundBtn, vibBtn;

    private float pulse;

    private static final int CYAN = 0xFF00E5FF;
    private static final int PURPLE = 0xFF7C4DFF;
    private static final int GOLD = 0xFFFFD740;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DIM = 0xCC050510;

    public UIOverlay(int sw, int sh) {
        this.sw = sw;
        this.sh = sh;
        pulse = 0;

        titlePaint = makePaint(CYAN, sw * 0.1f, Paint.Align.CENTER, true);
        subtitlePaint = makePaint(WHITE, sw * 0.035f, Paint.Align.CENTER, false);
        subtitlePaint.setAlpha(180);
        scorePaint = makePaint(WHITE, sw * 0.065f, Paint.Align.CENTER, true);
        smallPaint = makePaint(0xFFB0BEC5, sw * 0.03f, Paint.Align.CENTER, false);

        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnPaint.setStyle(Paint.Style.FILL);
        btnPaint.setColor(0x33FFFFFF);

        btnOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnOutlinePaint.setStyle(Paint.Style.STROKE);
        btnOutlinePaint.setStrokeWidth(2f);
        btnOutlinePaint.setColor(CYAN);
        btnOutlinePaint.setAlpha(120);

        btnTextPaint = makePaint(WHITE, sw * 0.04f, Paint.Align.CENTER, true);

        dimPaint = new Paint();
        dimPaint.setColor(DIM);
        dimPaint.setStyle(Paint.Style.FILL);

        accentPaint = makePaint(CYAN, sw * 0.045f, Paint.Align.CENTER, true);
        labelPaint = makePaint(0xFFB0BEC5, sw * 0.032f, Paint.Align.LEFT, false);
        valuePaint = makePaint(WHITE, sw * 0.035f, Paint.Align.RIGHT, true);

        hudScorePaint = makePaint(WHITE, sw * 0.055f, Paint.Align.LEFT, true);
        hudLabelPaint = makePaint(0x99FFFFFF, sw * 0.022f, Paint.Align.LEFT, false);

        diffBarBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        diffBarBg.setColor(0x33FFFFFF);
        diffBarFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        diffBarFill.setColor(CYAN);

        float cx = sw / 2f;
        float bw = sw * 0.55f, bh = sh * 0.065f;
        playBtn = new RectF(cx - bw/2, sh * 0.52f, cx + bw/2, sh * 0.52f + bh);
        settingsBtn = new RectF(cx - bw/2, sh * 0.61f, cx + bw/2, sh * 0.61f + bh);

        float sbw = sw * 0.7f, sbh = sh * 0.055f;
        float sy = sh * 0.35f;
        diffBtn = new RectF(cx - sbw/2, sy, cx + sbw/2, sy + sbh);
        soundBtn = new RectF(cx - sbw/2, sy + sbh * 1.6f, cx + sbw/2, sy + sbh * 2.6f);
        vibBtn = new RectF(cx - sbw/2, sy + sbh * 3.2f, cx + sbw/2, sy + sbh * 4.2f);
        backBtn = new RectF(cx - bw/2, sh * 0.72f, cx + bw/2, sh * 0.72f + bh);
    }

    private Paint makePaint(int color, float size, Paint.Align align, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTextSize(size);
        p.setTextAlign(align);
        if (bold) p.setTypeface(Typeface.DEFAULT_BOLD);
        return p;
    }

    public void render(Canvas c, int state, int score, int highScore,
                       float difficulty, SettingsManager settings) {
        pulse += 0.04f;
        switch (state) {
            case Constants.STATE_MENU:
                drawMenu(c, highScore);
                break;
            case Constants.STATE_PLAYING:
                drawHUD(c, score, difficulty);
                break;
            case Constants.STATE_GAME_OVER:
                drawGameOver(c, score, highScore);
                break;
            case Constants.STATE_SETTINGS:
                drawSettings(c, settings);
                break;
        }
    }

    private void drawMenu(Canvas c, int highScore) {
        float p = (float)(Math.sin(pulse) * 0.08 + 0.92);
        float cx = sw / 2f;

        titlePaint.setTextSize(sw * 0.1f * p);
        titlePaint.setShader(new LinearGradient(
            cx - sw * 0.3f, sh * 0.22f, cx + sw * 0.3f, sh * 0.22f,
            CYAN, PURPLE, Shader.TileMode.CLAMP));
        c.drawText("STELLAR", cx, sh * 0.22f, titlePaint);

        titlePaint.setTextSize(sw * 0.12f * p);
        c.drawText("DRIFT", cx, sh * 0.32f, titlePaint);
        titlePaint.setShader(null);

        for (int i = 0; i < 3; i++) {
            float sx = cx + (float)(Math.sin(pulse * 2 + i * 2.1) * sw * 0.35);
            float sy = sh * (0.15f + i * 0.08f);
            smallPaint.setAlpha(60);
            c.drawText("✦", sx, sy, smallPaint);
        }

        if (highScore > 0) {
            smallPaint.setAlpha(180);
            c.drawText("★ BEST: " + highScore, cx, sh * 0.42f, smallPaint);
        }

        drawButton(c, playBtn, "▶  PLAY", CYAN);
        drawButton(c, settingsBtn, "⚙  SETTINGS", PURPLE);

        float pa = (float)(Math.sin(pulse * 1.5) * 0.3 + 0.7);
        subtitlePaint.setAlpha((int)(150 * pa));
        c.drawText("Tap to navigate the stars", cx, sh * 0.85f, subtitlePaint);

        smallPaint.setAlpha(80);
        c.drawText("v1.0", cx, sh * 0.95f, smallPaint);
    }

    private void drawHUD(Canvas c, int score, float difficulty) {
        float pad = sw * 0.04f;

        hudLabelPaint.setAlpha(150);
        c.drawText("SCORE", pad, sh * 0.04f, hudLabelPaint);
        hudScorePaint.setColor(WHITE);
        c.drawText(String.valueOf(score), pad, sh * 0.075f, hudScorePaint);

        float barW = sw * 0.25f, barH = sh * 0.006f;
        float barX = sw - pad - barW, barY = sh * 0.04f;
        RectF bgR = new RectF(barX, barY, barX + barW, barY + barH);
        c.drawRoundRect(bgR, barH, barH, diffBarBg);

        float fill = Math.min(1f, (difficulty - 1f) / (Constants.MAX_DIFFICULTY - 1f));
        RectF fillR = new RectF(barX, barY, barX + barW * fill, barY + barH);
        int barCol = fill < 0.5f ? CYAN : (fill < 0.8f ? GOLD : 0xFFFF1744);
        diffBarFill.setColor(barCol);
        c.drawRoundRect(fillR, barH, barH, diffBarFill);

        hudLabelPaint.setTextAlign(Paint.Align.RIGHT);
        c.drawText("SPEED", sw - pad, sh * 0.065f, hudLabelPaint);
        hudLabelPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawGameOver(Canvas c, int score, int highScore) {
        c.drawRect(0, 0, sw, sh, dimPaint);
        float cx = sw / 2f;

        accentPaint.setColor(0xFFFF1744);
        accentPaint.setTextSize(sw * 0.08f);
        c.drawText("GAME OVER", cx, sh * 0.28f, accentPaint);

        scorePaint.setColor(WHITE);
        scorePaint.setTextSize(sw * 0.12f);
        c.drawText(String.valueOf(score), cx, sh * 0.42f, scorePaint);
        smallPaint.setAlpha(180);
        c.drawText("SCORE", cx, sh * 0.45f, smallPaint);

        if (score >= highScore && score > 0) {
            accentPaint.setColor(GOLD);
            accentPaint.setTextSize(sw * 0.04f);
            float np = (float)(Math.sin(pulse * 3) * 0.15 + 0.85);
            accentPaint.setAlpha((int)(255 * np));
            c.drawText("★ NEW BEST! ★", cx, sh * 0.52f, accentPaint);
        } else {
            smallPaint.setAlpha(140);
            c.drawText("BEST: " + highScore, cx, sh * 0.52f, smallPaint);
        }

        float p2 = (float)(Math.sin(pulse * 1.5) * 0.3 + 0.7);
        subtitlePaint.setAlpha((int)(180 * p2));
        c.drawText("Tap to continue", cx, sh * 0.68f, subtitlePaint);
    }

    private void drawSettings(Canvas c, SettingsManager settings) {
        c.drawRect(0, 0, sw, sh, dimPaint);
        float cx = sw / 2f;

        accentPaint.setColor(CYAN);
        accentPaint.setTextSize(sw * 0.06f);
        c.drawText("SETTINGS", cx, sh * 0.2f, accentPaint);

        drawSettingRow(c, diffBtn, "DIFFICULTY",
            settings.getDifficultyName(), settings.getDifficultyColor());
        drawSettingRow(c, soundBtn, "SOUND",
            settings.isSoundEnabled() ? "ON" : "OFF",
            settings.isSoundEnabled() ? 0xFF00E676 : 0xFFFF1744);
        drawSettingRow(c, vibBtn, "VIBRATION",
            settings.isVibrationEnabled() ? "ON" : "OFF",
            settings.isVibrationEnabled() ? 0xFF00E676 : 0xFFFF1744);

        drawButton(c, backBtn, "◀  BACK", PURPLE);
    }

    private void drawSettingRow(Canvas c, RectF rect, String label,
                                String value, int valueColor) {
        c.drawRoundRect(rect, rect.height() * 0.4f, rect.height() * 0.4f, btnPaint);
        c.drawRoundRect(rect, rect.height() * 0.4f, rect.height() * 0.4f, btnOutlinePaint);

        float cy = rect.centerY() + sw * 0.012f;
        labelPaint.setTextAlign(Paint.Align.LEFT);
        c.drawText(label, rect.left + sw * 0.05f, cy, labelPaint);

        valuePaint.setTextAlign(Paint.Align.RIGHT);
        valuePaint.setColor(valueColor);
        c.drawText(value, rect.right - sw * 0.05f, cy, valuePaint);
    }

    private void drawButton(Canvas c, RectF rect, String text, int accentColor) {
        float rad = rect.height() * 0.45f;
        c.drawRoundRect(rect, rad, rad, btnPaint);
        btnOutlinePaint.setColor(accentColor);
        btnOutlinePaint.setAlpha(140);
        c.drawRoundRect(rect, rad, rad, btnOutlinePaint);
        btnTextPaint.setColor(WHITE);
        c.drawText(text, rect.centerX(), rect.centerY() + sw * 0.014f, btnTextPaint);
    }

    // Hit detection
    public boolean isPlayHit(float x, float y) { return playBtn.contains(x, y); }
    public boolean isSettingsHit(float x, float y) { return settingsBtn.contains(x, y); }
    public boolean isBackHit(float x, float y) { return backBtn.contains(x, y); }
    public boolean isDiffHit(float x, float y) { return diffBtn.contains(x, y); }
    public boolean isSoundHit(float x, float y) { return soundBtn.contains(x, y); }
    public boolean isVibHit(float x, float y) { return vibBtn.contains(x, y); }
}
