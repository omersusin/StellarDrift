package com.stellardrift.game.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class VibrationManager {

    private final Vibrator vibrator;
    private boolean enabled = true;

    private boolean heartbeatActive = false;
    private float heartbeatTimer = 0f;
    private static final float HEARTBEAT_INTERVAL = 1.2f;

    public VibrationManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vm.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    public void vibrateShoot() { vibrate(8, 40); }
    public void vibrateHit() { vibrate(15, 80); }
    
    // ═════════════════════════════════════════════════
    // EKSIK OLAN METOD BURADA EKLENDİ
    // ═════════════════════════════════════════════════
    public void vibrateExplosion() { vibratePattern(new long[]{0, 40, 30, 80, 20, 50}, new int[]{0, 200, 0, 255, 0, 180}); }
    
    public void vibrateCollect() { vibrate(10, 50); }
    public void vibratePowerUp() { vibrate(25, 120); }
    public void vibrateOvercharge() { vibratePattern(new long[]{0, 30, 20, 50, 20, 30}, new int[]{0, 200, 0, 255, 0, 150}); }
    public void vibrateNearMiss() { vibrate(12, 100); }
    public void vibrateCombo() { vibrate(8, 60); }
    public void vibrateDeath() { vibratePattern(new long[]{0, 100, 30, 80, 40, 200}, new int[]{0, 255, 0, 200, 0, 150}); }
    public void vibrateMenuClick() { vibrate(5, 30); }
    public void vibratePurchase() { vibratePattern(new long[]{0, 20, 40, 30}, new int[]{0, 150, 0, 200}); }
    public void vibrateUpgrade() { vibratePattern(new long[]{0, 15, 30, 15, 30, 25}, new int[]{0, 100, 0, 150, 0, 200}); }
    public void vibrateError() { vibratePattern(new long[]{0, 30, 20, 30}, new int[]{0, 120, 0, 80}); }

    public void updateHeartbeat(float dt, boolean fuelCritical) {
        if (!enabled) return;
        if (fuelCritical && !heartbeatActive) {
            heartbeatActive = true; heartbeatTimer = 0;
        } else if (!fuelCritical) {
            heartbeatActive = false; return;
        }
        if (heartbeatActive) {
            heartbeatTimer += dt;
            if (heartbeatTimer >= HEARTBEAT_INTERVAL) {
                heartbeatTimer = 0;
                vibratePattern(new long[]{0, 25, 80, 40}, new int[]{0, 100, 0, 180});
            }
        }
    }

    private void vibrate(int durationMs, int amplitude) {
        if (!enabled || vibrator == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, Math.min(255, Math.max(1, amplitude))));
            } else { vibrator.vibrate(durationMs); }
        } catch (Exception ignored) {}
    }

    private void vibratePattern(long[] timings, int[] amplitudes) {
        if (!enabled || vibrator == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
            } else { vibrator.vibrate(timings, -1); }
        } catch (Exception ignored) {}
    }

    public void setEnabled(boolean e) { enabled = e; }
    public boolean isEnabled() { return enabled; }
    public void cancel() { if (vibrator != null) { try { vibrator.cancel(); } catch (Exception ignored) {} } }
}
