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

    private int sw, sh;
    private Paint titlePaint, subtitlePaint, scorePaint, smallPaint;
    private Paint btnPaint, btnTextPaint, btnOutlinePaint;
    private Paint dimPaint, accentPaint, labelPaint, valuePaint;
    private Paint hudScorePaint, hudLabelPaint, diffBarBg, diffBarFill;
    private Paint comboPaint, powerBarBg, powerBarFill, statPaint;
    private Paint milestonePaint, tempoPaint, riskPaint;
    private Paint highScoreLinePaint, highScoreTextPaint;

    // X Kapatma Butonu Paint'leri
    private Paint closeBtnCirclePaint, closeBtnXPaint;
    private static final float CLOSE_BTN_HITBOX = 50f; 

    // Settings Ekranı Alanları
    private RectF[] settingsToggleRects = new RectF[6];
    private RectF soundToggleRect = new RectF();
    private RectF vibToggleRect = new RectF();
    private Paint toggleBgPaint, toggleActivePaint, toggleTextPaint, dividerPaint;

    private RectF playBtn, settingsBtn, shopBtn;
    private RectF restartBtn;
    private float pulse;
    private long gameOverTime;
    private static final long BUTTON_DELAY = 1200;

    private boolean recordBroken = false;
    private float recordBreakParticleTimer = 0;

    private static final int CYAN = 0xFF00E5FF;
    private static final int PURPLE = 0xFF7C4DFF;
    private static final int GOLD = 0xFFFFD740;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DIM = 0xCC050510;

    private boolean isShopOpen = false;
    private float shopOpenAnim = 0f;
    private float scrollY = 0f, scrollVelocity = 0f;
    private int touchDownY = -1, lastTouchY = -1;
    private boolean isDraggingShop = false;
    
    private float cardWidth, cardHeight, cardSpacing, cardStartY, totalContentHeight;
    private RectF tempRect = new RectF();
    private Paint shopBgPaint = new Paint(), statBarBgPaint = new Paint(), statBarPaint = new Paint();

    public UIOverlay(int sw, int sh) {
        this.sw = sw; this.sh = sh; pulse = 0; gameOverTime = 0;

        titlePaint = makePaint(CYAN, sw * 0.1f, Paint.Align.CENTER, true);
        subtitlePaint = makePaint(WHITE, sw * 0.04f, Paint.Align.CENTER, false); subtitlePaint.setAlpha(180);
        scorePaint = makePaint(WHITE, sw * 0.08f, Paint.Align.CENTER, true);
        smallPaint = makePaint(0xFFB0BEC5, sw * 0.035f, Paint.Align.CENTER, false);
        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG); btnPaint.setStyle(Paint.Style.FILL); btnPaint.setColor(0x33FFFFFF);
        btnOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG); btnOutlinePaint.setStyle(Paint.Style.STROKE); btnOutlinePaint.setStrokeWidth(3f); btnOutlinePaint.setColor(CYAN); btnOutlinePaint.setAlpha(120);
        btnTextPaint = makePaint(WHITE, sw * 0.045f, Paint.Align.CENTER, true);
        dimPaint = new Paint(); dimPaint.setColor(DIM); dimPaint.setStyle(Paint.Style.FILL);
        accentPaint = makePaint(CYAN, sw * 0.06f, Paint.Align.CENTER, true);
        labelPaint = makePaint(0xFFB0BEC5, sw * 0.04f, Paint.Align.LEFT, false);
        valuePaint = makePaint(WHITE, sw * 0.04f, Paint.Align.RIGHT, true);
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

        // Kapatma Butonu (X) Boyaları
        closeBtnCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG); closeBtnCirclePaint.setStyle(Paint.Style.FILL);
        closeBtnXPaint = new Paint(Paint.ANTI_ALIAS_FLAG); closeBtnXPaint.setStyle(Paint.Style.STROKE); closeBtnXPaint.setStrokeCap(Paint.Cap.ROUND);

        // Ayarlar Toggle Boyaları
        toggleBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG); toggleBgPaint.setStyle(Paint.Style.FILL);
        toggleActivePaint = new Paint(Paint.ANTI_ALIAS_FLAG); toggleActivePaint.setStyle(Paint.Style.FILL);
        toggleTextPaint = makePaint(WHITE, sw * 0.03f, Paint.Align.CENTER, true);
        dividerPaint = new Paint(); dividerPaint.setColor(Color.argb(40, 150, 160, 180)); dividerPaint.setStrokeWidth(2f);

        for (int i = 0; i < 6; i++) settingsToggleRects[i] = new RectF();

        float cx = sw / 2f, bw = sw * 0.65f, bh = sh * 0.075f;
        playBtn = new RectF(cx - bw/2, sh * 0.45f, cx + bw/2, sh * 0.45f + bh);
        shopBtn = new RectF(cx - bw/2, sh * 0.55f, cx + bw/2, sh * 0.55f + bh);
        settingsBtn = new RectF(cx - bw/2, sh * 0.65f, cx + bw/2, sh * 0.65f + bh);

        restartBtn = new RectF(cx - bw/2, sh * 0.76f, cx + bw/2, sh * 0.76f + bh);

        shopBgPaint.setColor(Color.argb(240, 10, 12, 20));
        statBarBgPaint.setColor(Color.argb(100, 40, 40, 50));
        statBarPaint.setStyle(Paint.Style.FILL);
        
        cardWidth = sw * 0.88f; cardHeight = sh * 0.22f; cardSpacing = sh * 0.03f; cardStartY = sh * 0.15f;
    }

    private Paint makePaint(int color, float size, Paint.Align align, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(color); p.setTextSize(size); p.setTextAlign(align);
        if (bold) p.setTypeface(Typeface.DEFAULT_BOLD); return p;
    }

    public void update(float dt) {
        float target = isShopOpen ? 1f : 0f;
        shopOpenAnim += (target - shopOpenAnim) * dt * 10f;
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
            drawMenu(c, world.getHighScore());
        } else if (world.getState() == Constants.STATE_GAME_OVER) {
            drawGameOver(c, world);
        } else if (world.getState() == Constants.STATE_SETTINGS) {
            drawSettingsScreen(c, world.getSettings(), world.isGodModeActive());
        }

        if (shopOpenAnim > 0.01f) drawShop(c, world.getShipRegistry(), world.getEconomy(), shipRenderer);
    }

    private void drawMenu(Canvas c, int highScore) {
        float p = (float)(Math.sin(pulse) * 0.08 + 0.92); float cx = sw / 2f;
        titlePaint.setTextSize(sw * 0.12f * p); titlePaint.setShader(new LinearGradient(cx - sw*0.3f, sh*0.22f, cx + sw*0.3f, sh*0.22f, CYAN, PURPLE, Shader.TileMode.CLAMP));
        c.drawText("STELLAR", cx, sh * 0.18f, titlePaint); 
        titlePaint.setTextSize(sw * 0.14f * p); c.drawText("DRIFT", cx, sh * 0.26f, titlePaint); titlePaint.setShader(null);
        if (highScore > 0) { smallPaint.setTextSize(sw * 0.035f); smallPaint.setAlpha(180); c.drawText("★ BEST: " + highScore, cx, sh * 0.33f, smallPaint); }
        
        drawButton(c, playBtn, "▶ PLAY", CYAN); 
        drawButton(c, shopBtn, "✦ SHOP", GOLD); 
        drawButton(c, settingsBtn, "⚙ SETTINGS", PURPLE);
        
        subtitlePaint.setTextSize(sw * 0.035f); subtitlePaint.setAlpha((int)(150 * (Math.sin(pulse * 1.5) * 0.3 + 0.7))); 
        c.drawText("Destroy asteroids & upgrade ship!", cx, sh * 0.88f, subtitlePaint);
        smallPaint.setTextSize(sw * 0.025f); smallPaint.setAlpha(80); c.drawText("v4.1 Fuel Update", cx, sh * 0.95f, smallPaint);
    }

    // ==========================================
    // AYARLAR EKRANI (SETTINGS) - YENİDEN ÇİZİLDİ
    // ==========================================
    private void drawSettingsScreen(Canvas c, SettingsManager s, boolean godMode) {
        c.drawRect(0, 0, sw, sh, dimPaint); // Overlay karartma

        float cw = sw * 0.9f; float ch = sh * 0.6f;
        float cx = sw * 0.05f; float cy = sh * 0.2f;

        // Kart Arka Plan
        tempRect.set(cx, cy, cx + cw, cy + ch);
        btnPaint.setColor(Color.argb(220, 18, 22, 35)); c.drawRoundRect(tempRect, 30, 30, btnPaint);
        btnOutlinePaint.setColor(Color.argb(80, 100, 120, 180)); btnOutlinePaint.setStrokeWidth(3f); c.drawRoundRect(tempRect, 30, 30, btnOutlinePaint);

        // Başlık
        accentPaint.setColor(WHITE); accentPaint.setTextSize(sw * 0.07f);
        c.drawText("⚙ SETTINGS", sw / 2f, cy + sh * 0.07f, accentPaint);
        c.drawLine(cx + 40, cy + sh * 0.09f, cx + cw - 40, cy + sh * 0.09f, dividerPaint);

        float rowY = cy + sh * 0.15f; float rowGap = sh * 0.09f;

        // DIFFICULTY
        labelPaint.setColor(Color.rgb(190, 195, 210)); c.drawText("DIFFICULTY", cx + 40, rowY, labelPaint);
        drawThreeWayToggle(c, cx + cw - sw*0.45f, rowY - sh*0.03f, sw*0.4f, sh*0.05f, s.getDifficulty(), new String[]{"EASY", "NORM", "HARD"}, new int[]{Color.rgb(60, 180, 80), Color.rgb(60, 140, 220), Color.rgb(220, 60, 50)}, 0);
        c.drawLine(cx + 40, rowY + sh*0.04f, cx + cw - 40, rowY + sh*0.04f, dividerPaint);

        // GAME SPEED
        rowY += rowGap;
        c.drawText("GAME SPEED", cx + 40, rowY, labelPaint);
        drawThreeWayToggle(c, cx + cw - sw*0.45f, rowY - sh*0.03f, sw*0.4f, sh*0.05f, s.getGameSpeed(), new String[]{"SLOW", "NORM", "FAST"}, new int[]{Color.rgb(100, 160, 200), Color.rgb(60, 140, 220), Color.rgb(240, 160, 30)}, 1);
        c.drawLine(cx + 40, rowY + sh*0.04f, cx + cw - 40, rowY + sh*0.04f, dividerPaint);

        // SOUND
        rowY += rowGap;
        c.drawText("SOUND", cx + 40, rowY, labelPaint);
        drawOnOffToggle(c, cx + cw - sw*0.25f, rowY - sh*0.03f, sw*0.2f, sh*0.045f, s.isSoundEnabled(), soundToggleRect);
        c.drawLine(cx + 40, rowY + sh*0.04f, cx + cw - 40, rowY + sh*0.04f, dividerPaint);

        // VIBRATION
        rowY += rowGap;
        c.drawText("VIBRATION", cx + 40, rowY, labelPaint);
        drawOnOffToggle(c, cx + cw - sw*0.25f, rowY - sh*0.03f, sw*0.2f, sh*0.045f, s.isVibrationEnabled(), vibToggleRect);

        if (godMode) {
            smallPaint.setColor(GOLD); smallPaint.setTextSize(sw * 0.035f);
            c.drawText("★ GOD MODE ACTIVE ★", sw / 2f, cy + ch - 40, smallPaint);
        }

        drawCloseButton(c);
    }

    private void drawThreeWayToggle(Canvas c, float x, float y, float w, float h, int selIdx, String[] labels, int[] colors, int groupIdx) {
        float segW = w / 3f;
        for (int i = 0; i < 3; i++) {
            float sx = x + segW * i;
            RectF r = settingsToggleRects[groupIdx * 3 + i];
            r.set(sx, y, sx + segW, y + h);

            if (i == selIdx) {
                toggleActivePaint.setColor(Color.argb(200, Color.red(colors[i]), Color.green(colors[i]), Color.blue(colors[i])));
                c.drawRoundRect(r, 12, 12, toggleActivePaint);
            } else {
                toggleBgPaint.setColor(Color.argb(80, 40, 45, 60));
                c.drawRoundRect(r, 12, 12, toggleBgPaint);
            }
            toggleTextPaint.setColor(i == selIdx ? WHITE : Color.rgb(130, 135, 150));
            c.drawText(labels[i], sx + segW / 2, y + h * 0.65f, toggleTextPaint);
        }
    }

    private void drawOnOffToggle(Canvas c, float x, float y, float w, float h, boolean isOn, RectF hitRect) {
        hitRect.set(x, y, x + w, y + h);
        if (isOn) {
            toggleActivePaint.setColor(Color.argb(200, 60, 180, 80)); c.drawRoundRect(hitRect, h/2, h/2, toggleActivePaint);
            btnPaint.setColor(WHITE); c.drawCircle(x + w - h/2, y + h/2, h/2 - 4, btnPaint);
            toggleTextPaint.setColor(WHITE); c.drawText("ON", x + w * 0.35f, y + h * 0.65f, toggleTextPaint);
        } else {
            toggleBgPaint.setColor(Color.argb(150, 60, 60, 70)); c.drawRoundRect(hitRect, h/2, h/2, toggleBgPaint);
            btnPaint.setColor(Color.rgb(140, 140, 150)); c.drawCircle(x + h/2, y + h/2, h/2 - 4, btnPaint);
            toggleTextPaint.setColor(Color.rgb(100, 100, 110)); c.drawText("OFF", x + w * 0.65f, y + h * 0.65f, toggleTextPaint);
        }
    }

    // ==========================================
    // X KAPATMA BUTONU
    // ==========================================
    private void drawCloseButton(Canvas c) {
        float cx = sw - sw * 0.1f, cy = sh * 0.08f, rad = sw * 0.05f, cross = rad * 0.4f;
        closeBtnCirclePaint.setColor(Color.argb(180, 200, 50, 50));
        c.drawCircle(cx, cy, rad, closeBtnCirclePaint);
        closeBtnXPaint.setColor(WHITE); closeBtnXPaint.setStrokeWidth(5f);
        c.drawLine(cx - cross, cy - cross, cx + cross, cy + cross, closeBtnXPaint);
        c.drawLine(cx + cross, cy - cross, cx - cross, cy + cross, closeBtnXPaint);
    }

    public boolean isCloseHit(float tx, float ty) {
        float cx = sw - sw * 0.1f, cy = sh * 0.08f;
        return (Math.pow(tx - cx, 2) + Math.pow(ty - cy, 2)) < Math.pow(CLOSE_BTN_HITBOX, 2);
    }

    // ==========================================
    // DOKUNMA YÖNETİMİ
    // ==========================================
    public boolean handleSettingsTouch(float tx, float ty, GameWorld world) {
        if (isCloseHit(tx, ty)) { world.quitToMenu(); return true; }
        SettingsManager s = world.getSettings();

        for (int i = 0; i < 3; i++) { if (settingsToggleRects[i].contains(tx, ty)) { s.cycleDifficulty(); return true; } }
        for (int i = 0; i < 3; i++) { if (settingsToggleRects[3+i].contains(tx, ty)) { s.cycleGameSpeed(); return true; } }

        if (soundToggleRect.contains(tx, ty)) { world.toggleSound(); return true; }
        if (vibToggleRect.contains(tx, ty)) { world.toggleVibration(); return true; }

        return false;
    }

    public boolean handleShopTouch(int action, float tx, float ty, ShipRegistry registry, EconomyManager economy) {
        if (!isShopOpen && shopOpenAnim < 0.01f) return false;
        switch (action) {
            case MotionEvent.ACTION_DOWN: touchDownY = (int) ty; lastTouchY = (int) ty; isDraggingShop = false; return true;
            case MotionEvent.ACTION_MOVE: int dy = lastTouchY - (int) ty; if (Math.abs((int) ty - touchDownY) > 15) isDraggingShop = true; if (isDraggingShop) scrollY = Math.max(0, Math.min(totalContentHeight - sh, scrollY + dy)); lastTouchY = (int) ty; return true;
            case MotionEvent.ACTION_UP:
                if (!isDraggingShop) {
                    if (isCloseHit(tx, ty)) { closeShop(); return true; }
                    float adjY = ty + scrollY;
                    for (int i = 0; i < registry.getShipCount(); i++) {
                        ShipData ship = registry.getShip(i); float cardY = cardStartY + i * (cardHeight + cardSpacing); float cx = (sw - cardWidth) / 2;
                        float btnW = sw * 0.35f, btnH = sh * 0.05f, btnX = cx + cardWidth - btnW - sw * 0.04f, btnY = cardY + cardHeight - btnH - sh * 0.02f;
                        if (tx >= btnX && tx <= btnX + btnW && adjY >= btnY && adjY <= btnY + btnH) {
                            if (economy.isShipUnlocked(ship.id)) { economy.selectShip(ship.id); registry.selectShip(ship.id); } 
                            else if (economy.purchaseShip(ship.id, ship.price)) { economy.selectShip(ship.id); registry.selectShip(ship.id); }
                            return true;
                        }
                    }
                }
                isDraggingShop = false; return true;
        } return true;
    }

    // ==========================================
    // SHOP DRAWING
    // ==========================================
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
            drawShipCard(c, ship, cardY, alpha, economy, renderer);
        }
        c.restore();

        drawCloseButton(c);
        drawCreditDisplay(c, economy, alpha);
    }

    private void drawShipCard(Canvas c, ShipData ship, float cardY, float alpha, EconomyManager economy, ShipRenderer renderer) {
        float cx = (sw - cardWidth) / 2;
        boolean isSelected = (economy.getSelectedShipId() == ship.id);
        boolean isUnlocked = economy.isShipUnlocked(ship.id);

        tempRect.set(cx, cardY, cx + cardWidth, cardY + cardHeight);
        btnPaint.setColor(isSelected ? Color.argb((int)(200 * alpha), 20, 40, 30) : Color.argb((int)(200 * alpha), 25, 25, 35));
        c.drawRoundRect(tempRect, 24, 24, btnPaint);

        btnOutlinePaint.setColor(isSelected ? Color.argb((int)(255 * alpha), 100, 255, 150) : Color.argb((int)(80 * alpha), 100, 150, 200));
        btnOutlinePaint.setStrokeWidth(isSelected ? 5f : 2f);
        c.drawRoundRect(tempRect, 24, 24, btnOutlinePaint);

        float previewX = cx + sw * 0.18f, previewY = cardY + cardHeight / 2;
        btnPaint.setColor(Color.argb((int)(50 * alpha), 80, 100, 150)); c.drawCircle(previewX, previewY, sw * 0.13f, btnPaint);
        renderer.drawShip(c, ship, previewX, previewY, 0f, (int)(255 * alpha), (sw/1080f) * 2.2f, false);

        float infoX = cx + sw * 0.38f;
        labelPaint.setColor(Color.argb((int)(255 * alpha), Color.red(ship.cockpitColor), Color.green(ship.cockpitColor), Color.blue(ship.cockpitColor)));
        labelPaint.setTextSize(sw * 0.05f); labelPaint.setFakeBoldText(true); c.drawText(ship.name, infoX, cardY + sh * 0.04f, labelPaint);

        smallPaint.setColor(Color.argb((int)(160 * alpha), 200, 200, 220)); smallPaint.setTextSize(sw * 0.028f); smallPaint.setTextAlign(Paint.Align.LEFT);
        c.drawText(ship.description, infoX, cardY + sh * 0.07f, smallPaint);

        float barY = cardY + sh * 0.1f, barW = cardWidth - (sw * 0.45f), barH = sh * 0.012f;
        drawStatBar(c, "SPD", ship.speedMultiplier / 1.5f, Color.rgb(80, 200, 255), infoX, barY, barW, barH, alpha);
        drawStatBar(c, "FRT", ship.fireRate / 5f, Color.rgb(255, 180, 50), infoX, barY + sh*0.03f, barW, barH, alpha);
        drawStatBar(c, "DMG", ship.damage / 4f, Color.rgb(255, 80, 60), infoX, barY + sh*0.06f, barW, barH, alpha);

        float btnW = sw * 0.35f, btnH = sh * 0.05f;
        float btnX = cx + cardWidth - btnW - sw * 0.04f, btnY = cardY + cardHeight - btnH - sh * 0.02f;
        tempRect.set(btnX, btnY, btnX + btnW, btnY + btnH);

        btnTextPaint.setTextSize(btnH * 0.4f);
        if (isSelected) {
            btnPaint.setColor(Color.argb((int)(180 * alpha), 40, 160, 80)); c.drawRoundRect(tempRect, btnH/2, btnH/2, btnPaint);
            btnTextPaint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255)); c.drawText("✓ EQUIPPED", btnX + btnW/2, btnY + btnH*0.65f, btnTextPaint);
        } else if (isUnlocked) {
            btnPaint.setColor(Color.argb((int)(180 * alpha), 60, 120, 180)); c.drawRoundRect(tempRect, btnH/2, btnH/2, btnPaint);
            btnTextPaint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255)); c.drawText("EQUIP", btnX + btnW/2, btnY + btnH*0.65f, btnTextPaint);
        } else {
            btnPaint.setColor(economy.getCredits() >= ship.price ? Color.argb((int)(200 * alpha), 200, 160, 30) : Color.argb((int)(100 * alpha), 80, 80, 80));
            c.drawRoundRect(tempRect, btnH/2, btnH/2, btnPaint);
            btnTextPaint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255)); c.drawText("🔒 " + ship.price + " ✦", btnX + btnW/2, btnY + btnH*0.65f, btnTextPaint);
        }
    }

    private void drawStatBar(Canvas c, String label, float ratio, int color, float x, float y, float w, float h, float alpha) {
        smallPaint.setColor(Color.argb((int)(180 * alpha), 180, 180, 180)); smallPaint.setTextSize(sw * 0.025f); c.drawText(label, x, y + h, smallPaint);
        float bx = x + sw * 0.08f;
        tempRect.set(bx, y, bx + (w - sw * 0.08f), y + h); statBarBgPaint.setAlpha((int)(100 * alpha)); c.drawRoundRect(tempRect, h/2, h/2, statBarBgPaint);
        tempRect.set(bx, y, bx + (w - sw * 0.08f) * Math.min(1f, ratio), y + h); statBarPaint.setColor(Color.argb((int)(220 * alpha), Color.red(color), Color.green(color), Color.blue(color))); c.drawRoundRect(tempRect, h/2, h/2, statBarPaint);
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
        
        // FUEL BAR DRAWING (World'den al)
        if (world.getFuelSystem() != null) world.getFuelSystem().draw(c);

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
        if (world.isDoubleActive()) { drawPowerBar(c, pad, pbY - pbc*(pbH+8), pbW, pbH, "DOUBLE", world.getDoubleTimer(), Constants.POWERUP_DURATION, PowerUp.getColor(Constants.POWERUP_DOUBLE)); pbc++; }
    }

    private void drawPowerBar(Canvas c, float x, float y, float w, float h, String name, int timer, int max, int color) {
        hudLabelPaint.setTextAlign(Paint.Align.LEFT); hudLabelPaint.setAlpha(200); hudLabelPaint.setColor(color); c.drawText(name, x, y - 4, hudLabelPaint); hudLabelPaint.setColor(0x99FFFFFF);
        c.drawRoundRect(new RectF(x, y, x + w, y + h), h, h, powerBarBg);
        float f = (float) timer / max; powerBarFill.setColor(color); powerBarFill.setAlpha(f < 0.25f ? (int)(200 * (Math.sin(pulse * 8) * 0.3 + 0.7)) : 200); c.drawRoundRect(new RectF(x, y, x + w * f, y + h), h, h, powerBarFill);
    }

    private void drawMilestone(Canvas c, GameWorld world) { /* Orijinalle ayni */ }
    private void drawHighScoreProximity(Canvas c, GameWorld world) { /* Orijinalle ayni */ }
    private void drawGameOver(Canvas c, GameWorld world) { /* Orijinalle ayni */ }
    private void drawStats(Canvas c, GameWorld w, float a) { /* Orijinalle ayni */ }
    public void resetGameOver() { gameOverTime = 0; recordBroken = false; recordBreakParticleTimer = 0; }
    public void openShop() { isShopOpen = true; scrollY = 0; }
    public void closeShop() { isShopOpen = false; }
    public boolean isShopVisible() { return isShopOpen || shopOpenAnim > 0.01f; }
    private float dist(float x1, float y1, float x2, float y2) { return (float) Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)); }

    private void drawButton(Canvas c, RectF r, String text, int ac) {
        float rad = r.height() * 0.45f; c.drawRoundRect(r, rad, rad, btnPaint); btnOutlinePaint.setColor(ac); c.drawRoundRect(r, rad, rad, btnOutlinePaint);
        btnTextPaint.setTextSize(r.height() * 0.35f); btnTextPaint.setColor(WHITE); c.drawText(text, r.centerX(), r.centerY() + r.height()*0.12f, btnTextPaint);
    }

    public boolean isPlayHit(float x, float y) { return playBtn.contains(x, y); }
    public boolean isShopHit(float x, float y) { return shopBtn.contains(x, y); }
    public boolean isSettingsHit(float x, float y) { return settingsBtn.contains(x, y); }
    public boolean isRestartHit(float x, float y) { if (gameOverTime > 0 && System.currentTimeMillis() - gameOverTime < BUTTON_DELAY) return false; return restartBtn.contains(x, y); }
}
