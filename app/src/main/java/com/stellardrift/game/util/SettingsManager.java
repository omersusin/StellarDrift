package com.stellardrift.game.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private SharedPreferences prefs;
    private int difficulty;
    private boolean vibrationEnabled;
    private boolean soundEnabled;
    private int highScore;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(
            Constants.PREFS_NAME, Context.MODE_PRIVATE);
        load();
    }

    private void load() {
        difficulty = prefs.getInt(Constants.KEY_DIFFICULTY, Constants.DIFF_NORMAL);
        vibrationEnabled = prefs.getBoolean(Constants.KEY_VIBRATION, true);
        soundEnabled = prefs.getBoolean(Constants.KEY_SOUND, true);
        highScore = prefs.getInt(Constants.KEY_HIGH_SCORE, 0);
    }

    private void save() {
        prefs.edit()
            .putInt(Constants.KEY_DIFFICULTY, difficulty)
            .putBoolean(Constants.KEY_VIBRATION, vibrationEnabled)
            .putBoolean(Constants.KEY_SOUND, soundEnabled)
            .putInt(Constants.KEY_HIGH_SCORE, highScore)
            .apply();
    }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int d) {
        difficulty = Math.max(0, Math.min(2, d));
        save();
    }
    public void cycleDifficulty() {
        difficulty = (difficulty + 1) % 3;
        save();
    }

    public boolean isVibrationEnabled() { return vibrationEnabled; }
    public void toggleVibration() {
        vibrationEnabled = !vibrationEnabled;
        save();
    }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void toggleSound() {
        soundEnabled = !soundEnabled;
        save();
    }

    public int getHighScore() { return highScore; }
    public boolean setHighScore(int score) {
        if (score > highScore) {
            highScore = score;
            save();
            return true;
        }
        return false;
    }

    public float getSpeedMultiplier() {
        return Constants.DIFF_SPEED_MULT[difficulty];
    }

    public int getSpawnInterval() {
        return Constants.DIFF_SPAWN_MULT[difficulty];
    }

    public String getDifficultyName() {
        return Constants.DIFF_NAMES[difficulty];
    }

    public int getDifficultyColor() {
        return Constants.DIFF_COLORS[difficulty];
    }
}
