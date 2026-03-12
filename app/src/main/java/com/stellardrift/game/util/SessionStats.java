package com.stellardrift.game.util;

import android.graphics.Color;

public class SessionStats {

    // Oyun sırasında toplanan ham veriler
    public int score = 0;
    public int wave = 0;
    public float survivalTime = 0f;    // saniye
    public int starDustCollected = 0;
    public int asteroidsDestroyed = 0;
    public int nearMisses = 0;
    public int maxCombo = 0;
    public int grazeChains = 0;
    public int powerUpsCollected = 0;
    public int plasmaCoresCollected = 0;
    public int creditsEarned = 0;
    public int shotsFired = 0;
    public int shotsHit = 0;
    public float minFuelReached = 100f;  // en düşük yakıt seviyesi

    // ── Hesaplanmış istatistikler ──
    public float getAccuracy() {
        return shotsFired > 0 ? (float) shotsHit / shotsFired * 100f : 0f;
    }

    public String getSurvivalTimeFormatted() {
        int min = (int)(survivalTime / 60);
        int sec = (int)(survivalTime % 60);
        return min + ":" + (sec < 10 ? "0" : "") + sec;
    }

    // ── Performans notu (A-F) ──
    public String getGrade() {
        float gradeScore = 0;
        gradeScore += Math.min(wave * 8, 30);              // max 30 puan
        gradeScore += Math.min(maxCombo * 3, 20);           // max 20 puan
        gradeScore += Math.min(nearMisses * 2, 15);         // max 15 puan
        gradeScore += Math.min(getAccuracy() * 0.2f, 15);   // max 15 puan
        gradeScore += Math.min(asteroidsDestroyed, 20);      // max 20 puan

        if (gradeScore >= 85) return "S";
        if (gradeScore >= 70) return "A";
        if (gradeScore >= 55) return "B";
        if (gradeScore >= 40) return "C";
        if (gradeScore >= 25) return "D";
        return "F";
    }

    public int getGradeColor() {
        switch (getGrade()) {
            case "S": return Color.rgb(255, 215, 0);    // altın
            case "A": return Color.rgb(100, 255, 130);   // yeşil
            case "B": return Color.rgb(80, 200, 255);    // mavi
            case "C": return Color.rgb(255, 200, 60);    // sarı
            case "D": return Color.rgb(255, 130, 50);    // turuncu
            default:  return Color.rgb(255, 60, 50);     // kırmızı
        }
    }

    public void reset() {
        score = 0; wave = 0; survivalTime = 0;
        starDustCollected = 0; asteroidsDestroyed = 0;
        nearMisses = 0; maxCombo = 0; grazeChains = 0;
        powerUpsCollected = 0; plasmaCoresCollected = 0;
        creditsEarned = 0; shotsFired = 0; shotsHit = 0;
        minFuelReached = 100f;
    }
}
