package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class SpawnIndicator {

    private static final int MAX_INDICATORS = 12;

    // Ring buffer
    private final float[] indX = new float[MAX_INDICATORS];
    private final float[] indLife = new float[MAX_INDICATORS];  // 1→0
    private final float[] indSize = new float[MAX_INDICATORS];  // asteroid büyüklüğüne göre
    private final boolean[] indActive = new boolean[MAX_INDICATORS];
    private int cursor = 0;

    private final Paint triPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path triPath = new Path();
    private float sw;

    public SpawnIndicator(float sw) {
        this.sw = sw;
        triPaint.setStyle(Paint.Style.FILL);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * Asteroid spawn olmadan 0.8 saniye ÖNCE çağrılır.
     * Asteroitin X pozisyonunu ve büyüklüğünü alır.
     */
    public void showIncoming(float x, float asteroidRadius) {
        indX[cursor] = x;
        indLife[cursor] = 1f;
        indSize[cursor] = asteroidRadius;
        indActive[cursor] = true;
        cursor = (cursor + 1) % MAX_INDICATORS;
    }

    public void update(float dt) {
        for (int i = 0; i < MAX_INDICATORS; i++) {
            if (!indActive[i]) continue;
            indLife[i] -= dt * 1.25f; // 0.8 saniyede söner
            if (indLife[i] <= 0) indActive[i] = false;
        }
    }

    public void draw(Canvas canvas) {
        for (int i = 0; i < MAX_INDICATORS; i++) {
            if (!indActive[i]) continue;

            float x = indX[i];
            float life = indLife[i];
            float size = indSize[i];

            // Opaklık: başta soluk, ortada parlak, sonda soluk
            float alpha;
            if (life > 0.7f) {
                alpha = (1f - life) / 0.3f; // fade in
            } else {
                alpha = life / 0.7f;
            }

            // Küçük aşağı bakan üçgen (▼)
            float triH = sw * 0.015f + size * 0.08f;
            float triW = triH * 0.8f;
            float triY = sw * 0.01f; // ekranın en üstüne yakın

            triPath.reset();
            triPath.moveTo(x, triY + triH);            // alt uç
            triPath.lineTo(x - triW * 0.5f, triY);     // sol üst
            triPath.lineTo(x + triW * 0.5f, triY);     // sağ üst
            triPath.close();

            // Büyük asteroidler kırmızımsı, küçükler sarımsı
            float dangerRatio = Math.min(size / 40f, 1f);
            int r = (int)(255 * (0.6f + 0.4f * dangerRatio));
            int g = (int)(200 * (1f - dangerRatio * 0.6f));
            int b = (int)(60 * (1f - dangerRatio));

            triPaint.setColor(Color.argb((int)(alpha * 180), r, g, b));
            canvas.drawPath(triPath, triPaint);

            // Altında ince dikey kesikli çizgi
            linePaint.setColor(Color.argb((int)(alpha * 40), r, g, b));
            linePaint.setStrokeWidth(sw * 0.002f);
            float lineBottom = triY + triH + sw * 0.04f * (1f - life); // uzar
            float dashLen = sw * 0.008f;
            float dashGap = sw * 0.005f;
            for (float ly = triY + triH; ly < lineBottom; ly += dashLen + dashGap) {
                float segEnd = Math.min(ly + dashLen, lineBottom);
                canvas.drawLine(x, ly, x, segEnd, linePaint);
            }
        }
    }

    public void reset() {
        for (int i = 0; i < MAX_INDICATORS; i++) indActive[i] = false;
        cursor = 0;
    }
}
