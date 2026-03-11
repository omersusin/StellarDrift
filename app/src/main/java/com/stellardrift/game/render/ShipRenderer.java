package com.stellardrift.game.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import com.stellardrift.game.world.ShipData;
import com.stellardrift.game.world.ShipRegistry;

public class ShipRenderer {

    private final Paint hullPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint wingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cockpitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint detailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint enginePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float engineFlicker = 0f;

    public ShipRenderer() {
        hullPaint.setStyle(Paint.Style.FILL);
        wingPaint.setStyle(Paint.Style.FILL);
        cockpitPaint.setStyle(Paint.Style.FILL);
        accentPaint.setStyle(Paint.Style.FILL);
        enginePaint.setStyle(Paint.Style.FILL);
        glowPaint.setStyle(Paint.Style.FILL);

        detailPaint.setStyle(Paint.Style.STROKE);
        detailPaint.setStrokeWidth(1.2f);
        detailPaint.setStrokeCap(Paint.Cap.ROUND);

        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(1.5f);
    }

    public void drawShip(Canvas canvas, ShipData ship, float x, float y,
                         float bankAngle, int alpha, float scale, boolean isOverdrive) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(bankAngle);
        canvas.scale(scale, scale);

        drawEngines(canvas, ship, alpha, isOverdrive);

        wingPaint.setColor(applyAlpha(ship.wingColor, alpha));
        canvas.drawPath(ship.leftWingPath, wingPaint);
        canvas.drawPath(ship.rightWingPath, wingPaint);

        outlinePaint.setColor(applyAlpha(brighten(ship.wingColor, 0.3f), (int)(alpha * 0.4f)));
        canvas.drawPath(ship.leftWingPath, outlinePaint);
        canvas.drawPath(ship.rightWingPath, outlinePaint);

        hullPaint.setColor(applyAlpha(isOverdrive ? Color.rgb(74, 20, 140) : ship.hullColor, alpha));
        canvas.drawPath(ship.hullPath, hullPaint);

        int outCol = isOverdrive ? Color.rgb(255, 109, 0) : brighten(ship.hullColor, 0.25f);
        outlinePaint.setColor(applyAlpha(outCol, (int)(alpha * (isOverdrive ? 0.8f : 0.5f))));
        canvas.drawPath(ship.hullPath, outlinePaint);

        if (ship.accentPath != null) {
            accentPaint.setColor(applyAlpha(brighten(ship.hullColor, 0.15f), alpha));
            canvas.drawPath(ship.accentPath, accentPaint);
        }

        if (ship.detailPaths != null) {
            boolean isPhantom = (ship.id == ShipRegistry.PHANTOM);
            int detailColor = isPhantom
                ? applyAlpha(ship.cockpitGlowColor, (int)(alpha * 0.5f))
                : applyAlpha(brighten(ship.hullColor, 0.2f), (int)(alpha * 0.3f));
            detailPaint.setColor(detailColor);
            detailPaint.setStrokeWidth(isPhantom ? 1.5f : 0.8f);
            for (Path detail : ship.detailPaths) canvas.drawPath(detail, detailPaint);
        }

        cockpitPaint.setColor(applyAlpha(ship.cockpitColor, alpha));
        canvas.drawPath(ship.cockpitPath, cockpitPaint);

        cockpitPaint.setColor(applyAlpha(ship.cockpitGlowColor, (int)(alpha * 0.5f)));
        canvas.save(); canvas.scale(0.65f, 0.65f); canvas.drawPath(ship.cockpitPath, cockpitPaint); canvas.restore();

        canvas.restore();
    }

    private void drawEngines(Canvas canvas, ShipData ship, int alpha, boolean isOverdrive) {
        engineFlicker = 0.7f + (float)(Math.random() * 0.3f);
        int glowCol = isOverdrive ? Color.rgb(255, 109, 0) : ship.engineGlowColor;

        for (int i = 0; i < ship.engineX.length; i++) {
            float ex = ship.engineX[i], ey = ship.engineY[i];
            float er = ship.engineRadius[i] * engineFlicker * (isOverdrive ? 1.5f : 1f);

            glowPaint.setColor(applyAlpha(glowCol, (int)(alpha * 0.15f)));
            canvas.drawCircle(ex, ey, er * 3f, glowPaint);
            glowPaint.setColor(applyAlpha(glowCol, (int)(alpha * 0.35f)));
            canvas.drawCircle(ex, ey, er * 1.8f, glowPaint);

            int coreColor = brighten(glowCol, 0.6f);
            enginePaint.setColor(applyAlpha(coreColor, alpha));
            canvas.drawCircle(ex, ey, er, enginePaint);
        }
    }

    private static int brighten(int color, float amount) {
        int r = Math.min(255, Color.red(color) + (int)((255 - Color.red(color)) * amount));
        int g = Math.min(255, Color.green(color) + (int)((255 - Color.green(color)) * amount));
        int b = Math.min(255, Color.blue(color) + (int)((255 - Color.blue(color)) * amount));
        return Color.rgb(r, g, b);
    }
    private static int applyAlpha(int color, int alpha) { return Color.argb(Math.max(0, Math.min(255, alpha)), Color.red(color), Color.green(color), Color.blue(color)); }
}
