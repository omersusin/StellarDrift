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
import com.stellardrift.game.world.GameWorld;
import com.stellardrift.game.world.PowerUp;

public class UIOverlay {

    private int sw, sh;
    private Paint titlePaint, subtitlePaint, scorePaint, smallPaint;
    private Paint btnPaint, btnTextPaint, btnOutlinePaint;
    private Paint dimPaint, accentPaint, labelPaint, valuePaint;
    private Paint hudScorePaint, hudLabelPaint, diffBarBg, diffBarFill;
    private Paint comboPaint, powerBarBg, powerBarFill, statPaint;

    private RectF playBtn, settingsBtn, backBtn;
    private RectF diffBtn, soundBtn, vibBtn;
    private RectF restartBtn;

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
        btnPaint.setStyle(Paint.Style.FILL); btnPaint.setColor(0x33FFFFFF);
        btnOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnOutlinePaint.setStyle(Paint.Style.STROKE);
        btnOutlinePaint.setStrokeWidth(2f); btnOutlinePaint.setColor(CYAN);
        btnOutlinePaint.setAlpha(120);
        btnTextPaint = makePaint(WHITE, sw * 0.04f, Paint.Align.CENTER, true);

        dimPaint = new Paint(); dimPaint.setColor(DIM); dimPaint.setStyle(Paint.Style.FILL);
        accentPaint = makePaint(CYAN, sw * 0.045f, Paint.Align.CENTER, true);
        labelPaint = makePaint(0xFFB0BEC5, sw * 0.032f, Paint.Align.LEFT, false);
        valuePaint = makePaint(WHITE, sw * 0.035f, Paint.Align.RIGHT, true);

        hudScorePaint = makePaint(WHITE, sw * 0.055f, Paint.Align.LEFT, true);
        hudLabelPaint = makePaint(0x99FFFFFF, sw * 0.022f, Paint.Align.LEFT, false);

        comboPaint = makePaint(GOLD, sw * 0.05f, Paint.Align.CENTER, true);

        diffBarBg = new Paint(Paint.ANTI_ALIAS_FLAG); diffBarBg.setColor(0x33FFFFFF);
        diffBarFill = new Paint(Paint.ANTI_ALIAS_FLAG); diffBarFill.setColor(CYAN);

        powerBarBg = new Paint(Paint.ANTI_ALIAS_FLAG); powerBarBg.setColor(0x33FFFFFF);
        powerBarFill = new Paint(Paint.ANTI_ALIAS_FLAG);

        statPaint = makePaint(0xFFB0BEC5, sw * 0.03f, Paint.Align.LEFT, false);

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

        restartBtn = new RectF(cx - bw/2, sh * 0.74f, cx + bw/2, sh * 0.74f + bh);
    }

    private Paint makePaint(int color, float size, Paint.Align align, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color); p.setTextSize(size); p.setTextAlign(align);
        if (bold) p.setTypeface(Typeface.DEFAULT_BOLD);
        return p;
    }

    public void render(Canvas c, int state, int score, int highScore,
                       float difficulty, SettingsManager settings) {
        pulse += 0.04f;
        switch (state) {
            case Constants.STATE_MENU: drawMenu(c, highScore); break;
            case Constants.STATE_PLAYING: drawHUD(c, score, difficulty, null); break;
            case Constants.STATE_GAME_OVER: drawGameOver(c, score, highScore, null); break;
            case Constants.STATE_SETTINGS: drawSettings(c, settings); break;
        }
    }

    public void renderFull(Canvas c, GameWorld world) {
        pulse += 0.04f;
        int state = world.getState();
        switch (state) {
            case Constants.STATE_MENU:
                drawMenu(c, world.getHighScore());
                break;
            case Constants.STATE_PLAYING:
                drawHUD(c, world.getScore(), world.getDifficulty(), world);
                break;
            case Constants.STATE_GAME_OVER:
                drawGameOver(c, world.getScore(), world.getHighScore(), world);
                break;
            case Constants.STATE_SETTINGS:
                drawSettings(c, world.getSettings());
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
        c.drawText("Touch & drag to navigate the stars", cx, sh * 0.85f, subtitlePaint);
        smallPaint.setAlpha(80);
        c.drawText("v1.1", cx, sh * 0.95f, smallPaint);
    }

    private void drawHUD(Canvas c, int score, float difficulty, GameWorld world) {
        float pad = sw * 0.04f;

        hudLabelPaint.setAlpha(150);
        hudLabelPaint.setTextAlign(Paint.Align.LEFT);
        c.drawText("SCORE", pad, sh * 0.04f, hudLabelPaint);
        hudScorePaint.setColor(WHITE);
        c.drawText(String.valueOf(score), pad, sh * 0.075f, hudScorePaint);

        // Speed bar
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

        if (world == null) return;

        // Combo display
        int combo = world.getCombo();
        if (combo > 1) {
            float cp = (float)(Math.sin(pulse * 4) * 0.1 + 0.9);
            comboPaint.setTextSize(sw * (0.04f + combo * 0.003f) * cp);
            comboPaint.setColor(combo >= 10 ? 0xFFFF6D00 : combo >= 5 ? GOLD : WHITE);
            comboPaint.setAlpha(220);
            c.drawText("x" + combo + " COMBO", sw / 2f, sh * 0.13f, comboPaint);
        }

        // Power-up bars
        float pbY = sh * 0.92f;
        float pbH = sh * 0.012f;
        float pbW = sw * 0.25f;
        int pbCount = 0;

        if (world.isMagnetActive()) {
            drawPowerBar(c, pad, pbY - pbCount * (pbH + 8), pbW, pbH,
                "MAGNET", world.getMagnetTimer(), Constants.POWERUP_DURATION,
                PowerUp.getColor(Constants.POWERUP_MAGNET));
            pbCount++;
        }
        if (world.isSlowmoActive()) {
            drawPowerBar(c, pad, pbY - pbCount * (pbH + 8), pbW, pbH,
                "SLOW-MO", world.getSlowmoTimer(), Constants.POWERUP_DURATION,
                PowerUp.getColor(Constants.POWERUP_SLOWMO));
            pbCount++;
        }
        if (world.isDoubleActive()) {
            drawPowerBar(c, pad, pbY - pbCount * (pbH + 8), pbW, pbH,
                "DOUBLE", world.getDoubleTimer(), Constants.POWERUP_DURATION,
                PowerUp.getColor(Constants.POWERUP_DOUBLE));
            pbCount++;
        }
    }

    private void drawPowerBar(Canvas c, float x, float y, float w, float h,
                              String name, int timer, int maxTimer, int color) {
        hudLabelPaint.setTextAlign(Paint.Align.LEFT);
        hudLabelPaint.setAlpha(200); hudLabelPaint.setColor(color);
        c.drawText(name, x, y - 4, hudLabelPaint);
        hudLabelPaint.setColor(0x99FFFFFF);

        RectF bg = new RectF(x, y, x + w, y + h);
        c.drawRoundRect(bg, h, h, powerBarBg);

        float frac = (float) timer / maxTimer;
        RectF fl = new RectF(x, y, x + w * frac, y + h);
        powerBarFill.setColor(color);
        if (frac < 0.25f) powerBarFill.setAlpha((int)(200 * (Math.sin(pulse * 8) * 0.3 + 0.7)));
        else powerBarFill.setAlpha(200);
        c.drawRoundRect(fl, h, h, powerBarFill);
    }

    private void drawGameOver(Canvas c, int score, int highScore, GameWorld world) {
        c.drawRect(0, 0, sw, sh, dimPaint);
        float cx = sw / 2f;

        accentPaint.setColor(0xFFFF1744);
        accentPaint.setTextSize(sw * 0.08f);
        c.drawText("GAME OVER", cx, sh * 0.2f, accentPaint);

        scorePaint.setColor(WHITE);
        scorePaint.setTextSize(sw * 0.12f);
        c.drawText(String.valueOf(score), cx, sh * 0.32f, scorePaint);
        smallPaint.setAlpha(180);
        c.drawText("SCORE", cx, sh * 0.35f, smallPaint);

        if (score >= highScore && score > 0) {
            accentPaint.setColor(GOLD);
            accentPaint.setTextSize(sw * 0.04f);
            float np = (float)(Math.sin(pulse * 3) * 0.15 + 0.85);
            accentPaint.setAlpha((int)(255 * np));
            c.drawText("★ NEW BEST! ★", cx, sh * 0.41f, accentPaint);
        } else {
            smallPaint.setAlpha(140);
            c.drawText("BEST: " + highScore, cx, sh * 0.41f, smallPaint);
        }

        // Stats
        if (world != null) {
            float statX = sw * 0.2f;
            float statY = sh * 0.48f;
            float gap = sh * 0.04f;

            statPaint.setColor(0xFFB0BEC5); statPaint.setTextAlign(Paint.Align.LEFT);
            Paint sv = new Paint(statPaint);
            sv.setTextAlign(Paint.Align.RIGHT); sv.setColor(WHITE);
            sv.setTypeface(Typeface.DEFAULT_BOLD);

            float rightX = sw * 0.8f;

            c.drawText("Orbs Collected", statX, statY, statPaint);
            c.drawText(String.valueOf(world.getOrbsCollected()), rightX, statY, sv);

            c.drawText("Near Misses", statX, statY + gap, statPaint);
            c.drawText(String.valueOf(world.getNearMissCount()), rightX, statY + gap, sv);

            c.drawText("Max Combo", statX, statY + gap * 2, statPaint);
            sv.setColor(world.getMaxCombo() >= 5 ? GOLD : WHITE);
            c.drawText("x" + world.getMaxCombo(), rightX, statY + gap * 2, sv);

            c.drawText("Survival Time", statX, statY + gap * 3, statPaint);
            sv.setColor(WHITE);
            c.drawText(world.getSurvivalTime() + "s", rightX, statY + gap * 3, sv);
        }

        // Restart button
        drawButton(c, restartBtn, "▶  PLAY AGAIN", CYAN);

        float p2 = (float)(Math.sin(pulse * 1.5) * 0.3 + 0.7);
        subtitlePaint.setAlpha((int)(120 * p2));
        c.drawText("or tap anywhere to go to menu", cx, sh * 0.87f, subtitlePaint);
    }

    private void drawSettings(Canvas c, SettingsManager settings) {
        c.drawRect(0, 0, sw, sh, dimPaint);
        float cx = sw / 2f;
        accentPaint.setColor(CYAN); accentPaint.setTextSize(sw * 0.06f);
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
        btnOutlinePaint.setColor(accentColor); btnOutlinePaint.setAlpha(140);
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
    public boolean isRestartHit(float x, float y) { return restartBtn.contains(x, y); }
}
