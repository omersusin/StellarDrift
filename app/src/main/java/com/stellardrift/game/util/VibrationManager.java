package com.stellardrift.game.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class VibrationManager {

    private Vibrator vibrator;
    private boolean enabled;

    @SuppressWarnings("deprecation")
    public VibrationManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager)
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vm != null ? vm.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator)
                context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        enabled = true;
    }

    public void setEnabled(boolean e) { enabled = e; }

    public void vibrateCollect() { vibrate(25); }

    public void vibrateExplosion() {
        if (!enabled || vibrator == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(
                    new long[]{0, 40, 30, 80, 20, 50}, -1));
            } else {
                vibrate(180);
            }
        } catch (Exception ignored) {}
    }

    public void vibrateClick() { vibrate(8); }

    private void vibrate(int ms) {
        if (!enabled || vibrator == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(
                    ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        } catch (Exception ignored) {}
    }
}
