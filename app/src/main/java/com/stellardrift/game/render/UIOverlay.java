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
    private Paint milestonePaint, tempoPaint, riskPaint;

    private RectF playBtn, settingsBtn, backBtn;
    private RectF diffBtn, speedBtn, soundBtn, vibBtn;
    private RectF restartBtn;

    private float pulse;
    private long gameOverTime;
    private static final long BUTTON_DELAY = 1200;

    private static final int CYAN = 0xFF00E5FF;
    private static final int PURPLE = 0xFF7C4DFF;
    private static final int GOLD = 0xFFFFD740;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DIM = 0xCC050510;

    public UIOverlay(int sw, int sh) {
        this.sw = sw; this.sh = sh; pulse = 0; gameOverTime = 0;

        titlePaint = makePaint(CYAN, sw * 0.1f, Paint.Align.CENTER, true);
        subtitlePaint = makePaint(WHITE, sw * 0.035f, Paint.Align.CENTER, false); subtitlePaint.setAlpha(180);
        scorePaint = makePaint(WHITE, sw * 0.065f, Paint.Align.CENTER, true);
        smallPaint = makePaint(0xFFB0BEC5, sw * 0.03f, Paint.Align.CENTER, false);
        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG); btnPaint.setStyle(Paint.Style.FILL); btnPaint.setColor(0x33FFFFFF);
        btnOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG); btnOutlinePaint.setStyle(Paint.Style.STROKE); btnOutlinePaint.setStrokeWidth(2f); btnOutlinePaint.setColor(CYAN); btnOutlinePaint.setAlpha(120);
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
        milestonePaint = makePaint(GOLD, sw * 0.06f, Paint.Align.CENTER, true);
        tempoPaint = makePaint(WHITE, sw * 0.02f, Paint.Align.RIGHT, false);
        riskPaint = makePaint(GOLD, sw * 0.025f, Paint.Align.CENTER, true);

        float cx = sw / 2f, bw = sw * 0.55f, bh = sh * 0.065f;
        playBtn = new RectF(cx - bw/2, sh * 0.52f, cx + bw/2, sh * 0.52f + bh);
        settingsBtn = new RectF(cx - bw/2, sh * 0.61f, cx + bw/2, sh * 0.61f + bh);

        float sbw = sw * 0.7f, sbh = sh * 0.05f;
        float sy = sh * 0.28f;
        float gap = sbh * 1.4f;
        diffBtn = new RectF(cx - sbw/2, sy, cx + sbw/2, sy + sbh);
        speedBtn = new RectF(cx - sbw/2, sy + gap, cx + sbw/2, sy + gap + sbh);
        soundBtn = new RectF(cx - sbw/2, sy + gap * 2, cx + sbw/2, sy + gap * 2 + sbh);
        vibBtn = new RectF(cx - sbw/2, sy + gap * 3, cx + sbw/2, sy + gap * 3 + sbh);
        backBtn = new RectF(cx - bw/2, sh * 0.78f, cx + bw/2, sh * 0.78f + bh);
        restartBtn = new RectF(cx - bw/2, sh * 0.74f, cx + bw/2, sh * 0.74f + bh);
    }

    private Paint makePaint(int color, float size, Paint.Align align, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(color); p.setTextSize(size); p.setTextAlign(align);
        if (bold) p.setTypeface(Typeface.DEFAULT_BOLD); return p;
    }

    public void renderFull(Canvas c, GameWorld world) {
        pulse += 0.04f;
        switch (world.getState()) {
            case Constants.STATE_MENU: drawMenu(c, world.getHighScore()); break;
            case Constants.STATE_PLAYING: drawHUD(c, world); drawMilestone(c, world); break;
            case Constants.STATE_GAME_OVER: drawGameOver(c, world); break;
            case Constants.STATE_SETTINGS: drawSettings(c, world.getSettings()); break;
        }
    }

    private void drawMenu(Canvas c, int highScore) {
        float p = (float)(Math.sin(pulse) * 0.08 + 0.92); float cx = sw / 2f;
        titlePaint.setTextSize(sw * 0.1f * p);
        titlePaint.setShader(new LinearGradient(cx - sw*0.3f, sh*0.22f, cx + sw*0.3f, sh*0.22f, CYAN, PURPLE, Shader.TileMode.CLAMP));
        c.drawText("STELLAR", cx, sh * 0.22f, titlePaint);
        titlePaint.setTextSize(sw * 0.12f * p);
        c.drawText("DRIFT", cx, sh * 0.32f, titlePaint);
        titlePaint.setShader(null);
        if (highScore > 0) { smallPaint.setAlpha(180); c.drawText("★ BEST: " + highScore, cx, sh * 0.42f, smallPaint); }
        drawButton(c, playBtn, "▶  PLAY", CYAN);
        drawButton(c, settingsBtn, "⚙  SETTINGS", PURPLE);
        float pa = (float)(Math.sin(pulse * 1.5) * 0.3 + 0.7);
        subtitlePaint.setAlpha((int)(150 * pa));
        c.drawText("Use joystick to fly freely", cx, sh * 0.85f, subtitlePaint);
        smallPaint.setAlpha(80); c.drawText("v2.0", cx, sh * 0.95f, smallPaint);
    }

    private void drawHUD(Canvas c, GameWorld world) {
        float pad = sw * 0.04f;
        hudLabelPaint.setAlpha(150); hudLabelPaint.setTextAlign(Paint.Align.LEFT);
        c.drawText("SCORE", pad, sh * 0.04f, hudLabelPaint);
        hudScorePaint.setColor(WHITE);
        c.drawText(String.valueOf(world.getScore()), pad, sh * 0.075f, hudScorePaint);

        float barW = sw * 0.25f, barH = sh * 0.006f;
        float barX = sw - pad - barW, barY = sh * 0.04f;
        c.drawRoundRect(new RectF(barX, barY, barX + barW, barY + barH), barH, barH, diffBarBg);
        float fill = Math.min(1f, (world.getDifficulty() - 1f) / (Constants.MAX_DIFFICULTY - 1f));
        diffBarFill.setColor(fill < 0.5f ? CYAN : fill < 0.8f ? GOLD : 0xFFFF1744);
        c.drawRoundRect(new RectF(barX, barY, barX + barW * fill, barY + barH), barH, barH, diffBarFill);
        hudLabelPaint.setTextAlign(Paint.Align.RIGHT);
        c.drawText("SPEED", sw - pad, sh * 0.065f, hudLabelPaint);

        int tempo = world.getTempoPhase();
        if (tempo != Constants.TEMPO_CALM) {
            String tl = tempo == Constants.TEMPO_PRESSURE ? "▲ PRESSURE" : "★ REWARD";
            int tc = tempo == Constants.TEMPO_PRESSURE ? 0xFFFF1744 : GOLD;
            tempoPaint.setColor(tc); tempoPaint.setAlpha(180); tempoPaint.setTextAlign(Paint.Align.RIGHT);
            c.drawText(tl, sw - pad, sh * 0.095f, tempoPaint);
        }

        int combo = world.getCombo();
        if (combo > 1) {
            float cp = (float)(Math.sin(pulse * 4) * 0.1 + 0.9);
            comboPaint.setTextSize(sw * (0.04f + combo * 0.003f) * cp);
            comboPaint.setColor(combo >= 10 ? 0xFFFF6D00 : combo >= 5 ? GOLD : WHITE);
            comboPaint.setAlpha(220);
            c.drawText("x" + combo + " COMBO", sw / 2f, sh * 0.13f, comboPaint);
        }

        if (world.isRiskWindowActive()) {
            riskPaint.setAlpha((int)(220 * (Math.sin(pulse * 5) * 0.15 + 0.85)));
            c.drawText("⚡ RISK x1.5 ⚡", sw / 2f, sh * 0.16f, riskPaint);
        }

        float pbY = sh * 0.92f, pbH = sh * 0.012f, pbW = sw * 0.25f; int pbc = 0;
        if (world.isMagnetActive()) { drawPowerBar(c, pad, pbY - pbc*(pbH+8), pbW, pbH, "MAGNET", world.getMagnetTimer(), Constants.POWERUP_DURATION, PowerUp.getColor(Constants.POWERUP_MAGNET)); pbc++; }
        if (world.isSlowmoActive()) { drawPowerBar(c, pad, pbY - pbc*(pbH+8), pbW, pbH, "SLOW-MO", world.getSlowmoTimer(), Constants.POWERUP_DURATION, PowerUp.getColor(Constants.POWERUP_SLOWMO)); pbc++; }
        if (world.isDoubleActive()) { drawPowerBar(c, pad, pbY - pbc*(pbH+8), pbW, pbH, "DOUBLE", world.getDoubleTimer(), Constants.POWERUP_DURATION, PowerUp.getColor(Constants.POWERUP_DOUBLE)); pbc++; }
    }

    private void drawPowerBar(Canvas c, float x, float y, float w, float h, String name, int timer, int max, int color) {
        hudLabelPaint.setTextAlign(Paint.Align.LEFT); hudLabelPaint.setAlpha(200); hudLabelPaint.setColor(color);
        c.drawText(name, x, y - 4, hudLabelPaint); hudLabelPaint.setColor(0x99FFFFFF);
        c.drawRoundRect(new RectF(x, y, x + w, y + h), h, h, powerBarBg);
        float f = (float) timer / max; powerBarFill.setColor(color);
        powerBarFill.setAlpha(f < 0.25f ? (int)(200 * (Math.sin(pulse * 8) * 0.3 + 0.7)) : 200);
        c.drawRoundRect(new RectF(x, y, x + w * f, y + h), h, h, powerBarFill);
    }

    private void drawMilestone(Canvas c, GameWorld world) {
        if (world.getMilestoneTimer() <= 0 || world.getMilestoneText() == null) return;
        float p = world.getMilestoneTimer() / 90f;
        float sc = p > 0.8f ? 1f + (p - 0.8f) / 0.2f * 0.5f : 1f;
        milestonePaint.setTextSize(sw * 0.06f * sc); milestonePaint.setAlpha((int)(255 * Math.min(1f, p * 2)));
        c.drawText(world.getMilestoneText(), sw / 2f, sh * 0.35f, milestonePaint);
    }

    private void drawGameOver(Canvas c, GameWorld world) {
        int score = world.getScore(), hs = world.getHighScore();
        if (gameOverTime == 0) gameOverTime = System.currentTimeMillis();
        long el = System.currentTimeMillis() - gameOverTime;
        c.drawRect(0, 0, sw, sh, dimPaint);
        float cx = sw / 2f;

        accentPaint.setColor(0xFFFF1744); accentPaint.setTextSize(sw * 0.08f); accentPaint.setAlpha(255);
        c.drawText("GAME OVER", cx, sh * 0.2f, accentPaint);

        float cp = Math.min(1f, el / 800f); cp = 1f - (1f - cp) * (1f - cp);
        scorePaint.setColor(WHITE); scorePaint.setTextSize(sw * 0.12f); scorePaint.setAlpha(255);
        c.drawText(String.valueOf((int)(score * cp)), cx, sh * 0.32f, scorePaint);
        smallPaint.setAlpha(180); c.drawText("SCORE", cx, sh * 0.35f, smallPaint);

        if (el > 800) {
            if (score >= hs && score > 0) {
                accentPaint.setColor(GOLD); accentPaint.setTextSize(sw * 0.04f);
                accentPaint.setAlpha((int)(255 * (Math.sin(pulse * 3) * 0.15 + 0.85)));
                c.drawText("★ NEW BEST! ★", cx, sh * 0.41f, accentPaint);
            } else { smallPaint.setAlpha(140); c.drawText("BEST: " + hs, cx, sh * 0.41f, smallPaint); }
        }

        if (el > 600) {
            float a = Math.min(1f, (el - 600) / 400f);
            drawStats(c, world, a);
        }

        if (el > BUTTON_DELAY) {
            float ba = Math.min(1f, (el - BUTTON_DELAY) / 500f);
            btnPaint.setAlpha((int)(0x33 * ba)); btnOutlinePaint.setAlpha((int)(140 * ba)); btnTextPaint.setAlpha((int)(255 * ba));
            drawButton(c, restartBtn, "▶  PLAY AGAIN", CYAN);
            btnPaint.setAlpha(0x33); btnOutlinePaint.setAlpha(120); btnTextPaint.setAlpha(255);
            subtitlePaint.setAlpha((int)(80 * ba));
            c.drawText("or tap anywhere for menu", cx, sh * 0.87f, subtitlePaint);
        }
    }

    private void drawStats(Canvas c, GameWorld w, float a) {
        float sx = sw*0.2f, sy = sh*0.48f, g = sh*0.04f, rx = sw*0.8f;
        int sa = (int)(200*a), va = (int)(255*a);
        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT);
        c.drawText("Orbs Collected", sx, sy, statPaint);
        statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD);
        c.drawText(String.valueOf(w.getOrbsCollected()), rx, sy, statPaint);

        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT);
        c.drawText("Near Misses", sx, sy+g, statPaint);
        statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD);
        c.drawText(String.valueOf(w.getNearMissCount()), rx, sy+g, statPaint);

        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT);
        c.drawText("Max Combo", sx, sy+g*2, statPaint);
        statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(w.getMaxCombo()>=5?GOLD:WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD);
        c.drawText("x"+w.getMaxCombo(), rx, sy+g*2, statPaint);

        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT);
        c.drawText("Survival Time", sx, sy+g*3, statPaint);
        statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD);
        c.drawText(w.getSurvivalTime()+"s", rx, sy+g*3, statPaint);
    }

    public void resetGameOver() { gameOverTime = 0; }

    private void drawSettings(Canvas c, SettingsManager s) {
        c.drawRect(0, 0, sw, sh, dimPaint);
        accentPaint.setColor(CYAN); accentPaint.setTextSize(sw * 0.06f); accentPaint.setAlpha(255);
        c.drawText("SETTINGS", sw / 2f, sh * 0.2f, accentPaint);
        drawSettingRow(c, diffBtn, "DIFFICULTY", s.getDifficultyName(), s.getDifficultyColor());
        drawSettingRow(c, speedBtn, "GAME SPEED", s.getGameSpeedName(), s.getGameSpeedColor());
        drawSettingRow(c, soundBtn, "SOUND", s.isSoundEnabled() ? "ON" : "OFF", s.isSoundEnabled() ? 0xFF00E676 : 0xFFFF1744);
        drawSettingRow(c, vibBtn, "VIBRATION", s.isVibrationEnabled() ? "ON" : "OFF", s.isVibrationEnabled() ? 0xFF00E676 : 0xFFFF1744);
        drawButton(c, backBtn, "◀  BACK", PURPLE);
    }

    private void drawSettingRow(Canvas c, RectF r, String label, String val, int vc) {
        c.drawRoundRect(r, r.height()*0.4f, r.height()*0.4f, btnPaint);
        c.drawRoundRect(r, r.height()*0.4f, r.height()*0.4f, btnOutlinePaint);
        float cy = r.centerY() + sw * 0.012f;
        labelPaint.setTextAlign(Paint.Align.LEFT); labelPaint.setAlpha(200);
        c.drawText(label, r.left + sw*0.05f, cy, labelPaint);
        valuePaint.setTextAlign(Paint.Align.RIGHT); valuePaint.setColor(vc); valuePaint.setAlpha(255);
        c.drawText(val, r.right - sw*0.05f, cy, valuePaint);
    }

    private void drawButton(Canvas c, RectF r, String text, int ac) {
        float rad = r.height() * 0.45f;
        c.drawRoundRect(r, rad, rad, btnPaint);
        btnOutlinePaint.setColor(ac); c.drawRoundRect(r, rad, rad, btnOutlinePaint);
        btnTextPaint.setColor(WHITE); c.drawText(text, r.centerX(), r.centerY() + sw*0.014f, btnTextPaint);
    }

    public boolean isPlayHit(float x, float y) { return playBtn.contains(x, y); }
    public boolean isSettingsHit(float x, float y) { return settingsBtn.contains(x, y); }
    public boolean isBackHit(float x, float y) { return backBtn.contains(x, y); }
    public boolean isDiffHit(float x, float y) { return diffBtn.contains(x, y); }
    public boolean isSpeedHit(float x, float y) { return speedBtn.contains(x, y); }
    public boolean isSoundHit(float x, float y) { return soundBtn.contains(x, y); }
    public boolean isVibHit(float x, float y) { return vibBtn.contains(x, y); }
    public boolean isRestartHit(float x, float y) {
        if (gameOverTime > 0 && System.currentTimeMillis() - gameOverTime < BUTTON_DELAY) return false;
        return restartBtn.contains(x, y);
    }
}
