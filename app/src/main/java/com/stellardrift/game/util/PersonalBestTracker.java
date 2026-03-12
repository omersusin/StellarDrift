package com.stellardrift.game.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class PersonalBestTracker {

    private final SharedPreferences prefs;

    // Kaydedilen en iyi değerler
    private int bestScore;
    private int bestWave;
    private float bestSurvivalTime;
    private int bestCombo;
    private int bestAsteroidsDestroyed;
    private float bestAccuracy;

    // Bu oturumda kırılan rekorlar
    private final boolean[] newRecords = new boolean[6];
    private static final String[] RECORD_NAMES = {
        "Skor", "Dalga", "Süre", "Combo", "Yok Edilen", "İsabet"
    };

    public PersonalBestTracker(Context context) {
        prefs = context.getSharedPreferences("stellar_drift_records", Context.MODE_PRIVATE);
        loadAll();
    }

    private void loadAll() {
        bestScore = prefs.getInt("best_score", 0);
        bestWave = prefs.getInt("best_wave", 0);
        bestSurvivalTime = prefs.getFloat("best_time", 0f);
        bestCombo = prefs.getInt("best_combo", 0);
        bestAsteroidsDestroyed = prefs.getInt("best_destroyed", 0);
        bestAccuracy = prefs.getFloat("best_accuracy", 0f);
    }

    /**
     * Oturum sonunda çağrılır.
     * Yeni rekorları tespit eder ve kaydeder.
     * @return Kırılan rekor sayısı
     */
    public int evaluateSession(SessionStats stats) {
        int recordCount = 0;
        java.util.Arrays.fill(newRecords, false);

        if (stats.score > bestScore) {
            bestScore = stats.score; newRecords[0] = true; recordCount++;
        }
        if (stats.wave > bestWave) {
            bestWave = stats.wave; newRecords[1] = true; recordCount++;
        }
        if (stats.survivalTime > bestSurvivalTime) {
            bestSurvivalTime = stats.survivalTime; newRecords[2] = true; recordCount++;
        }
        if (stats.maxCombo > bestCombo) {
            bestCombo = stats.maxCombo; newRecords[3] = true; recordCount++;
        }
        if (stats.asteroidsDestroyed > bestAsteroidsDestroyed) {
            bestAsteroidsDestroyed = stats.asteroidsDestroyed; newRecords[4] = true; recordCount++;
        }
        if (stats.getAccuracy() > bestAccuracy && stats.shotsFired > 20) {
            bestAccuracy = stats.getAccuracy(); newRecords[5] = true; recordCount++;
        }

        if (recordCount > 0) saveAll();
        return recordCount;
    }

    private void saveAll() {
        prefs.edit()
            .putInt("best_score", bestScore)
            .putInt("best_wave", bestWave)
            .putFloat("best_time", bestSurvivalTime)
            .putInt("best_combo", bestCombo)
            .putInt("best_destroyed", bestAsteroidsDestroyed)
            .putFloat("best_accuracy", bestAccuracy)
            .apply();
    }

    public boolean isNewRecord(int index) { return newRecords[index]; }
    public int getBestScore() { return bestScore; }
    public int getBestWave() { return bestWave; }

    // Game Over ekranında rekor kırılan satırların yanında "★ NEW!" göstermek için
    public void drawNewRecordBadges(Canvas canvas, SessionStats stats,
                                     float panelX, float panelW, float startY,
                                     float rowH, float alpha, float sw, float sh) {
        Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgePaint.setTextSize(panelW * 0.04f);
        badgePaint.setFakeBoldText(true);
        badgePaint.setTextAlign(Paint.Align.LEFT);
        badgePaint.setStyle(Paint.Style.FILL);

        float pulse = (float)(0.6 + 0.4 * Math.sin(System.currentTimeMillis() * 0.008));
        badgePaint.setColor(Color.argb((int)(pulse * 255 * alpha), 255, 215, 0));

        // Satır indekslerini Game Over stat listesiyle eşleştir
        // UIOverlay'deki sıra: 
        // 0: Süre (PB 2)
        // 1: Dalga (PB 1)
        // 2: Skor (PB 0)
        // 3: Yıldız Tozu
        // 4: Asteroid (PB 4)
        // 5: Max Combo (PB 3)
        // 6: Sıyırmalar
        // 7: İsabet Oranı (PB 5)
        int[] statToRowMap = {2, 1, 0, 5, 4, 7}; // PB index -> Row index

        float rowStartY = startY + sh * 0.04f + rowH;

        for (int i = 0; i < newRecords.length; i++) {
            if (!newRecords[i]) continue;
            int rowIdx = statToRowMap[i];
            if (rowIdx < 0) continue;

            float y = rowStartY + rowIdx * rowH;
            canvas.drawText("★ NEW!", panelX + panelW + sw * 0.02f, y, badgePaint);
        }
    }
}
