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

    private SettingsManager settings;
    private SoundManager sound;
    private VibrationManager vibration;

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

        state = Constants.STATE_MENU;
        score = 0;
        difficulty = 1f;
        frameCount = 0;
    }

    public void update(float touchX, boolean touching) {
        if (state != Constants.STATE_PLAYING) return;

        frameCount++;
        float speedMult = settings.getSpeedMultiplier();
        difficulty = Math.min(Constants.MAX_DIFFICULTY,
            1f + frameCount * Constants.DIFFICULTY_RATE);

        player.update(touchX, touching);

        int spawnInt = settings.getSpawnInterval();
        if (frameCount % Math.max(5, (int)(spawnInt / difficulty)) == 0)
            asteroids.add(new Asteroid(screenW, screenH));
        if (frameCount % Math.max(10, (int)(Constants.STARDUST_SPAWN_INTERVAL / difficulty)) == 0)
            starDusts.add(new StarDust(screenW, screenH));

        float effDiff = difficulty * speedMult;

        for (Asteroid a : asteroids) a.update(effDiff);
        for (StarDust s : starDusts) s.update(effDiff);

        Iterator<Particle> pi = particles.iterator();
        while (pi.hasNext()) {
            Particle p = pi.next();
            p.update();
            if (!p.isAlive()) pi.remove();
        }

        checkCollisions();
        cleanOffscreen();
    }

    private void checkCollisions() {
        RectF pb = player.getBounds();

        Iterator<StarDust> si = starDusts.iterator();
        while (si.hasNext()) {
            StarDust s = si.next();
            if (RectF.intersects(pb, s.getBounds())) {
                score += Constants.STARDUST_SCORE;
                spawnParticles(s.getX(), s.getY(),
                    Constants.COLLECT_PARTICLES, 0xFFFFD740);
                sound.playCollect();
                vibration.vibrateCollect();
                si.remove();
            }
        }

        if (player.isShielded()) return;

        Iterator<Asteroid> ai = asteroids.iterator();
        while (ai.hasNext()) {
            Asteroid a = ai.next();
            if (RectF.intersects(pb, a.getBounds())) {
                spawnParticles(player.getX(), player.getY(),
                    Constants.EXPLOSION_PARTICLES, 0xFFFF1744);
                spawnParticles(a.getX(), a.getY(),
                    Constants.EXPLOSION_PARTICLES / 2, 0xFF78909C);
                sound.playExplosion();
                vibration.vibrateExplosion();
                gameOver();
                return;
            }
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
            int c = tweakColor(color);
            particles.add(new Particle(px, py, vx, vy, sz, c, life));
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
        score = 0;
        difficulty = 1f;
        frameCount = 0;
        asteroids.clear();
        starDusts.clear();
        particles.clear();
        player.reset();
    }

    // Settings helpers
    public void openSettings() { state = Constants.STATE_SETTINGS; }
    public void closeSettings() { state = Constants.STATE_MENU; }

    public void cycleDifficulty() {
        settings.cycleDifficulty();
        sound.playClick();
        vibration.vibrateClick();
    }

    public void toggleSound() {
        settings.toggleSound();
        sound.setEnabled(settings.isSoundEnabled());
        if (settings.isSoundEnabled()) sound.playClick();
    }

    public void toggleVibration() {
        settings.toggleVibration();
        vibration.setEnabled(settings.isVibrationEnabled());
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
    public SettingsManager getSettings() { return settings; }

    public void releaseResources() {
        if (sound != null) sound.release();
    }
}
