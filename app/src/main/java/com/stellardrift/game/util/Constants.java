package com.stellardrift.game.util;

public final class Constants {
    public static final int STATE_MENU = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_GAME_OVER = 2;
    public static final int STATE_SETTINGS = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_SHOP = 5; // HATA 1 ÇÖZÜLDÜ

    public static final int DIFF_EASY = 0, DIFF_NORMAL = 1, DIFF_HARD = 2;
    public static final float[] DIFF_SPEED_MULT = {0.7f, 1.0f, 1.4f};
    public static final int[] DIFF_SPAWN_MULT = {38, 25, 15};
    public static final String[] DIFF_NAMES = {"EASY", "NORMAL", "HARD"};
    public static final int[] DIFF_COLORS = {0xFF00E676, 0xFFFFD740, 0xFFFF1744};

    public static final int SPEED_SLOW = 0, SPEED_NORMAL = 1, SPEED_FAST = 2;
    public static final float[] SPEED_MULT = {0.7f, 1.0f, 1.4f};
    public static final String[] SPEED_NAMES = {"SLOW", "NORMAL", "FAST"};
    public static final int[] SPEED_COLORS = {0xFF448AFF, 0xFFFFD740, 0xFFFF1744};

    public static final float PLAYER_SIZE_RATIO = 0.07f, PLAYER_MOVE_SPEED = 0.014f;
    public static final float PLAYER_MAX_BANK_ANGLE = 12f, PLAYER_BANK_SPEED = 0.15f;
    public static final float PLAYER_Y_MIN_RATIO = 0.12f, PLAYER_Y_MAX_RATIO = 0.92f, PLAYER_START_Y_RATIO = 0.80f;

    public static final float JOY_BASE_RATIO = 0.13f, JOY_KNOB_RATIO = 0.045f, JOY_DEAD_ZONE = 0.12f;

    public static final float ASTEROID_MIN_SPEED = 3f, ASTEROID_MAX_SPEED = 8f;
    public static final int ASTEROID_SPAWN_INTERVAL = 25;
    public static final float ASTEROID_MIN_SIZE = 0.035f, ASTEROID_MAX_SIZE = 0.08f;
    public static final int ASTEROID_FADEIN_FRAMES = 15;
    public static final float ASTEROID_SAFE_ZONE = 0.15f, ASTEROID_SINE_CHANCE = 0.3f, ASTEROID_SINE_AMP = 0.04f, ASTEROID_SINE_FREQ = 0.03f;

    public static final float STARDUST_SPEED = 4f;
    public static final int STARDUST_SPAWN_INTERVAL = 40, STARDUST_SCORE = 10;
    public static final float STARDUST_SIZE_RATIO = 0.018f, STARDUST_CHAIN_CHANCE = 0.18f;
    public static final int STARDUST_CHAIN_MIN = 3, STARDUST_CHAIN_MAX = 6, STARDUST_CHAIN_BONUS = 15;

    public static final int EXPLOSION_PARTICLES = 35, COLLECT_PARTICLES = 15, PARTICLE_LIFETIME = 45;
    public static final float PARTICLE_MAX_SPEED = 6f;

    public static final int BG_STARS_L1 = 60, BG_STARS_L2 = 35, BG_STARS_L3 = 15, BG_NEBULA_COUNT = 4;
    public static final float BG_SPEED_L1 = 0.3f, BG_SPEED_L2 = 0.8f, BG_SPEED_L3 = 1.5f;

    public static final float DIFFICULTY_RATE = 0.0008f, MAX_DIFFICULTY = 3.0f;
    public static final int TARGET_FPS = 60;
    public static final long FRAME_PERIOD = 1000L / TARGET_FPS;

    public static final String PREFS_NAME = "StellarDriftPrefs";
    public static final String KEY_HIGH_SCORE = "highScore", KEY_DIFFICULTY = "difficulty", KEY_VIBRATION = "vibration", KEY_SOUND = "sound", KEY_GAME_SPEED = "gameSpeed", KEY_GAMES_PLAYED = "gamesPlayed";

    public static final int COMBO_TIMEOUT = 120;
    public static final float COMBO_MULTIPLIER = 0.2f;

    public static final int NEAR_MISS_BONUS = 5, NEAR_MISS_COOLDOWN = 90, NEAR_MISS_FLASH_LIFE = 10;
    public static final float NEAR_MISS_RANGE = 1.25f;

    public static final int GRAZE_CHAIN_WINDOW = 180, RISK_WINDOW_DURATION = 120;
    public static final float NATURAL_MAGNET_RADIUS_RATIO = 0.1f, NATURAL_MAGNET_STRENGTH = 0.15f, POWERUP_MAGNET_STRENGTH = 0.40f, RISK_WINDOW_MULT = 1.5f;

    public static final int TEMPO_CALM = 0, TEMPO_PRESSURE = 1, TEMPO_REWARD = 2;
    public static final int TEMPO_CALM_DURATION = 400, TEMPO_PRESSURE_DURATION = 250, TEMPO_REWARD_DURATION = 200;

    public static final int OVERDRIVE_COMBO_THRESHOLD = 8, OVERDRIVE_DURATION = 180, FREEZE_FRAMES = 8;
    public static final float SHAKE_INTENSITY = 12f, SHAKE_DECAY = 0.88f;
    public static final int WARMUP_FRAMES = 180;

    public static final int POWERUP_MAGNET = 0, POWERUP_SLOWMO = 1, POWERUP_DOUBLE = 2, POWERUP_SHIELD = 3, POWERUP_DURATION = 300, POWERUP_SPAWN_INTERVAL = 600;
    public static final float MAGNET_RANGE = 0.35f, SLOWMO_FACTOR = 0.5f;

    public static final int POPUP_LIFETIME = 30, TUTORIAL_MAX_GAMES = 3;
    public static final float TRANSITION_SPEED_IN = 0.04f, TRANSITION_SPEED_OUT = 0.05f, HIT_STALL_DURATION = 0.033f, HIT_STALL_TIME_SCALE = 0.7f, SPAWN_BIAS_STRENGTH = 0.6f;

    public static final int[] COMBO_TRAIL_COLORS = {0xFF64B5F6, 0xFF66BB6A, 0xFFAB47BC, 0xFFFFD740, 0xFFFF6D00};

    private Constants() {}
}
