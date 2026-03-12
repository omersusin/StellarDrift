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
import com.stellardrift.game.world.GameWorld;
import com.stellardrift.game.world.PowerUp;
import com.stellardrift.game.world.ShipData;
import com.stellardrift.game.world.ShipRegistry;

public class UIOverlay {

    private float sw, sh; 
    
    // Boyalar
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
    private Paint selectedGlow, creditPaint;
    private Paint bgBlackPaint, bgOverlayPaint;

    // Menü Buton Rect'leri
    private RectF playBtn = new RectF();
    private RectF settingsBtn = new RectF();
    private RectF shopBtn = new RectF();
    private RectF restartBtn = new RectF();
    private RectF tempRect = new RectF();

    // Settings Toggle Alanları
    private RectF[] settingsToggleRects = new RectF[6];
    private RectF soundToggleRect = new RectF();
    private RectF vibToggleRect = new RectF();

    // Shop Değişkenleri
    private RectF[][] upgradeButtonRects;
    private RectF[] mainButtonRects;
    private float scrollY = 0f, scrollVelocity = 0f;
    private int touchDownY = -1, lastTouchY = -1;
    private boolean isDraggingShop = false;
    private float cardWidth, cardHeight, cardSpacing, cardStartY, totalContentHeight;
    private float cardLeftX, cardRightX, previewSize, previewCenterX, infoPanelX, infoPanelWidth;
    private float statLabelWidth, statBarWidth, upgradeBtnWidth, upgradeBtnHeight, statGap, barBtnGap;
    private float mainBtnWidth, mainBtnHeight;

    private float pulse;
    private long gameOverTime;
    
    public UIOverlay(float screenW, float screenH) {
        this.sw = screenW; this.sh = screenH; pulse = 0; gameOverTime = 0;

        initPaints();
        initLayout(sw, sh);
        
        for (int i = 0; i < 6; i++) settingsToggleRects[i] = new RectF();
        
        float cx = sw / 2f, bw = sw * 0.65f, bh = sh * 0.075f;
        restartBtn.set(cx - bw/2, sh * 0.76f, cx + bw/2, sh * 0.76f + bh);
        playBtn.set(cx - bw/2, sh * 0.40f, cx + bw/2, sh * 0.40f + bh);
        shopBtn.set(cx - bw/2, sh * 0.50f, cx + bw/2, sh * 0.50f + bh);
        settingsBtn.set(cx - bw/2, sh * 0.60f, cx + bw/2, sh * 0.60f + bh);
    }

    private void initPaints() {
        bgBlackPaint = new Paint(); bgBlackPaint.setStyle(Paint.Style.FILL);
        bgOverlayPaint = new Paint(); bgOverlayPaint.setStyle(Paint.Style.FILL);
        cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG); cardPaint.setStyle(Paint.Style.FILL);
        cardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG); cardBorderPaint.setStyle(Paint.Style.STROKE); cardBorderPaint.setStrokeWidth(1.5f);
        btnTextPaint = makePaint(Color.WHITE, sw * 0.045f, Paint.Align.CENTER, true);
        textPaint = makePaint(Color.WHITE, sw * 0.04f, Paint.Align.LEFT, false);
        dimPaint = new Paint(); dimPaint.setColor(0xCC050510); dimPaint.setStyle(Paint.Style.FILL);
        labelPaint = makePaint(0xFFB0BEC5, sw * 0.04f, Paint.Align.LEFT, false);
        valuePaint = makePaint(Color.WHITE, sw * 0.04f, Paint.Align.RIGHT, true);
        accentPaint = makePaint(0xFF00E5FF, sw * 0.06f, Paint.Align.CENTER, true); 
        hudScorePaint = makePaint(Color.WHITE, sw * 0.06f, Paint.Align.LEFT, true);
        hudLabelPaint = makePaint(0x99FFFFFF, sw * 0.025f, Paint.Align.LEFT, false);
        comboPaint = makePaint(0xFFFFD740, sw * 0.06f, Paint.Align.CENTER, true);
        statPaint = makePaint(0xFFB0BEC5, sw * 0.035f, Paint.Align.LEFT, false);
        milestonePaint = makePaint(0xFFFFD740, sw * 0.07f, Paint.Align.CENTER, true);
        tempoPaint = makePaint(Color.WHITE, sw * 0.025f, Paint.Align.RIGHT, false);
        riskPaint = makePaint(0xFFFFD740, sw * 0.03f, Paint.Align.CENTER, true);
        highScoreLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG); highScoreLinePaint.setColor(0xFFFFD740); highScoreLinePaint.setStrokeWidth(3f);
        highScoreTextPaint = makePaint(0xFFFFD740, sw * 0.03f, Paint.Align.RIGHT, true);
        closeBtnCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG); closeBtnCirclePaint.setStyle(Paint.Style.FILL);
        closeBtnXPaint = new Paint(Paint.ANTI_ALIAS_FLAG); closeBtnXPaint.setStyle(Paint.Style.STROKE); closeBtnXPaint.setStrokeCap(Paint.Cap.ROUND);
        toggleBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG); toggleBgPaint.setStyle(Paint.Style.FILL);
        toggleActivePaint = new Paint(Paint.ANTI_ALIAS_FLAG); toggleActivePaint.setStyle(Paint.Style.FILL);
        toggleTextPaint = makePaint(Color.WHITE, sw * 0.03f, Paint.Align.CENTER, true);
        dividerPaint = new Paint(); dividerPaint.setColor(Color.argb(40, 150, 160, 180)); dividerPaint.setStrokeWidth(2f);
        statBarBgPaint = new Paint(); statBarBgPaint.setColor(Color.argb(80, 40, 45, 60)); statBarBgPaint.setStyle(Paint.Style.FILL);
        statBarPaint = new Paint(); statBarPaint.setStyle(Paint.Style.FILL);
        statBarFill = new Paint(); statBarFill.setStyle(Paint.Style.FILL);
        upgradeBtnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG); dotPaint.setStyle(Paint.Style.FILL);
        selectedGlow = new Paint(Paint.ANTI_ALIAS_FLAG); selectedGlow.setStyle(Paint.Style.STROKE); selectedGlow.setStrokeWidth(2f); selectedGlow.setColor(Color.rgb(80, 255, 160));
        creditPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        diffBarBg = new Paint(Paint.ANTI_ALIAS_FLAG); diffBarBg.setColor(0x33FFFFFF);
        diffBarFill = new Paint(Paint.ANTI_ALIAS_FLAG); diffBarFill.setColor(0xFF00E5FF);
        powerBarBg = new Paint(Paint.ANTI_ALIAS_FLAG); powerBarBg.setColor(0x33FFFFFF);
        powerBarFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        titlePaint = makePaint(0xFF00E5FF, sw * 0.1f, Paint.Align.CENTER, true);
        subtitlePaint = makePaint(Color.WHITE, sw * 0.04f, Paint.Align.CENTER, false); subtitlePaint.setAlpha(180);
        scorePaint = makePaint(Color.WHITE, sw * 0.08f, Paint.Align.CENTER, true);
        smallPaint = makePaint(0xFFB0BEC5, sw * 0.035f, Paint.Align.CENTER, false);
    }

    private void initLayout(float w, float h) {
        cardPadding = w * 0.04f; 
        cardLeftX = cardPadding;
        cardRightX = w - cardPadding;
        cardWidth = cardRightX - cardLeftX; 
        cardHeight = Math.max(260, Math.min(320, h * 0.25f)); 
        cardSpacing = h * 0.02f;
        cardStartY = h * 0.13f;

        float innerPad = cardWidth * 0.04f;
        previewSize = Math.min(cardWidth * 0.25f, 100);
        previewCenterX = cardLeftX + innerPad + (previewSize / 2f);

        infoPanelX = cardLeftX + innerPad + previewSize + innerPad;
        infoPanelWidth = cardRightX - infoPanelX - innerPad;

        statLabelWidth = infoPanelWidth * 0.12f;  
        statGap = infoPanelWidth * 0.02f;         
        upgradeBtnWidth = infoPanelWidth * 0.28f; 
        upgradeBtnHeight = cardHeight * 0.09f;
        barBtnGap = infoPanelWidth * 0.03f;       

        statBarWidth = Math.max(40, infoPanelWidth - statLabelWidth - statGap - barBtnGap - upgradeBtnWidth);
        mainBtnWidth = cardWidth * 0.35f; 
        mainBtnHeight = cardHeight * 0.16f;
    }

    private Paint makePaint(int color, float size, Paint.Align align, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(color); p.setTextSize(size); p.setTextAlign(align);
        if (bold) p.setTypeface(Typeface.DEFAULT_BOLD); return p;
    }

    public void initPrefs(ShipRegistry registry) {
        int shipCount = registry.getShipCount();
        upgradeButtonRects = new RectF[shipCount][ShipData.STAT_COUNT]; mainButtonRects = new RectF[shipCount];
        for (int s = 0; s < shipCount; s++) { mainButtonRects[s] = new RectF(); for (int st = 0; st < ShipData.STAT_COUNT; st++) upgradeButtonRects[s][st] = new RectF(); }
        totalContentHeight = cardStartY + shipCount * (cardHeight + cardSpacing) + sh * 0.1f;
    }

    public void update(float dt) {
        if (!isDraggingShop) { scrollY += scrollVelocity * dt; scrollVelocity *= 0.92f; scrollY = Math.max(0, Math.min(totalContentHeight - sh, scrollY)); }
    }

    private void resetAllPaintsToSafeState() {
        Paint[] allPaints = { titlePaint, subtitlePaint, cardPaint, cardBorderPaint, textPaint, toggleTextPaint };
        for (Paint p : allPaints) { if (p == null) continue; p.setShader(null); p.setMaskFilter(null); p.setColorFilter(null); p.setAlpha(255); p.setFakeBoldText(false); p.setStrikeThruText(false); p.setUnderlineText(false); }
    }

    public void renderFull(Canvas c, GameWorld world, ShipRenderer shipRenderer) {
        pulse += 0.04f;
        resetAllPaintsToSafeState(); // Garanti
        
        int state = world.getState();
        if (state == Constants.STATE_PLAYING) {
            drawHUD(c, world); drawMilestone(c, world); drawInGameCredits(c, world.getEconomy());
        } else if (state == Constants.STATE_MENU) {
            drawMenu(c, world.getHighScore(), world.getEconomy());
        } else if (state == Constants.STATE_GAME_OVER) {
            drawGameOver(c, world);
        } else if (state == Constants.STATE_SETTINGS) {
            drawSettingsScreen(c, world.getSettings());
        } else if (state == Constants.STATE_SHOP) {
            drawShop(c, world.getShipRegistry(), world.getEconomy(), shipRenderer);
        }
    }

    private void drawMenu(Canvas c, int highScore, EconomyManager economy) {
        c.drawColor(Color.rgb(5, 7, 14));
        
        titlePaint.setColor(Color.WHITE); titlePaint.setTextSize(sw * 0.12f); titlePaint.setTextAlign(Paint.Align.CENTER); titlePaint.setFakeBoldText(true); titlePaint.setStyle(Paint.Style.FILL);
        float titleY = sh * 0.17f; c.drawText("STELLAR", sw / 2f, titleY, titlePaint);
        titlePaint.setColor(Color.rgb(80, 195, 255)); titlePaint.setTextSize(sw * 0.14f); c.drawText("DRIFT", sw / 2f, titleY + sh * 0.06f, titlePaint);
        
        subtitlePaint.setColor(Color.argb(100, 170, 180, 200)); subtitlePaint.setTextSize(sw * 0.035f); subtitlePaint.setFakeBoldText(false);
        c.drawText("v5.0 — The Masterpiece Edition", sw / 2f, titleY + sh * 0.09f, subtitlePaint);
        
        drawMenuButton(c, "▶ PLAY", playBtn, Color.argb(185, 18, 28, 48), Color.argb(150, 70, 170, 255), Color.WHITE);
        drawMenuButton(c, "✦ SHOP", shopBtn, Color.argb(175, 28, 26, 38), Color.argb(140, 195, 165, 38), Color.rgb(228, 198, 55));
        drawMenuButton(c, "⚙ SETTINGS", settingsBtn, Color.argb(165, 22, 25, 38), Color.argb(120, 115, 125, 155), Color.rgb(175, 185, 205));
        
        subtitlePaint.setAlpha((int)(150 * (Math.sin(pulse * 1.5) * 0.3 + 0.7))); 
        c.drawText("Destroy asteroids & upgrade ship!", sw / 2f, sh * 0.88f, subtitlePaint);

        creditPaint.setColor(Color.rgb(195, 175, 45)); creditPaint.setTextSize(sw * 0.05f); creditPaint.setTextAlign(Paint.Align.CENTER); creditPaint.setFakeBoldText(true); 
        c.drawText("✦ " + (economy != null ? economy.getDisplayedCredits() : 0), sw / 2f, sh * 0.76f, creditPaint);
        smallPaint.setColor(Color.argb(90, 155, 165, 185)); c.drawText("Best: " + highScore, sw / 2f, sh * 0.81f, smallPaint);
    }

    private void drawMenuButton(Canvas c, String text, RectF outRect, int bgCol, int borderCol, int textCol) {
        cardPaint.setColor(bgCol); cardPaint.setStyle(Paint.Style.FILL); c.drawRoundRect(outRect, outRect.height()/2, outRect.height()/2, cardPaint);
        cardBorderPaint.setColor(borderCol); cardBorderPaint.setStyle(Paint.Style.STROKE); cardBorderPaint.setStrokeWidth(3f); c.drawRoundRect(outRect, outRect.height()/2, outRect.height()/2, cardBorderPaint);
        btnTextPaint.setColor(textCol); btnTextPaint.setTextSize(outRect.height() * 0.35f); btnTextPaint.setTextAlign(Paint.Align.CENTER); btnTextPaint.setFakeBoldText(true);
        c.drawText(text, sw / 2f, outRect.top + outRect.height()/2 + outRect.height()*0.12f, btnTextPaint);
    }

    private void drawSettingsScreen(Canvas c, SettingsManager s) {
        bgBlackPaint.setColor(Color.argb(255, 0, 0, 0)); c.drawRect(0, 0, sw, sh, bgBlackPaint);
        bgOverlayPaint.setColor(Color.argb(245, 8, 10, 20)); c.drawRect(0, 0, sw, sh, bgOverlayPaint);

        float cw = sw * 0.9f, ch = sh * 0.6f, cx = sw * 0.05f, cy = sh * 0.2f;
        tempRect.set(cx, cy, cx + cw, cy + ch);
        cardPaint.setColor(Color.argb(220, 18, 22, 35)); cardPaint.setStyle(Paint.Style.FILL); c.drawRoundRect(tempRect, 30, 30, cardPaint);
        cardBorderPaint.setColor(Color.argb(80, 100, 120, 180)); cardBorderPaint.setStyle(Paint.Style.STROKE); c.drawRoundRect(tempRect, 30, 30, cardBorderPaint);

        accentPaint.setColor(WHITE); accentPaint.setTextSize(sw * 0.07f); c.drawText("⚙ SETTINGS", sw / 2f, cy + sh * 0.07f, accentPaint);
        c.drawLine(cx + 40, cy + sh * 0.09f, cx + cw - 40, cy + sh * 0.09f, dividerPaint);

        float rowY = cy + sh * 0.15f, rowGap = sh * 0.09f;
        drawSettingsLabel(c, "DIFFICULTY", cx + 40, rowY); drawThreeWayToggle(c, cx + cw - sw*0.45f, rowY - sh*0.03f, sw*0.4f, sh*0.05f, s.getDifficulty(), new String[]{"EASY", "NORM", "HARD"}, new int[]{Color.rgb(60, 180, 80), Color.rgb(60, 140, 220), Color.rgb(220, 60, 50)}, 0);
        rowY += rowGap; drawSettingsLabel(c, "GAME SPEED", cx + 40, rowY); drawThreeWayToggle(c, cx + cw - sw*0.45f, rowY - sh*0.03f, sw*0.4f, sh*0.05f, s.getGameSpeed(), new String[]{"SLOW", "NORM", "FAST"}, new int[]{Color.rgb(100, 160, 200), Color.rgb(60, 140, 220), Color.rgb(240, 160, 30)}, 1);
        rowY += rowGap; drawSettingsLabel(c, "SOUND", cx + 40, rowY); drawOnOffToggle(c, cx + cw - sw*0.25f, rowY - sh*0.03f, sw*0.2f, sh*0.045f, s.isSoundEnabled(), soundToggleRect);
        rowY += rowGap; drawSettingsLabel(c, "VIBRATION", cx + 40, rowY); drawOnOffToggle(c, cx + cw - sw*0.25f, rowY - sh*0.03f, sw*0.2f, sh*0.045f, s.isVibrationEnabled(), vibToggleRect);
        
        drawCloseButton(c, sw - sw * 0.1f, sh * 0.08f);
    }

    private void drawSettingsLabel(Canvas c, String label, float x, float y) { labelPaint.setColor(Color.rgb(190, 195, 210)); labelPaint.setTextSize(sw * 0.04f); labelPaint.setFakeBoldText(true); c.drawText(label, x, y, labelPaint); }
    private void drawThreeWayToggle(Canvas c, float x, float y, float w, float h, int selIdx, String[] labels, int[] colors, int groupIdx) { float segW = w / 3f; for (int i = 0; i < 3; i++) { float sx = x + segW * i; RectF r = settingsToggleRects[groupIdx * 3 + i]; r.set(sx, y, sx + segW, y + h); if (i == selIdx) { toggleActivePaint.setColor(Color.argb(200, Color.red(colors[i]), Color.green(colors[i]), Color.blue(colors[i]))); c.drawRoundRect(r, 12, 12, toggleActivePaint); } else { toggleBgPaint.setColor(Color.argb(80, 40, 45, 60)); c.drawRoundRect(r, 12, 12, toggleBgPaint); } toggleTextPaint.setColor(i == selIdx ? WHITE : Color.rgb(130, 135, 150)); c.drawText(labels[i], sx + segW / 2, y + h * 0.65f, toggleTextPaint); } }
    private void drawOnOffToggle(Canvas c, float x, float y, float w, float h, boolean isOn, RectF hitRect) { hitRect.set(x, y, x + w, y + h); if (isOn) { toggleActivePaint.setColor(Color.argb(200, 60, 180, 80)); c.drawRoundRect(hitRect, h/2, h/2, toggleActivePaint); cardPaint.setColor(WHITE); c.drawCircle(x + w - h/2, y + h/2, h/2 - 4, cardPaint); toggleTextPaint.setColor(WHITE); c.drawText("ON", x + w * 0.35f, y + h * 0.65f, toggleTextPaint); } else { toggleBgPaint.setColor(Color.argb(150, 60, 60, 70)); c.drawRoundRect(hitRect, h/2, h/2, toggleBgPaint); cardPaint.setColor(Color.rgb(140, 140, 150)); c.drawCircle(x + h/2, y + h/2, h/2 - 4, cardPaint); toggleTextPaint.setColor(Color.rgb(100, 100, 110)); c.drawText("OFF", x + w * 0.65f, y + h * 0.65f, toggleTextPaint); } }
    private void drawCloseButton(Canvas c, float cx, float cy) { closeBtnCirclePaint.setColor(Color.argb(180, 200, 50, 50)); c.drawCircle(cx, cy, sw * 0.05f, closeBtnCirclePaint); closeBtnXPaint.setColor(Color.argb(240, 255, 255, 255)); closeBtnXPaint.setStrokeWidth(5f); float cross = sw * 0.02f; c.drawLine(cx - cross, cy - cross, cx + cross, cy + cross, closeBtnXPaint); c.drawLine(cx + cross, cy - cross, cx - cross, cy + cross, closeBtnXPaint); }

    private void drawShop(Canvas c, ShipRegistry registry, EconomyManager economy, ShipRenderer renderer) {
        bgBlackPaint.setColor(Color.BLACK); c.drawRect(0, 0, sw, sh, bgBlackPaint);
        bgOverlayPaint.setColor(Color.argb(245, 6, 8, 16)); c.drawRect(0, 0, sw, sh, bgOverlayPaint);
        c.save(); c.translate(0, -scrollY);
        accentPaint.setTextSize(sw * 0.08f); accentPaint.setColor(Color.WHITE); c.drawText("✦ HANGAR ✦", sw / 2f, sh * 0.08f + scrollY * 0.5f, accentPaint); 
        for (int i = 0; i < registry.getShipCount(); i++) { ShipData ship = registry.getShip(i); float cardY = cardStartY + i * (cardHeight + cardSpacing); if (cardY - scrollY > sh + 50 || cardY + cardHeight - scrollY < -50) continue; drawShipCard(c, ship, i, cardY, economy, renderer); }
        c.restore();
        drawCloseButton(c, sw - sw * 0.1f, sh * 0.08f);
        drawCreditDisplay(c, economy);
    }

    private void drawShipCard(Canvas c, ShipData ship, int shipIndex, float cardY, EconomyManager economy, ShipRenderer renderer) {
        boolean isSelected = (economy.getSelectedShipId() == ship.id); boolean isUnlocked = economy.isShipUnlocked(ship.id);
        tempRect.set(cardLeftX, cardY, cardRightX, cardY + cardHeight); cardPaint.setColor(isSelected ? Color.argb(200, 20, 40, 30) : Color.argb(200, 25, 25, 35)); c.drawRoundRect(tempRect, 24, 24, cardPaint);
        cardBorderPaint.setColor(isSelected ? Color.argb(255, 100, 255, 150) : Color.argb(80, 100, 150, 200)); cardBorderPaint.setStrokeWidth(isSelected ? 5f : 2f); c.drawRoundRect(tempRect, 24, 24, cardBorderPaint);
        
        float prevCY = cardY + cardHeight * 0.4f; cardPaint.setColor(Color.argb(50, 80, 100, 150)); c.drawCircle(previewCenterX, prevCY, previewSize * 0.5f, cardPaint);
        renderer.drawShip(c, ship, previewCenterX, prevCY, 0f, 255, previewSize / 75f, false);
        
        float textStartY = cardY + cardHeight * 0.12f; textPaint.setTextSize(sw * 0.045f); textPaint.setFakeBoldText(true); textPaint.setColor(Color.rgb(Color.red(ship.cockpitColor), Color.green(ship.cockpitColor), Color.blue(ship.cockpitColor))); c.drawText(ship.name, infoPanelX, textStartY, textPaint);
        smallPaint.setColor(Color.argb(160, 200, 200, 220)); smallPaint.setTextSize(sw * 0.028f); smallPaint.setTextAlign(Paint.Align.LEFT); String desc = ship.description; if (smallPaint.measureText(desc) > infoPanelWidth) { while (desc.length() > 3 && smallPaint.measureText(desc + "...") > infoPanelWidth) desc = desc.substring(0, desc.length() - 1); desc += "..."; } c.drawText(desc, infoPanelX, textStartY + sw * 0.04f, smallPaint);
        
        float dividerY = textStartY + sw * 0.07f; dividerPaint.setColor(Color.argb(30, 150, 170, 200)); c.drawLine(infoPanelX, dividerY, cardRightX - sw * 0.035f, dividerY, dividerPaint);
        float statStartY = dividerY + sw * 0.035f, statRowH = cardHeight * 0.16f, barH = statRowH * 0.35f;
        for (int st = 0; st < ShipData.STAT_COUNT; st++) { drawStatRowWithUpgrade(c, ship, shipIndex, st, statStartY + st * statRowH, barH, isUnlocked, economy); }
        
        float mainBtnX = cardRightX - mainBtnWidth - sw * 0.035f, mainBtnY = cardY + cardHeight - mainBtnHeight - sw * 0.035f; mainButtonRects[shipIndex].set(mainBtnX, mainBtnY, mainBtnX + mainBtnWidth, mainBtnY + mainBtnHeight);
        drawMainButton(c, ship, shipIndex, isSelected, isUnlocked, economy);
    }

    private void drawStatRowWithUpgrade(Canvas c, ShipData ship, int shipIndex, int statIndex, float rowY, float barH, boolean isUnlocked, EconomyManager economy) {
        float labelX = infoPanelX, barX = infoPanelX + statLabelWidth + statGap, upgBtnX = barX + statBarWidth + barBtnGap; float maxBtnRight = cardRightX - sw * 0.035f; if (upgBtnX + upgradeBtnWidth > maxBtnRight) upgBtnX = maxBtnRight - upgradeBtnWidth;
        String statName = ship.getStatName(statIndex); int statColor = ship.getStatColor(statIndex); float ratio = ship.getStatBarRatio(statIndex); int level = ship.getUpgradeLevel(statIndex);
        textPaint.setTextSize(sw * 0.025f); textPaint.setFakeBoldText(true); textPaint.setColor(Color.argb(160, 180, 185, 200)); c.drawText(statName, labelX, rowY + barH, textPaint);
        tempRect.set(barX, rowY, barX + statBarWidth, rowY + barH); statBarBgPaint.setAlpha(80); c.drawRoundRect(tempRect, barH/2, barH/2, statBarBgPaint);
        float fillWidth = statBarWidth * Math.max(0, Math.min(1, ratio)); if (fillWidth > barH) { tempRect.set(barX, rowY, barX + fillWidth, rowY + barH); statBarFill.setColor(Color.rgb(Color.red(statColor), Color.green(statColor), Color.blue(statColor))); c.drawRoundRect(tempRect, barH/2, barH/2, statBarFill); }
        float dotY = rowY - 4, dotR = barH * 0.3f, dotSpacing = statBarWidth / ShipData.MAX_UPGRADE_LEVEL;
        for (int lv = 0; lv < ShipData.MAX_UPGRADE_LEVEL; lv++) { float dotX = barX + dotSpacing * (lv + 0.5f); dotPaint.setColor(lv < level ? Color.rgb(Color.red(statColor), Color.green(statColor), Color.blue(statColor)) : Color.argb(40, 150, 160, 180)); c.drawCircle(dotX, dotY, dotR, dotPaint); }
        float ubtnY = rowY + (barH - upgradeBtnHeight) / 2f;
        if (isUnlocked && level < ShipData.MAX_UPGRADE_LEVEL) {
            int cost = ship.getUpgradeCost(statIndex); boolean canAfford = economy.getCredits() >= cost; upgradeButtonRects[shipIndex][statIndex].set(upgBtnX, ubtnY, upgBtnX + upgradeBtnWidth, ubtnY + upgradeBtnHeight);
            upgradeBtnPaint.setColor(canAfford ? Color.argb(180, 40, 140, 50) : Color.argb(80, 60, 60, 70)); tempRect.set(upgBtnX, ubtnY, upgBtnX + upgradeBtnWidth, ubtnY + upgradeBtnHeight); c.drawRoundRect(tempRect, upgradeBtnHeight / 2, upgradeBtnHeight / 2, upgradeBtnPaint);
            float btnTextSize = upgradeBtnHeight * 0.55f; textPaint.setTextSize(btnTextSize); textPaint.setTextAlign(Paint.Align.CENTER); textPaint.setColor(canAfford ? Color.WHITE : Color.argb(100, 140, 145, 155)); c.drawText("+" + cost + "✦", upgBtnX + upgradeBtnWidth / 2, ubtnY + upgradeBtnHeight / 2 + btnTextSize * 0.35f, textPaint); textPaint.setTextAlign(Paint.Align.LEFT);
        } else if (isUnlocked && level >= ShipData.MAX_UPGRADE_LEVEL) {
            upgradeButtonRects[shipIndex][statIndex].setEmpty(); float maxTextSize = upgradeBtnHeight * 0.5f; textPaint.setTextSize(maxTextSize); textPaint.setTextAlign(Paint.Align.CENTER); textPaint.setColor(Color.argb(120, Color.red(statColor), Color.green(statColor), Color.blue(statColor))); c.drawText("MAX", upgBtnX + upgradeBtnWidth / 2, ubtnY + upgradeBtnHeight / 2 + maxTextSize * 0.35f, textPaint);
        } else { upgradeButtonRects[shipIndex][statIndex].setEmpty(); float lockSize = upgradeBtnHeight * 0.5f; textPaint.setTextSize(lockSize); textPaint.setTextAlign(Paint.Align.CENTER); textPaint.setColor(Color.argb(50, 120, 125, 140)); c.drawText("🔒", upgBtnX + upgradeBtnWidth / 2, ubtnY + upgradeBtnHeight / 2 + lockSize * 0.35f, textPaint); }
        textPaint.setTextAlign(Paint.Align.LEFT); textPaint.setFakeBoldText(false);
    }

    private void drawMainButton(Canvas c, ShipData ship, int index, boolean isSelected, boolean isUnlocked, EconomyManager economy) {
        RectF br = mainButtonRects[index]; float btnTextSize = mainBtnHeight * 0.45f; textPaint.setTextAlign(Paint.Align.CENTER); textPaint.setFakeBoldText(true); textPaint.setTextSize(btnTextSize); float textY = br.centerY() + btnTextSize * 0.35f; float radius = mainBtnHeight / 2;
        if (isSelected) { cardPaint.setColor(Color.argb(170, 35, 150, 70)); c.drawRoundRect(br, radius, radius, cardPaint); textPaint.setColor(Color.WHITE); c.drawText("✓ EQUIPPED", br.centerX(), textY, textPaint); } 
        else if (isUnlocked) { cardPaint.setColor(Color.argb(170, 50, 110, 170)); c.drawRoundRect(br, radius, radius, cardPaint); textPaint.setColor(Color.WHITE); c.drawText("EQUIP", br.centerX(), textY, textPaint); } 
        else { boolean canAfford = economy.getCredits() >= ship.price; cardPaint.setColor(canAfford ? Color.argb(190, 190, 155, 30) : Color.argb(90, 65, 65, 72)); c.drawRoundRect(br, radius, radius, cardPaint); textPaint.setColor(canAfford ? Color.WHITE : Color.argb(140, 160, 160, 170)); String priceText = "🔒 " + ship.price + " ✦"; if (textPaint.measureText(priceText) > br.width() - 10) textPaint.setTextSize(btnTextSize - 2); c.drawText(priceText, br.centerX(), textY, textPaint); }
        textPaint.setTextAlign(Paint.Align.LEFT); textPaint.setFakeBoldText(false);
    }

    private void drawCreditDisplay(Canvas c, EconomyManager economy) {
        float cx = sw * 0.05f, cy = sh * 0.07f; int credits = economy.getDisplayedCredits(); float flash = economy.getCreditFlash(); int r = (int)(200 + 55 * flash), g = (int)(180 + 75 * flash);
        creditPaint.setColor(Color.argb(255, Math.min(255, r), Math.min(255, g), 50)); creditPaint.setTextSize(sw * 0.06f); creditPaint.setTextAlign(Paint.Align.LEFT); c.drawText("✦ " + credits, cx, cy, creditPaint);
    }

    private void drawInGameCredits(Canvas c, EconomyManager economy) {
        int credits = economy.getDisplayedCredits(); float flash = economy.getCreditFlash(); int r = (int)(200 + 55 * flash), g = (int)(180 + 75 * flash);
        creditPaint.setColor(Color.argb(180, Math.min(255, r), Math.min(255, g), 50)); creditPaint.setTextSize(sw * 0.045f); creditPaint.setTextAlign(Paint.Align.RIGHT); c.drawText("✦ " + credits, sw - sw * 0.04f, sh * 0.11f, creditPaint);
    }

    private void drawHUD(Canvas c, GameWorld world) {
        float pad = sw * 0.04f; hudLabelPaint.setAlpha(150); hudLabelPaint.setTextAlign(Paint.Align.LEFT); c.drawText("SCORE", pad, sh * 0.04f, hudLabelPaint); hudScorePaint.setColor(WHITE); c.drawText(String.valueOf(world.getScore()), pad, sh * 0.075f, hudScorePaint);
        int tempo = world.getTempoPhase(); if (tempo != Constants.TEMPO_CALM) { String tl = tempo == Constants.TEMPO_PRESSURE ? "▲ PRESSURE" : "★ REWARD"; tempoPaint.setColor(tempo == Constants.TEMPO_PRESSURE ? 0xFFFF1744 : GOLD); tempoPaint.setAlpha(180); tempoPaint.setTextAlign(Paint.Align.RIGHT); c.drawText(tl, sw - pad, sh * 0.095f, tempoPaint); }
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
        int score = world.getScore(), hs = world.getHighScore(); if (gameOverTime == 0) gameOverTime = System.currentTimeMillis(); long el = System.currentTimeMillis() - gameOverTime; c.drawRect(0, 0, sw, sh, dimPaint); float cx = sw / 2f;
        accentPaint.setColor(0xFFFF1744); accentPaint.setTextSize(sw * 0.08f); accentPaint.setAlpha(255); c.drawText("GAME OVER", cx, sh * 0.2f, accentPaint);
        float cp = Math.min(1f, el / 800f); cp = 1f - (1f - cp) * (1f - cp); scorePaint.setColor(WHITE); scorePaint.setTextSize(sw * 0.12f); scorePaint.setAlpha(255); c.drawText(String.valueOf((int)(score * cp)), cx, sh * 0.32f, scorePaint); smallPaint.setAlpha(180); c.drawText("SCORE", cx, sh * 0.35f, smallPaint);
        if (el > 800) { if (score >= hs && score > 0) { accentPaint.setColor(GOLD); accentPaint.setTextSize(sw * 0.04f); accentPaint.setAlpha((int)(255 * (Math.sin(pulse * 3) * 0.15 + 0.85))); c.drawText("★ NEW BEST! ★", cx, sh * 0.41f, accentPaint); } else { smallPaint.setAlpha(140); c.drawText("BEST: " + hs, cx, sh * 0.41f, smallPaint); } }
        if (el > 600) drawStats(c, world, Math.min(1f, (el - 600) / 400f));
        if (el > BUTTON_DELAY) { float ba = Math.min(1f, (el - BUTTON_DELAY) / 500f); drawMenuButton(c, "▶ PLAY AGAIN", restartBtn, Color.argb((int)(0x33*ba),255,255,255), Color.argb((int)(140*ba),0,229,255), WHITE); }
    }

    private void drawStats(Canvas c, GameWorld w, float a) {
        float sx = sw*0.2f, sy = sh*0.48f, g = sh*0.04f, rx = sw*0.8f; int sa = (int)(200*a), va = (int)(255*a);
        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Orbs Collected", sx, sy, statPaint); statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText(String.valueOf(w.getOrbsCollected()), rx, sy, statPaint);
        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Near Misses", sx, sy+g, statPaint); statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText(String.valueOf(w.getNearMissCount()), rx, sy+g, statPaint);
        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Max Combo", sx, sy+g*2, statPaint); statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(w.getMaxCombo()>=5?GOLD:WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText("x"+w.getMaxCombo(), rx, sy+g*2, statPaint);
        statPaint.setTextAlign(Paint.Align.LEFT); statPaint.setColor(0xFFB0BEC5); statPaint.setAlpha(sa); statPaint.setTypeface(Typeface.DEFAULT); c.drawText("Survival Time", sx, sy+g*3, statPaint); statPaint.setTextAlign(Paint.Align.RIGHT); statPaint.setColor(WHITE); statPaint.setAlpha(va); statPaint.setTypeface(Typeface.DEFAULT_BOLD); c.drawText(w.getSurvivalTime()+"s", rx, sy+g*3, statPaint);
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
                        ShipData ship = registry.getShip(i);
                        for (int st = 0; st < ShipData.STAT_COUNT; st++) {
                            RectF ubr = upgradeButtonRects[i][st];
                            if (!ubr.isEmpty() && tx >= ubr.left && tx <= ubr.right && adjY >= ubr.top && adjY <= ubr.bottom) {
                                if (economy.purchaseUpgrade(ship.id, st)) { economy.syncUpgradesToShipData(registry.getAllShips()); }
                                return true;
                            }
                        }
                        RectF mbr = mainButtonRects[i];
                        if (tx >= mbr.left && tx <= mbr.right && adjY >= mbr.top && adjY <= mbr.bottom) {
                            if (economy.isShipUnlocked(ship.id)) { economy.selectShip(ship.id); registry.selectShip(ship.id); } 
                            else if (economy.purchaseShip(ship.id, ship.price)) { economy.selectShip(ship.id); registry.selectShip(ship.id); }
                            return true;
                        }
                    }
                }
                isDraggingShop = false; return true;
        } return true;
    }

    private void saveSettings() { if (settingsManager != null) { settingsManager.setDifficulty(difficultyLevel); settingsManager.setGameSpeed(gameSpeedLevel); settingsManager.setSoundEnabled(soundEnabled); settingsManager.setVibrationEnabled(vibrationEnabled); } }

    public void openShop() { isShopOpen = true; scrollY = 0; }
    public void closeShop() { isShopOpen = false; }
    public boolean isShopVisible() { return isShopOpen || shopOpenAnim > 0.01f; }
}
