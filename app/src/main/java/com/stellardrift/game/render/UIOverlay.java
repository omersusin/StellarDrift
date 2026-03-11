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
    private float screenW, screenH;

    // ── İZOLE PAINT'LER (Paint State Leak Fix) ──
    private final Paint bgBlackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private final Paint menuTitlePaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuTitlePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuBtnBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuBtnBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuBtnTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint menuCreditPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint smallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hudScorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hudLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint comboPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint statPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint milestonePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tempoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint riskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highScoreLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highScoreTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint closeBtnCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint closeBtnXPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private final Paint diffBarBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint diffBarFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint powerBarBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint powerBarFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint statBarBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint statBarFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint upgradeBtnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint creditPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedGlow = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── REUSABLE RECTS ──
    private final RectF playBtn = new RectF();
    private final RectF settingsBtn = new RectF();
    private final RectF shopBtn = new RectF();
    private final RectF restartBtn = new RectF();
    private final RectF tempRect = new RectF();
    private final RectF btnRect = new RectF();

    private final RectF[] settingsToggleRects = new RectF[6];
    private final RectF soundToggleRect = new RectF();
    private final RectF vibToggleRect = new RectF();
    
    private RectF[][] upgradeButtonRects;
    private RectF[] mainButtonRects;

    // ── STATE DEĞİŞKENLERİ ──
    private float pulse;
    private long gameOverTime;
    private static final long BUTTON_DELAY = 1200;
    
    private boolean recordBroken = false;
    private float recordBreakParticleTimer = 0;

    private boolean isShopOpen = false;
    private float shopOpenAnim = 0f;
    private float scrollY = 0f, scrollVelocity = 0f;
    private int touchDownY = -1, lastTouchY = -1;
    private boolean isDraggingShop = false;
    
    private float purchaseFlashTimer = 0f;
    private int purchaseFlashShipId = -1;
    private float upgradeFlashTimer = 0f;
    private int upgradeFlashShipId = -1, upgradeFlashStat = -1;

    private int difficultyLevel = 1, gameSpeedLevel = 1;
    private boolean soundEnabled = true, vibrationEnabled = true;

    private FuelSystem fuelSystem;
    private SettingsManager settingsManager;
    private ShipRegistry shipRegistry;

    // ── KART LAYOUT (Orantılı) ──
    private float cardWidth, cardHeight, cardSpacing, cardStartY, cardLeftX;
    private float cardPadding, previewSize, previewCenterX;
    private float infoPanelX, infoPanelWidth;
    private float statLabelWidth, statBarWidth, upgradeBtnWidth, upgradeBtnHeight;
    private float statGap, barBtnGap, mainBtnWidth, mainBtnHeight, cardRight;
    private float totalContentHeight;

    public UIOverlay(float sw, float sh) {
        this.screenW = sw; 
        this.screenH = sh; 
        pulse = 0; 
        gameOverTime = 0;

        initPaints();
        initLayout(sw, sh);
        
        for (int i = 0; i < 6; i++) settingsToggleRects[i] = new RectF();
        
        float cx = sw / 2f, bw = sw * 0.65f, bh = sh * 0.075f;
        restartBtn.set(cx - bw/2, sh * 0.76f, cx + bw/2, sh * 0.76f + bh);
    }

    private void initLayout(float w, float h) {
        cardPadding = w * 0.03f;
        cardWidth = w - cardPadding * 2;
        cardHeight = h * 0.33f;
        cardHeight = Math.max(260, Math.min(320, cardHeight));
        cardSpacing = h * 0.015f;
        cardStartY = h * 0.11f;
        cardLeftX = cardPadding;

        float innerPad = cardWidth * 0.035f;

        previewSize = cardWidth * 0.22f;
        previewSize = Math.min(previewSize, 100);
        previewCenterX = cardLeftX + innerPad + previewSize / 2;

        float previewAreaWidth = previewSize + innerPad * 2;
        infoPanelX = cardLeftX + previewAreaWidth;
        infoPanelWidth = cardWidth - previewAreaWidth - innerPad;

        statLabelWidth = infoPanelWidth * 0.12f;
        statGap = infoPanelWidth * 0.02f;
        upgradeBtnWidth = infoPanelWidth * 0.26f;
        upgradeBtnHeight = cardHeight * 0.06f;
        upgradeBtnHeight = Math.max(16, Math.min(22, upgradeBtnHeight));
        barBtnGap = infoPanelWidth * 0.02f;

        statBarWidth = infoPanelWidth - statLabelWidth - statGap - barBtnGap - upgradeBtnWidth;
        statBarWidth = Math.max(40, statBarWidth);

        mainBtnWidth = infoPanelWidth * 0.48f;
        mainBtnWidth = Math.max(100, Math.min(140, mainBtnWidth));
        mainBtnHeight = cardHeight * 0.12f;
        mainBtnHeight = Math.max(30, Math.min(38, mainBtnHeight));

        cardRight = cardLeftX + cardWidth;
    }

    private void initPaints() {
        bgBlackPaint.setStyle(Paint.Style.FILL);
        bgOverlayPaint.setStyle(Paint.Style.FILL);
        cardPaint.setStyle(Paint.Style.FILL);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);
        
        selectedGlow.setStyle(Paint.Style.STROKE);
        selectedGlow.setStrokeWidth(2f);
        selectedGlow.setColor(Color.rgb(80, 255, 160));
        
        statBarBg.setStyle(Paint.Style.FILL);
        statBarFill.setStyle(Paint.Style.FILL);
        dotPaint.setStyle(Paint.Style.FILL);
        upgradeBtnPaint.setStyle(Paint.Style.FILL);
        buttonPaint.setStyle(Paint.Style.FILL);
        
        dividerPaint.setColor(Color.argb(30, 150, 170, 200));
        dividerPaint.setStrokeWidth(1f);
        
        closeXPaint.setStyle(Paint.Style.STROKE);
        closeXPaint.setStrokeWidth(2.5f);
        closeXPaint.setStrokeCap(Paint.Cap.ROUND);
        closeXPaint.setColor(Color.WHITE);

        titlePaint = makePaint(0xFF00E5FF, screenW * 0.1f, Paint.Align.CENTER, true);
        subtitlePaint = makePaint(Color.WHITE, screenW * 0.04f, Paint.Align.CENTER, false);
        scorePaint = makePaint(Color.WHITE, screenW * 0.08f, Paint.Align.CENTER, true);
        smallPaint = makePaint(0xFFB0BEC5, screenW * 0.035f, Paint.Align.CENTER, false);
        
        dimPaint.setColor(0xCC050510); dimPaint.setStyle(Paint.Style.FILL);
        labelPaint = makePaint(0xFFB0BEC5, screenW * 0.04f, Paint.Align.LEFT, false);
        valuePaint = makePaint(Color.WHITE, screenW * 0.04f, Paint.Align.RIGHT, true);
        accentPaint = makePaint(0xFF00E5FF, screenW * 0.06f, Paint.Align.CENTER, true);
        hudScorePaint = makePaint(Color.WHITE, screenW * 0.06f, Paint.Align.LEFT, true);
        hudLabelPaint = makePaint(0x99FFFFFF, screenW * 0.025f, Paint.Align.LEFT, false);
        comboPaint = makePaint(0xFFFFD740, screenW * 0.06f, Paint.Align.CENTER, true);
        statPaint = makePaint(0xFFB0BEC5, screenW * 0.035f, Paint.Align.LEFT, false);
        milestonePaint = makePaint(0xFFFFD740, screenW * 0.07f, Paint.Align.CENTER, true);
        tempoPaint = makePaint(Color.WHITE, screenW * 0.025f, Paint.Align.RIGHT, false);
        riskPaint = makePaint(0xFFFFD740, screenW * 0.03f, Paint.Align.CENTER, true);
        highScoreTextPaint = makePaint(0xFFFFD740, screenW * 0.03f, Paint.Align.RIGHT, true);

        diffBarBg.setColor(0x33FFFFFF); diffBarFill.setColor(0xFF00E5FF);
        powerBarBg.setColor(0x33FFFFFF); powerBarFill.setColor(Color.WHITE);
        highScoreLinePaint.setColor(0xFFFFD740); highScoreLinePaint.setStrokeWidth(3f);
    }

    private Paint makePaint(int color, float size, Paint.Align align, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(color); p.setTextSize(size); p.setTextAlign(align);
        if (bold) p.setTypeface(Typeface.DEFAULT_BOLD); return p;
    }

    private void resetAllPaintsToSafeState() {
        Paint[] allPaints = { titlePaint, subtitlePaint, menuBtnBgPaint, menuBtnBorderPaint, menuBtnTextPaint, menuCreditPaint, cardPaint, borderPaint, textPaint };
        for (Paint p : allPaints) {
            if (p == null) continue;
            p.setShader(null); p.setMaskFilter(null); p.setColorFilter(null); p.setAlpha(255); p.setFakeBoldText(false); p.setStrikeThruText(false); p.setUnderlineText(false);
        }
    }

    public void setState(int newState) {
        if (currentState != newState) resetAllPaintsToSafeState();
        currentState = newState;
    }

    public int getCurrentState() { return currentState; }

    public void setFuelSystem(FuelSystem fs) { this.fuelSystem = fs; }

    public void initPrefs(SettingsManager settings, ShipRegistry registry) {
        this.settingsManager = settings;
        this.shipRegistry = registry;
        difficultyLevel = settings.getDifficulty(); gameSpeedLevel = settings.getGameSpeed();
        soundEnabled = settings.isSoundEnabled(); vibrationEnabled = settings.isVibrationEnabled();

        int shipCount = registry.getShipCount();
        upgradeButtonRects = new RectF[shipCount][ShipData.STAT_COUNT];
        mainButtonRects = new RectF[shipCount];
        for (int s = 0; s < shipCount; s++) {
            mainButtonRects[s] = new RectF();
            for (int st = 0; st < ShipData.STAT_COUNT; st++) upgradeButtonRects[s][st] = new RectF();
        }
        totalContentHeight = cardStartY + shipCount * (cardHeight + cardSpacing) + screenH * 0.1f;
    }

    public void update(float dt) {
        float target = isShopOpen ? 1f : 0f;
        shopOpenAnim += (target - shopOpenAnim) * dt * 10f;
        if (purchaseFlashTimer > 0) purchaseFlashTimer -= dt;
        if (upgradeFlashTimer > 0) upgradeFlashTimer -= dt;
        if (isShopOpen && !isDraggingShop) {
            scrollY += scrollVelocity * dt; scrollVelocity *= 0.92f;
            scrollY = Math.max(0, Math.min(totalContentHeight - screenH, scrollY));
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

    // ═══════════════════════════════════════════════════
    //  ANA MENÜ (Tamamen İzole Boyalar)
    // ═══════════════════════════════════════════════════
    private void drawMenu(Canvas c, int highScore, EconomyManager economy) {
        resetAllPaintsToSafeState();
        c.drawColor(Color.BLACK);

        menuTitlePaint1.setShader(null); menuTitlePaint1.setAlpha(255); menuTitlePaint1.setColor(Color.WHITE); menuTitlePaint1.setTextSize(screenW * 0.12f); menuTitlePaint1.setTextAlign(Paint.Align.CENTER); menuTitlePaint1.setFakeBoldText(true); menuTitlePaint1.setStyle(Paint.Style.FILL);
        float titleY = screenH * 0.18f; c.drawText("STELLAR", screenW / 2f, titleY, menuTitlePaint1);

        menuTitlePaint2.setShader(null); menuTitlePaint2.setAlpha(255); menuTitlePaint2.setColor(Color.rgb(100, 200, 255)); menuTitlePaint2.setTextSize(screenW * 0.14f); menuTitlePaint2.setTextAlign(Paint.Align.CENTER); menuTitlePaint2.setFakeBoldText(true); menuTitlePaint2.setStyle(Paint.Style.FILL);
        c.drawText("DRIFT", screenW / 2f, titleY + screenH * 0.06f, menuTitlePaint2);

        menuSubPaint.setShader(null); menuSubPaint.setAlpha(255); menuSubPaint.setColor(Color.argb(120, 180, 190, 210)); menuSubPaint.setTextSize(screenW * 0.035f); menuSubPaint.setTextAlign(Paint.Align.CENTER); menuSubPaint.setFakeBoldText(false); menuSubPaint.setStyle(Paint.Style.FILL);
        c.drawText("v4.7 — The Masterpiece", screenW / 2f, titleY + screenH * 0.09f, menuSubPaint);
        
        drawMenuButton(c, "▶ PLAY", screenH * 0.40f, screenH * 0.075f, Color.argb(190, 20, 30, 50), Color.argb(160, 80, 180, 255), Color.WHITE, playBtn);
        drawMenuButton(c, "✦ SHOP", screenH * 0.50f, screenH * 0.07f, Color.argb(180, 30, 28, 40), Color.argb(150, 200, 170, 40), Color.rgb(230, 200, 60), shopBtn);
        drawMenuButton(c, "⚙ SETTINGS", screenH * 0.59f, screenH * 0.065f, Color.argb(170, 25, 28, 40), Color.argb(130, 120, 130, 160), Color.rgb(180, 190, 210), settingsBtn);
        
        menuSubPaint.setAlpha((int)(150 * (Math.sin(pulse * 1.5) * 0.3 + 0.7))); 
        c.drawText("Destroy asteroids & upgrade ship!", screenW / 2f, screenH * 0.88f, menuSubPaint);

        menuCreditPaint.setShader(null); menuCreditPaint.setAlpha(255); menuCreditPaint.setColor(Color.rgb(200, 180, 50)); menuCreditPaint.setTextSize(screenW * 0.05f); menuCreditPaint.setTextAlign(Paint.Align.CENTER); menuCreditPaint.setFakeBoldText(true); menuCreditPaint.setStyle(Paint.Style.FILL);
        int credits = (economy != null) ? economy.getDisplayedCredits() : 0;
        c.drawText("✦ " + credits, screenW / 2f, screenH * 0.76f, menuCreditPaint);

        menuSubPaint.setColor(Color.argb(100, 160, 170, 190));
        c.drawText("Best: " + highScore, screenW / 2f, screenH * 0.81f, menuSubPaint);
    }

    private void drawMenuButton(Canvas c, String text, float y, float h, int bgCol, int borderCol, int textCol, RectF outRect) {
        float w = screenW * 0.55f; float bx = screenW / 2f - w / 2f;
        outRect.set(bx, y, bx + w, y + h);
        
        menuBtnBgPaint.setShader(null); menuBtnBgPaint.setStyle(Paint.Style.FILL); menuBtnBgPaint.setColor(bgCol); menuBtnBgPaint.setAlpha(Color.alpha(bgCol));
        c.drawRoundRect(outRect, h/2, h/2, menuBtnBgPaint);
        
        menuBtnBorderPaint.setShader(null); menuBtnBorderPaint.setStyle(Paint.Style.STROKE); menuBtnBorderPaint.setStrokeWidth(3f); menuBtnBorderPaint.setColor(borderCol); menuBtnBorderPaint.setAlpha(Color.alpha(borderCol));
        c.drawRoundRect(outRect, h/2, h/2, menuBtnBorderPaint);
        
        menuBtnTextPaint.setShader(null); menuBtnTextPaint.setColor(textCol); menuBtnTextPaint.setAlpha(255); menuBtnTextPaint.setTextSize(h * 0.35f); menuBtnTextPaint.setTextAlign(Paint.Align.CENTER); menuBtnTextPaint.setFakeBoldText(true); menuBtnTextPaint.setStyle(Paint.Style.FILL);
        c.drawText(text, screenW / 2f, y + h/2 + h * 0.12f, menuBtnTextPaint);
    }

    // ==========================================
    // SHOP (MARKET) EKRANI — ORANTILI & SIFIR TAŞMA
    // ==========================================
    private void drawShop(Canvas c, ShipRegistry registry, EconomyManager economy, ShipRenderer renderer) {
        float al = shopOpenAnim;

        int blackAlpha = (int)(255 * al);
        bgBlackPaint.setColor(Color.argb(blackAlpha, 0, 0, 0));
        c.drawRect(0, 0, screenW, screenH, bgBlackPaint);

        int overlayAlpha = (int)(245 * al);
        bgOverlayPaint.setColor(Color.argb(overlayAlpha, 6, 8, 16));
        c.drawRect(0, 0, screenW, screenH, bgOverlayPaint);

        c.save(); c.translate(0, -scrollY);
        
        titlePaint.setShader(null); titlePaint.setColor(Color.WHITE); titlePaint.setTextSize(Math.min(24, screenW * 0.055f)); titlePaint.setAlpha((int)(255 * al)); titlePaint.setTextAlign(Paint.Align.CENTER); titlePaint.setFakeBoldText(true); titlePaint.setStyle(Paint.Style.FILL);
        c.drawText("✦ HANGAR ✦", screenW / 2f, cardStartY - 20, titlePaint); 

        for (int i = 0; i < registry.getShipCount(); i++) {
            ShipData ship = registry.getShip(i);
            float cardY = cardStartY + i * (cardHeight + cardSpacing);
            if (cardY - scrollY > screenH + 50 || cardY + cardHeight - scrollY < -50) continue;
            drawShipCard(c, ship, i, cardY, al, economy, renderer);
        }
        c.restore();

        drawCloseButton(c, screenW - 45, 45, al);
        drawCreditBadge(c, economy, al);
    }

    private void drawShipCard(Canvas c, ShipData ship, int index, float cardY, float al, EconomyManager economy, ShipRenderer renderer) {
        boolean isSelected = (economy.getSelectedShipId() == ship.id);
        boolean isUnlocked = economy.isShipUnlocked(ship.id);

        tempRect.set(cardLeftX, cardY, cardRight, cardY + cardHeight);
        int bgColor = isSelected ? Color.argb((int)(200 * al), 18, 35, 28) : Color.argb((int)(200 * al), 16, 18, 28);
        cardPaint.setColor(bgColor); cardPaint.setStyle(Paint.Style.FILL);
        c.drawRoundRect(tempRect, 14, 14, cardPaint);

        if (isSelected) {
            selectedGlow.setAlpha((int)(200 * al)); c.drawRoundRect(tempRect, 14, 14, selectedGlow);
        } else {
            borderPaint.setColor(Color.argb((int)(50 * al), 100, 120, 160)); c.drawRoundRect(tempRect, 14, 14, borderPaint);
        }

        float prevCY = cardY + cardHeight * 0.38f;
        cardPaint.setColor(Color.argb((int)(35 * al), 80, 100, 140));
        c.drawCircle(previewCenterX, prevCY, previewSize * 0.48f, cardPaint);

        float shipScale = Math.max(1.2f, Math.min(1.8f, previewSize / 70f));
        renderer.drawShip(c, ship, previewCenterX, prevCY, 0f, (int)(255 * al), shipScale, false);

        float textStartY = cardY + cardHeight * 0.1f;
        textPaint.setShader(null); textPaint.setAlpha((int)(255 * al)); textPaint.setTextSize(Math.min(18, screenW * 0.04f)); textPaint.setFakeBoldText(true); textPaint.setTextAlign(Paint.Align.LEFT); textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.argb((int)(255 * al), Color.red(ship.cockpitColor), Color.green(ship.cockpitColor), Color.blue(ship.cockpitColor)));
        c.drawText(ship.name, infoPanelX, textStartY + 16, textPaint);

        textPaint.setTextSize(Math.min(11, screenW * 0.025f)); textPaint.setFakeBoldText(false); textPaint.setColor(Color.argb((int)(140 * al), 190, 195, 210));
        String desc = ship.description; float descMaxW = infoPanelWidth - 10;
        if (textPaint.measureText(desc) > descMaxW) { while (desc.length() > 3 && textPaint.measureText(desc + "...") > descMaxW) desc = desc.substring(0, desc.length() - 1); desc += "..."; }
        c.drawText(desc, infoPanelX, textStartY + 34, textPaint);

        float dividerY = textStartY + 42;
        dividerPaint.setColor(Color.argb((int)(30 * al), 150, 170, 200));
        c.drawLine(infoPanelX, dividerY, infoPanelX + infoPanelWidth - 5, dividerY, dividerPaint);

        float statStartY = dividerY + 12;
        float statRowH = Math.max(22, Math.min(30, cardHeight * 0.13f));
        float barH = Math.max(7, Math.min(10, statRowH * 0.35f));

        for (int st = 0; st < ShipData.STAT_COUNT; st++) {
            drawStatRowWithUpgrade(c, ship, index, st, statStartY + st * statRowH, barH, al, isUnlocked, economy);
        }

        float mainBtnX = cardRight - mainBtnWidth - cardWidth * 0.04f;
        float mainBtnY = cardY + cardHeight - mainBtnHeight - cardHeight * 0.05f;
        mainButtonRects[index].set(mainBtnX, mainBtnY, mainBtnX + mainBtnWidth, mainBtnY + mainBtnHeight);
        drawMainButton(c, ship, index, isSelected, isUnlocked, al, economy);

        if (purchaseFlashShipId == ship.id && purchaseFlashTimer > 0) {
            cardPaint.setColor(Color.argb((int)(purchaseFlashTimer / 0.8f * 60), 255, 220, 50));
            tempRect.set(cardLeftX, cardY, cardRight, cardY + cardHeight);
            c.drawRoundRect(tempRect, 14, 14, cardPaint);
        }
    }

    private void drawStatRowWithUpgrade(Canvas canvas, ShipData ship, int shipIndex, int statIndex, float rowY, float barH, float al, boolean isUnlocked, EconomyManager economy) {
        float labelX = infoPanelX;
        float barX = infoPanelX + statLabelWidth + statGap;
        float upgBtnX = barX + statBarWidth + barBtnGap;
        
        float maxBtnRight = cardRight - cardWidth * 0.03f;
        if (upgBtnX + upgradeBtnWidth > maxBtnRight) upgBtnX = maxBtnRight - upgradeBtnWidth;

        String statName = ship.getStatName(statIndex); int statColor = ship.getStatColor(statIndex); float ratio = ship.getStatBarRatio(statIndex); int level = ship.getUpgradeLevel(statIndex);

        textPaint.setTextSize(Math.min(10, screenW * 0.023f)); textPaint.setFakeBoldText(true); textPaint.setColor(Color.argb((int)(160 * al), 180, 185, 200)); textPaint.setTextAlign(Paint.Align.LEFT); textPaint.setStyle(Paint.Style.FILL); textPaint.setShader(null);
        canvas.drawText(statName, labelX, rowY + barH + 1, textPaint);

        tempRect.set(barX, rowY, barX + statBarWidth, rowY + barH);
        statBarBg.setColor(Color.argb((int)(80 * al), 40, 45, 60));
        canvas.drawRoundRect(tempRect, barH / 2, barH / 2, statBarBg);

        float fillWidth = statBarWidth * Math.max(0, Math.min(1, ratio));
        if (fillWidth > barH) {
            tempRect.set(barX, rowY, barX + fillWidth, rowY + barH);
            statBarFill.setColor(Color.argb((int)(200 * al), Color.red(statColor), Color.green(statColor), Color.blue(statColor)));
            canvas.drawRoundRect(tempRect, barH / 2, barH / 2, statBarFill);
        }

        float dotY = rowY - 3; float dotR = Math.max(1.5f, barH * 0.2f); float dotSpacing = statBarWidth / ShipData.MAX_UPGRADE_LEVEL;
        for (int lv = 0; lv < ShipData.MAX_UPGRADE_LEVEL; lv++) {
            float dotX = barX + dotSpacing * (lv + 0.5f);
            if (lv < level) dotPaint.setColor(Color.argb((int)(220 * al), Color.red(statColor), Color.green(statColor), Color.blue(statColor)));
            else dotPaint.setColor(Color.argb((int)(40 * al), 150, 160, 180));
            canvas.drawCircle(dotX, dotY, dotR, dotPaint);
        }

        float ubtnY = rowY + (barH - upgradeBtnHeight) / 2;

        if (isUnlocked && level < ShipData.MAX_UPGRADE_LEVEL) {
            int cost = ship.getUpgradeCost(statIndex); boolean canAfford = economy.getCredits() >= cost;
            upgradeButtonRects[shipIndex][statIndex].set(upgBtnX, ubtnY, upgBtnX + upgradeBtnWidth, ubtnY + upgradeBtnHeight);
            int btnColor = canAfford ? Color.argb((int)(180 * al), 40, 140, 50) : Color.argb((int)(80 * al), 55, 58, 65);
            upgradeBtnPaint.setColor(btnColor); upgradeBtnPaint.setStyle(Paint.Style.FILL);
            tempRect.set(upgBtnX, ubtnY, upgBtnX + upgradeBtnWidth, ubtnY + upgradeBtnHeight);
            canvas.drawRoundRect(tempRect, upgradeBtnHeight / 2, upgradeBtnHeight / 2, upgradeBtnPaint);

            float btnTextSize = Math.max(7, Math.min(9, upgradeBtnHeight * 0.55f));
            textPaint.setTextSize(btnTextSize); textPaint.setFakeBoldText(true); textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(canAfford ? Color.argb((int)(255 * al), 255, 255, 255) : Color.argb((int)(100 * al), 140, 145, 155));
            canvas.drawText("+" + cost + "✦", upgBtnX + upgradeBtnWidth / 2, ubtnY + upgradeBtnHeight / 2 + btnTextSize * 0.35f, textPaint);

            if (upgradeFlashShipId == shipIndex && upgradeFlashStat == statIndex && upgradeFlashTimer > 0) {
                upgradeBtnPaint.setColor(Color.argb((int)(upgradeFlashTimer / 0.4f * 80), 100, 255, 120));
                canvas.drawRoundRect(tempRect, upgradeBtnHeight / 2, upgradeBtnHeight / 2, upgradeBtnPaint);
            }
            textPaint.setTextAlign(Paint.Align.LEFT);
        } else if (isUnlocked && level >= ShipData.MAX_UPGRADE_LEVEL) {
            upgradeButtonRects[shipIndex][statIndex].setEmpty();
            float maxTextSize = Math.min(8, upgradeBtnHeight * 0.5f);
            textPaint.setTextSize(maxTextSize); textPaint.setFakeBoldText(true); textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(Color.argb((int)(120 * al), Color.red(statColor), Color.green(statColor), Color.blue(statColor)));
            canvas.drawText("MAX", upgBtnX + upgradeBtnWidth / 2, ubtnY + upgradeBtnHeight / 2 + maxTextSize * 0.35f, textPaint);
        } else {
            upgradeButtonRects[shipIndex][statIndex].setEmpty();
            float lockSize = Math.min(8, upgradeBtnHeight * 0.5f);
            textPaint.setTextSize(lockSize); textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(Color.argb((int)(50 * al), 120, 125, 140));
            canvas.drawText("🔒", upgBtnX + upgradeBtnWidth / 2, ubtnY + upgradeBtnHeight / 2 + lockSize * 0.35f, textPaint);
        }
        textPaint.setTextAlign(Paint.Align.LEFT); textPaint.setFakeBoldText(false);
    }

    private void drawMainButton(Canvas canvas, ShipData ship, int index, boolean isSelected, boolean isUnlocked, float al, EconomyManager economy) {
        RectF br = mainButtonRects[index];
        float btnTextSize = Math.max(10, Math.min(13, mainBtnHeight * 0.4f));
        textPaint.setShader(null); textPaint.setTextAlign(Paint.Align.CENTER); textPaint.setFakeBoldText(true); textPaint.setTextSize(btnTextSize); textPaint.setStyle(Paint.Style.FILL);
        float textY = br.centerY() + btnTextSize * 0.35f; float radius = mainBtnHeight / 2;

        if (isSelected) {
            buttonPaint.setColor(Color.argb((int)(170 * al), 35, 150, 70)); buttonPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(br, radius, radius, buttonPaint);
            textPaint.setColor(Color.argb((int)(255 * al), 255, 255, 255));
            canvas.drawText("✓ EQUIPPED", br.centerX(), textY, textPaint);
        } else if (isUnlocked) {
            buttonPaint.setColor(Color.argb((int)(170 * al), 50, 110, 170)); buttonPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(br, radius, radius, buttonPaint);
            textPaint.setColor(Color.argb((int)(255 * al), 255, 255, 255));
            canvas.drawText("EQUIP", br.centerX(), textY, textPaint);
        } else {
            boolean canAfford = economy.getCredits() >= ship.price;
            buttonPaint.setColor(canAfford ? Color.argb((int)(190 * al), 190, 155, 30) : Color.argb((int)(90 * al), 65, 65, 72)); buttonPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(br, radius, radius, buttonPaint);
            textPaint.setColor(canAfford ? Color.argb((int)(255 * al), 255, 255, 255) : Color.argb((int)(140 * al), 160, 160, 170));
            String priceText = "🔒 " + ship.price + " ✦";
            if (textPaint.measureText(priceText) > br.width() - 10) textPaint.setTextSize(btnTextSize - 2);
            canvas.drawText(priceText, br.centerX(), textY, textPaint);
        }
        textPaint.setTextAlign(Paint.Align.LEFT); textPaint.setFakeBoldText(false);
    }

    private void drawCreditBadge(Canvas canvas, EconomyManager economy, float al) {
        float badgeW = screenW * 0.28f; badgeW = Math.max(90, Math.min(130, badgeW));
        float badgeH = 26; float badgeX = screenW - badgeW - 15; float badgeY = 25 + scrollY;

        tempRect.set(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH);
        cardPaint.setColor(Color.argb((int)(150 * al), 10, 10, 20)); cardPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(tempRect, 12, 12, cardPaint);
        borderPaint.setColor(Color.argb((int)(70 * al), 200, 175, 45));
        canvas.drawRoundRect(tempRect, 12, 12, borderPaint);

        float flash = economy.getCreditFlash(); int cr = Math.min(255, (int)(200 + 55 * flash)); int cg = Math.min(255, (int)(180 + 75 * flash));
        creditPaint.setShader(null); creditPaint.setColor(Color.argb((int)(255 * al), cr, cg, 50)); creditPaint.setTextSize(Math.min(15, badgeH * 0.55f)); creditPaint.setTextAlign(Paint.Align.CENTER); creditPaint.setFakeBoldText(true); creditPaint.setStyle(Paint.Style.FILL);
        canvas.drawText("✦ " + economy.getDisplayedCredits(), badgeX + badgeW / 2, badgeY + badgeH / 2 + 5, creditPaint);
    }

    private void drawCloseButton(Canvas canvas, float cx, float cy, float alpha) {
        closeBtnCirclePaint.setColor(Color.argb((int)(180*alpha), 200, 50, 50));
        c.drawCircle(cx, cy, sw * 0.05f, closeBtnCirclePaint);
        closeBtnXPaint.setColor(Color.argb((int)(240*alpha), 255, 255, 255)); 
        closeBtnXPaint.setStrokeWidth(5f);
        float cross = sw * 0.02f;
        c.drawLine(cx - cross, cy - cross, cx + cross, cy + cross, closeBtnXPaint);
        c.drawLine(cx + cross, cy - cross, cx - cross, cy + cross, closeBtnXPaint);
    }

    // Touch Event Handling
    public boolean handleShopTouch(int action, float tx, float ty, ShipRegistry registry, EconomyManager economy, SoundManager sm, VibrationManager vm) {
        if (!isShopOpen && shopOpenAnim < 0.01f) return false;
        switch (action) {
            case MotionEvent.ACTION_DOWN: touchDownY = (int) ty; lastTouchY = (int) ty; isDraggingShop = false; return true;
            case MotionEvent.ACTION_MOVE: int dy = lastTouchY - (int) ty; if (Math.abs((int) ty - touchDownY) > 15) isDraggingShop = true; if (isDraggingShop) scrollY = Math.max(0, Math.min(totalContentHeight - sh, scrollY + dy)); lastTouchY = (int) ty; return true;
            case MotionEvent.ACTION_UP:
                if (!isDraggingShop) {
                    float closeCx = screenW - 45; float closeCy = 45;
                    float dx = tx - closeCx; float dy = ty - closeCy;
                    if (dx*dx + dy*dy < CLOSE_BTN_HITBOX * CLOSE_BTN_HITBOX) { closeShop(); currentState = STATE_MAIN_MENU; if(sm!=null) sm.playMenuClick(); return true; }
                    float adjY = ty + scrollY;
                    for (int i = 0; i < registry.getShipCount(); i++) {
                        ShipData ship = registry.getShip(i);
                        for (int st = 0; st < ShipData.STAT_COUNT; st++) {
                            RectF ubr = upgradeButtonRects[i][st];
                            if (!ubr.isEmpty() && tx >= ubr.left && tx <= ubr.right && adjY >= ubr.top && adjY <= ubr.bottom) {
                                if (economy.purchaseUpgrade(ship.id, st)) { economy.syncUpgradesToShipData(registry.getAllShips()); upgradeFlashShipId = i; upgradeFlashStat = st; upgradeFlashTimer = 0.4f; if(sm!=null) sm.playUpgrade(); if(vm!=null) vm.vibrateUpgrade(); } else { if(sm!=null) sm.playError(); if(vm!=null) vm.vibrateError(); }
                                return true;
                            }
                        }
                        RectF mbr = mainButtonRects[i];
                        if (tx >= mbr.left && tx <= mbr.right && adjY >= mbr.top && adjY <= mbr.bottom) {
                            if (economy.isShipUnlocked(ship.id)) { economy.selectShip(ship.id); registry.selectShip(ship.id); if(sm!=null) sm.playMenuClick(); } 
                            else if (economy.purchaseShip(ship.id, ship.price)) { economy.selectShip(ship.id); registry.selectShip(ship.id); economy.syncUpgradesToShipData(registry.getAllShips()); purchaseFlashShipId = ship.id; purchaseFlashTimer = 0.8f; if(sm!=null) sm.playPurchase(); if(vm!=null) vm.vibratePurchase(); } else { if(sm!=null) sm.playError(); if(vm!=null) vm.vibrateError(); }
                            return true;
                        }
                    }
                }
                isDraggingShop = false; return true;
        } return true;
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

    public boolean isCloseHit(float tx, float ty) { float cx = sw - sw * 0.1f, cy = sh * 0.08f; return (Math.pow(tx - cx, 2) + Math.pow(ty - cy, 2)) < Math.pow(CLOSE_BTN_HITBOX, 2); }

    public boolean handleSettingsTouch(float tx, float ty, GameWorld world, SoundManager sm, VibrationManager vm) {
        if (isCloseHit(tx, ty)) { world.quitToMenu(); if (sm != null && sm.isEnabled()) sm.playMenuClick(); if (vm != null && vm.isEnabled()) vm.vibrateMenuClick(); return true; }
        for (int i = 0; i < 3; i++) { if (settingsToggleRects[i].contains(tx, ty)) { difficultyLevel = i; saveSettings(sm); if(sm!=null) sm.playMenuClick(); return true; } }
        for (int i = 0; i < 3; i++) { if (settingsToggleRects[3+i].contains(tx, ty)) { gameSpeedLevel = i; saveSettings(sm); if(sm!=null) sm.playMenuClick(); return true; } }
        if (soundToggleRect.contains(tx, ty)) { soundEnabled = !soundEnabled; soundToggleCount++; if (soundToggleCount >= 10) godModeActive = true; saveSettings(sm); if(sm!=null) sm.playMenuClick(); return true; }
        if (vibToggleRect.contains(tx, ty)) { vibrationEnabled = !vibrationEnabled; vibrationToggleCount++; if (vibrationToggleCount >= 5) godModeActive = false; saveSettings(sm); if(sm!=null) sm.playMenuClick(); return true; }
        return false;
    }

    private void saveSettings(SoundManager sm) { 
        if (settingsManager != null) { 
            settingsManager.setDifficulty(difficultyLevel); settingsManager.setGameSpeed(gameSpeedLevel); 
            settingsManager.setSoundEnabled(soundEnabled); settingsManager.setVibrationEnabled(vibrationEnabled); 
            if(sm!=null) { sm.setEnabled(soundEnabled); if(soundEnabled) sm.startDrone(); else sm.stopDrone(); }
        } 
    }

    public void openShop() { isShopOpen = true; scrollY = 0; }
    public void closeShop() { isShopOpen = false; }
    public boolean isShopVisible() { return isShopOpen || shopOpenAnim > 0.01f; }
    public boolean isPlayHit(float x, float y) { return playBtn.contains(x, y); }
    public boolean isShopHit(float x, float y) { return shopBtn.contains(x, y); }
    public boolean isSettingsHit(float x, float y) { return settingsBtn.contains(x, y); }
    public boolean isRestartHit(float x, float y) { if (gameOverTime > 0 && System.currentTimeMillis() - gameOverTime < BUTTON_DELAY) return false; return restartBtn.contains(x, y); }
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isVibrationEnabled() { return vibrationEnabled; }
}
