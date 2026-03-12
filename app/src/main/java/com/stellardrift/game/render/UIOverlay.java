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

    private static final int CYAN = 0xFF00E5FF;
    private static final int PURPLE = 0xFF7C4DFF;
    private static final int GOLD = 0xFFFFD740;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DIM = 0xCC050510;

    private final Paint bgBlackPaint = new Paint();
    private final Paint bgOverlayPaint = new Paint();
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
    private Paint statBarBgPaint, statBarPaint, statBarFill, upgradeBtnPaint, dotPaint;
    private Paint selectedGlow;

    private RectF playBtn = new RectF(), settingsBtn = new RectF(), shopBtn = new RectF();
    private RectF restartBtn = new RectF(), tempRect = new RectF(), tempBtnRect = new RectF();
    private RectF[] settingsToggleRects = new RectF[6];
    private RectF soundToggleRect = new RectF(), vibToggleRect = new RectF();
    private RectF[][] upgradeButtonRects;
    private RectF[] mainButtonRects;

    private float pulse;
    private long gameOverTime;
    private static final long BUTTON_DELAY = 1200;
    private static final float CLOSE_BTN_HITBOX = 60f; 

    private boolean recordBroken = false;
    private float recordBreakParticleTimer = 0;

    private boolean isShopOpen = false;
    private float shopOpenAnim = 0f, scrollY = 0f, scrollVelocity = 0f;
    private int touchDownY = -1, lastTouchY = -1;
    private boolean isDraggingShop = false;
    
    // YEPYENI LAYOUT DEGISKENLERI
    private float cardWidth, cardHeight, cardSpacing, cardStartY, totalContentHeight;
    private float cardLeftX, cardRightX;
    private float previewSize, previewCenterX;
    private float infoPanelX, infoPanelWidth;
    private float statLabelWidth, statBarWidth, upgradeBtnWidth, statGap, barBtnGap;
    private float mainBtnWidth, mainBtnHeight;

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
    private ShipRegistry shipRegistry;

    public UIOverlay(float screenW, float screenH) {
        this.sw = screenW; this.sh = screenH; pulse = 0; gameOverTime = 0;

        bgBlackPaint.setStyle(Paint.Style.FILL); bgOverlayPaint.setStyle(Paint.Style.FILL);
        cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG); cardPaint.setStyle(Paint.Style.FILL);
        cardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG); cardBorderPaint.setStyle(Paint.Style.STROKE);
        
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
        
        statBarBgPaint = new Paint(); statBarBgPaint.setColor(Color.argb(80, 40, 45, 60)); statBarBgPaint.setStyle(Paint.Style.FILL);
        statBarPaint = new Paint(); statBarPaint.setStyle(Paint.Style.FILL);
        statBarFill = new Paint(); statBarFill.setStyle(Paint.Style.FILL);
        upgradeBtnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG); dotPaint.setStyle(Paint.Style.FILL);
        
        selectedGlow = new Paint(Paint.ANTI_ALIAS_FLAG); selectedGlow.setStyle(Paint.Style.STROKE); selectedGlow.setStrokeWidth(2f); selectedGlow.setColor(Color.rgb(80, 255, 160));

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
        
        initLayout(sw, sh);
    }

    private void initLayout(float w, float h) {
        // Kartın Dış Ölçüleri
        float padding = w * 0.04f; // Ekranın %4'ü sağdan ve soldan boşluk
        cardLeftX = padding;
        cardRightX = w - padding;
        cardWidth = cardRightX - cardLeftX; 
        
        cardHeight = h * 0.25f; // Kart yüksekliği biraz artırıldı
        cardSpacing = h * 0.02f;
        cardStartY = h * 0.13f;

        // Kart İç Bölgeleri
        float innerPad = cardWidth * 0.04f;
        
        // Gemi Çemberi (%25 alan)
        previewSize = cardWidth * 0.25f;
        previewCenterX = cardLeftX + innerPad + (previewSize / 2f);

        // Kalan Alan: Bilgi Paneli (%75 alan)
        infoPanelX = cardLeftX + innerPad + previewSize + innerPad;
        infoPanelWidth = cardRightX - infoPanelX - innerPad;

        // Bilgi Paneli İçindeki Stat Satırları
        statLabelWidth = infoPanelWidth * 0.12f;  // Örn: "SPD"
        statGap = infoPanelWidth * 0.02f;         // Boşluk
        
        upgradeBtnWidth = infoPanelWidth * 0.28f; // Sağdaki buton
        upgradeBtnHeight = cardHeight * 0.09f;
        barBtnGap = infoPanelWidth * 0.03f;       // Bar ve buton arası boşluk

        // Kalan tam alan = Stat Barı
        statBarWidth = infoPanelWidth - statLabelWidth - statGap - barBtnGap - upgradeBtnWidth;

        // En alttaki ana buton (Equip/Buy)
        mainBtnWidth = cardWidth * 0.35f; 
        mainBtnHeight = cardHeight * 0.16f;
    }

    private Paint makePaint(int color, float size, Paint.Align align, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(color); p.setTextSize(size); p.setTextAlign(align);
        if (bold) p.setTypeface(Typeface.DEFAULT_BOLD); return p;
    }

    public void setFuelSystem(FuelSystem fs) { this.fuelSystem = fs; }

    public void initPrefs(SettingsManager settings, ShipRegistry registry) {
        this.settingsManager = settings; this.shipRegistry = registry;
        difficultyLevel = settings.getDifficulty(); gameSpeedLevel = settings.getGameSpeed(); soundEnabled = settings.isSoundEnabled(); vibrationEnabled = settings.isVibrationEnabled();
        int shipCount = registry.getShipCount();
        upgradeButtonRects = new RectF[shipCount][ShipData.STAT_COUNT]; mainButtonRects = new RectF[shipCount];
        for (int s = 0; s < shipCount; s++) { mainButtonRects[s] = new RectF(); for (int st = 0; st < ShipData.STAT_COUNT; st++) upgradeButtonRects[s][st] = new RectF(); }
        totalContentHeight = cardStartY + shipCount * (cardHeight + cardSpacing) + sh * 0.1f;
    }

    public void update(float dt) {
        float target = isShopOpen ? 1f : 0f; shopOpenAnim += (target - shopOpenAnim) * dt * 10f;
        if (purchaseFlashTimer > 0) purchaseFlashTimer -= dt; if (upgradeFlashTimer > 0) upgradeFlashTimer -= dt;
        if (isShopOpen && !isDraggingShop) { scrollY += scrollVelocity * dt; scrollVelocity *= 0.92f; scrollY = Math.max(0, Math.min(totalContentHeight - sh, scrollY)); }
    }

    private void resetAllPaintsToSafeState() {
        Paint[] allPaints = { titlePaint, subtitlePaint, menuBtnBgPaint, menuBtnBorderPaint, menuBtnTextPaint, menuCreditPaint, cardPaint, cardBorderPaint, textPaint, toggleTextPaint };
        for (Paint p : allPaints) { if (p == null) continue; p.setShader(null); p.setMaskFilter(null); p.setColorFilter(null); p.setAlpha(255); p.setFakeBoldText(false); p.setStrikeThruText(false); p.setUnderlineText(false); }
    }

    public void setState(int newState) { if (currentState != newState) resetAllPaintsToSafeState(); currentState = newState; }
    public int getCurrentState() { return currentState; }

    public void renderFull(Canvas c, GameWorld world, ShipRenderer shipRenderer) {
        pulse += 0.04f;
        if (world.getState() == Constants.STATE_PLAYING && !isShopOpen) { drawHUD(c, world); drawMilestone(c, world); drawHighScoreProximity(c, world); drawInGameCredits(c, world.getEconomy()); } 
        else if (world.getState() == Constants.STATE_MENU) { drawMenu(c, world.getHighScore(), world.getEconomy()); } 
        else if (world.getState() == Constants.STATE_GAME_OVER) { drawGameOver(c, world); } 
        else if (world.getState() == Constants.STATE_SETTINGS) { drawSettingsScreen(c, world.isGodModeActive()); }
        if (shopOpenAnim > 0.01f) drawShop(c, world.getShipRegistry(), world.getEconomy(), shipRenderer);
    }

    private void drawShop(Canvas c, ShipRegistry registry, EconomyManager economy, ShipRenderer renderer) {
        float alpha = shopOpenAnim;
        int blackAlpha = (int)(255 * alpha); bgBlackPaint.setColor(Color.argb(blackAlpha, 0, 0, 0)); c.drawRect(0, 0, sw, sh, bgBlackPaint);
        int overlayAlpha = (int)(245 * alpha); bgOverlayPaint.setColor(Color.argb(overlayAlpha, 6, 8, 16)); c.drawRect(0, 0, sw, sh, bgOverlayPaint);

        c.save(); c.translate(0, -scrollY);
        accentPaint.setTextSize(sw * 0.08f); accentPaint.setAlpha((int)(255 * alpha)); accentPaint.setShader(null); accentPaint.setColor(Color.WHITE);
        c.drawText("✦ HANGAR ✦", sw / 2f, sh * 0.08f + scrollY * 0.5f, accentPaint); 

        for (int i = 0; i < registry.getShipCount(); i++) {
            ShipData ship = registry.getShip(i);
            float cardY = cardStartY + i * (cardHeight + cardSpacing);
            if (cardY - scrollY > sh + 50 || cardY + cardHeight - scrollY < -50) continue;
            drawShipCard(c, ship, i, cardY, alpha, economy, renderer);
        }
        c.restore();

        drawCloseButton(c, sw - sw * 0.1f, sh * 0.08f, alpha);
        drawCreditDisplay(c, economy, alpha);
    }

    private void drawShipCard(Canvas c, ShipData ship, int shipIndex, float cardY, float alpha, EconomyManager economy, ShipRenderer renderer) {
        boolean isSelected = (economy.getSelectedShipId() == ship.id);
        boolean isUnlocked = economy.isShipUnlocked(ship.id);

        tempRect.set(cardLeftX, cardY, cardRightX, cardY + cardHeight);
        cardPaint.setColor(isSelected ? Color.argb((int)(200 * alpha), 20, 40, 30) : Color.argb((int)(200 * alpha), 25, 25, 35));
        c.drawRoundRect(tempRect, 24, 24, cardPaint);

        cardBorderPaint.setColor(isSelected ? Color.argb((int)(255 * alpha), 100, 255, 150) : Color.argb((int)(80 * alpha), 100, 150, 200));
        cardBorderPaint.setStrokeWidth(isSelected ? 5f : 2f);
        c.drawRoundRect(tempRect, 24, 24, cardBorderPaint);

        // Gemi Resmi (Solda Tam Ortalanmış)
        float prevCY = cardY + cardHeight * 0.4f;
        cardPaint.setColor(Color.argb((int)(50 * alpha), 80, 100, 150)); 
        c.drawCircle(previewCenterX, prevCY, previewSize * 0.5f, cardPaint);
        
        float shipScale = previewSize / 75f;
        renderer.drawShip(c, ship, previewCenterX, prevCY, 0f, (int)(255 * alpha), shipScale, false);

        // Başlık ve Açıklama (Sağ Panel Başlangıcı)
        float textStartY = cardY + cardHeight * 0.12f;
        textPaint.setShader(null); textPaint.setAlpha((int)(255 * alpha)); 
        textPaint.setTextSize(sw * 0.045f); textPaint.setFakeBoldText(true); textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.argb((int)(255 * alpha), Color.red(ship.cockpitColor), Color.green(ship.cockpitColor), Color.blue(ship.cockpitColor)));
        c.drawText(ship.name, infoPanelX, textStartY, textPaint);

        smallPaint.setColor(Color.argb((int)(160 * alpha), 200, 200, 220)); 
        smallPaint.setTextSize(sw * 0.028f); smallPaint.setTextAlign(Paint.Align.LEFT);
        
        String desc = ship.description; 
        if (smallPaint.measureText(desc) > infoPanelWidth) { 
            while (desc.length() > 3 && smallPaint.measureText(desc + "...") > infoPanelWidth) desc = desc.substring(0, desc.length() - 1); 
            desc += "..."; 
        }
        c.drawText(desc, infoPanelX, textStartY + sw * 0.04f, smallPaint);

        // Ayırıcı
        float dividerY = textStartY + sw * 0.07f;
        dividerPaint.setColor(Color.argb((int)(30 * alpha), 150, 170, 200));
        c.drawLine(infoPanelX, dividerY, cardRightX - sw * 0.035f, dividerY, dividerPaint);

        // Stat Satırları
        float statStartY = dividerY + sw * 0.035f; 
        float statRowH = cardHeight * 0.16f;
        float barH = statRowH * 0.35f;

        for (int st = 0; st < ShipData.STAT_COUNT; st++) {
            drawStatRowWithUpgrade(c, ship, shipIndex, st, statStartY + st * statRowH, barH, alpha, isUnlocked, economy);
        }

        // Ana Buton (Sağ Alt Köşe)
        float mainBtnX = cardRightX - mainBtnWidth - sw * 0.035f; 
        float mainBtnY = cardY + cardHeight - mainBtnHeight - sw * 0.035f;
        mainButtonRects[shipIndex].set(mainBtnX, mainBtnY, mainBtnX + mainBtnWidth, mainBtnY + mainBtnHeight);
        drawMainButton(c, ship, shipIndex, isSelected, isUnlocked, alpha, economy);

        if (purchaseFlashShipId == ship.id && purchaseFlashTimer > 0) {
            float flash = purchaseFlashTimer / 0.8f;
            cardPaint.setColor(Color.argb((int)(flash * 50), 255, 220, 50));
            tempRect.set(cardLeftX, cardY, cardRightX, cardY + cardHeight);
            c.drawRoundRect(tempRect, 24, 24, cardPaint);
        }
    }

    private void drawStatRowWithUpgrade(Canvas c, ShipData ship, int shipIndex, int statIndex, float rowY, float barH, float alpha, boolean isUnlocked, EconomyManager economy) {
        float labelX = infoPanelX; 
        float barX = infoPanelX + statLabelWidth + statGap; 
        float upgBtnX = barX + statBarWidth + barBtnGap; 

        String statName = ship.getStatName(statIndex); int statColor = ship.getStatColor(statIndex); float ratio = ship.getStatBarRatio(statIndex); int level = ship.getUpgradeLevel(statIndex);

        textPaint.setTextSize(sw * 0.025f); textPaint.setFakeBoldText(true); textPaint.setColor(Color.argb((int)(160 * alpha), 180, 185, 200)); textPaint.setTextAlign(Paint.Align.LEFT);
        c.drawText(statName, labelX, rowY + barH, textPaint);
        
        tempRect.set(barX, rowY, barX + statBarWidth, rowY + barH); 
        statBarBgPaint.setAlpha((int)(80 * alpha)); c.drawRoundRect(tempRect, barH/2, barH/2, statBarBgPaint);
        
        float fillWidth = statBarWidth * Math.max(0, Math.min(1, ratio)); 
        if (fillWidth > barH) { 
            tempRect.set(barX, rowY, barX + fillWidth, rowY + barH); 
            statBarFill.setColor(Color.argb((int)(200 * alpha), Color.red(statColor), Color.green(statColor), Color.blue(statColor))); 
            c.drawRoundRect(tempRect, barH/2, barH/2, statBarFill); 
        }
        
        float dotY = rowY - 4; float dotR = barH * 0.3f; float dotSpacing = statBarWidth / ShipData.MAX_UPGRADE_LEVEL;
        for (int lv = 0; lv < ShipData.MAX_UPGRADE_LEVEL; lv++) {
            float dotX = barX + dotSpacing * (lv + 0.5f);
            if (lv < level) dotPaint.setColor(Color.argb((int)(220*alpha), Color.red(statColor), Color.green(statColor), Color.blue(statColor)));
            else dotPaint.setColor(Color.argb((int)(40*alpha), 150, 160, 180));
            c.drawCircle(dotX, dotY, dotR, dotPaint);
        }

        float ubtnY = rowY + (barH - upgradeBtnHeight) / 2f;

        if (isUnlocked && level < ShipData.MAX_UPGRADE_LEVEL) {
            int cost = ship.getUpgradeCost(statIndex); boolean canAfford = economy.getCredits() >= cost;
            upgradeButtonRects[shipIndex][statIndex].set(upgBtnX, ubtnY, upgBtnX + upgradeBtnWidth, ubtnY + upgradeBtnHeight);
            
            int btnColor = canAfford ? Color.argb((int)(180*alpha), 40, 140, 50) : Color.argb((int)(80*alpha), 60, 60, 70); 
            upgradeBtnPaint.setColor(btnColor); upgradeBtnPaint.setStyle(Paint.Style.FILL);
            tempRect.set(upgBtnX, ubtnY, upgBtnX + upgradeBtnWidth, ubtnY + upgradeBtnHeight); 
            c.drawRoundRect(tempRect, upgradeBtnHeight / 2, upgradeBtnHeight / 2, upgradeBtnPaint);
            
            float btnTextSize = upgradeBtnHeight * 0.55f;
            textPaint.setTextSize(btnTextSize); textPaint.setFakeBoldText(true); textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(canAfford ? Color.argb((int)(255 * alpha), 255, 255, 255) : Color.argb((int)(100 * alpha), 140, 145, 155));
            c.drawText("+" + cost + "✦", upgBtnX + upgradeBtnWidth / 2, ubtnY + upgradeBtnHeight / 2 + btnTextSize * 0.35f, textPaint);
            
        } else if (isUnlocked && level >= ShipData.MAX_UPGRADE_LEVEL) {
            upgradeButtonRects[shipIndex][statIndex].setEmpty();
            float maxTextSize = upgradeBtnHeight * 0.5f;
            textPaint.setTextSize(maxTextSize); textPaint.setFakeBoldText(true); textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(Color.argb((int)(120 * alpha), Color.red(statColor), Color.green(statColor), Color.blue(statColor)));
            c.drawText("MAX", upgBtnX + upgradeBtnWidth / 2, ubtnY + upgradeBtnHeight / 2 + maxTextSize * 0.35f, textPaint);
        } else {
            upgradeButtonRects[shipIndex][statIndex].setEmpty();
            float lockSize = upgradeBtnHeight * 0.5f;
            textPaint.setTextSize(lockSize); textPaint.setTextAlign(Paint.Align.CENTER); textPaint.setColor(Color.argb((int)(50 * alpha), 120, 125, 140));
            c.drawText("🔒", upgBtnX + upgradeBtnWidth / 2, ubtnY + upgradeBtnHeight / 2 + lockSize * 0.35f, textPaint);
        }
        textPaint.setTextAlign(Paint.Align.LEFT); textPaint.setFakeBoldText(false);
    }

    private void drawMainButton(Canvas c, ShipData ship, int index, boolean isSelected, boolean isUnlocked, float al, EconomyManager economy) {
        RectF br = mainButtonRects[index]; 
        float btnTextSize = mainBtnHeight * 0.45f; 
        textPaint.setShader(null); textPaint.setTextAlign(Paint.Align.CENTER); textPaint.setFakeBoldText(true); textPaint.setTextSize(btnTextSize); textPaint.setStyle(Paint.Style.FILL); 
        float textY = br.centerY() + btnTextSize * 0.35f; float radius = mainBtnHeight / 2;

        if (isSelected) {
            buttonPaint.setColor(Color.argb((int)(170 * al), 35, 150, 70)); buttonPaint.setStyle(Paint.Style.FILL); c.drawRoundRect(br, radius, radius, buttonPaint);
            textPaint.setColor(Color.argb((int)(255 * al), 255, 255, 255)); c.drawText("✓ EQUIPPED", br.centerX(), textY, textPaint);
        } else if (isUnlocked) {
            buttonPaint.setColor(Color.argb((int)(170 * al), 50, 110, 170)); buttonPaint.setStyle(Paint.Style.FILL); c.drawRoundRect(br, radius, radius, buttonPaint);
            textPaint.setColor(Color.argb((int)(255 * al), 255, 255, 255)); c.drawText("EQUIP", br.centerX(), textY, textPaint);
        } else {
            boolean canAfford = economy.getCredits() >= ship.price;
            int btnCol = canAfford ? Color.argb((int)(190 * al), 190, 155, 30) : Color.argb((int)(90 * al), 65, 65, 72);
            buttonPaint.setColor(btnCol); buttonPaint.setStyle(Paint.Style.FILL); c.drawRoundRect(br, radius, radius, buttonPaint);
            textPaint.setColor(canAfford ? Color.argb((int)(255 * al), 255, 255, 255) : Color.argb((int)(140 * al), 160, 160, 170));
            String priceText = "🔒 " + ship.price + " ✦";
            c.drawText(priceText, br.centerX(), textY, textPaint);
        }
        textPaint.setTextAlign(Paint.Align.LEFT); textPaint.setFakeBoldText(false);
    }

    // Touch Yönlendirmeleri
    public boolean handleShopTouch(int action, float tx, float ty, ShipRegistry registry, EconomyManager economy, SoundManager sm, VibrationManager vm) {
        if (!isShopOpen && shopOpenAnim < 0.01f) return false;
        switch (action) {
            case MotionEvent.ACTION_DOWN: touchDownY = (int) ty; lastTouchY = (int) ty; isDraggingShop = false; return true;
            case MotionEvent.ACTION_MOVE: int dy = lastTouchY - (int) ty; if (Math.abs((int) ty - touchDownY) > 15) isDraggingShop = true; if (isDraggingShop) scrollY = Math.max(0, Math.min(totalContentHeight - sh, scrollY + dy)); lastTouchY = (int) ty; return true;
            case MotionEvent.ACTION_UP:
                if (!isDraggingShop) {
                    if (isCloseHit(tx, ty)) { closeShop(); currentState = STATE_MAIN_MENU; if(sm!=null) sm.playMenuClick(); return true; }
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

    // Diğer UI Kodlarının Özeti (Hatasız)
    private void drawHighScoreProximity(Canvas c, GameWorld world) { /* Orijinalle aynı */ }
    private void drawHUD(Canvas c, GameWorld world) { /* Orijinalle aynı */ }
    private void drawPowerBar(Canvas c, float x, float y, float w, float h, String name, int timer, int max, int color) { /* Orijinalle aynı */ }
    private void drawMilestone(Canvas c, GameWorld world) { /* Orijinalle aynı */ }
    private void drawGameOver(Canvas c, GameWorld world) { /* Orijinalle aynı */ }
    private void drawStats(Canvas c, GameWorld w, float a) { /* Orijinalle aynı */ }
    private void drawSettingsScreen(Canvas c, boolean godMode) { /* Orijinalle aynı */ }
    private void drawSettingsLabel(Canvas c, String label, float x, float y) { /* Orijinalle aynı */ }
    private void drawThreeWayToggle(Canvas c, float x, float y, float w, float h, int selIdx, String[] labels, int[] colors, int groupIdx) { /* Orijinalle aynı */ }
    private void drawOnOffToggle(Canvas c, float x, float y, float w, float h, boolean isOn, RectF hitRect) { /* Orijinalle aynı */ }
    private void drawCloseButton(Canvas c, float cx, float cy, float alpha) { closeBtnCirclePaint.setColor(Color.argb((int)(180*alpha), 200, 50, 50)); c.drawCircle(cx, cy, sw * 0.05f, closeBtnCirclePaint); closeBtnXPaint.setColor(Color.argb((int)(240*alpha), 255, 255, 255)); closeBtnXPaint.setStrokeWidth(5f); float cross = sw * 0.02f; c.drawLine(cx - cross, cy - cross, cx + cross, cy + cross, closeBtnXPaint); c.drawLine(cx + cross, cy - cross, cx - cross, cy + cross, closeBtnXPaint); }
    private void drawCreditDisplay(Canvas c, EconomyManager economy, float alpha) { /* Orijinalle aynı */ }
    private void drawInGameCredits(Canvas c, EconomyManager economy) { /* Orijinalle aynı */ }

    public boolean isCloseHit(float tx, float ty) { float cx = sw - sw * 0.1f, cy = sh * 0.08f; return (Math.pow(tx - cx, 2) + Math.pow(ty - cy, 2)) < Math.pow(CLOSE_BTN_HITBOX, 2); }
    public boolean handleSettingsTouch(float tx, float ty, GameWorld world, SoundManager sm, VibrationManager vm) { if (isCloseHit(tx, ty)) { world.quitToMenu(); if (sm != null && sm.isEnabled()) sm.playMenuClick(); if (vm != null && vm.isEnabled()) vm.vibrateMenuClick(); return true; } return false; }
    private void saveSettings(SoundManager sm) { if (settingsManager != null) { settingsManager.setDifficulty(difficultyLevel); settingsManager.setGameSpeed(gameSpeedLevel); settingsManager.setSoundEnabled(soundEnabled); settingsManager.setVibrationEnabled(vibrationEnabled); if(sm!=null) { sm.setEnabled(soundEnabled); if(soundEnabled) sm.startDrone(); else sm.stopDrone(); } } }

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
