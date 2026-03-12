package com.stellardrift.game.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.stellardrift.game.util.EconomyManager;
import com.stellardrift.game.world.ShipData;
import com.stellardrift.game.world.ShipRegistry;

/**
 * STELLAR DRIFT — Responsive Hangar UI System (Full Rewrite)
 * All measurements are calculated via screen width (sw) and height (sh) percentages.
 */
public class ShopUI {

    // ═══════════════════════════════════════════════════
    //  DEPENDENCIES
    // ═══════════════════════════════════════════════════
    private final ShipRegistry shipRegistry;
    private final EconomyManager economy;
    private final ShipRenderer shipRenderer;

    // ═══════════════════════════════════════════════════
    //  SCREEN DIMENSIONS
    // ═══════════════════════════════════════════════════
    private float sw, sh; // screen width, screen height

    // ═══════════════════════════════════════════════════
    //  LAYOUT CACHE — Calculated only in initLayout()
    //  No measurements performed during render
    // ═══════════════════════════════════════════════════
    private static final class Layout {
        // Card
        float cardMarginX;      // horizontal card margin
        float cardWidth;        // total card width
        float cardHeight;       // total card height
        float cardSpacing;      // vertical spacing between cards
        float cardCornerR;      // card corner radius
        float firstCardY;       // first card's Y position
        float totalScrollH;     // total scrollable height

        // Card inner padding
        float padL, padR, padT, padB; // left, right, top, bottom inner padding

        // Preview area (left side)
        float previewDiameter;
        float previewCxOffset;  // relative to card's left edge
        float previewCyOffset;  // relative to card's top edge

        // Info panel (right side)
        float infoX;            // info panel's left edge (relative to card)
        float infoW;            // info panel's width

        // Texts
        float titleSize;
        float titleY;           // relative to card top
        float descSize;
        float descY;
        float dividerY;

        // Stat rows
        float statStartY;       // first stat row's Y relative to card
        float statRowH;         // height of each stat row
        float statLabelW;       // "SPD" label width
        float statBarH;         // bar height
        float statBarX;         // bar start X (relative to card)
        float statBarW;         // bar width
        float statDotR;         // level dot radius
        float statDotY;         // dot Y offset (relative to bar)

        // Upgrade button
        float upgBtnW;
        float upgBtnH;
        float upgBtnX;          // relative to card
        float upgBtnTextSize;

        // Main button
        float mainBtnW;
        float mainBtnH;
        float mainBtnX;         // relative to card
        float mainBtnY;         // relative to card
        float mainBtnTextSize;
        float mainBtnCornerR;

        // Header
        float headerSize;
        float headerY;

        // Credit badge
        float badgeW, badgeH;
        float badgeX, badgeY;   // relative to screen
        float badgeTextSize;
        float badgeCornerR;

        // Close button
        float closeBtnR;
        float closeBtnCx, closeBtnCy;
        float closeBtnHitR;     // touch radius
        float closeCrossSize;
    }

    private final Layout L = new Layout();

    // ═══════════════════════════════════════════════════
    //  HITBOX CACHE — Dimensions calculated in initLayout()
    //  Positions updated in drawShipCard()
    // ═══════════════════════════════════════════════════
    private final RectF[][] upgradeHitboxes;  // [ship][stat]
    private final RectF[] mainBtnHitboxes;    // [ship]

    // ═══════════════════════════════════════════════════
    //  SCROLL
    // ═══════════════════════════════════════════════════
    private float scrollY = 0f;
    private float scrollVelocity = 0f;
    private int touchDownY = -1, lastTouchY = -1;
    private boolean isDragging = false;

    // ═══════════════════════════════════════════════════
    //  ANIMATION
    // ═══════════════════════════════════════════════════
    private boolean visible = false;
    private float openAnim = 0f;
    private float purchaseFlashTimer = 0f;
    private int purchaseFlashId = -1;
    private float upgradeFlashTimer = 0f;
    private int upgradeFlashShip = -1, upgradeFlashStat = -1;

    // ═══════════════════════════════════════════════════
    //  PAINTS — Each used solely for its purpose
    // ═══════════════════════════════════════════════════
    private final Paint pBgBlack      = new Paint();
    private final Paint pBgOverlay    = new Paint();
    private final Paint pCard         = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCardBorder   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCardGlow     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTitle        = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pName         = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pDesc         = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pDivider      = new Paint();
    private final Paint pStatLabel    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pStatBarBg    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pStatBarFill  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pStatDot      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pUpgBtn       = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pUpgText      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pMainBtn      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pMainBtnText  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBadgeBg      = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBadgeBorder  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBadgeText    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCloseCircle  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCloseCross   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pPreviewBg    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pFlash        = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Reusable RectF
    private final RectF rc = new RectF();

    // ═══════════════════════════════════════════════════
    //  CALLBACK
    // ═══════════════════════════════════════════════════
    public interface Listener {
        void onShopClosed();
        void onShipPurchased();
        void onShipEquipped();
        void onUpgraded();
        void onActionFailed();
    }
    private Listener listener;
    public void setListener(Listener l) { this.listener = l; }

    // ═══════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ═══════════════════════════════════════════════════
    public ShopUI(ShipRegistry registry, EconomyManager economy,
                  ShipRenderer renderer, float sw, float sh) {
        this.shipRegistry = registry;
        this.economy = economy;
        this.shipRenderer = renderer;

        int n = registry.getShipCount();
        upgradeHitboxes = new RectF[n][ShipData.STAT_COUNT];
        mainBtnHitboxes = new RectF[n];
        for (int i = 0; i < n; i++) {
            mainBtnHitboxes[i] = new RectF();
            for (int j = 0; j < ShipData.STAT_COUNT; j++) {
                upgradeHitboxes[i][j] = new RectF();
            }
        }

        initPaints();
        initLayout(sw, sh);
    }

    // ═══════════════════════════════════════════════════
    //  PAINT INITIALIZATION — Only once, immutable properties
    // ═══════════════════════════════════════════════════
    private void initPaints() {
        pBgBlack.setStyle(Paint.Style.FILL);
        pBgOverlay.setStyle(Paint.Style.FILL);
        pCard.setStyle(Paint.Style.FILL);
        pCardBorder.setStyle(Paint.Style.STROKE);
        pCardGlow.setStyle(Paint.Style.STROKE);
        pStatBarBg.setStyle(Paint.Style.FILL);
        pStatBarFill.setStyle(Paint.Style.FILL);
        pStatDot.setStyle(Paint.Style.FILL);
        pUpgBtn.setStyle(Paint.Style.FILL);
        pMainBtn.setStyle(Paint.Style.FILL);
        pBadgeBg.setStyle(Paint.Style.FILL);
        pBadgeBorder.setStyle(Paint.Style.STROKE);
        pCloseCircle.setStyle(Paint.Style.FILL);
        pFlash.setStyle(Paint.Style.FILL);
        pPreviewBg.setStyle(Paint.Style.FILL);
        pDivider.setStrokeWidth(1f);

        pCloseCross.setStyle(Paint.Style.STROKE);
        pCloseCross.setStrokeCap(Paint.Cap.ROUND);
        pCloseCross.setColor(Color.WHITE);

        pTitle.setFakeBoldText(true);
        pTitle.setTextAlign(Paint.Align.CENTER);
        pName.setFakeBoldText(true);
        pName.setTextAlign(Paint.Align.LEFT);
        pDesc.setTextAlign(Paint.Align.LEFT);
        pStatLabel.setFakeBoldText(true);
        pStatLabel.setTextAlign(Paint.Align.LEFT);
        pUpgText.setFakeBoldText(true);
        pUpgText.setTextAlign(Paint.Align.CENTER);
        pMainBtnText.setFakeBoldText(true);
        pMainBtnText.setTextAlign(Paint.Align.CENTER);
        pBadgeText.setFakeBoldText(true);
        pBadgeText.setTextAlign(Paint.Align.CENTER);
    }

    // ═══════════════════════════════════════════════════
    //  LAYOUT CALCULATION — All measures are sw/sh percentages
    //  Not re-calculated during render
    // ═══════════════════════════════════════════════════
    private void initLayout(float sw, float sh) {
        this.sw = sw;
        this.sh = sh;

        // ── Card ──
        L.cardMarginX   = sw * 0.03f;
        L.cardWidth     = sw - L.cardMarginX * 2;
        L.cardHeight    = sh * 0.28f;
        L.cardSpacing   = sh * 0.018f;
        L.cardCornerR   = sw * 0.03f;
        L.firstCardY    = sh * 0.12f;

        // Card inner padding
        L.padL = L.cardWidth * 0.03f;
        L.padR = L.cardWidth * 0.03f;
        L.padT = L.cardHeight * 0.06f;
        L.padB = L.cardHeight * 0.06f;

        // ── Ship preview ──
        L.previewDiameter  = L.cardWidth * 0.20f;
        L.previewCxOffset  = L.padL + L.previewDiameter * 0.5f;
        L.previewCyOffset  = L.cardHeight * 0.38f;

        // ── Info panel ──
        float previewTotalW = L.padL + L.previewDiameter + L.cardWidth * 0.03f;
        L.infoX = previewTotalW;
        L.infoW = L.cardWidth - previewTotalW - L.padR;

        // ── Texts ──
        L.titleSize = sw * 0.038f;
        L.titleY    = L.padT + L.titleSize;
        L.descSize  = sw * 0.024f;
        L.descY     = L.titleY + L.cardHeight * 0.08f;
        L.dividerY  = L.descY + L.cardHeight * 0.04f;

        // ── Stat rows ──
        L.statStartY = L.dividerY + L.cardHeight * 0.05f;
        L.statRowH   = L.cardHeight * 0.125f;
        L.statBarH   = L.statRowH * 0.38f;
        L.statDotR   = L.statBarH * 0.25f;
        L.statDotY   = -L.statBarH * 0.6f;

        // Horizontal partitioning:
        // [label 13%] [gap 2%] [bar rest] [gap 2%] [upgBtn 25%]
        L.statLabelW = L.infoW * 0.13f;
        float gapA   = L.infoW * 0.02f;
        float gapB   = L.infoW * 0.02f;
        L.upgBtnW    = L.infoW * 0.25f;
        L.upgBtnH    = L.statRowH * 0.65f;
        L.upgBtnTextSize = sw * 0.02f;

        L.statBarX   = L.infoX + L.statLabelW + gapA;
        L.statBarW   = L.infoW - L.statLabelW - gapA - gapB - L.upgBtnW;
        L.upgBtnX    = L.statBarX + L.statBarW + gapB;

        // ── Stat label font size ──
        pStatLabel.setTextSize(sw * 0.022f);

        // ── Main button ──
        L.mainBtnW       = L.infoW * 0.48f;
        L.mainBtnH       = L.cardHeight * 0.12f;
        L.mainBtnX       = L.infoX + L.infoW - L.mainBtnW;
        L.mainBtnY       = L.cardHeight - L.padB - L.mainBtnH;
        L.mainBtnTextSize= sw * 0.028f;
        L.mainBtnCornerR = L.mainBtnH * 0.5f;

        // ── Header ──
        L.headerSize = sw * 0.055f;
        L.headerY    = L.firstCardY - sh * 0.03f;

        // ── Credit badge ──
        L.badgeW      = sw * 0.26f;
        L.badgeH      = sh * 0.032f;
        L.badgeX      = sw - L.badgeW - sw * 0.04f;
        L.badgeY      = sh * 0.03f;
        L.badgeTextSize = sw * 0.032f;
        L.badgeCornerR  = L.badgeH * 0.5f;

        // ── Close button ──
        L.closeBtnR     = sw * 0.04f;
        L.closeBtnCx    = sw - sw * 0.08f;
        L.closeBtnCy    = sh * 0.05f;
        L.closeBtnHitR  = L.closeBtnR + sw * 0.03f;
        L.closeCrossSize= L.closeBtnR * 0.45f;

        // ── Border thicknesses ──
        pCardBorder.setStrokeWidth(sw * 0.003f);
        pCardGlow.setStrokeWidth(sw * 0.004f);
        pBadgeBorder.setStrokeWidth(sw * 0.003f);
        pCloseCross.setStrokeWidth(sw * 0.005f);

        // ── Font sizes (Set to Paints) ──
        pTitle.setTextSize(L.headerSize);
        pName.setTextSize(L.titleSize);
        pDesc.setTextSize(L.descSize);
        pUpgText.setTextSize(L.upgBtnTextSize);
        pMainBtnText.setTextSize(L.mainBtnTextSize);
        pBadgeText.setTextSize(L.badgeTextSize);

        // ── Total scroll height ──
        int n = shipRegistry.getShipCount();
        L.totalScrollH = L.firstCardY + n * (L.cardHeight + L.cardSpacing) + sh * 0.08f;
    }

    // ═══════════════════════════════════════════════════
    //  MAIN RENDER
    // ═══════════════════════════════════════════════════
    public void draw(Canvas c) {
        if (openAnim < 0.005f) return;
        float a = openAnim;

        // ── Full opaque background ──
        pBgBlack.setColor(Color.argb((int)(255 * a), 0, 0, 0));
        c.drawRect(0, 0, sw, sh, pBgBlack);
        pBgOverlay.setColor(Color.argb((int)(242 * a), 6, 7, 15));
        c.drawRect(0, 0, sw, sh, pBgOverlay);

        c.save();
        c.translate(0, -scrollY);

        // ── Header ──
        pTitle.setColor(Color.argb((int)(255 * a), 255, 255, 255));
        pTitle.setAlpha((int)(255 * a));
        c.drawText("✦  H A N G A R  ✦", sw * 0.5f, L.headerY, pTitle);

        // ── Cards ──
        int n = shipRegistry.getShipCount();
        for (int i = 0; i < n; i++) {
            float cy = L.firstCardY + i * (L.cardHeight + L.cardSpacing);
            if (cy - scrollY > sh + L.cardHeight) continue;
            if (cy + L.cardHeight - scrollY < -L.cardHeight) continue;
            drawShipCard(c, shipRegistry.getShip(i), i, cy, a);
        }

        c.restore();

        // ── Static UI (not affected by scroll) ──
        drawCreditBadge(c, a);
        drawCloseButton(c, a);
    }

    // ═══════════════════════════════════════════════════
    //  SHIP CARD
    // ═══════════════════════════════════════════════════
    private void drawShipCard(Canvas c, ShipData ship, int idx,
                               float cardY, float a) {
        float cx = L.cardMarginX;
        boolean selected = economy.getSelectedShipId() == ship.id;
        boolean unlocked = economy.isShipUnlocked(ship.id);

        // ─── Card Background ───
        rc.set(cx, cardY, cx + L.cardWidth, cardY + L.cardHeight);

        pCard.setColor(selected
                ? Color.argb((int)(195 * a), 14, 32, 26)
                : Color.argb((int)(195 * a), 14, 16, 26));
        c.drawRoundRect(rc, L.cardCornerR, L.cardCornerR, pCard);

        // Border
        if (selected) {
            pCardGlow.setColor(Color.argb((int)(180 * a), 70, 240, 140));
            c.drawRoundRect(rc, L.cardCornerR, L.cardCornerR, pCardGlow);
        } else {
            pCardBorder.setColor(Color.argb((int)(45 * a), 90, 110, 150));
            c.drawRoundRect(rc, L.cardCornerR, L.cardCornerR, pCardBorder);
        }

        // ─── Ship Preview ───
        float pvCx = cx + L.previewCxOffset;
        float pvCy = cardY + L.previewCyOffset;
        float pvR = L.previewDiameter * 0.48f;

        pPreviewBg.setColor(Color.argb((int)(30 * a), 70, 95, 135));
        c.drawCircle(pvCx, pvCy, pvR, pPreviewBg);

        float shipScale = L.previewDiameter / sw * 5.5f;
        shipScale = Math.max(1.1f, Math.min(2.0f, shipScale));
        shipRenderer.drawShip(c, ship, pvCx, pvCy, 0f, (int)(255 * a), shipScale, false);


        // ─── Name ───
        float absInfoX = cx + L.infoX;

        pName.setColor(Color.argb((int)(255 * a),
                Color.red(ship.cockpitColor),
                Color.green(ship.cockpitColor),
                Color.blue(ship.cockpitColor)));
        c.drawText(ship.name, absInfoX, cardY + L.titleY, pName);

        // ─── Description ───
        pDesc.setColor(Color.argb((int)(130 * a), 185, 190, 208));

        String desc = ship.description;
        float maxDescW = L.infoW - sw * 0.01f;
        while (desc.length() > 4 && pDesc.measureText(desc + "..") > maxDescW) {
            desc = desc.substring(0, desc.length() - 1);
        }
        if (desc.length() < ship.description.length()) desc += "..";
        c.drawText(desc, absInfoX, cardY + L.descY, pDesc);

        // ─── Divider ───
        pDivider.setColor(Color.argb((int)(28 * a), 140, 160, 195));
        c.drawLine(absInfoX, cardY + L.dividerY,
                absInfoX + L.infoW - sw * 0.01f,
                cardY + L.dividerY, pDivider);

        // ─── Stat Rows ───
        for (int st = 0; st < ShipData.STAT_COUNT; st++) {
            drawStatRow(c, ship, idx, st, cx, cardY, a, unlocked);
        }

        // ─── Main Button ───
        drawMainButton(c, ship, idx, cx, cardY, a, selected, unlocked);

        // ─── Flash Effect ───
        if (purchaseFlashId == ship.id && purchaseFlashTimer > 0) {
            pFlash.setColor(Color.argb(
                    (int)(purchaseFlashTimer / 0.7f * 45), 255, 215, 40));
            rc.set(cx, cardY, cx + L.cardWidth, cardY + L.cardHeight);
            c.drawRoundRect(rc, L.cardCornerR, L.cardCornerR, pFlash);
        }
    }

    // ═══════════════════════════════════════════════════
    //  STAT ROW
    // ═══════════════════════════════════════════════════
    private void drawStatRow(Canvas c, ShipData ship, int shipIdx, int statIdx,
                              float cardX, float cardY, float a, boolean unlocked) {

        float rowY = cardY + L.statStartY + statIdx * L.statRowH;
        float absInfoX = cardX + L.infoX;

        String name = ship.getStatName(statIdx);
        int color = ship.getStatColor(statIdx);
        float ratio = ship.getStatBarRatio(statIdx);
        int level = ship.getUpgradeLevel(statIdx);

        // ── Label ──
        pStatLabel.setColor(Color.argb((int)(155 * a), 175, 180, 200));
        c.drawText(name, absInfoX, rowY + L.statBarH * 0.85f, pStatLabel);

        // ── Bar background ──
        float bx = cardX + L.statBarX;
        float by = rowY;
        rc.set(bx, by, bx + L.statBarW, by + L.statBarH);
        pStatBarBg.setColor(Color.argb((int)(65 * a), 38, 42, 58));
        c.drawRoundRect(rc, L.statBarH * 0.5f, L.statBarH * 0.5f, pStatBarBg);

        // ── Bar fill ──
        float fill = L.statBarW * Math.max(0, Math.min(1, ratio));
        if (fill > L.statBarH * 0.5f) {
            rc.set(bx, by, bx + fill, by + L.statBarH);
            pStatBarFill.setColor(Color.argb((int)(195 * a),
                    Color.red(color), Color.green(color), Color.blue(color)));
            c.drawRoundRect(rc, L.statBarH * 0.5f, L.statBarH * 0.5f, pStatBarFill);
        }

        // ── Level dots ──
        float dotStep = L.statBarW / ShipData.MAX_UPGRADE_LEVEL;
        for (int lv = 0; lv < ShipData.MAX_UPGRADE_LEVEL; lv++) {
            float dx = bx + dotStep * (lv + 0.5f);
            float dy = by + L.statDotY;
            pStatDot.setColor(lv < level
                    ? Color.argb((int)(210 * a), Color.red(color), Color.green(color), Color.blue(color))
                    : Color.argb((int)(35 * a), 140, 150, 175));
            c.drawCircle(dx, dy, L.statDotR, pStatDot);
        }

        // ── Upgrade button ──
        float ubx = cardX + L.upgBtnX;
        float uby = rowY + (L.statBarH - L.upgBtnH) * 0.5f;

        if (unlocked && level < ShipData.MAX_UPGRADE_LEVEL) {
            int cost = ShipData.UPGRADE_PRICES[level];
            boolean afford = economy.getCredits() >= cost;

            // Save Hitbox
            upgradeHitboxes[shipIdx][statIdx].set(ubx, uby, ubx + L.upgBtnW, uby + L.upgBtnH);

            // Button
            pUpgBtn.setColor(afford
                    ? Color.argb((int)(175 * a), 35, 135, 48)
                    : Color.argb((int)(75 * a), 50, 52, 62));
            rc.set(ubx, uby, ubx + L.upgBtnW, uby + L.upgBtnH);
            c.drawRoundRect(rc, L.upgBtnH * 0.5f, L.upgBtnH * 0.5f, pUpgBtn);

            // Text
            pUpgText.setColor(afford
                    ? Color.argb((int)(255 * a), 255, 255, 255)
                    : Color.argb((int)(90 * a), 135, 140, 150));
            c.drawText("+" + cost + "✦",
                    ubx + L.upgBtnW * 0.5f,
                    uby + L.upgBtnH * 0.5f + L.upgBtnTextSize * 0.35f,
                    pUpgText);

            // Flash
            if (upgradeFlashShip == shipIdx && upgradeFlashStat == statIdx
                    && upgradeFlashTimer > 0) {
                pFlash.setColor(Color.argb(
                        (int)(upgradeFlashTimer / 0.4f * 70), 90, 255, 110));
                c.drawRoundRect(rc, L.upgBtnH * 0.5f, L.upgBtnH * 0.5f, pFlash);
            }

        } else if (unlocked) {
            upgradeHitboxes[shipIdx][statIdx].setEmpty();
            pUpgText.setColor(Color.argb((int)(110 * a),
                    Color.red(color), Color.green(color), Color.blue(color)));
            c.drawText("MAX", ubx + L.upgBtnW * 0.5f,
                    uby + L.upgBtnH * 0.5f + L.upgBtnTextSize * 0.35f, pUpgText);
        } else {
            upgradeHitboxes[shipIdx][statIdx].setEmpty();
            pUpgText.setColor(Color.argb((int)(40 * a), 110, 115, 130));
            c.drawText("🔒", ubx + L.upgBtnW * 0.5f,
                    uby + L.upgBtnH * 0.5f + L.upgBtnTextSize * 0.35f, pUpgText);
        }
    }

    // ═══════════════════════════════════════════════════
    //  MAIN BUTTON
    // ═══════════════════════════════════════════════════
    private void drawMainButton(Canvas c, ShipData ship, int idx,
                                 float cardX, float cardY, float a,
                                 boolean selected, boolean unlocked) {

        float bx = cardX + L.mainBtnX;
        float by = cardY + L.mainBtnY;
        mainBtnHitboxes[idx].set(bx, by, bx + L.mainBtnW, by + L.mainBtnH);
        rc.set(bx, by, bx + L.mainBtnW, by + L.mainBtnH);

        float textY = by + L.mainBtnH * 0.5f + L.mainBtnTextSize * 0.32f;

        if (selected) {
            pMainBtn.setColor(Color.argb((int)(165 * a), 30, 145, 65));
            c.drawRoundRect(rc, L.mainBtnCornerR, L.mainBtnCornerR, pMainBtn);
            pMainBtnText.setColor(Color.argb((int)(255 * a), 255, 255, 255));
            c.drawText("✓ EQUIPPED", rc.centerX(), textY, pMainBtnText);

        } else if (unlocked) {
            pMainBtn.setColor(Color.argb((int)(165 * a), 45, 105, 165));
            c.drawRoundRect(rc, L.mainBtnCornerR, L.mainBtnCornerR, pMainBtn);
            pMainBtnText.setColor(Color.argb((int)(255 * a), 255, 255, 255));
            c.drawText("EQUIP", rc.centerX(), textY, pMainBtnText);

        } else {
            boolean afford = economy.getCredits() >= ship.price;
            pMainBtn.setColor(afford
                    ? Color.argb((int)(185 * a), 185, 150, 28)
                    : Color.argb((int)(85 * a), 60, 60, 68));
            c.drawRoundRect(rc, L.mainBtnCornerR, L.mainBtnCornerR, pMainBtn);
            pMainBtnText.setColor(afford
                    ? Color.argb((int)(255 * a), 255, 255, 255)
                    : Color.argb((int)(130 * a), 150, 150, 160));

            String priceStr = "🔒 " + ship.price + " ✦";
            if (pMainBtnText.measureText(priceStr) > L.mainBtnW * 0.9f) {
                pMainBtnText.setTextSize(L.mainBtnTextSize * 0.8f);
            }
            c.drawText(priceStr, rc.centerX(), textY, pMainBtnText);
            pMainBtnText.setTextSize(L.mainBtnTextSize);
        }
    }

    // ═══════════════════════════════════════════════════
    //  CREDIT BADGE
    // ═══════════════════════════════════════════════════
    private void drawCreditBadge(Canvas c, float a) {
        float bx = L.badgeX, by = L.badgeY;
        rc.set(bx, by, bx + L.badgeW, by + L.badgeH);

        pBadgeBg.setColor(Color.argb((int)(145 * a), 10, 10, 20));
        c.drawRoundRect(rc, L.badgeCornerR, L.badgeCornerR, pBadgeBg);

        float flash = economy.getCreditFlash();
        int br = Math.min(255, (int)(195 + 60 * flash));
        int bg = Math.min(255, (int)(175 + 80 * flash));

        pBadgeBorder.setColor(Color.argb((int)(65 * a), br, bg, 40));
        c.drawRoundRect(rc, L.badgeCornerR, L.badgeCornerR, pBadgeBorder);

        pBadgeText.setColor(Color.argb((int)(255 * a), br, bg, 45));
        c.drawText("✦ " + economy.getDisplayedCredits(),
                bx + L.badgeW * 0.5f,
                by + L.badgeH * 0.5f + L.badgeTextSize * 0.32f,
                pBadgeText);
    }

    // ═══════════════════════════════════════════════════
    //  CLOSE BUTTON
    // ═══════════════════════════════════════════════════
    private void drawCloseButton(Canvas c, float a) {
        float cx = L.closeBtnCx, cy = L.closeBtnCy;

        pCloseCircle.setColor(Color.argb((int)(165 * a), 170, 38, 38));
        c.drawCircle(cx, cy, L.closeBtnR, pCloseCircle);

        pCloseCross.setAlpha((int)(230 * a));
        float s = L.closeCrossSize;
        c.drawLine(cx - s, cy - s, cx + s, cy + s, pCloseCross);
        c.drawLine(cx + s, cy - s, cx - s, cy + s, pCloseCross);
    }

    // ═══════════════════════════════════════════════════
    //  TOUCH MANAGEMENT
    // ═══════════════════════════════════════════════════
    public boolean handleTouch(int action, float tx, float ty) {
        if (!visible && openAnim < 0.01f) return false;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchDownY = (int) ty;
                lastTouchY = (int) ty;
                isDragging = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                int dy = lastTouchY - (int) ty;
                if (Math.abs((int) ty - touchDownY) > sh * 0.015f) isDragging = true;
                if (isDragging) {
                    scrollY = clampScroll(scrollY + dy);
                }
                lastTouchY = (int) ty;
                return true;

            case MotionEvent.ACTION_UP:
                if (!isDragging) return handleClick(tx, ty);
                isDragging = false;
                return true;
        }
        return true;
    }

    private boolean handleClick(float tx, float ty) {
        // Close button
        float dx = tx - L.closeBtnCx, dy = ty - L.closeBtnCy;
        if (dx * dx + dy * dy < L.closeBtnHitR * L.closeBtnHitR) {
            close();
            if (listener != null) listener.onShopClosed();
            return true;
        }

        float aty = ty + scrollY; // scroll-adjusted Y

        int n = shipRegistry.getShipCount();
        for (int i = 0; i < n; i++) {
            ShipData ship = shipRegistry.getShip(i);

            // Upgrade buttons
            for (int st = 0; st < ShipData.STAT_COUNT; st++) {
                RectF h = upgradeHitboxes[i][st];
                if (!h.isEmpty() && h.contains(tx, aty)) {
                    if (economy.purchaseUpgrade(ship.id, st)) {
                        economy.syncUpgradesToShipData(shipRegistry.getAllShips());
                        upgradeFlashShip = i;
                        upgradeFlashStat = st;
                        upgradeFlashTimer = 0.4f;
                        if (listener != null) listener.onUpgraded();
                    } else {
                        if (listener != null) listener.onActionFailed();
                    }
                    return true;
                }
            }

            // Main button
            RectF mb = mainBtnHitboxes[i];
            if (mb.contains(tx, aty)) {
                handleMainBtn(ship);
                return true;
            }
        }
        return true;
    }

    private void handleMainBtn(ShipData ship) {
        if (economy.getSelectedShipId() == ship.id) return;

        if (economy.isShipUnlocked(ship.id)) {
            economy.selectShip(ship.id);
            shipRegistry.selectShip(ship.id);
            if (listener != null) listener.onShipEquipped();
        } else if (economy.purchaseShip(ship.id, ship.price)) {
            ship.unlocked = true;
            economy.selectShip(ship.id);
            shipRegistry.selectShip(ship.id);
            economy.syncUpgradesToShipData(shipRegistry.getAllShips());
            purchaseFlashId = ship.id;
            purchaseFlashTimer = 0.7f;
            if (listener != null) listener.onShipPurchased();
        } else {
            if (listener != null) listener.onActionFailed();
        }
    }

    // ═══════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════
    private float clampScroll(float s) {
        float maxScroll = Math.max(0, L.totalScrollH - sh);
        return Math.max(0, Math.min(maxScroll, s));
    }

    public void update(float dt) {
        openAnim += ((visible ? 1f : 0f) - openAnim) * dt * 8f;
        if (purchaseFlashTimer > 0) purchaseFlashTimer -= dt;
        if (upgradeFlashTimer > 0) upgradeFlashTimer -= dt;
        scrollY = clampScroll(scrollY);
    }

    public void open()  { visible = true; scrollY = 0; }
    public void close() { visible = false; }
    public boolean isVisible() { return visible || openAnim > 0.01f; }
}
