package com.stellardrift.game.world;

import android.content.Context;
import android.graphics.Color;
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
    private int state, score, frameCount;
    private float difficulty;

    private Player player;
    private List<Asteroid> asteroids;
    private List<StarDust> starDusts;
    private List<Particle> particles;
    private List<PowerUp> powerUps;
    private List<ScorePopup> popups;

    private SettingsManager settings;
    private SoundManager sound;
    private VibrationManager vibration;

    private int combo, comboTimer, nearMissCooldown;
    private boolean magnetActive, slowmoActive, doubleActive;
    private int magnetTimer, slowmoTimer, doubleTimer;
    private int powerUpSpawnTimer;
    private float shakeX, shakeY, shakeIntensity;
    private int orbsCollected, nearMissCount, maxCombo;
    private long startTime;

    private int tempoPhase, tempoTimer;
    private boolean riskWindowActive;
    private int riskWindowTimer;
    private boolean overdriveTriggered;
    private int freezeTimer;
    private Asteroid killerAsteroid;
    private boolean shockwaveActive;
    private float shockwaveX, shockwaveY, shockwaveRadius, shockwaveAlpha;
    private int lastMilestone;
    private String milestoneText;
    private int milestoneTimer;
    private float dangerLevel;
    private List<float[]> spawnWarnings;
    private List<float[]> nearMissFlashes;

    // Graze Chain
    private int grazeChainCount;
    private int grazeChainTimer;

    // Ring Bursts
    private List<float[]> ringBursts; // x, y, dirX, dirY, speed, life, color

    private int chainCounter;
    private int chainTarget;
    private boolean chainActive;
    private boolean firstStarDustSeen, firstNearMiss;
    private float transitionAlpha;
    private boolean transitioningIn;

    private static final int[] MILESTONES = {1000, 2500, 5000, 10000, 25000, 50000};

    public GameWorld(int sw, int sh, Context ctx) {
        screenW = sw; screenH = sh;
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
        spawnWarnings = new ArrayList<>();
        nearMissFlashes = new ArrayList<>();
        ringBursts = new ArrayList<>();

        state = Constants.STATE_MENU;
        tempoPhase = Constants.TEMPO_CALM;
        tempoTimer = Constants.TEMPO_CALM_DURATION;
        transitionAlpha = 0;
    }

    public void update(float joyDirX, float joyDirY, float joyMag) {
        updatePopups();
        updateShake();
        updateShockwave();
        updateNearMissFlashes();
        updateRingBursts();
        updateTransition();

        if (state == Constants.STATE_PAUSED) return;
        if (state != Constants.STATE_PLAYING) return;
        if (freezeTimer > 0) { freezeTimer--; if (freezeTimer <= 0) triggerGameOver(); return; }

        frameCount++;
        float speedMult = settings.getSpeedMultiplier();
        float gameSpeedMult = settings.getGameSpeedMultiplier();
        difficulty = Math.min(Constants.MAX_DIFFICULTY, 1f + frameCount * Constants.DIFFICULTY_RATE);

        player.update(joyDirX, joyDirY, joyMag);
        
        // Combo bilgilerini yolla
        float comboProg = comboTimer > 0 ? (float)comboTimer / Constants.COMBO_TIMEOUT : 0f;
        player.setComboInfo(combo, comboProg);
        
        updateTimers();
        updateTempo();
        updateDangerLevel();
        handleSpawning();
        updateSpawnWarnings();

        float effDiff = getEffectiveDifficulty(speedMult, gameSpeedMult);
        moveEntities(effDiff);
        
        // StarDust Magnet (Hem Power-Up hem Doğal)
        applyStarDustMagnet();
        
        checkCollisions();
        cleanOffscreen();
        checkMilestone();
        checkOverdrive();
    }

    private void updateTimers() {
        if (nearMissCooldown > 0) nearMissCooldown--;
        if (grazeChainTimer > 0) { grazeChainTimer--; if (grazeChainTimer <= 0) grazeChainCount = 0; }
        if (combo > 0) { comboTimer--; if (comboTimer <= 0) { combo = 0; overdriveTriggered = false; chainActive = false; } }
        if (magnetActive) { magnetTimer--; if (magnetTimer <= 0) magnetActive = false; }
        if (slowmoActive) { slowmoTimer--; if (slowmoTimer <= 0) slowmoActive = false; }
        if (doubleActive) { doubleTimer--; if (doubleTimer <= 0) doubleActive = false; }
        if (riskWindowActive) { riskWindowTimer--; if (riskWindowTimer <= 0) riskWindowActive = false; }
        if (milestoneTimer > 0) milestoneTimer--;
    }

    private void updatePopups() {
        Iterator<ScorePopup> it = popups.iterator();
        while (it.hasNext()) { ScorePopup sp = it.next(); sp.update(); if (!sp.isAlive()) it.remove(); }
    }

    private void updateShake() {
        if (shakeIntensity > 0.3f) {
            shakeX = (float)(Math.random() * shakeIntensity * 2 - shakeIntensity);
            shakeY = (float)(Math.random() * shakeIntensity * 2 - shakeIntensity);
            shakeIntensity *= Constants.SHAKE_DECAY;
        } else { shakeX = 0; shakeY = 0; shakeIntensity = 0; }
    }

    private void updateNearMissFlashes() {
        Iterator<float[]> it = nearMissFlashes.iterator();
        while (it.hasNext()) { float[] f = it.next(); f[4]--; if (f[4] <= 0) it.remove(); }
    }
    
    private void updateRingBursts() {
        Iterator<float[]> it = ringBursts.iterator();
        while (it.hasNext()) { 
            float[] b = it.next(); 
            b[0] += b[2] * b[4] * 0.016f; // x += dirX * speed * dt
            b[1] += b[3] * b[4] * 0.016f; // y += dirY * speed * dt
            b[4] *= 0.92f; // Yavaşla
            b[5] -= 0.016f * 5f; // Life azalır (~200ms)
            if (b[5] <= 0) it.remove(); 
        }
    }

    private void updateTransition() {
        if (transitioningIn) {
            transitionAlpha -= Constants.TRANSITION_SPEED_IN;
            if (transitionAlpha <= 0) { transitionAlpha = 0; transitioningIn = false; }
        }
    }

    private void updateTempo() {
        if (frameCount < Constants.WARMUP_FRAMES) return;
        tempoTimer--;
        if (tempoTimer <= 0) {
            switch (tempoPhase) {
                case Constants.TEMPO_CALM: tempoPhase = Constants.TEMPO_PRESSURE; tempoTimer = Constants.TEMPO_PRESSURE_DURATION; break;
                case Constants.TEMPO_PRESSURE: tempoPhase = Constants.TEMPO_REWARD; tempoTimer = Constants.TEMPO_REWARD_DURATION; break;
                case Constants.TEMPO_REWARD: tempoPhase = Constants.TEMPO_CALM; tempoTimer = Constants.TEMPO_CALM_DURATION; break;
            }
        }
    }

    private float getTempoSpawnMult() { return tempoPhase == Constants.TEMPO_PRESSURE ? 1.8f : tempoPhase == Constants.TEMPO_REWARD ? 0.3f : 1f; }
    private float getTempoStarDustMult() { return tempoPhase == Constants.TEMPO_REWARD ? 3f : 1f; }
    private float getTempoSpeedMult() { return tempoPhase == Constants.TEMPO_PRESSURE ? 1.15f : 1f; }

    private void handleSpawning() {
        boolean warmup = frameCount < Constants.WARMUP_FRAMES;
        int baseSpawn = settings.getSpawnInterval();
        float tMult = getTempoSpawnMult();

        if (!warmup) {
            int astInt = Math.max(5, (int)(baseSpawn / (difficulty * tMult)));
            if (frameCount % astInt == 0) {
                Asteroid a = new Asteroid(screenW, screenH, player.getX());
                asteroids.add(a);
                spawnWarnings.add(new float[]{a.getX(), 20});
            }
        }

        float sdMult = getTempoStarDustMult();
        int sdInt = Math.max(8, (int)(Constants.STARDUST_SPAWN_INTERVAL / (difficulty * sdMult)));
        if (frameCount % sdInt == 0) {
            if (Math.random() < Constants.STARDUST_CHAIN_CHANCE && !chainActive) {
                spawnStarDustChain();
            } else {
                starDusts.add(new StarDust(screenW, screenH));
            }
        }

        powerUpSpawnTimer++;
        if (powerUpSpawnTimer >= Constants.POWERUP_SPAWN_INTERVAL) {
            powerUpSpawnTimer = 0;
            powerUps.add(new PowerUp(screenW, screenH, (int)(Math.random() * 4)));
        }
    }

    private void spawnStarDustChain() {
        int count = Constants.STARDUST_CHAIN_MIN + (int)(Math.random() * (Constants.STARDUST_CHAIN_MAX - Constants.STARDUST_CHAIN_MIN + 1));
        chainTarget = count; chainCounter = 0; chainActive = true;
        float startX = screenW * 0.15f + (float)(Math.random() * screenW * 0.7f);
        float endX = screenW * 0.15f + (float)(Math.random() * screenW * 0.7f);
        for (int i = 0; i < count; i++) {
            float t = (float) i / (count - 1);
            float cx = startX + (endX - startX) * t;
            float cy = -(i * 100 + 50);
            float wobble = (float)(Math.sin(i * 1.2) * screenW * 0.04);
            StarDust sd = new StarDust(screenW, screenH);
            sd.setPosition(cx + wobble, cy);
            starDusts.add(sd);
        }
    }

    private void updateSpawnWarnings() {
        Iterator<float[]> it = spawnWarnings.iterator();
        while (it.hasNext()) { float[] w = it.next(); w[1]--; if (w[1] <= 0) it.remove(); }
    }

    private float getEffectiveDifficulty(float speedMult, float gameSpeedMult) {
        float eff = difficulty * speedMult * gameSpeedMult * getTempoSpeedMult();
        if (slowmoActive) eff *= Constants.SLOWMO_FACTOR;
        return eff;
    }

    private void moveEntities(float effDiff) {
        for (Asteroid a : asteroids) a.update(effDiff);
        for (StarDust s : starDusts) s.update(effDiff);
        for (PowerUp p : powerUps) p.update(effDiff);
        Iterator<Particle> pi = particles.iterator();
        while (pi.hasNext()) { Particle p = pi.next(); p.update(); if (!p.isAlive()) pi.remove(); }
    }

    private void applyStarDustMagnet() {
        float px = player.getX(), py = player.getY();
        for (StarDust s : starDusts) {
            s.applyMagnet(px, py, screenW, magnetActive);
        }
    }

    private void checkCollisions() {
        RectF pb = player.getBounds();
        float px = player.getX(), py = player.getY();

        Iterator<StarDust> si = starDusts.iterator();
        while (si.hasNext()) { StarDust s = si.next();
            if (RectF.intersects(pb, s.getBounds())) {
                if (!firstStarDustSeen) firstStarDustSeen = true;
                collectStarDust(s); si.remove();
            }
        }

        Iterator<PowerUp> pui = powerUps.iterator();
        while (pui.hasNext()) { PowerUp pu = pui.next();
            if (RectF.intersects(pb, pu.getBounds())) { collectPowerUp(pu); pui.remove(); }
        }

        if (player.isShielded()) return;

        for (Asteroid a : asteroids) {
            if (RectF.intersects(pb, a.getBounds())) { onPlayerHit(a, px, py); return; }
            if (nearMissCooldown <= 0) {
                float dx = px - a.getX(), dy = py - a.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float threshold = (player.getSize() + a.getSize()) * Constants.NEAR_MISS_RANGE;
                float hitDist = (player.getSize() + a.getSize()) * 0.65f;
                if (dist < threshold && dist > hitDist) { onNearMiss(px, py, a.getX(), a.getY()); break; }
            }
        }
    }

    private void spawnRingBurst(float x, float y, int color) {
        int count = 7;
        float angleStep = (float)(2 * Math.PI / count);
        for (int i = 0; i < count; i++) {
            float angle = angleStep * i + (float)(Math.random() * 0.3);
            float dirX = (float) Math.cos(angle);
            float dirY = (float) Math.sin(angle);
            float speed = 150 + (float)(Math.random() * 80);
            ringBursts.add(new float[]{x, y, dirX, dirY, speed, 1f, color});
        }
    }

    private void collectStarDust(StarDust s) {
        combo++; comboTimer = Constants.COMBO_TIMEOUT;
        if (combo > maxCombo) maxCombo = combo;
        int pts = Constants.STARDUST_SCORE;
        int cm = 1 + (int)(combo * Constants.COMBO_MULTIPLIER);
        if (doubleActive) cm *= 2;
        if (riskWindowActive) pts = (int)(pts * Constants.RISK_WINDOW_MULT);
        int fp = pts * cm;
        score += fp; orbsCollected++;

        if (chainActive) {
            chainCounter++;
            if (chainCounter >= chainTarget) {
                chainActive = false;
                int chainBonus = Constants.STARDUST_CHAIN_BONUS * chainTarget;
                score += chainBonus;
                popups.add(new ScorePopup(s.getX(), s.getY() - 40, "CHAIN +" + chainBonus + "!", 0xFF00E5FF));
                spawnParticles(s.getX(), s.getY(), 20, 0xFF00E5FF);
            }
        }

        popups.add(ScorePopup.createCollect(s.getX(), s.getY(), fp, cm));
        spawnRingBurst(s.getX(), s.getY(), 0xFFFFD740); // Ring Burst eklendi
        sound.playCollect(); vibration.vibrateCollect();
    }

    private void collectPowerUp(PowerUp pu) {
        activatePowerUp(pu.getType());
        popups.add(ScorePopup.createPowerUp(pu.getX(), pu.getY(), pu.getType()));
        spawnRingBurst(pu.getX(), pu.getY(), PowerUp.getColor(pu.getType())); // Ring Burst
        sound.playCollect(); vibration.vibrateCollect();
    }

    private void activatePowerUp(int type) {
        switch (type) {
            case Constants.POWERUP_MAGNET: magnetActive = true; magnetTimer = Constants.POWERUP_DURATION; break;
            case Constants.POWERUP_SLOWMO: slowmoActive = true; slowmoTimer = Constants.POWERUP_DURATION; break;
            case Constants.POWERUP_DOUBLE: doubleActive = true; doubleTimer = Constants.POWERUP_DURATION; break;
            case Constants.POWERUP_SHIELD: player.activateShield(Constants.POWERUP_DURATION); break;
        }
    }

    private void onNearMiss(float px, float py, float ax, float ay) {
        nearMissCooldown = Constants.NEAR_MISS_COOLDOWN; nearMissCount++;
        if (!firstNearMiss) firstNearMiss = true;
        
        // Graze Chain Logic
        grazeChainCount++;
        grazeChainTimer = Constants.GRAZE_CHAIN_WINDOW;
        
        // Katlanarak artan puan
        int grazePoints = Constants.NEAR_MISS_BONUS * (1 << (Math.min(grazeChainCount, 6) - 1));
        if (riskWindowActive) grazePoints = (int)(grazePoints * Constants.RISK_WINDOW_MULT);
        score += grazePoints;
        
        int textColor;
        switch (grazeChainCount) {
            case 1: textColor = Color.WHITE; break;
            case 2: textColor = Color.CYAN; break;
            case 3: textColor = Color.MAGENTA; break;
            default: textColor = Color.rgb(255, 100, 0); break;
        }
        
        if (grazeChainCount >= 3) {
            popups.add(new ScorePopup(px, py - 70, grazeChainCount >= 5 ? "INSANE!" : "DAREDEVIL!", textColor));
        }
        
        popups.add(new ScorePopup(px, py - 40, "+" + grazePoints, textColor));
        
        riskWindowActive = true; riskWindowTimer = Constants.RISK_WINDOW_DURATION;
        nearMissFlashes.add(new float[]{px, py, ax, ay, Constants.NEAR_MISS_FLASH_LIFE});
        
        // Daha güçlü titreşim
        if (grazeChainCount > 1) vibration.vibrateExplosion(); 
    }

    private void checkOverdrive() {
        if (!overdriveTriggered && combo >= Constants.OVERDRIVE_COMBO_THRESHOLD && !player.isOverdrive()) {
            overdriveTriggered = true; player.activateOverdrive();
            popups.add(new ScorePopup(player.getX(), player.getY() - 60, "OVERDRIVE!", 0xFFFF6D00));
            spawnParticles(player.getX(), player.getY(), 20, 0xFFFF6D00);
            vibration.vibrateExplosion();
        }
    }

    private void onPlayerHit(Asteroid a, float px, float py) {
        killerAsteroid = a;
        spawnParticles(px, py, Constants.EXPLOSION_PARTICLES, 0xFFFF1744);
        spawnParticles(a.getX(), a.getY(), Constants.EXPLOSION_PARTICLES / 2, 0xFF78909C);
        sound.playExplosion(); vibration.vibrateExplosion();
        shakeIntensity = Constants.SHAKE_INTENSITY;
        shockwaveActive = true; shockwaveX = px; shockwaveY = py; shockwaveRadius = 0; shockwaveAlpha = 1f;
        freezeTimer = Constants.FREEZE_FRAMES;
    }

    private void triggerGameOver() {
        state = Constants.STATE_GAME_OVER;
        settings.setHighScore(score);
        settings.incrementGamesPlayed();
        sound.playGameOver();
    }

    private void updateShockwave() {
        if (!shockwaveActive) return;
        shockwaveRadius += 25;
        float maxR = Math.max(screenW, screenH) * 0.8f;
        shockwaveAlpha = 1f - (shockwaveRadius / maxR);
        if (shockwaveAlpha <= 0) { shockwaveActive = false; shockwaveAlpha = 0; }
    }

    private void checkMilestone() {
        for (int m : MILESTONES) {
            if (score >= m && lastMilestone < m) {
                lastMilestone = m;
                milestoneText = m >= 10000 ? "★ " + (m/1000) + "K ★" : "★ " + m + " ★";
                milestoneTimer = 90;
                spawnParticles(screenW / 2f, screenH * 0.3f, 25, 0xFFFFD740);
                vibration.vibrateCollect(); break;
            }
        }
    }

    private void updateDangerLevel() {
        float minDist = Float.MAX_VALUE;
        float px = player.getX(), py = player.getY();
        for (Asteroid a : asteroids) {
            float dx = px - a.getX(), dy = py - a.getY();
            float dist = (float) Math.sqrt(dx * dx + dy * dy) - a.getSize();
            if (dist < minDist) minDist = dist;
        }
        dangerLevel = Math.max(0, Math.min(1f, 1f - minDist / (screenW * 0.2f)));
    }

    private void spawnParticles(float px, float py, int count, int color) {
        for (int i = 0; i < count; i++) {
            float angle = (float)(Math.random() * Math.PI * 2);
            float spd = (float)(Math.random() * Constants.PARTICLE_MAX_SPEED);
            float vx = (float)(Math.cos(angle) * spd), vy = (float)(Math.sin(angle) * spd);
            float sz = 2f + (float)(Math.random() * 5);
            int life = Constants.PARTICLE_LIFETIME / 2 + (int)(Math.random() * Constants.PARTICLE_LIFETIME / 2);
            particles.add(new Particle(px, py, vx, vy, sz, tweakColor(color), life));
        }
    }

    private int tweakColor(int b) {
        int r = Math.min(255, Math.max(0, ((b >> 16)&0xFF) + (int)(Math.random()*40-20)));
        int g = Math.min(255, Math.max(0, ((b >> 8)&0xFF) + (int)(Math.random()*40-20)));
        int bl = Math.min(255, Math.max(0, (b&0xFF) + (int)(Math.random()*40-20)));
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    private void cleanOffscreen() {
        Iterator<Asteroid> ai = asteroids.iterator(); while (ai.hasNext()) if (ai.next().isOffScreen(screenH)) ai.remove();
        Iterator<StarDust> si = starDusts.iterator(); while (si.hasNext()) if (si.next().isOffScreen(screenH)) si.remove();
        Iterator<PowerUp> pi = powerUps.iterator(); while (pi.hasNext()) if (pi.next().isOffScreen(screenH)) pi.remove();
    }

    public void handleTap() {
        if (state == Constants.STATE_GAME_OVER) { state = Constants.STATE_MENU; sound.playClick(); vibration.vibrateClick(); }
    }

    public void startGame() {
        state = Constants.STATE_PLAYING; score = 0; difficulty = 1f; frameCount = 0;
        combo = 0; comboTimer = 0; nearMissCooldown = 0;
        magnetActive = false; slowmoActive = false; doubleActive = false;
        magnetTimer = 0; slowmoTimer = 0; doubleTimer = 0;
        powerUpSpawnTimer = 0; overdriveTriggered = false;
        riskWindowActive = false; riskWindowTimer = 0;
        freezeTimer = 0; killerAsteroid = null; shockwaveActive = false;
        shakeIntensity = 0; shakeX = 0; shakeY = 0;
        orbsCollected = 0; nearMissCount = 0; maxCombo = 0;
        lastMilestone = 0; milestoneText = null; milestoneTimer = 0;
        dangerLevel = 0; tempoPhase = Constants.TEMPO_CALM; tempoTimer = Constants.TEMPO_CALM_DURATION;
        chainActive = false; chainCounter = 0; chainTarget = 0;
        firstStarDustSeen = false; firstNearMiss = false;
        transitionAlpha = 1f; transitioningIn = true;
        grazeChainCount = 0; grazeChainTimer = 0;
        startTime = System.currentTimeMillis();
        asteroids.clear(); starDusts.clear(); particles.clear();
        powerUps.clear(); popups.clear(); spawnWarnings.clear(); nearMissFlashes.clear(); ringBursts.clear();
        player.reset();
    }

    public void pauseGame() { if (state == Constants.STATE_PLAYING) state = Constants.STATE_PAUSED; }
    public void resumeGame() { if (state == Constants.STATE_PAUSED) state = Constants.STATE_PLAYING; }
    public void quitToMenu() { state = Constants.STATE_MENU; }
    public void openSettings() { state = Constants.STATE_SETTINGS; }
    public void closeSettings() { state = Constants.STATE_MENU; }
    public void cycleDifficulty() { settings.cycleDifficulty(); sound.playClick(); vibration.vibrateClick(); }
    public void cycleGameSpeed() { settings.cycleGameSpeed(); sound.playClick(); vibration.vibrateClick(); }
    public void toggleSound() { settings.toggleSound(); sound.setEnabled(settings.isSoundEnabled()); if (settings.isSoundEnabled()) sound.playClick(); }
    public void toggleVibration() { settings.toggleVibration(); vibration.setEnabled(settings.isVibrationEnabled()); vibration.vibrateClick(); }

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
    public long getSurvivalTime() { return startTime == 0 ? 0 : (System.currentTimeMillis() - startTime) / 1000; }
    public int getTempoPhase() { return tempoPhase; }
    public boolean isRiskWindowActive() { return riskWindowActive; }
    public int getRiskWindowTimer() { return riskWindowTimer; }
    public boolean isFreezing() { return freezeTimer > 0; }
    public Asteroid getKillerAsteroid() { return killerAsteroid; }
    public boolean isShockwaveActive() { return shockwaveActive; }
    public float getShockwaveX() { return shockwaveX; }
    public float getShockwaveY() { return shockwaveY; }
    public float getShockwaveRadius() { return shockwaveRadius; }
    public float getShockwaveAlpha() { return shockwaveAlpha; }
    public float getDangerLevel() { return dangerLevel; }
    public String getMilestoneText() { return milestoneText; }
    public int getMilestoneTimer() { return milestoneTimer; }
    public List<float[]> getSpawnWarnings() { return spawnWarnings; }
    public List<float[]> getNearMissFlashes() { return nearMissFlashes; }
    public List<float[]> getRingBursts() { return ringBursts; }
    public float getTransitionAlpha() { return transitionAlpha; }
    public boolean isFirstStarDustSeen() { return firstStarDustSeen; }
    public boolean isFirstNearMiss() { return firstNearMiss; }
    public int getFrameCount() { return frameCount; }
    public void releaseResources() { if (sound != null) sound.release(); }
}
