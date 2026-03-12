package com.stellardrift.game.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;

import com.stellardrift.game.util.Constants;
import com.stellardrift.game.util.EconomyManager;
import com.stellardrift.game.util.SettingsManager;
import com.stellardrift.game.util.SoundManager;
import com.stellardrift.game.util.VibrationManager;
import com.stellardrift.game.world.FuelSystem;
import com.stellardrift.game.world.GameWorld;
import com.stellardrift.game.world.PowerUp;
import com.stellardrift.game.world.ShipData;
import com.stellardrift.game.world.ShipRegistry;

public class UIOverlay {

    public static final int STATE_MAIN_MENU = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_GAME_OVER = 3;
    public static final int STATE_SETTINGS = 4;
    public static final int STATE_SHOP = 5;

    private int currentState = STATE_MAIN_MENU;
    private float sw, sh; 
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GOLD = 0xFFFFD740;
    private static final int CYAN = 0xFF00E5FF;
    private static final int PURPLE = 0xFF7C4DFF;
    private static final int DIM = 0xCC050510;
    private static final long BUTTON_DELAY = 1200;
    private static final float CLOSE_BTN_HITBOX = 60f; 
    private int difficultyLevel = 1, gameSpeedLevel = 1;
    private boolean soundEnabled = true, vibrationEnabled = true;
    private int soundToggleCount = 0, vibrationToggleCount = 0;
    private boolean godModeActive = false;
    private SettingsManager settingsManager;
    private ShopUI shopUI;

    private final Paint bgBlackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuTitlePaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuTitlePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuBtnBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuBtnBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuBtnTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuCreditPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint creditPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint titlePaint, subtitlePaint, scorePaint, smallPaint;
    private Paint cardPaint, cardBorderPaint, btnTextPaint, textPaint; 
    private Paint dimPaint, labelPaint, valuePaint, accentPaint;
    private Paint hudScorePaint, hudLabelPaint, diffBarBg, diffBarFill;
    private Paint comboPaint, powerBarBg, powerBarFill, statPaint;
    private Paint milestonePaint, tempoPaint, riskPaint;
    private Paint highScoreLinePaint, highScoreTextPaint;
    private Paint closeBtnCirclePaint, closeBtnXPaint;
    private Paint toggleBgPaint, toggleActivePaint, toggleTextPaint, dividerPaint;

    private RectF playBtn = new RectF(), settingsBtn = new RectF(), shopBtn = new RectF();
    private RectF restartBtn = new RectF(), tempRect = new RectF();
    private RectF[] settingsToggleRects = new RectF[6];
    private RectF soundToggleRect = new RectF(), vibToggleRect = new RectF();

    private float pulse;
    private long gameOverTime;

    private boolean recordBroken = false;
    private float recordBreakParticleTimer = 0;

    private FuelSystem fuelSystem;

    public UIOverlay(float screenW, float screenH) {
        this.sw = screenW; this.sh = screenH; pulse = 0; gameOverTime = 0;

        bgBlackPaint.setStyle(Paint.Style.FILL); bgOverlayPaint.setStyle(Paint.Style.FILL);
        cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG); cardPaint.setStyle(Paint.Style.FILL);
        cardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG); cardBorderPaint.setStyle(Paint.Style.STROKE); cardBorderPaint.setStrokeWidth(1.5f);
        btnTextPaint = makePaint(WHITE, sw * 0.045f, Paint.Align.CENTER, true);
        textPaint = makePaint(WHITE, sw * 0.04f, Paint.Align.LEFT, false);
        dimPaint = new Paint(); dimPaint.setColor(DIM); dimPaint.setStyle(Paint.Style.FILL);
        labelPaint = makePaint(0xFFB0BEC5, sw * 0.04f, Paint.Align.LEFT, false);
        valuePaint = makePaint(WHITE, sw * 0.04f, Paint.Align.RIGHT, true);
        accentPaint = makePaint(CYAN, sw * 0.06f, Paint.Align.CENTER, true); 
        hudScorePaint = makePaint(WHITE, sw * 0.06f, Paint.Align.LEFT, true);
        hudLabelPaint = makePaint(0x99FFFFFF, sw * 0.025f, Paint.Align.LEFT, false);
        comboPaint = makePaint(GOLD, sw * 0.06f, Paint.Align.CENTER, true);
        diffBarBg = new Paint(Paint.ANTI_ALIAS_FLAG); diffBarBg.setColor(0x33FFFFFF);
        diffBarFill = new Paint(Paint.ANTI_ALIAS_FLAG); diffBarFill.setColor(CYAN);
        powerBarBg = new Paint(Paint.ANTI_ALIAS_FLAG); powerBarBg.setColor(0x33FFFFFF);
        powerBarFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        statPaint = makePaint(0xFFB0BEC5, sw * 0.035f, Paint.Align.LEFT, false);
        milestonePaint = makePaint(GOLD, sw * 0.07f, Paint.Align.CENTER, true);
        tempoPaint = makePaint(WHITE, sw * 0.025f, Paint.Align.RIGHT, false);
        riskPaint = makePaint(GOLD, sw * 0.03f, Paint.Align.CENTER, true);
        highScoreLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG); highScoreLinePaint.setColor(GOLD); highScoreLinePaint.setStrokeWidth(3f);
        highScoreTextPaint = makePaint(GOLD, sw * 0.03f, Paint.Align.RIGHT, true);
        closeBtnCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG); closeBtnCirclePaint.setStyle(Paint.Style.FILL);
        closeBtnXPaint = new Paint(Paint.ANTI_ALIAS_FLAG); closeBtnXPaint.setStyle(Paint.Style.STROKE); closeBtnXPaint.setStrokeCap(Paint.Cap.ROUND);
        toggleBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG); toggleBgPaint.setStyle(Paint.Style.FILL);
        toggleActivePaint = new Paint(Paint.ANTI_ALIAS_FLAG); toggleActivePaint.setStyle(Paint.Style.FILL);
        toggleTextPaint = makePaint(WHITE, sw * 0.03f, Paint.Align.CENTER, true);
        dividerPaint = new Paint(); dividerPaint.setColor(Color.argb(40, 150, 160, 180)); dividerPaint.setStrokeWidth(2f);

        for (int i = 0; i < 6; i++) settingsToggleRects[i] = new RectF();

        titlePaint = makePaint(CYAN, sw * 0.1f, Paint.Align.CENTER, true);
        subtitlePaint = makePaint(WHITE, sw * 0.04f, Paint.Align.CENTER, false); subtitlePaint.setAlpha(180);
        scorePaint = makePaint(WHITE, sw * 0.08f, Paint.Align.CENTER, true);
        smallPaint = makePaint(0xFFB0BEC5, sw * 0.035f, Paint.Align.CENTER, false);

        float cx = sw / 2f, bw = sw * 0.65f, bh = sh * 0.075f;
        restartBtn.set(cx - bw/2, sh * 0.76f, cx + bw/2, sh * 0.76f + bh);
        playBtn.set(cx - bw/2, sh * 0.45f, cx + bw/2, sh * 0.45f + bh);
        shopBtn.set(cx - bw/2, sh * 0.55f, cx + bw/2, sh * 0.55f + bh);
        settingsBtn.set(cx - bw/2, sh * 0.65f, cx + bw/2, sh * 0.65f + bh);
    }

    private Paint makePaint(int color, float size, Paint.Align align, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(color); p.setTextSize(size); p.setTextAlign(align);
        if (bold) p.setTypeface(Typeface.DEFAULT_BOLD); return p;
    }

    public void setFuelSystem(FuelSystem fs) { this.fuelSystem = fs; }
    public void initPrefs(SettingsManager settings, ShipRegistry registry, EconomyManager economy, ShipRenderer renderer, final SoundManager sm, final VibrationManager vm) {
        this.settingsManager = settings;
        difficultyLevel = settings.getDifficulty(); gameSpeedLevel = settings.getGameSpeed(); soundEnabled = settings.isSoundEnabled(); vibrationEnabled = settings.isVibrationEnabled();
        
        shopUI = new ShopUI(registry, economy, renderer, sw, sh);
        shopUI.setListener(new ShopUI.Listener() {
            @Override public void onShopClosed() { if (sm != null) sm.playMenuClick(); }
            @Override public void onShipPurchased() { if (sm != null) sm.playPurchase(); if (vm != null) vm.vibratePurchase(); }
            @Override public void onShipEquipped() { if (sm != null) sm.playMenuClick(); }
            @Override public void onUpgraded() { if (sm != null) sm.playUpgrade(); if (vm != null) vm.vibrateUpgrade(); }
            @Override public void onActionFailed() { if (sm != null) sm.playError(); if (vm != null) vm.vibrateError(); }
        });
    }

    public void resetGameOver() { gameOverTime = 0; recordBroken = false; recordBreakParticleTimer = 0; }
    public void setState(int newState) { if (currentState != newState) resetAllPaintsToSafeState(); currentState = newState; }

    public void update(float dt) {
        if (shopUI != null) shopUI.update(dt);
    }

    private void resetAllPaintsToSafeState() {
        Paint[] allPaints = { titlePaint, subtitlePaint, menuBtnBgPaint, menuBtnBorderPaint, menuBtnTextPaint, menuCreditPaint, cardPaint, cardBorderPaint, textPaint, toggleTextPaint };
        for (Paint p : allPaints) { if (p == null) continue; p.setShader(null); p.setMaskFilter(null); p.setColorFilter(null); p.setAlpha(255); p.setFakeBoldText(false); p.setStrikeThruText(false); p.setUnderlineText(false); }
    }

    public int getCurrentState() { return currentState; }

    public void renderFull(Canvas c, GameWorld world, ShipRenderer shipRenderer) {
        pulse += 0.04f;
        if (world.getState() == Constants.STATE_PLAYING && !isShopVisible()) { drawHUD(c, world); drawMilestone(c, world); drawHighScoreProximity(c, world); drawInGameCredits(c, world.getEconomy()); } 
        else if (world.getState() == Constants.STATE_MENU) { drawMenu(c, world.getHighScore(), world.getEconomy()); } 
        else if (world.getState() == Constants.STATE_GAME_OVER) { drawGameOver(c, world); } 
        else if (world.getState() == Constants.STATE_SETTINGS) { drawSettingsScreen(c, world.isGodModeActive()); }

        if (shopUI != null && shopUI.isVisible()) shopUI.draw(c);
    }

    private void drawHighScoreProximity(Canvas c, GameWorld world) {
        int hs = world.getHighScore(); if (hs <= 500) return;
        float ratio = (float) world.getScore() / hs;
        if (ratio >= 0.85f && ratio < 1.0f && !recordBroken) {
            float progress = (ratio - 0.85f) / 0.15f; float lineY = sh * 0.20f * (1f - progress) + sh * 0.10f; int alpha = (int)(60 + 150 * progress);
            highScoreLinePaint.setColor(Color.argb(alpha, 255, 215, 0)); highScoreLinePaint.setStrokeWidth(3f);
            float dashOffset = (System.currentTimeMillis() % 1000) / 1000f * 30f;
            for (float x = -dashOffset; x < sw; x += 40f) c.drawLine(x, lineY, x + 20f, lineY, highScoreLinePaint);
            highScoreTextPaint.setColor(Color.argb(alpha, 255, 215, 0)); c.drawText("● RECORD", sw - sw * 0.04f, lineY - 10, highScoreTextPaint);
        } else if (ratio >= 1.0f && !recordBroken) { recordBroken = true; recordBreakParticleTimer = 1.0f; }

        if (recordBroken && recordBreakParticleTimer > 0) {
            recordBreakParticleTimer -= 0.016f;
            if (recordBreakParticleTimer > 0) { highScoreTextPaint.setColor(Color.argb((int)(255 * recordBreakParticleTimer), 255, 215, 0)); highScoreTextPaint.setTextSize(sw * 0.035f); c.drawText("NEW RECORD!", sw / 2f, sh * 0.18f, highScoreTextPaint); highScoreTextPaint.setTextSize(sw * 0.025f); }
        }
    }

    private void drawMenu(Canvas c, int highScore, EconomyManager economy) {
        c.drawColor(Color.BLACK);
        menuTitlePaint1.setColor(Color.WHITE); menuTitlePaint1.setTextSize(sw * 0.12f); menuTitlePaint1.setTextAlign(Paint.Align.CENTER); menuTitlePaint1.setFakeBoldText(true); menuTitlePaint1.setStyle(Paint.Style.FILL); menuTitlePaint1.setShader(null); menuTitlePaint1.setAlpha(255);
        float titleY = sh * 0.17f; c.drawText("STELLAR", sw / 2f, titleY, menuTitlePaint1);

        menuTitlePaint2.setColor(Color.rgb(80, 195, 255)); menuTitlePaint2.setTextSize(sw * 0.14f); menuTitlePaint2.setTextAlign(Paint.Align.CENTER); menuTitlePaint2.setFakeBoldText(true); menuTitlePaint2.setStyle(Paint.Style.FILL); menuTitlePaint2.setShader(null); menuTitlePaint2.setAlpha(255);
        c.drawText("DRIFT", sw / 2f, titleY + sh * 0.06f, menuTitlePaint2);

        menuSubPaint.setColor(Color.argb(100, 170, 180, 200)); menuSubPaint.setTextSize(sw * 0.035f); menuSubPaint.setTextAlign(Paint.Align.CENTER); menuSubPaint.setFakeBoldText(false); menuSubPaint.setStyle(Paint.Style.FILL); menuSubPaint.setShader(null); menuSubPaint.setAlpha(100);
        c.drawText("v5.0.1 — Masterpiece Edition", sw / 2f, titleY + sh * 0.09f, menuSubPaint);
        
        drawMenuButton(c, "▶ PLAY", playBtn, Color.argb(185, 18, 28, 48), Color.argb(150, 70, 170, 255), Color.WHITE);
        drawMenuButton(c, "✦ SHOP", shopBtn, Color.argb(175, 28, 26, 38), Color.argb(140, 195, 165, 38), Color.rgb(228, 198, 55));
        drawMenuButton(c, "⚙ SETTINGS", settingsBtn, Color.argb(165, 22, 25, 38), Color.argb(120, 115, 125, 155), Color.rgb(175, 185, 205));
        
        subtitlePaint.setTextSize(sw * 0.035f); subtitlePaint.setAlpha((int)(150 * (Math.sin(pulse * 1.5) * 0.3 + 0.7))); 
        c.drawText("Destroy asteroids & upgrade ship!", sw / 2f, sh * 0.88f, subtitlePaint);

        menuCreditPaint.setColor(Color.rgb(195, 175, 45)); menuCreditPaint.setTextSize(sw * 0.05f); menuCreditPaint.setTextAlign(Paint.Align.CENTER); menuCreditPaint.setFakeBoldText(true); menuCreditPaint.setStyle(Paint.Style.FILL); menuCreditPaint.setShader(null); menuCreditPaint.setAlpha(255);
        int credits = (economy != null) ? economy.getDisplayedCredits() : 0;
        c.drawText("✦ " + credits, sw / 2f, sh * 0.76f, menuCreditPaint);

        menuSubPaint.setColor(Color.argb(90, 155, 165, 185)); menuSubPaint.setAlpha(90);
        c.drawText("Best: " + highScore, sw / 2f, sh * 0.81f, menuSubPaint);
    }

    private void drawMenuButton(Canvas c, String text, RectF outRect, int bgCol, int borderCol, int textCol) {
        menuBtnBgPaint.setColor(bgCol); menuBtnBgPaint.setStyle(Paint.Style.FILL); menuBtnBgPaint.setShader(null);
        c.drawRoundRect(outRect, outRect.height()/2, outRect.height()/2, menuBtnBgPaint);
        menuBtnBorderPaint.setColor(borderCol); menuBtnBorderPaint.setStyle(Paint.Style.STROKE); menuBtnBorderPaint.setStrokeWidth(3f); menuBtnBorderPaint.setShader(null);
        c.drawRoundRect(outRect, outRect.height()/2, outRect.height()/2, menuBtnBorderPaint);
        menuBtnTextPaint.setColor(textCol); menuBtnTextPaint.setTextSize(outRect.height() * 0.35f); menuBtnTextPaint.setTextAlign(Paint.Align.CENTER); menuBtnTextPaint.setFakeBoldText(true); menuBtnTextPaint.setStyle(Paint.Style.FILL); menuBtnTextPaint.setShader(null); menuBtnTextPaint.setAlpha(255);
        c.drawText(text, sw / 2f, outRect.top + outRect.height()/2 + outRect.height()*0.12f, menuBtnTextPaint);
    }

    private void drawSettingsScreen(Canvas c, boolean godMode) {
        bgBlackPaint.setColor(Color.argb(255, 0, 0, 0)); c.drawRect(0, 0, sw, sh, bgBlackPaint);
        bgOverlayPaint.setColor(Color.argb(245, 8, 10, 20)); c.drawRect(0, 0, sw, sh, bgOverlayPaint);

        float cw = sw * 0.9f; float ch = sh * 0.6f;
        float cx = sw * 0.05f; float cy = sh * 0.2f;

        tempRect.set(cx, cy, cx + cw, cy + ch);
        cardPaint.setColor(Color.argb(220, 18, 22, 35)); cardPaint.setStyle(Paint.Style.FILL); c.drawRoundRect(tempRect, 30, 30, cardPaint);
        cardBorderPaint.setColor(Color.argb(80, 100, 120, 180)); cardBorderPaint.setStyle(Paint.Style.STROKE); c.drawRoundRect(tempRect, 30, 30, cardBorderPaint);

        accentPaint.setColor(WHITE); accentPaint.setTextSize(sw * 0.07f); accentPaint.setShader(null);
        c.drawText("⚙ SETTINGS", sw / 2f, cy + sh * 0.07f, accentPaint);
        c.drawLine(cx + 40, cy + sh * 0.09f, cx + cw - 40, cy + sh * 0.09f, dividerPaint);

        float rowY = cy + sh * 0.15f; float rowGap = sh * 0.09f;

        drawSettingsLabel(c, "DIFFICULTY", cx + 40, rowY);
        drawThreeWayToggle(c, cx + cw - sw*0.45f, rowY - sh*0.03f, sw*0.4f, sh*0.05f, difficultyLevel, new String[]{"EASY", "NORM", "HARD"}, new int[]{Color.rgb(60, 180, 80), Color.rgb(60, 140, 220), Color.rgb(220, 60, 50)}, 0);
        c.drawLine(cx + 40, rowY + sh*0.04f, cx + cw - 40, rowY + sh*0.04f, dividerPaint);

        rowY += rowGap;
        drawSettingsLabel(c, "GAME SPEED", cx + 40, rowY);
        drawThreeWayToggle(c, cx + cw - sw*0.45f, rowY - sh*0.03f, sw*0.4f, sh*0.05f, gameSpeedLevel, new String[]{"SLOW", "NORM", "FAST"}, new int[]{Color.rgb(100, 160, 200), Color.rgb(60, 140, 220), Color.rgb(240, 160, 30)}, 1);
        c.drawLine(cx + 40, rowY + sh*0.04f, cx + cw - 40, rowY + sh*0.04f, dividerPaint);

        rowY += rowGap;
        drawSettingsLabel(c, "SOUND", cx + 40, rowY);
        drawOnOffToggle(c, cx + cw - sw*0.25f, rowY - sh*0.03f, sw*0.2f, sh*0.045f, soundEnabled, soundToggleRect);
        c.drawLine(cx + 40, rowY + sh*0.04f, cx + cw - 40, rowY + sh*0.04f, dividerPaint);

        rowY += rowGap;
        drawSettingsLabel(c, "VIBRATION", cx + 40, rowY);
        drawOnOffToggle(c, cx + cw - sw*0.25f, rowY - sh*0.03f, sw*0.2f, sh*0.045f, vibrationEnabled, vibToggleRect);

        if (godMode) {
            smallPaint.setColor(GOLD); smallPaint.setTextSize(sw * 0.035f);
            c.drawText("★ GOD MODE ACTIVE ★", sw / 2f, cy + ch - 40, smallPaint);
        }

        drawCloseButton(c, sw - sw * 0.1f, sh * 0.08f, 1f);
    }

    private void drawSettingsLabel(Canvas c, String label, float x, float y) {
        labelPaint.setColor(Color.rgb(190, 195, 210)); labelPaint.setTextSize(sw * 0.04f); labelPaint.setFakeBoldText(true);
        c.drawText(label, x, y, labelPaint);
    }

    private void drawThreeWayToggle(Canvas c, float x, float y, float w, float h, int selIdx, String[] labels, int[] colors, int groupIdx) {
        float segW = w / 3f;
        for (int i = 0; i < 3; i++) {
            float sx = x + segW * i;
            RectF r = settingsToggleRects[groupIdx * 3 + i]; r.set(sx, y, sx + segW, y + h);
            if (i == selIdx) {
                toggleActivePaint.setColor(Color.argb(200, Color.red(colors[i]), Color.green(colors[i]), Color.blue(colors[i])));
                c.drawRoundRect(r, 12, 12, toggleActivePaint);
            } else {
                toggleBgPaint.setColor(Color.argb(80, 40, 45, 60)); c.drawRoundRect(r, 12, 12, toggleBgPaint);
            }
            toggleTextPaint.setColor(i == selIdx ? WHITE : Color.rgb(130, 135, 150));
            c.drawText(labels[i], sx + segW / 2, y + h * 0.65f, toggleTextPaint);
        }
    }

    private void drawOnOffToggle(Canvas c, float x, float y, float w, float h, boolean isOn, RectF hitRect) {
        hitRect.set(x, y, x + w, y + h);
        if (isOn) {
            toggleActivePaint.setColor(Color.argb(200, 60, 180, 80)); c.drawRoundRect(hitRect, h/2, h/2, toggleActivePaint);
            cardPaint.setColor(WHITE); c.drawCircle(x + w - h/2, y + h/2, h/2 - 4, cardPaint);
            toggleTextPaint.setColor(WHITE); c.drawText("ON", x + w * 0.35f, y + h * 0.65f, toggleTextPaint);
        } else {
            toggleBgPaint.setColor(Color.argb(150, 60, 60, 70)); c.drawRoundRect(hitRect, h/2, h/2, toggleBgPaint);
            cardPaint.setColor(Color.rgb(140, 140, 150)); c.drawCircle(x + h/2, y + h/2, h/2 - 4, cardPaint);
            toggleTextPaint.setColor(Color.rgb(100, 100, 110)); c.drawText("OFF", x + w * 0.65f, y + h * 0.65f, toggleTextPaint);
        }
    }

    private void drawCloseButton(Canvas c, float cx, float cy, float alpha) {
        closeBtnCirclePaint.setColor(Color.argb((int)(180*alpha), 200, 50, 50));
        c.drawCircle(cx, cy, sw * 0.05f, closeBtnCirclePaint);
        closeBtnXPaint.setColor(Color.argb((int)(240*alpha), 255, 255, 255)); 
        closeBtnXPaint.setStrokeWidth(5f);
        float cross = sw * 0.02f;
        c.drawLine(cx - cross, cy - cross, cx + cross, cy + cross, closeBtnXPaint);
        c.drawLine(cx + cross, cy - cross, cx - cross, cy + cross, closeBtnXPaint);
    }

    private void drawHUD(Canvas c, GameWorld world) {
        float pad = sw * 0.04f;
        hudLabelPaint.setAlpha(150); hudLabelPaint.setTextAlign(Paint.Align.LEFT); c.drawText("SCORE", pad, sh * 0.04f, hudLabelPaint);
        hudScorePaint.setColor(WHITE); c.drawText(String.valueOf(world.getScore()), pad, sh * 0.075f, hudScorePaint);
        if (fuelSystem != null) fuelSystem.draw(c);
        int tempo = world.getTempoPhase();
        if (tempo != Constants.TEMPO_CALM) { String tl = tempo == Constants.TEMPO_PRESSURE ? "▲ PRESSURE" : "★ REWARD"; tempoPaint.setColor(tempo == Constants.TEMPO_PRESSURE ? 0xFFFF1744 : GOLD); tempoPaint.setAlpha(180); tempoPaint.setTextAlign(Paint.Align.RIGHT); c.drawText(tl, sw - pad, sh * 0.095f, tempoPaint); }
        if (world.isRiskWindowActive()) { riskPaint.setAlpha((int)(220 * (Math.sin(pulse * 5) * 0.15 + 0.85))); c.drawText("⚡ RISK x1.5 ⚡", sw / 2f, sh * 0.16f, riskPaint); }
        float pbY = sh * 0.92f, pbH = sh * 0.012f, pbW = sw * 0.25f; int pbc = 0;
        if (world.isMagnetActive()) { drawPowerBar(c, pad, pbY - pbc*(pbH+8), pbW, pbH, "MAGNET", world.getMagnetTimer(), Constants.POWERUP_DURATION, PowerUp.getColor(Constants.POWERUP_MAGNET)); pbc++; }
        if (world.isSlowmoActive()) { drawPowerBar(c, pad, pbY - pbc*(pbH+8), pbW, pbH, "SLOW-MO", world.getSlowmoTimer(), Constants.POWERUP_DURATION, PowerUp.getColor(Constants.POWERUP_SLOWMO)); pbc++; }
        if (world.getEconomy() != null && world.getEconomy().isDoubleActive()) { drawPowerBar(c, pad, pbY - pbc*(pbH+8), pbW, pbH, "DOUBLE", world.getDoubleTimer(), Constants.POWERUP_DURATION, PowerUp.getColor(Constants.POWERUP_DOUBLE)); pbc++; }
    }

    private void drawPowerBar(Canvas c, float x, float y, float w, float h, String name, int timer, int max, int color) {
        hudLabelPaint.setTextAlign(Paint.Align.LEFT); hudLabelPaint.setAlpha(200); hudLabelPaint.setColor(color); c.drawText(name, x, y - 4, hudLabelPaint); hudLabelPaint.setColor(0x99FFFFFF);
        c.drawRoundRect(new RectF(x, y, x + w, y + h), h, h, powerBarBg); float f = (float) timer / max; powerBarFill.setColor(color); powerBarFill.setAlpha(f < 0.25f ? (int)(200 * (Math.sin(pulse * 8) * 0.3 + 0.7)) : 200); c.drawRoundRect(new RectF(x, y, x + w * f, y + h), h, h, powerBarFill);
    }

    private void drawMilestone(Canvas c, GameWorld world) {
        if (world.getMilestoneTimer() <= 0 || world.getMilestoneText() == null) return;
        float p = world.getMilestoneTimer() / 90f, sc = p > 0.8f ? 1f + (p - 0.8f) / 0.2f * 0.5f : 1f;
        milestonePaint.setTextSize(sw * 0.06f * sc); milestonePaint.setAlpha((int)(255 * Math.min(1f, p * 2))); c.drawText(world.getMilestoneText(), sw / 2f, sh * 0.35f, milestonePaint);
    }

    private void drawGameOver(Canvas c, GameWorld world) {
        int score = world.getScore(), hs = world.getHighScore();
        if (gameOverTime == 0) gameOverTime = System.currentTimeMillis();
        long el = System.currentTimeMillis() - gameOverTime;
        c.drawRect(0, 0, sw, sh, dimPaint); float cx = sw / 2f;
        accentPaint.setColor(0xFFFF1744); accentPaint.setTextSize(sw * 0.08f); accentPaint.setAlpha(255); c.drawText("GAME OVER", cx, sh * 0.2f, accentPaint);

        float cp = Math.min(1f, el / 800f); cp = 1f - (1f - cp) * (1f - cp);
        scorePaint.setColor(WHITE); scorePaint.setTextSize(sw * 0.12f); scorePaint.setAlpha(255); c.drawText(String.valueOf((int)(score * cp)), cx, sh * 0.32f, scorePaint);
        smallPaint.setAlpha(180); c.drawText("SCORE", cx, sh * 0.35f, smallPaint);

        if (el > 800) {
            if (score >= hs && score > 0) { accentPaint.setColor(GOLD); accentPaint.setTextSize(sw * 0.04f); accentPaint.setAlpha((int)(255 * (Math.sin(pulse * 3) * 0.15 + 0.85))); c.drawText("★ NEW BEST! ★", cx, sh * 0.41f, accentPaint); }
            else { smallPaint.setAlpha(140); c.drawText("BEST: " + hs, cx, sh * 0.41f, smallPaint); }
        }

        if (el > 600) drawStats(c, world, Math.min(1f, (el - 600) / 400f));
        if (el > BUTTON_DELAY) {
            float ba = Math.min(1f, (el - BUTTON_DELAY) / 500f);
            drawMenuButton(c, "▶ PLAY AGAIN", restartBtn, Color.argb((int)(0x33*ba),255,255,255), Color.argb((int)(140*ba),0,229,255), WHITE);
            subtitlePaint.setAlpha((int)(80 * ba)); c.drawText("or tap anywhere for menu", cx, sh * 0.87f, subtitlePaint);
        }
    }

    private void drawStats(Canvas c, GameWorld w, float a) {
        float sx = sw*0.2f, sy = sh*0.48f, g = sh*0.04f, rx = sw*0.8f; int sa = (int)(200*a), va = (int)(255*a);
        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Orbs Collected", sx, sy, statPaint); statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText(String.valueOf(w.getOrbsCollected()), rx, sy, statPaint);
        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Near Misses", sx, sy+g, statPaint); statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText(String.valueOf(w.getNearMissCount()), rx, sy+g, statPaint);
        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Max Combo", sx, sy+g*2, statPaint); statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(w.getMaxCombo()>=5?GOLD:WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText("x"+w.getMaxCombo(), rx, sy+g*2, statPaint);
        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Survival Time", sx, sy+g*3, statPaint); statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText(w.getSurvivalTime()+"s", rx, sy+g*3, statPaint);
    }

    private void drawInGameCredits(Canvas c, EconomyManager economy) {
        int credits = economy.getDisplayedCredits(); float flash = economy.getCreditFlash();
        int r = (int)(200 + 55 * flash), g = (int)(180 + 75 * flash);
        creditPaint.setColor(Color.argb(180, Math.min(255, r), Math.min(255, g), 50));
        creditPaint.setTextSize(sw * 0.045f); creditPaint.setTextAlign(Paint.Align.RIGHT); c.drawText("✦ " + credits, sw - sw * 0.04f, sh * 0.11f, creditPaint); creditPaint.setTextAlign(Paint.Align.CENTER);
    }

    public boolean handleSettingsTouch(float tx, float ty, GameWorld world, SoundManager sm, VibrationManager vm) {
        if (isCloseHit(tx, ty)) { world.quitToMenu(); if (sm != null && sm.isEnabled()) sm.playMenuClick(); if (vm != null && vm.isEnabled()) vm.vibrateMenuClick(); return true; }
        for (int i = 0; i < 3; i++) { if (settingsToggleRects[i].contains(tx, ty)) { difficultyLevel = i; saveSettings(sm); if(sm!=null) sm.playMenuClick(); return true; } }
        for (int i = 0; i < 3; i++) { if (settingsToggleRects[3+i].contains(tx, ty)) { gameSpeedLevel = i; saveSettings(sm); if(sm!=null) sm.playMenuClick(); return true; } }
        if (soundToggleRect.contains(tx, ty)) { soundEnabled = !soundEnabled; soundToggleCount++; if (soundToggleCount >= 10) godModeActive = true; saveSettings(sm); if(sm!=null) sm.playMenuClick(); return true; }
        if (vibToggleRect.contains(tx, ty)) { vibrationEnabled = !vibrationEnabled; vibrationToggleCount++; if (vibrationToggleCount >= 5) godModeActive = false; saveSettings(sm); if(sm!=null) sm.playMenuClick(); return true; }
        return false;
    }

    public boolean handleShopTouch(int action, float tx, float ty, ShipRegistry registry, EconomyManager economy, SoundManager sm, VibrationManager vm) {
        if (shopUI == null) return false;
        return shopUI.handleTouch(action, tx, ty);
    }

    private void saveSettings(SoundManager sm) { if (settingsManager != null) { settingsManager.setDifficulty(difficultyLevel); settingsManager.setGameSpeed(gameSpeedLevel); settingsManager.setSoundEnabled(soundEnabled); settingsManager.setVibrationEnabled(vibrationEnabled); if(sm!=null) { sm.setEnabled(soundEnabled); if(soundEnabled) sm.startDrone(); else sm.stopDrone(); } } }

    public void openShop() { if (shopUI != null) shopUI.open(); }
    public void closeShop() { if (shopUI != null) shopUI.close(); }
    public boolean isShopVisible() { return shopUI != null && shopUI.isVisible(); }

    public boolean isPlayHit(float x, float y) { return playBtn.contains(x, y); }
    public boolean isShopHit(float x, float y) { return shopBtn.contains(x, y); }
    public boolean isSettingsHit(float x, float y) { return settingsBtn.contains(x, y); }
    public boolean isRestartHit(float x, float y) { if (gameOverTime > 0 && System.currentTimeMillis() - gameOverTime < BUTTON_DELAY) return false; return restartBtn.contains(x, y); }
    public boolean isCloseHit(float tx, float ty) { float cx = sw - sw * 0.1f, cy = sh * 0.08f; return (Math.pow(tx - cx, 2) + Math.pow(ty - cy, 2)) < Math.pow(CLOSE_BTN_HITBOX, 2); }

    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isVibrationEnabled() { return vibrationEnabled; }
}
