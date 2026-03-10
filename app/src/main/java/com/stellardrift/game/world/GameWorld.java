package com.stellardrift.game.world;

import android.content.Context;
import android.graphics.RectF;
import com.stellardrift.game.util.Constants;
import com.stellardrift.game.util.SettingsManager;
import com.stellardrift.game.util.SoundManager;
import com.stellardrift.game.util.VibrationManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameWorld {

    private int screenW, screenH;
    private int state;
    private int score;
    private float difficulty;
    private int frameCount;

    private Player player;
    private List<Asteroid> asteroids;
    private List<StarDust> starDusts;
    private List<Particle> particles;
    private List<PowerUp> powerUps;
    private List<ScorePopup> popups;

    private SettingsManager settings;
    private SoundManager sound;
    private VibrationManager vibration;

    // Combo
    private int combo;
    private int comboTimer;

    // Near-miss
    private boolean nearMissFrame;

    // Power-up states
    private boolean magnetActive, slowmoActive, doubleActive;
    private int magnetTimer, slowmoTimer, doubleTimer;
    private int powerUpSpawnTimer;

    // Screen shake
    private float shakeX, shakeY, shakeIntensity;

    // Stats for death screen
    private int orbsCollected, nearMissCount, maxCombo;
    private long startTime;

    public GameWorld(int sw, int sh, Context ctx) {
        screenW = sw;
        screenH = sh;
        settings = new SettingsManager(ctx);
        sound = new SoundManager();
        vibration = new VibrationManager(ctx);
        sound.setEnabled(settings.isSoundEnabled());
        vibration.setEnabled(settings.isVibrationEnabled());

        player = new Player(sw, sh);
        asteroids = new ArrayList<>();
        starDusts = new ArrayList<>();
        particles = new ArrayList<>();
        powerUps = new ArrayList<>();
        popups = new ArrayList<>();

        state = Constants.STATE_MENU;
        score = 0;
        difficulty = 1f;
        frameCount = 0;
    }

    public void update(float touchX, boolean touching) {
        // Update popups always (even game over)
        Iterator<ScorePopup> pi2 = popups.iterator();
        while (pi2.hasNext()) {
            ScorePopup sp = pi2.next();
            sp.update();
            if (!sp.isAlive()) pi2.remove();
        }

        // Update shake always
        if (shakeIntensity > 0.3f) {
            shakeX = (float)(Math.random() * shakeIntensity * 2 - shakeIntensity);
            shakeY = (float)(Math.random() * shakeIntensity * 2 - shakeIntensity);
            shakeIntensity *= Constants.SHAKE_DECAY;
        } else {
            shakeX = 0; shakeY = 0; shakeIntensity = 0;
        }

        if (state != Constants.STATE_PLAYING) return;

        frameCount++;
        nearMissFrame = false;
        float speedMult = settings.getSpeedMultiplier();
        difficulty = Math.min(Constants.MAX_DIFFICULTY,
            1f + frameCount * Constants.DIFFICULTY_RATE);

        player.update(touchX, touching);

        // Combo timer
        if (combo > 0) {
            comboTimer--;
            if (comboTimer <= 0) { combo = 0; }
        }

        // Power-up timers
        if (magnetActive) { magnetTimer--; if (magnetTimer <= 0) magnetActive = false; }
        if (slowmoActive) { slowmoTimer--; if (slowmoTimer <= 0) slowmoActive = false; }
        if (doubleActive) { doubleTimer--; if (doubleTimer <= 0) doubleActive = false; }

        // Spawning (skip warmup for asteroids)
        boolean warmup = frameCount < Constants.WARMUP_FRAMES;
        int spawnInt = settings.getSpawnInterval();

        if (!warmup && frameCount % Math.max(5, (int)(spawnInt / difficulty)) == 0)
            asteroids.add(new Asteroid(screenW, screenH));

        if (frameCount % Math.max(10, (int)(Constants.STARDUST_SPAWN_INTERVAL / difficulty)) == 0)
            starDusts.add(new StarDust(screenW, screenH));

        // Power-up spawn
        powerUpSpawnTimer++;
        if (powerUpSpawnTimer >= Constants.POWERUP_SPAWN_INTERVAL) {
            powerUpSpawnTimer = 0;
            int type = (int)(Math.random() * 4);
            powerUps.add(new PowerUp(screenW, screenH, type));
        }

        // Effective speed
        float effDiff = difficulty * speedMult;
        if (slowmoActive) effDiff *= Constants.SLOWMO_FACTOR;

        for (Asteroid a : asteroids) a.update(effDiff);
        for (StarDust s : starDusts) s.update(effDiff);
        for (PowerUp p : powerUps) p.update(effDiff);

        Iterator<Particle> pi = particles.iterator();
        while (pi.hasNext()) {
            Particle p = pi.next();
            p.update();
            if (!p.isAlive()) pi.remove();
        }

        // Magnet pull
        if (magnetActive) {
            float range = screenW * Constants.MAGNET_RANGE;
            float px = player.getX(), py = player.getY();
            for (StarDust s : starDusts) {
                float dx = px - s.getX();
                float dy = py - s.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < range && dist > 1) {
                    s.pull(dx / dist * 6f, dy / dist * 6f);
                }
            }
        }

        checkCollisions();
        cleanOffscreen();
    }

    private void checkCollisions() {
        RectF pb = player.getBounds();
        float px = player.getX(), py = player.getY();

        // StarDust collection
        Iterator<StarDust> si = starDusts.iterator();
        while (si.hasNext()) {
            StarDust s = si.next();
            if (RectF.intersects(pb, s.getBounds())) {
                combo++;
                comboTimer = Constants.COMBO_TIMEOUT;
                if (combo > maxCombo) maxCombo = combo;

                int points = Constants.STARDUST_SCORE;
                int comboMult = 1 + (int)(combo * Constants.COMBO_MULTIPLIER);
                if (doubleActive) comboMult *= 2;
                int finalPoints = points * comboMult;
                score += finalPoints;
                orbsCollected++;

                popups.add(ScorePopup.createCollect(
                    s.getX(), s.getY(), finalPoints, comboMult));
                spawnParticles(s.getX(), s.getY(),
                    Constants.COLLECT_PARTICLES, 0xFFFFD740);
                sound.playCollect();
                vibration.vibrateCollect();
                si.remove();
            }
        }

        // Power-up collection
        Iterator<PowerUp> pui = powerUps.iterator();
        while (pui.hasNext()) {
            PowerUp pu = pui.next();
            if (RectF.intersects(pb, pu.getBounds())) {
                activatePowerUp(pu.getType());
                popups.add(ScorePopup.createPowerUp(
                    pu.getX(), pu.getY(), pu.getType()));
                spawnParticles(pu.getX(), pu.getY(),
                    Constants.COLLECT_PARTICLES, PowerUp.getColor(pu.getType()));
                sound.playCollect();
                vibration.vibrateCollect();
                pui.remove();
            }
        }

        if (player.isShielded()) return;

        // Asteroid collision + near-miss
        Iterator<Asteroid> ai = asteroids.iterator();
        while (ai.hasNext()) {
            Asteroid a = ai.next();
            if (RectF.intersects(pb, a.getBounds())) {
                // Death
                spawnParticles(px, py,
                    Constants.EXPLOSION_PARTICLES, 0xFFFF1744);
                spawnParticles(a.getX(), a.getY(),
                    Constants.EXPLOSION_PARTICLES / 2, 0xFF78909C);
                sound.playExplosion();
                vibration.vibrateExplosion();
                shakeIntensity = Constants.SHAKE_INTENSITY;
                gameOver();
                return;
            }

            // Near-miss check
            if (!nearMissFrame) {
                float dx = px - a.getX();
                float dy = py - a.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float threshold = (player.getSize() + a.getSize()) * Constants.NEAR_MISS_RANGE;
                float hitDist = (player.getSize() + a.getSize()) * 0.65f;
                if (dist < threshold && dist > hitDist) {
                    nearMissFrame = true;
                    nearMissCount++;
                    score += Constants.NEAR_MISS_BONUS;
                    popups.add(ScorePopup.createNearMiss(px, py));
                    spawnParticles(px, py, 8, 0xFF00E5FF);
                }
            }
        }
    }

    private void activatePowerUp(int type) {
        switch (type) {
            case Constants.POWERUP_MAGNET:
                magnetActive = true;
                magnetTimer = Constants.POWERUP_DURATION;
                break;
            case Constants.POWERUP_SLOWMO:
                slowmoActive = true;
                slowmoTimer = Constants.POWERUP_DURATION;
                break;
            case Constants.POWERUP_DOUBLE:
                doubleActive = true;
                doubleTimer = Constants.POWERUP_DURATION;
                break;
            case Constants.POWERUP_SHIELD:
                player.activateShield(Constants.POWERUP_DURATION);
                break;
        }
    }

    private void spawnParticles(float px, float py, int count, int color) {
        for (int i = 0; i < count; i++) {
            float angle = (float)(Math.random() * Math.PI * 2);
            float spd = (float)(Math.random() * Constants.PARTICLE_MAX_SPEED);
            float vx = (float)(Math.cos(angle) * spd);
            float vy = (float)(Math.sin(angle) * spd);
            float sz = 2f + (float)(Math.random() * 5);
            int life = Constants.PARTICLE_LIFETIME / 2 +
                (int)(Math.random() * Constants.PARTICLE_LIFETIME / 2);
            particles.add(new Particle(px, py, vx, vy, sz, tweakColor(color), life));
        }
    }

    private int tweakColor(int base) {
        int r = Math.min(255, Math.max(0, ((base >> 16) & 0xFF) +
            (int)(Math.random() * 40 - 20)));
        int g = Math.min(255, Math.max(0, ((base >> 8) & 0xFF) +
            (int)(Math.random() * 40 - 20)));
        int b = Math.min(255, Math.max(0, (base & 0xFF) +
            (int)(Math.random() * 40 - 20)));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void cleanOffscreen() {
        Iterator<Asteroid> ai = asteroids.iterator();
        while (ai.hasNext()) if (ai.next().isOffScreen(screenH)) ai.remove();
        Iterator<StarDust> si = starDusts.iterator();
        while (si.hasNext()) if (si.next().isOffScreen(screenH)) si.remove();
        Iterator<PowerUp> pi = powerUps.iterator();
        while (pi.hasNext()) if (pi.next().isOffScreen(screenH)) pi.remove();
    }

    private void gameOver() {
        state = Constants.STATE_GAME_OVER;
        settings.setHighScore(score);
        sound.playGameOver();
    }

    public void handleTap() {
        switch (state) {
            case Constants.STATE_MENU:
                startGame();
                sound.playClick();
                vibration.vibrateClick();
                break;
            case Constants.STATE_GAME_OVER:
                state = Constants.STATE_MENU;
                sound.playClick();
                vibration.vibrateClick();
                break;
        }
    }

    public void startGame() {
        state = Constants.STATE_PLAYING;
        score = 0; difficulty = 1f; frameCount = 0;
        combo = 0; comboTimer = 0;
        magnetActive = false; slowmoActive = false; doubleActive = false;
        magnetTimer = 0; slowmoTimer = 0; doubleTimer = 0;
        powerUpSpawnTimer = 0;
        shakeIntensity = 0; shakeX = 0; shakeY = 0;
        orbsCollected = 0; nearMissCount = 0; maxCombo = 0;
        startTime = System.currentTimeMillis();
        asteroids.clear(); starDusts.clear();
        particles.clear(); powerUps.clear(); popups.clear();
        player.reset();
    }

    public void openSettings() { state = Constants.STATE_SETTINGS; }
    public void closeSettings() { state = Constants.STATE_MENU; }
    public void cycleDifficulty() {
        settings.cycleDifficulty(); sound.playClick(); vibration.vibrateClick();
    }
    public void toggleSound() {
        settings.toggleSound(); sound.setEnabled(settings.isSoundEnabled());
        if (settings.isSoundEnabled()) sound.playClick();
    }
    public void toggleVibration() {
        settings.toggleVibration(); vibration.setEnabled(settings.isVibrationEnabled());
        vibration.vibrateClick();
    }

    // Getters
    public int getState() { return state; }
    public int getScore() { return score; }
    public int getHighScore() { return settings.getHighScore(); }
    public float getDifficulty() { return difficulty; }
    public Player getPlayer() { return player; }
    public List<Asteroid> getAsteroids() { return asteroids; }
    public List<StarDust> getStarDusts() { return starDusts; }
    public List<Particle> getParticles() { return particles; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public List<ScorePopup> getPopups() { return popups; }
    public SettingsManager getSettings() { return settings; }

    public int getCombo() { return combo; }
    public int getComboTimer() { return comboTimer; }
    public boolean isMagnetActive() { return magnetActive; }
    public boolean isSlowmoActive() { return slowmoActive; }
    public boolean isDoubleActive() { return doubleActive; }
    public int getMagnetTimer() { return magnetTimer; }
    public int getSlowmoTimer() { return slowmoTimer; }
    public int getDoubleTimer() { return doubleTimer; }
    public float getShakeX() { return shakeX; }
    public float getShakeY() { return shakeY; }

    public int getOrbsCollected() { return orbsCollected; }
    public int getNearMissCount() { return nearMissCount; }
    public int getMaxCombo() { return maxCombo; }
    public long getSurvivalTime() {
        if (startTime == 0) return 0;
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    public void releaseResources() {
        if (sound != null) sound.release();
    }
}
