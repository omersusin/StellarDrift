package com.stellardrift.game.util;

public final class Constants {

    public static final int STATE_MENU = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_GAME_OVER = 2;
    public static final int STATE_SETTINGS = 3;

    public static final int DIFF_EASY = 0;
    public static final int DIFF_NORMAL = 1;
    public static final int DIFF_HARD = 2;
    public static final float[] DIFF_SPEED_MULT = {0.7f, 1.0f, 1.4f};
    public static final int[] DIFF_SPAWN_MULT = {38, 25, 15};
    public static final String[] DIFF_NAMES = {"EASY", "NORMAL", "HARD"};
    public static final int[] DIFF_COLORS = {0xFF00E676, 0xFFFFD740, 0xFFFF1744};

    public static final float PLAYER_SIZE_RATIO = 0.07f;
    public static final float PLAYER_FOLLOW_SPEED = 0.25f;

    public static final float ASTEROID_MIN_SPEED = 3f;
    public static final float ASTEROID_MAX_SPEED = 8f;
    public static final int ASTEROID_SPAWN_INTERVAL = 25;
    public static final float ASTEROID_MIN_SIZE = 0.035f;
    public static final float ASTEROID_MAX_SIZE = 0.08f;

    public static final float STARDUST_SPEED = 4f;
    public static final int STARDUST_SPAWN_INTERVAL = 40;
    public static final float STARDUST_SIZE_RATIO = 0.018f;
    public static final int STARDUST_SCORE = 10;

    public static final int EXPLOSION_PARTICLES = 35;
    public static final int COLLECT_PARTICLES = 15;
    public static final float PARTICLE_MAX_SPEED = 6f;
    public static final int PARTICLE_LIFETIME = 45;

    public static final int BG_STARS_L1 = 60;
    public static final int BG_STARS_L2 = 35;
    public static final int BG_STARS_L3 = 15;
    public static final float BG_SPEED_L1 = 0.3f;
    public static final float BG_SPEED_L2 = 0.8f;
    public static final float BG_SPEED_L3 = 1.5f;
    public static final int BG_NEBULA_COUNT = 4;

    public static final float DIFFICULTY_RATE = 0.0008f;
    public static final float MAX_DIFFICULTY = 3.0f;

    public static final int TARGET_FPS = 60;
    public static final long FRAME_PERIOD = 1000L / TARGET_FPS;

    public static final String PREFS_NAME = "StellarDriftPrefs";
    public static final String KEY_HIGH_SCORE = "highScore";
    public static final String KEY_DIFFICULTY = "difficulty";
    public static final String KEY_VIBRATION = "vibration";
    public static final String KEY_SOUND = "sound";

    // Combo
    public static final int COMBO_TIMEOUT = 120;
    public static final float COMBO_MULTIPLIER = 0.2f;

    // Near-miss
    public static final int NEAR_MISS_BONUS = 25;
    public static final float NEAR_MISS_RANGE = 1.8f;

    // Warm-up
    public static final int WARMUP_FRAMES = 180;

    // Power-ups
    public static final int POWERUP_MAGNET = 0;
    public static final int POWERUP_SLOWMO = 1;
    public static final int POWERUP_DOUBLE = 2;
    public static final int POWERUP_SHIELD = 3;
    public static final int POWERUP_DURATION = 300;
    public static final int POWERUP_SPAWN_INTERVAL = 600;
    public static final float MAGNET_RANGE = 0.3f;
    public static final float SLOWMO_FACTOR = 0.5f;

    // Score popup
    public static final int POPUP_LIFETIME = 40;

    // Screen shake
    public static final float SHAKE_INTENSITY = 12f;
    public static final float SHAKE_DECAY = 0.88f;

    private Constants() {}
}
