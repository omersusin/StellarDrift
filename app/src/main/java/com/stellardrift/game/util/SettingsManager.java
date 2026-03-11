package com.stellardrift.game.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private SharedPreferences prefs;
    private int difficulty, gameSpeed, highScore, gamesPlayed;
    private boolean vibrationEnabled, soundEnabled;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        load();
    }

    private void load() {
        difficulty = prefs.getInt(Constants.KEY_DIFFICULTY, Constants.DIFF_NORMAL);
        gameSpeed = prefs.getInt(Constants.KEY_GAME_SPEED, Constants.SPEED_NORMAL);
        vibrationEnabled = prefs.getBoolean(Constants.KEY_VIBRATION, true);
        soundEnabled = prefs.getBoolean(Constants.KEY_SOUND, true);
        highScore = prefs.getInt(Constants.KEY_HIGH_SCORE, 0);
        gamesPlayed = prefs.getInt(Constants.KEY_GAMES_PLAYED, 0);
    }

    private void save() {
        prefs.edit()
            .putInt(Constants.KEY_DIFFICULTY, difficulty)
            .putInt(Constants.KEY_GAME_SPEED, gameSpeed)
            .putBoolean(Constants.KEY_VIBRATION, vibrationEnabled)
            .putBoolean(Constants.KEY_SOUND, soundEnabled)
            .putInt(Constants.KEY_HIGH_SCORE, highScore)
            .putInt(Constants.KEY_GAMES_PLAYED, gamesPlayed)
            .apply();
    }

    public int getDifficulty() { return difficulty; }
    public void cycleDifficulty() { difficulty = (difficulty + 1) % 3; save(); }
    public float getSpeedMultiplier() { return Constants.DIFF_SPEED_MULT[difficulty]; }
    public int getSpawnInterval() { return Constants.DIFF_SPAWN_MULT[difficulty]; }
    public String getDifficultyName() { return Constants.DIFF_NAMES[difficulty]; }
    public int getDifficultyColor() { return Constants.DIFF_COLORS[difficulty]; }

    public int getGameSpeed() { return gameSpeed; }
    public void cycleGameSpeed() { gameSpeed = (gameSpeed + 1) % 3; save(); }
    public float getGameSpeedMultiplier() { return Constants.SPEED_MULT[gameSpeed]; }
    public String getGameSpeedName() { return Constants.SPEED_NAMES[gameSpeed]; }
    public int getGameSpeedColor() { return Constants.SPEED_COLORS[gameSpeed]; }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void toggleSound() { soundEnabled = !soundEnabled; save(); }
    public boolean isVibrationEnabled() { return vibrationEnabled; }
    public void toggleVibration() { vibrationEnabled = !vibrationEnabled; save(); }

    public int getHighScore() { return highScore; }
    public boolean setHighScore(int score) {
        if (score > highScore) { highScore = score; save(); return true; }
        return false;
    }

    public int getGamesPlayed() { return gamesPlayed; }
    public void incrementGamesPlayed() { gamesPlayed++; save(); }
    public boolean shouldShowTutorial() { return gamesPlayed < Constants.TUTORIAL_MAX_GAMES; }
}
