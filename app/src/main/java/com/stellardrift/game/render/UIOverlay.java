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
    private int sw, sh;

    private final Paint menuTitlePaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuTitlePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuBtnBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuBtnBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuBtnTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuCreditPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint titlePaint, subtitlePaint, scorePaint, smallPaint;
    private Paint cardPaint, cardBorderPaint, btnTextPaint; 
    private Paint dimPaint, labelPaint, valuePaint, accentPaint;
    private Paint hudScorePaint, hudLabelPaint, diffBarBg, diffBarFill;
    private Paint comboPaint, powerBarBg, powerBarFill, statPaint;
    private Paint milestonePaint, tempoPaint, riskPaint;
    private Paint highScoreLinePaint, highScoreTextPaint;
    private Paint closeBtnCirclePaint, closeBtnXPaint;
    private Paint toggleBgPaint, toggleActivePaint, toggleTextPaint, dividerPaint;
    private Paint shopBgPaint, statBarBgPaint, statBarPaint, statBarFill, upgradeBtnPaint;
    private Paint selectedGlow; 

    private RectF playBtn = new RectF(), settingsBtn = new RectF(), shopBtn = new RectF();
    private RectF restartBtn = new RectF();
    private RectF tempRect = new RectF(), tempBtnRect = new RectF();

    private RectF[] settingsToggleRects = new RectF[6];
    private RectF soundToggleRect = new RectF(), vibToggleRect = new RectF();
    private RectF[][] upgradeButtonRects;

    private float pulse;
    private long gameOverTime;
    private static final long BUTTON_DELAY = 1200;
    private static final float CLOSE_BTN_HITBOX = 50f;

    private boolean recordBroken = false;
    private float recordBreakParticleTimer = 0;

    private static final int CYAN = 0xFF00E5FF;
    private static final int PURPLE = 0xFF7C4DFF;
    private static final int GOLD = 0xFFFFD740;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DIM = 0xCC050510;

    private boolean isShopOpen = false;
    private float shopOpenAnim = 0f, scrollY = 0f, scrollVelocity = 0f;
    private int touchDownY = -1, lastTouchY = -1;
    private boolean isDraggingShop = false;
    private float cardWidth, cardHeight, cardSpacing, cardStartY, totalContentHeight;
    private float purchaseFlashTimer = 0f;
    private int purchaseFlashShipId = -1;
    private float upgradeFlashTimer = 0f;
    private int upgradeFlashShipId = -1, upgradeFlashStat = -1;

    private int difficultyLevel = 1, gameSpeedLevel = 1;
    private boolean soundEnabled = true, vibrationEnabled = true;
    private int soundToggleCount = 0, vibrationToggleCount = 0;
    private boolean godModeActive = false;

    private FuelSystem fuelSystem;
    private SettingsManager settingsManager;

    public UIOverlay(int sw, int sh) {
        this.sw = sw; this.sh = sh; pulse = 0; gameOverTime = 0;

        titlePaint = makePaint(CYAN, sw * 0.1f, Paint.Align.CENTER, true);
        subtitlePaint = makePaint(WHITE, sw * 0.04f, Paint.Align.CENTER, false); subtitlePaint.setAlpha(180);
        scorePaint = makePaint(WHITE, sw * 0.08f, Paint.Align.CENTER, true);
        smallPaint = makePaint(0xFFB0BEC5, sw * 0.035f, Paint.Align.CENTER, false);
        
        cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG); cardPaint.setStyle(Paint.Style.FILL);
        cardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG); cardBorderPaint.setStyle(Paint.Style.STROKE);
        
        btnTextPaint = makePaint(WHITE, sw * 0.045f, Paint.Align.CENTER, true); 
        
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
        highScoreLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highScoreTextPaint = makePaint(GOLD, sw * 0.03f, Paint.Align.RIGHT, true);

        closeBtnCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG); closeBtnCirclePaint.setStyle(Paint.Style.FILL);
        closeBtnXPaint = new Paint(Paint.ANTI_ALIAS_FLAG); closeBtnXPaint.setStyle(Paint.Style.STROKE); closeBtnXPaint.setStrokeCap(Paint.Cap.ROUND);
        toggleBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG); toggleBgPaint.setStyle(Paint.Style.FILL);
        toggleActivePaint = new Paint(Paint.ANTI_ALIAS_FLAG); toggleActivePaint.setStyle(Paint.Style.FILL);
        toggleTextPaint = makePaint(WHITE, sw * 0.03f, Paint.Align.CENTER, true);
        dividerPaint = new Paint(); dividerPaint.setColor(Color.argb(40, 150, 160, 180)); dividerPaint.setStrokeWidth(2f);
        
        shopBgPaint = new Paint(); shopBgPaint.setColor(Color.argb(235, 8, 10, 18));
        statBarBgPaint = new Paint(); statBarBgPaint.setColor(Color.argb(80, 40, 45, 60));
        statBarPaint = new Paint(); statBarPaint.setStyle(Paint.Style.FILL);
        statBarFill = new Paint(); statBarFill.setStyle(Paint.Style.FILL); 
        upgradeBtnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        selectedGlow = new Paint(Paint.ANTI_ALIAS_FLAG); 
        selectedGlow.setStyle(Paint.Style.STROKE); selectedGlow.setStrokeWidth(2f); selectedGlow.setColor(Color.rgb(80, 255, 160));

        for (int i = 0; i < 6; i++) settingsToggleRects[i] = new RectF();
        
        upgradeButtonRects = new RectF[ShipRegistry.SHIP_COUNT][ShipData.STAT_COUNT];
        for (int i = 0; i < ShipRegistry.SHIP_COUNT; i++) {
            for (int j = 0; j < ShipData.STAT_COUNT; j++) upgradeButtonRects[i][j] = new RectF();
        }

        float cx = sw / 2f, bw = sw * 0.65f, bh = sh * 0.075f;
        playBtn = new RectF(cx - bw/2, sh * 0.45f, cx + bw/2, sh * 0.45f + bh);
        shopBtn = new RectF(cx - bw/2, sh * 0.55f, cx + bw/2, sh * 0.55f + bh);
        settingsBtn = new RectF(cx - bw/2, sh * 0.65f, cx + bw/2, sh * 0.65f + bh);
        restartBtn = new RectF(cx - bw/2, sh * 0.76f, cx + bw/2, sh * 0.76f + bh);
        
        cardWidth = sw * 0.88f; cardHeight = sh * 0.32f; cardSpacing = sh * 0.03f; cardStartY = sh * 0.15f;
    }

    private Paint makePaint(int color, float size, Paint.Align align, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(color); p.setTextSize(size); p.setTextAlign(align);
        if (bold) p.setTypeface(Typeface.DEFAULT_BOLD); return p;
    }

    public void setFuelSystem(FuelSystem fs) { this.fuelSystem = fs; }
    public void initPrefs(SettingsManager settings) {
        this.settingsManager = settings;
        difficultyLevel = settings.getDifficulty(); gameSpeedLevel = settings.getGameSpeed();
        soundEnabled = settings.isSoundEnabled(); vibrationEnabled = settings.isVibrationEnabled();
    }

    public void update(float dt) {
        float target = isShopOpen ? 1f : 0f;
        shopOpenAnim += (target - shopOpenAnim) * dt * 10f;
        if (purchaseFlashTimer > 0) purchaseFlashTimer -= dt;
        if (upgradeFlashTimer > 0) upgradeFlashTimer -= dt;
        if (isShopOpen && !isDraggingShop) {
            scrollY += scrollVelocity * dt; scrollVelocity *= 0.92f;
            scrollY = Math.max(0, Math.min(totalContentHeight - sh, scrollY));
        }
    }

    public void renderFull(Canvas c, GameWorld world, ShipRenderer shipRenderer) {
        pulse += 0.04f;
        if (world.getState() == Constants.STATE_PLAYING && !isShopOpen) {
            drawHUD(c, world); drawMilestone(c, world); drawHighScoreProximity(c, world); drawInGameCredits(c, world.getEconomy());
        } else if (world.getState() == Constants.STATE_MENU) {
            drawMenu(c, world.getHighScore(), world.getEconomy());
        } else if (world.getState() == Constants.STATE_GAME_OVER) {
            drawGameOver(c, world);
        } else if (world.getState() == Constants.STATE_SETTINGS) {
            drawSettingsScreen(c, world.isGodModeActive());
        }
        if (shopOpenAnim > 0.01f) drawShop(c, world.getShipRegistry(), world.getEconomy(), shipRenderer);
    }

    private void drawHighScoreProximity(Canvas c, GameWorld world) {
        int hs = world.getHighScore();
        if (hs <= 500) return;
        float ratio = (float) world.getScore() / hs;
        if (ratio >= 0.85f && ratio < 1.0f && !recordBroken) {
            float progress = (ratio - 0.85f) / 0.15f;
            float lineY = sh * 0.20f * (1f - progress) + sh * 0.10f;
            int alpha = (int)(60 + 150 * progress);
            highScoreLinePaint.setColor(Color.argb(alpha, 255, 215, 0)); highScoreLinePaint.setStrokeWidth(3f);
            float dashOffset = (System.currentTimeMillis() % 1000) / 1000f * 30f;
            for (float x = -dashOffset; x < sw; x += 40f) c.drawLine(x, lineY, x + 20f, lineY, highScoreLinePaint);
            highScoreTextPaint.setColor(Color.argb(alpha, 255, 215, 0)); c.drawText("● RECORD", sw - sw * 0.04f, lineY - 10, highScoreTextPaint);
        } else if (ratio >= 1.0f && !recordBroken) { recordBroken = true; recordBreakParticleTimer = 1.0f; }

        if (recordBroken && recordBreakParticleTimer > 0) {
            recordBreakParticleTimer -= 0.016f;
            if (recordBreakParticleTimer > 0) {
                highScoreTextPaint.setColor(Color.argb((int)(255 * recordBreakParticleTimer), 255, 215, 0));
                highScoreTextPaint.setTextSize(sw * 0.035f); c.drawText("NEW RECORD!", sw / 2f, sh * 0.18f, highScoreTextPaint);
                highScoreTextPaint.setTextSize(sw * 0.025f);
            }
        }
    }

    public void resetGameOver() { gameOverTime = 0; recordBroken = false; recordBreakParticleTimer = 0; }

    private void drawMenu(Canvas c, int highScore, EconomyManager economy) {
        c.drawColor(Color.rgb(5, 7, 14));
        menuTitlePaint1.setColor(Color.WHITE); menuTitlePaint1.setTextSize(sw * 0.12f); menuTitlePaint1.setTextAlign(Paint.Align.CENTER); menuTitlePaint1.setFakeBoldText(true); menuTitlePaint1.setStyle(Paint.Style.FILL); menuTitlePaint1.setShader(null); menuTitlePaint1.setAlpha(255);
        float titleY = sh * 0.17f; c.drawText("STELLAR", sw / 2f, titleY, menuTitlePaint1);
        menuTitlePaint2.setColor(Color.rgb(80, 195, 255)); menuTitlePaint2.setTextSize(sw * 0.14f); menuTitlePaint2.setTextAlign(Paint.Align.CENTER); menuTitlePaint2.setFakeBoldText(true); menuTitlePaint2.setStyle(Paint.Style.FILL); menuTitlePaint2.setShader(null); menuTitlePaint2.setAlpha(255);
        c.drawText("DRIFT", sw / 2f, titleY + sh * 0.06f, menuTitlePaint2);
        menuSubPaint.setColor(Color.argb(100, 170, 180, 200)); menuSubPaint.setTextSize(sw * 0.035f); menuSubPaint.setTextAlign(Paint.Align.CENTER); menuSubPaint.setFakeBoldText(false); menuSubPaint.setStyle(Paint.Style.FILL); menuSubPaint.setShader(null); menuSubPaint.setAlpha(100);
        c.drawText("v4.5 — Upgrade Edition", sw / 2f, titleY + sh * 0.09f, menuSubPaint);
        
        // EKSIK METODU EKLENMIŞ HALİ (OVERLOAD)
        drawMenuButton(c, "▶ PLAY", playBtn, Color.argb(185, 18, 28, 48), Color.argb(150, 70, 170, 255), Color.WHITE);
        drawMenuButton(c, "✦ SHOP", shopBtn, Color.argb(175, 28, 26, 38), Color.argb(140, 195, 165, 38), Color.rgb(228, 198, 55));
        drawMenuButton(c, "⚙ SETTINGS", settingsBtn, Color.argb(165, 22, 25, 38), Color.argb(120, 115, 125, 155), Color.rgb(175, 185, 205));
        
        subtitlePaint.setTextSize(sw * 0.035f); subtitlePaint.setAlpha((int)(150 * (Math.sin(pulse * 1.5) * 0.3 + 0.7))); 
        c.drawText("Destroy asteroids & upgrade ship!", sw / 2f, sh * 0.88f, subtitlePaint);
        smallPaint.setTextSize(sw * 0.025f); smallPaint.setAlpha(80); c.drawText("v4.5 Mega Fleet", sw / 2f, sh * 0.95f, smallPaint);
    }

    // OVERLOAD - Orijinal Game Over çizerken kullanılan metod
    private void drawMenuButton(Canvas c, String text, RectF outRect, int bgCol, int borderCol, int textCol) {
        menuBtnBgPaint.setColor(bgCol); menuBtnBgPaint.setStyle(Paint.Style.FILL); menuBtnBgPaint.setShader(null);
        c.drawRoundRect(outRect, outRect.height()/2, outRect.height()/2, menuBtnBgPaint);
        menuBtnBorderPaint.setColor(borderCol); menuBtnBorderPaint.setStyle(Paint.Style.STROKE); menuBtnBorderPaint.setStrokeWidth(3f); menuBtnBorderPaint.setShader(null);
        c.drawRoundRect(outRect, outRect.height()/2, outRect.height()/2, menuBtnBorderPaint);
        menuBtnTextPaint.setColor(textCol); menuBtnTextPaint.setTextSize(outRect.height() * 0.35f); menuBtnTextPaint.setTextAlign(Paint.Align.CENTER); menuBtnTextPaint.setFakeBoldText(true); menuBtnTextPaint.setStyle(Paint.Style.FILL); menuBtnTextPaint.setShader(null); menuBtnTextPaint.setAlpha(255);
        c.drawText(text, sw / 2f, outRect.top + outRect.height()/2 + outRect.height()*0.12f, menuBtnTextPaint);
    }

    private void drawSettingsScreen(Canvas c, boolean godMode) {
        c.drawRect(0, 0, sw, sh, dimPaint);
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

        drawCloseButton(c, sw - sw * 0.1f, sh * 0.08f);
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

    private void drawCloseButton(Canvas c, float cx, float cy) {
        closeBtnCirclePaint.setColor(Color.argb(180, 200, 50, 50));
        c.drawCircle(cx, cy, sw * 0.05f, closeBtnCirclePaint);
        closeBtnXPaint.setColor(WHITE); closeBtnXPaint.setStrokeWidth(5f);
        float cross = sw * 0.02f;
        c.drawLine(cx - cross, cy - cross, cx + cross, cy + cross, closeBtnXPaint);
        c.drawLine(cx + cross, cy - cross, cx - cross, cy + cross, closeBtnXPaint);
    }

    private void drawShop(Canvas c, ShipRegistry registry, EconomyManager economy, ShipRenderer renderer) {
        float alpha = shopOpenAnim;
        shopBgPaint.setAlpha((int)(240 * alpha)); c.drawRect(0, 0, sw, sh, shopBgPaint);
        totalContentHeight = cardStartY + registry.getShipCount() * (cardHeight + cardSpacing) + 150;
        c.save(); c.translate(0, -scrollY);
        
        titlePaint.setTextSize(sw * 0.08f); titlePaint.setAlpha((int)(255 * alpha));
        c.drawText("✦ HANGAR ✦", sw / 2f, sh * 0.08f + scrollY * 0.5f, titlePaint); 

        for (int i = 0; i < registry.getShipCount(); i++) {
            ShipData ship = registry.getShip(i);
            float cardY = cardStartY + i * (cardHeight + cardSpacing);
            if (cardY - scrollY > sh + 50 || cardY + cardHeight - scrollY < -50) continue;
            drawShipCard(c, ship, i, cardY, alpha, economy, renderer);
        }
        c.restore();

        drawCloseButton(c, sw - sw * 0.1f, sh * 0.08f);
        drawCreditDisplay(c, economy, alpha);
    }

    private void drawShipCard(Canvas c, ShipData ship, int shipIndex, float cardY, float alpha, EconomyManager economy, ShipRenderer renderer) {
        float cx = (sw - cardWidth) / 2;
        boolean isSelected = (economy.getSelectedShipId() == ship.id);
        boolean isUnlocked = economy.isShipUnlocked(ship.id);

        tempRect.set(cx, cardY, cx + cardWidth, cardY + cardHeight);
        cardPaint.setColor(isSelected ? Color.argb((int)(200 * alpha), 20, 40, 30) : Color.argb((int)(200 * alpha), 25, 25, 35));
        c.drawRoundRect(tempRect, 24, 24, cardPaint);

        cardBorderPaint.setColor(isSelected ? Color.argb((int)(255 * alpha), 100, 255, 150) : Color.argb((int)(80 * alpha), 100, 150, 200));
        cardBorderPaint.setStrokeWidth(isSelected ? 5f : 2f);
        c.drawRoundRect(tempRect, 24, 24, cardBorderPaint);

        float previewX = cx + sw * 0.18f, previewY = cardY + sh * 0.12f;
        cardPaint.setColor(Color.argb((int)(50 * alpha), 80, 100, 150)); c.drawCircle(previewX, previewY, sw * 0.13f, cardPaint);
        renderer.drawShip(c, ship, previewX, previewY, 0f, (int)(255 * alpha), (sw/1080f) * 2.2f, false);

        float infoX = cx + sw * 0.38f;
        labelPaint.setColor(Color.argb((int)(255 * alpha), Color.red(ship.cockpitColor), Color.green(ship.cockpitColor), Color.blue(ship.cockpitColor)));
        labelPaint.setTextSize(sw * 0.05f); labelPaint.setFakeBoldText(true); c.drawText(ship.name, infoX, cardY + sh * 0.04f, labelPaint);

        smallPaint.setColor(Color.argb((int)(160 * alpha), 200, 200, 220)); smallPaint.setTextSize(sw * 0.028f); smallPaint.setTextAlign(Paint.Align.LEFT);
        c.drawText(ship.description, infoX, cardY + sh * 0.07f, smallPaint);

        float statStartY = cardY + sh * 0.10f; float barX = infoX + sw * 0.08f; float barW = cardWidth - (sw * 0.48f); float barH = sh * 0.012f; float rowH = sh * 0.045f;
        for (int st = 0; st < ShipData.STAT_COUNT; st++) {
            drawStatRowWithUpgrade(c, ship, shipIndex, st, infoX, barX, barW, barH, statStartY + st * rowH, alpha, isUnlocked, economy);
        }

        float btnW = sw * 0.35f, btnH = sh * 0.05f, btnX = cx + cardWidth - btnW - sw * 0.04f, btnY = cardY + cardHeight - btnH - sh * 0.02f;
        tempBtnRect.set(btnX, btnY, btnX + btnW, btnY + btnH);

        btnTextPaint.setTextSize(btnH * 0.4f);
        if (isSelected) {
            cardPaint.setColor(Color.argb((int)(180 * alpha), 40, 160, 80)); c.drawRoundRect(tempBtnRect, btnH/2, btnH/2, cardPaint);
            btnTextPaint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255)); c.drawText("✓ EQUIPPED", btnX + btnW/2, btnY + btnH*0.65f, btnTextPaint);
        } else if (isUnlocked) {
            cardPaint.setColor(Color.argb((int)(180 * alpha), 60, 120, 180)); c.drawRoundRect(tempBtnRect, btnH/2, btnH/2, cardPaint);
            btnTextPaint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255)); c.drawText("EQUIP", btnX + btnW/2, btnY + btnH*0.65f, btnTextPaint);
        } else {
            cardPaint.setColor(economy.getCredits() >= ship.price ? Color.argb((int)(200 * alpha), 200, 160, 30) : Color.argb((int)(100 * alpha), 80, 80, 80));
            c.drawRoundRect(tempBtnRect, btnH/2, btnH/2, cardPaint);
            btnTextPaint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255)); c.drawText("🔒 " + ship.price + " ✦", btnX + btnW/2, btnY + btnH*0.65f, btnTextPaint);
        }

        if (purchaseFlashShipId == ship.id && purchaseFlashTimer > 0) {
            cardPaint.setColor(Color.argb((int)(purchaseFlashTimer / 0.8f * 60), 255, 220, 50)); c.drawRoundRect(tempRect, 24, 24, cardPaint);
        }
    }

    private void drawStatRowWithUpgrade(Canvas c, ShipData ship, int shipIndex, int statIndex, float labelX, float barX, float barW, float barH, float y, float alpha, boolean isUnlocked, EconomyManager economy) {
        String statName = ship.getStatName(statIndex); int statColor = ship.getStatColor(statIndex); float ratio = ship.getStatBarRatio(statIndex); int level = ship.getUpgradeLevel(statIndex);
        smallPaint.setColor(Color.argb((int)(180 * alpha), 180, 185, 200)); smallPaint.setTextSize(sw * 0.025f); c.drawText(statName, labelX, y + barH, smallPaint);
        
        tempRect.set(barX, y, barX + barW, y + barH); statBarBgPaint.setAlpha((int)(80 * alpha)); c.drawRoundRect(tempRect, barH/2, barH/2, statBarBgPaint);
        statBarFill.setColor(Color.argb((int)(200 * alpha), Color.red(statColor), Color.green(statColor), Color.blue(statColor))); tempRect.set(barX, y, barX + barW * ratio, y + barH); c.drawRoundRect(tempRect, barH/2, barH/2, statBarFill);
        
        float dotSpacing = barW / ShipData.MAX_UPGRADE_LEVEL;
        for (int lv = 0; lv < ShipData.MAX_UPGRADE_LEVEL; lv++) {
            float dotX = barX + dotSpacing * (lv + 0.5f), dotY = y - 4, dotR = sw * 0.005f;
            dimPaint.setColor(lv < level ? Color.argb((int)(220*alpha), Color.red(statColor), Color.green(statColor), Color.blue(statColor)) : Color.argb((int)(40*alpha), 150, 160, 180)); c.drawCircle(dotX, dotY, dotR, dimPaint);
        }

        if (isUnlocked && level < ShipData.MAX_UPGRADE_LEVEL) {
            int cost = ship.getUpgradeCost(statIndex); boolean canAfford = economy.getCredits() >= cost;
            float ubtnW = sw * 0.15f, ubtnH = sh * 0.025f, ubtnX = barX + barW + sw * 0.02f, ubtnY = y + (barH - ubtnH) / 2;
            upgradeButtonRects[shipIndex][statIndex].set(ubtnX, ubtnY, ubtnX + ubtnW, ubtnY + ubtnH);
            upgradeBtnPaint.setColor(canAfford ? Color.argb((int)(180*alpha), 40, 140, 50) : Color.argb((int)(80*alpha), 60, 60, 70)); upgradeBtnPaint.setStyle(Paint.Style.FILL);
            tempRect.set(ubtnX, ubtnY, ubtnX + ubtnW, ubtnY + ubtnH); c.drawRoundRect(tempRect, ubtnH/2, ubtnH/2, upgradeBtnPaint);
            smallPaint.setTextSize(sw * 0.020f); smallPaint.setColor(canAfford ? Color.argb((int)(255*alpha), 255, 255, 255) : Color.argb((int)(100*alpha), 160, 160, 170)); smallPaint.setTextAlign(Paint.Align.CENTER);
            c.drawText("+" + cost + "✦", ubtnX + ubtnW/2, ubtnY + ubtnH * 0.7f, smallPaint); smallPaint.setTextAlign(Paint.Align.LEFT);
            if (upgradeFlashShipId == shipIndex && upgradeFlashStat == statIndex && upgradeFlashTimer > 0) {
                upgradeBtnPaint.setColor(Color.argb((int)(upgradeFlashTimer / 0.4f * 100), 100, 255, 120)); c.drawRoundRect(tempRect, ubtnH/2, ubtnH/2, upgradeBtnPaint);
            }
        } else if (isUnlocked && level >= ShipData.MAX_UPGRADE_LEVEL) {
            smallPaint.setTextSize(sw * 0.022f); smallPaint.setColor(Color.argb((int)(120*alpha), Color.red(statColor), Color.green(statColor), Color.blue(statColor))); c.drawText("MAX", barX + barW + sw * 0.02f, y + barH, smallPaint);
        } else {
            upgradeButtonRects[shipIndex][statIndex].setEmpty();
        }
    }

    private void drawCreditDisplay(Canvas c, EconomyManager economy, float alpha) {
        float cx = sw * 0.05f, cy = sh * 0.07f;
        int credits = economy.getDisplayedCredits(); float flash = economy.getCreditFlash();
        int r = (int)(200 + 55 * flash), g = (int)(180 + 75 * flash);
        btnTextPaint.setColor(Color.argb((int)(255 * alpha), Math.min(255, r), Math.min(255, g), 50));
        btnTextPaint.setTextSize(sw * 0.06f); btnTextPaint.setTextAlign(Paint.Align.LEFT); c.drawText("✦ " + credits, cx, cy, btnTextPaint); btnTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawInGameCredits(Canvas c, EconomyManager economy) {
        int credits = economy.getDisplayedCredits(); float flash = economy.getCreditFlash();
        int r = (int)(200 + 55 * flash), g = (int)(180 + 75 * flash);
        btnTextPaint.setColor(Color.argb(180, Math.min(255, r), Math.min(255, g), 50));
        btnTextPaint.setTextSize(sw * 0.045f); btnTextPaint.setTextAlign(Paint.Align.RIGHT); c.drawText("✦ " + credits, sw - sw * 0.04f, sh * 0.11f, btnTextPaint); btnTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawHUD(Canvas c, GameWorld world) {
        float pad = sw * 0.04f;
        hudLabelPaint.setAlpha(150); hudLabelPaint.setTextAlign(Paint.Align.LEFT); c.drawText("SCORE", pad, sh * 0.04f, hudLabelPaint);
        hudScorePaint.setColor(WHITE); c.drawText(String.valueOf(world.getScore()), pad, sh * 0.075f, hudScorePaint);
        
        if (fuelSystem != null) fuelSystem.draw(c);

        int tempo = world.getTempoPhase();
        if (tempo != Constants.TEMPO_CALM) {
            String tl = tempo == Constants.TEMPO_PRESSURE ? "▲ PRESSURE" : "★ REWARD";
            int tc = tempo == Constants.TEMPO_PRESSURE ? 0xFFFF1744 : GOLD;
            tempoPaint.setColor(tc); tempoPaint.setAlpha(180); tempoPaint.setTextAlign(Paint.Align.RIGHT);
            c.drawText(tl, sw - pad, sh * 0.095f, tempoPaint);
        }

        if (world.isRiskWindowActive()) {
            riskPaint.setAlpha((int)(220 * (Math.sin(pulse * 5) * 0.15 + 0.85))); c.drawText("⚡ RISK x1.5 ⚡", sw / 2f, sh * 0.16f, riskPaint);
        }

        float pbY = sh * 0.92f, pbH = sh * 0.012f, pbW = sw * 0.25f; int pbc = 0;
        if (world.isMagnetActive()) { drawPowerBar(c, pad, pbY - pbc*(pbH+8), pbW, pbH, "MAGNET", world.getMagnetTimer(), Constants.POWERUP_DURATION, PowerUp.getColor(Constants.POWERUP_MAGNET)); pbc++; }
        if (world.isSlowmoActive()) { drawPowerBar(c, pad, pbY - pbc*(pbH+8), pbW, pbH, "SLOW-MO", world.getSlowmoTimer(), Constants.POWERUP_DURATION, PowerUp.getColor(Constants.POWERUP_SLOWMO)); pbc++; }
        if (world.getEconomy() != null && world.getEconomy().isDoubleActive()) { drawPowerBar(c, pad, pbY - pbc*(pbH+8), pbW, pbH, "DOUBLE", world.getDoubleTimer(), Constants.POWERUP_DURATION, PowerUp.getColor(Constants.POWERUP_DOUBLE)); pbc++; }
    }

    private void drawPowerBar(Canvas c, float x, float y, float w, float h, String name, int timer, int max, int color) {
        hudLabelPaint.setTextAlign(Paint.Align.LEFT); hudLabelPaint.setAlpha(200); hudLabelPaint.setColor(color); c.drawText(name, x, y - 4, hudLabelPaint); hudLabelPaint.setColor(0x99FFFFFF);
        c.drawRoundRect(new RectF(x, y, x + w, y + h), h, h, powerBarBg);
        float f = (float) timer / max; powerBarFill.setColor(color); powerBarFill.setAlpha(f < 0.25f ? (int)(200 * (Math.sin(pulse * 8) * 0.3 + 0.7)) : 200); c.drawRoundRect(new RectF(x, y, x + w * f, y + h), h, h, powerBarFill);
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
        c.drawRect(0, 0, sw, sh, dimPaint);
        float cx = sw / 2f;

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
        float sx = sw*0.2f, sy = sh*0.48f, g = sh*0.04f, rx = sw*0.8f;
        int sa = (int)(200*a), va = (int)(255*a);
        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Orbs Collected", sx, sy, statPaint);
        statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText(String.valueOf(w.getOrbsCollected()), rx, sy, statPaint);

        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Near Misses", sx, sy+g, statPaint);
        statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText(String.valueOf(w.getNearMissCount()), rx, sy+g, statPaint);

        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Max Combo", sx, sy+g*2, statPaint);
        statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(w.getMaxCombo()>=5?GOLD:WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText("x"+w.getMaxCombo(), rx, sy+g*2, statPaint);

        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Survival Time", sx, sy+g*3, statPaint);
        statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText(w.getSurvivalTime()+"s", rx, sy+g*3, statPaint);
    }

    public boolean isCloseHit(float tx, float ty) { float cx = sw - sw * 0.1f, cy = sh * 0.08f; return (Math.pow(tx - cx, 2) + Math.pow(ty - cy, 2)) < Math.pow(CLOSE_BTN_HITBOX, 2); }

    public boolean handleSettingsTouch(float tx, float ty, GameWorld world) {
        if (isCloseHit(tx, ty)) { world.quitToMenu(); return true; }
        for (int i = 0; i < 3; i++) { if (settingsToggleRects[i].contains(tx, ty)) { difficultyLevel = i; saveSettings(); return true; } }
        for (int i = 0; i < 3; i++) { if (settingsToggleRects[3+i].contains(tx, ty)) { gameSpeedLevel = i; saveSettings(); return true; } }
        if (soundToggleRect.contains(tx, ty)) { soundEnabled = !soundEnabled; soundToggleCount++; if (soundToggleCount >= 10) godModeActive = true; saveSettings(); return true; }
        if (vibToggleRect.contains(tx, ty)) { vibrationEnabled = !vibrationEnabled; vibrationToggleCount++; if (vibrationToggleCount >= 5) godModeActive = false; saveSettings(); return true; }
        return false;
    }

    public boolean handleShopTouch(int action, float tx, float ty, ShipRegistry registry, EconomyManager economy) {
        if (!isShopOpen && shopOpenAnim < 0.01f) return false;
        switch (action) {
            case MotionEvent.ACTION_DOWN: touchDownY = (int) ty; lastTouchY = (int) ty; isDraggingShop = false; return true;
            case MotionEvent.ACTION_MOVE: int dy = lastTouchY - (int) ty; if (Math.abs((int) ty - touchDownY) > 15) isDraggingShop = true; if (isDraggingShop) scrollY = Math.max(0, Math.min(totalContentHeight - sh, scrollY + dy)); lastTouchY = (int) ty; return true;
            case MotionEvent.ACTION_UP:
                if (!isDraggingShop) {
                    if (isCloseHit(tx, ty)) { closeShop(); currentState = STATE_MAIN_MENU; return true; }
                    float adjY = ty + scrollY;
                    for (int i = 0; i < registry.getShipCount(); i++) {
                        ShipData ship = registry.getShip(i); float cardY = cardStartY + i * (cardHeight + cardSpacing); float cx = (sw - cardWidth) / 2;
                        
                        for (int st = 0; st < ShipData.STAT_COUNT; st++) {
                            RectF ubr = upgradeButtonRects[i][st];
                            if (!ubr.isEmpty() && tx >= ubr.left && tx <= ubr.right && adjY >= ubr.top && adjY <= ubr.bottom) {
                                if (economy.purchaseUpgrade(ship.id, st)) { economy.syncUpgradesToShipData(registry.getAllShips()); upgradeFlashShipId = i; upgradeFlashStat = st; upgradeFlashTimer = 0.4f; }
                                return true;
                            }
                        }

                        float btnW = sw * 0.35f, btnH = sh * 0.05f, btnX = cx + cardWidth - btnW - sw * 0.04f, btnY = cardY + cardHeight - btnH - sh * 0.02f;
                        if (tx >= btnX && tx <= btnX + btnW && adjY >= btnY && adjY <= btnY + btnH) {
                            if (economy.isShipUnlocked(ship.id)) { economy.selectShip(ship.id); registry.selectShip(ship.id); } 
                            else if (economy.purchaseShip(ship.id, ship.price)) { economy.selectShip(ship.id); registry.selectShip(ship.id); economy.syncUpgradesToShipData(registry.getAllShips()); purchaseFlashShipId = ship.id; purchaseFlashTimer = 0.8f; }
                            return true;
                        }
                    }
                }
                isDraggingShop = false; return true;
        } return true;
    }

    private void saveSettings() { if (settingsManager != null) { settingsManager.setDifficulty(difficultyLevel); settingsManager.setGameSpeed(gameSpeedLevel); settingsManager.setSoundEnabled(soundEnabled); settingsManager.setVibrationEnabled(vibrationEnabled); } }

    public void resetGameOver() { gameOverTime = 0; recordBroken = false; recordBreakParticleTimer = 0; }
    public void openShop() { isShopOpen = true; scrollY = 0; }
    public void closeShop() { isShopOpen = false; }
    public boolean isShopVisible() { return isShopOpen || shopOpenAnim > 0.01f; }
    public int getCurrentState() { return currentState; }
    public void setState(int state) { currentState = state; }

    public boolean isPlayHit(float x, float y) { return playBtn.contains(x, y); }
    public boolean isShopHit(float x, float y) { return shopBtn.contains(x, y); }
    public boolean isSettingsHit(float x, float y) { return settingsBtn.contains(x, y); }
    public boolean isRestartHit(float x, float y) { if (gameOverTime > 0 && System.currentTimeMillis() - gameOverTime < BUTTON_DELAY) return false; return restartBtn.contains(x, y); }
}
