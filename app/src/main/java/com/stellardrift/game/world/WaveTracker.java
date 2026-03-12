package com.stellardrift.game.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class WaveTracker {

    private int currentWave = 1;
    private int phaseIndex = 0;  // 0=CALM, 1=PRESSURE, 2=REWARD
    private static final String[] PHASE_NAMES = {"CALM", "PRESSURE", "REWARD"};

    // Dalga geçiş animasyonu
    private float waveTransitionTimer = 0f;
    private float waveNumberScale = 1f;
    private boolean showingNewWave = false;

    // Paint'ler (izole)
    private final Paint waveLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint waveNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint phasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float sw, sh;

    public WaveTracker(float sw, float sh) {
        this.sw = sw;
        this.sh = sh;

        waveLabelPaint.setTextAlign(Paint.Align.CENTER);
        waveLabelPaint.setFakeBoldText(false);
        waveLabelPaint.setStyle(Paint.Style.FILL);
        waveLabelPaint.setTextSize(sw * 0.022f);

        waveNumberPaint.setTextAlign(Paint.Align.CENTER);
        waveNumberPaint.setFakeBoldText(true);
        waveNumberPaint.setStyle(Paint.Style.FILL);
        waveNumberPaint.setTextSize(sw * 0.045f);

        phasePaint.setTextAlign(Paint.Align.CENTER);
        phasePaint.setFakeBoldText(true);
        phasePaint.setStyle(Paint.Style.FILL);
        phasePaint.setTextSize(sw * 0.02f);
    }

    /**
     * Tempo sistemi faz değiştirdiğinde çağrılır.
     * Bir tam döngü (CALM→PRESSURE→REWARD) tamamlandığında wave artar.
     */
    public void onPhaseChanged(int newPhaseIndex) {
        // Geri sayım kontrolü: yeni faz öncekinden küçükse döngü tamamlandı
        if (newPhaseIndex == 0 && phaseIndex == 2) {
            // REWARD → CALM geçişi = yeni dalga
            currentWave++;
            showingNewWave = true;
            waveTransitionTimer = 2.0f; // 2 saniye göster
            waveNumberScale = 2.0f;      // büyük başla, küçülecek
        }
        phaseIndex = newPhaseIndex;
    }

    public void update(float dt) {
        if (waveTransitionTimer > 0) {
            waveTransitionTimer -= dt;
            // Elastic ease ile küçül
            float t = 1f - (waveTransitionTimer / 2.0f);
            t = Math.min(1f, t);
            if (t < 0.3f) {
                waveNumberScale = 2.0f - t / 0.3f * 1.5f; // 2.0 → 0.5
            } else if (t < 0.5f) {
                waveNumberScale = 0.5f + (t - 0.3f) / 0.2f * 0.7f; // 0.5 → 1.2
            } else {
                waveNumberScale = 1.2f - (t - 0.5f) / 0.5f * 0.2f; // 1.2 → 1.0
            }
            if (waveTransitionTimer <= 0) {
                showingNewWave = false;
                waveNumberScale = 1f;
            }
        }
    }

    public void draw(Canvas canvas) {
        float cx = sw * 0.5f;
        float baseY = sh * 0.025f;

        // Faz göstergesi (her zaman görünür, soluk)
        int phaseColor;
        switch (phaseIndex) {
            case 0: phaseColor = Color.argb(100, 80, 180, 255); break;   // CALM mavi
            case 1: phaseColor = Color.argb(120, 255, 100, 60); break;   // PRESSURE turuncu
            case 2: phaseColor = Color.argb(120, 100, 255, 130); break;  // REWARD yeşil
            default: phaseColor = Color.argb(80, 180, 180, 180); break;
        }
        phasePaint.setColor(phaseColor);
        canvas.drawText(PHASE_NAMES[phaseIndex], cx, baseY + sh * 0.01f, phasePaint);

        // Wave numarası
        if (showingNewWave) {
            // Yeni dalga animasyonu — büyük, parlak
            int flashAlpha = (int)(255 * Math.min(1f, waveTransitionTimer));
            waveNumberPaint.setColor(Color.argb(flashAlpha, 255, 255, 255));
            waveNumberPaint.setTextSize(sw * 0.045f * waveNumberScale);
            canvas.drawText("WAVE " + currentWave, cx, baseY + sh * 0.045f, waveNumberPaint);
            waveNumberPaint.setTextSize(sw * 0.045f); // reset
        } else {
            // Normal — küçük ve soluk
            waveLabelPaint.setColor(Color.argb(70, 200, 210, 225));
            canvas.drawText("WAVE " + currentWave, cx, baseY + sh * 0.04f, waveLabelPaint);
        }
    }

    // Game Over ekranında gösterilecek
    public int getCurrentWave() { return currentWave; }

    public void reset() {
        currentWave = 1;
        phaseIndex = 0;
        waveTransitionTimer = 0;
        showingNewWave = false;
        waveNumberScale = 1f;
    }
}
