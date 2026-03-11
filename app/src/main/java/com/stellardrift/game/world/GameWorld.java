package com.stellardrift.game.world;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import com.stellardrift.game.util.Constants;
import com.stellardrift.game.util.EconomyManager;
import com.stellardrift.game.util.SettingsManager;
import com.stellardrift.game.util.SoundManager;
import com.stellardrift.game.util.VibrationManager;
import com.stellardrift.game.render.ShipRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameWorld {

    private int screenW, screenH;
    private int state, score, frameCount;
    private float difficulty;

    private Player player;
    private ShipRegistry shipRegistry;
    private EconomyManager economy;
    private ProjectileSystem projectiles;
    private FuelSystem fuelSystem;
    private PlasmaCore plasmaCore;

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
    private float[] directionalDangers = new float[4]; 
    private List<float[]> spawnWarnings;
    private List<float[]> nearMissFlashes;

    private int grazeChainCount;
    private int grazeChainTimer;
    private List<float[]> ringBursts; 

    private int chainCounter;
    private int chainTarget;
    private boolean chainActive;
    private boolean firstStarDustSeen, firstNearMiss;
    private float transitionAlpha;
    private boolean transitioningIn;
    
    private int soundToggleClicks = 0, vibToggleClicks = 0;
    private boolean isGodMode = false;

    private float cosmicBreathTimer = 0f, cosmicBreathValue = 0f, hitStallTimer = 0f, spawnBiasAngle = 0f;

    private static final int[] MILESTONES = {1000, 2500, 5000, 10000, 25000, 50000};

    public void setAudioEngine(SoundManager sm, VibrationManager vm) {
        this.sound = sm;
        this.vibration = vm;
    }

    public GameWorld(int sw, int sh, Context ctx) {
        screenW = sw; screenH = sh;
        settings = new SettingsManager(ctx);
        
        shipRegistry = new ShipRegistry();
        economy = new EconomyManager(ctx, ShipRegistry.SHIP_COUNT);
        fuelSystem = new FuelSystem(sw, sh);
        plasmaCore = new PlasmaCore(sw, sh);
        player = new Player(sw, sh, shipRegistry, fuelSystem);
        projectiles = new ProjectileSystem(sw, sh);

        economy.syncUpgradesToShipData(shipRegistry.getAllShips());
        player.setShip(shipRegistry.getSelectedShip());

        asteroids = new ArrayList<>(); starDusts = new ArrayList<>(); particles = new ArrayList<>();
        powerUps = new ArrayList<>(); popups = new ArrayList<>(); spawnWarnings = new ArrayList<>();
        nearMissFlashes = new ArrayList<>(); ringBursts = new ArrayList<>();

        state = Constants.STATE_MENU; tempoPhase = Constants.TEMPO_CALM; tempoTimer = Constants.TEMPO_CALM_DURATION; transitionAlpha = 0;
        directionalDangers[0] = 0f; directionalDangers[1] = 0f; directionalDangers[2] = 0f; directionalDangers[3] = 0f;
    }

    public void update(float joyDirX, float joyDirY, float joyMag) {
        float dt = 0.016f; 

        updateCosmicBreath(dt); updatePopups(dt); updateShake(); updateShockwave(); updateNearMissFlashes(); updateRingBursts(dt); updateTransition();
        
        if (state == Constants.STATE_PAUSED || state != Constants.STATE_PLAYING) return;
        if (freezeTimer > 0) { freezeTimer--; if (freezeTimer <= 0) triggerGameOver(); return; }

        frameCount++;
        
        fuelSystem.setDrainPaused(isGodMode);
        fuelSystem.update(dt);
        
        hitStallTimer = Math.max(0, hitStallTimer - dt);
        float effectiveDtScale = (hitStallTimer > 0) ? Constants.HIT_STALL_TIME_SCALE : 1f;

        float speedMult = settings.getSpeedMultiplier();
        float gameSpeedMult = settings.getGameSpeedMultiplier();
        difficulty = Math.min(Constants.MAX_DIFFICULTY, 1f + frameCount * Constants.DIFFICULTY_RATE);

        player.setOverchargeSpeedBoost(plasmaCore.getSpeedMultiplier());
        player.update(joyDirX, joyDirY, joyMag, dt);
        float comboProg = comboTimer > 0 ? (float)comboTimer / Constants.COMBO_TIMEOUT : 0f;
        player.setComboInfo(combo, comboProg);
        updateSpawnBias(dt, joyDirX, joyDirY, joyMag);
        
        ShipData currentShip = shipRegistry.getSelectedShip();
        float overchargeFireMult = plasmaCore.getFireRateMultiplier();
        float recoil = projectiles.autoFire(dt, player.getX(), player.getY(), player.getBankAngle(), currentShip, overchargeFireMult);
        if (recoil > 0) { shakeIntensity = Math.max(shakeIntensity, recoil * 1.5f); }
        
        projectiles.setDamageOverride(currentShip.getEffectiveDamage());
        projectiles.update(dt);
        plasmaCore.update(dt);

        if (hitStallTimer <= 0) { updateTimers(dt); updateTempo(); handleSpawning(); updateSpawnWarnings(); }

        updateDirectionalDangers(dt);
        float effDiff = getEffectiveDifficulty(speedMult, gameSpeedMult) * effectiveDtScale;
        moveEntities(effDiff);
        applyStarDustMagnet();
        
        checkCollisions(); cleanOffscreen(); checkMilestone(); checkOverdrive(); economy.update(dt);
    }

    private void updateCosmicBreath(float dt) { cosmicBreathTimer += dt; cosmicBreathValue = (float) Math.sin(cosmicBreathTimer * Math.PI * 2.0 / 4.0) * 0.5f + 0.5f; }
    private void updateSpawnBias(float dt, float dirX, float dirY, float mag) { if (mag > 0.2f) { float targetAngle = (float) Math.atan2(dirY, dirX); float diff = targetAngle - spawnBiasAngle; while (diff > Math.PI) diff -= 2 * Math.PI; while (diff < -Math.PI) diff += 2 * Math.PI; spawnBiasAngle += diff * Math.min(dt * 3f, 1f); } }
    
    private void updateDirectionalDangers(float dt) {
        float tTop = 0, tBot = 0, tLeft = 0, tRight = 0, threatRadius = screenH * 0.4f;
        for (Asteroid a : asteroids) {
            float dx = a.getX() - player.getX(), dy = a.getY() - player.getY(), dist = (float) Math.sqrt(dx*dx + dy*dy);
            if (dist > threatRadius) continue;
            float intensity = 1f - (dist / threatRadius); intensity *= intensity; 
            if (dy < -Math.abs(dx) * 0.5f) tTop = Math.max(tTop, intensity); if (dy > Math.abs(dx) * 0.5f) tBot = Math.max(tBot, intensity);
            if (dx < -Math.abs(dy) * 0.5f) tLeft = Math.max(tLeft, intensity); if (dx > Math.abs(dy) * 0.5f) tRight = Math.max(tRight, intensity);
        }
        float smooth = dt * 6f; directionalDangers[0] += (tTop - directionalDangers[0]) * smooth; directionalDangers[1] += (tBot - directionalDangers[1]) * smooth; directionalDangers[2] += (tLeft - directionalDangers[2]) * smooth; directionalDangers[3] += (tRight - directionalDangers[3]) * smooth;
        dangerLevel = Math.max(Math.max(directionalDangers[0], directionalDangers[1]), Math.max(directionalDangers[2], directionalDangers[3]));
    }
    
    private void updateTimers(float dt) {
        if (nearMissCooldown > 0) nearMissCooldown--; if (grazeChainTimer > 0) { grazeChainTimer--; if (grazeChainTimer <= 0) grazeChainCount = 0; }
        if (combo > 0) { comboTimer--; if (comboTimer <= 0) { combo = 0; overdriveTriggered = false; chainActive = false; } }
        if (magnetActive) { magnetTimer--; if (magnetTimer <= 0) magnetActive = false; } 
        if (slowmoActive) { slowmoTimer--; if (slowmoTimer <= 0) slowmoActive = false; }
        if (economy.isDoubleActive()) { doubleTimer--; if (doubleTimer <= 0) economy.setDoubleActive(false); } 
        if (riskWindowActive) { riskWindowTimer--; if (riskWindowTimer <= 0) riskWindowActive = false; }
        if (milestoneTimer > 0) milestoneTimer--;
    }
    
    private void updatePopups(float dt) { Iterator<ScorePopup> it = popups.iterator(); while (it.hasNext()) { ScorePopup sp = it.next(); sp.update(dt); if (!sp.isAlive()) it.remove(); } }
    private void updateShake() { if (shakeIntensity > 0.3f) { shakeX = (float)(Math.random() * shakeIntensity * 2 - shakeIntensity); shakeY = (float)(Math.random() * shakeIntensity * 2 - shakeIntensity); shakeIntensity *= Constants.SHAKE_DECAY; } else { shakeX = 0; shakeY = 0; shakeIntensity = 0; } }
    private void updateNearMissFlashes() { Iterator<float[]> it = nearMissFlashes.iterator(); while (it.hasNext()) { float[] f = it.next(); f[4]--; if (f[4] <= 0) it.remove(); } }
    private void updateRingBursts(float dt) { Iterator<float[]> it = ringBursts.iterator(); while (it.hasNext()) { float[] b = it.next(); b[0] += b[2] * b[4] * dt; b[1] += b[3] * b[4] * dt; b[4] *= 0.92f; b[5] -= dt * 5f; if (b[5] <= 0) it.remove(); } }
    private void updateTransition() { if (transitioningIn) { transitionAlpha -= Constants.TRANSITION_SPEED_IN; if (transitionAlpha <= 0) { transitionAlpha = 0; transitioningIn = false; } } }
    private void updateTempo() {
        if (isGodMode) { tempoPhase = Constants.TEMPO_REWARD; return; }
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
        boolean warmup = frameCount < Constants.WARMUP_FRAMES; int baseSpawn = settings.getSpawnInterval(); float tMult = getTempoSpawnMult();
        if (!warmup && !isGodMode) { int astInt = Math.max(5, (int)(baseSpawn / (difficulty * tMult))); if (frameCount % astInt == 0) { Asteroid a = new Asteroid(screenW, screenH, player.getX()); asteroids.add(a); spawnWarnings.add(new float[]{a.getX(), 20}); } }
        
        float sdMult = getTempoStarDustMult(); if (isGodMode) sdMult = 5f; 
        int sdInt = Math.max(8, (int)(Constants.STARDUST_SPAWN_INTERVAL / (difficulty * sdMult)));
        
        if (frameCount % sdInt == 0) { 
            if (Math.random() < 0.15f) { plasmaCore.trySpawn((float)(Math.random() * screenW), -50, Constants.STARDUST_SPEED * (screenW / 1080f)); } 
            else if (Math.random() < Constants.STARDUST_CHAIN_CHANCE && !chainActive) { spawnStarDustChain(); } 
            else { starDusts.add(spawnBiasedStarDust()); }
        }
        
        powerUpSpawnTimer++; int pInterval = isGodMode ? Constants.POWERUP_SPAWN_INTERVAL / 2 : Constants.POWERUP_SPAWN_INTERVAL;
        if (powerUpSpawnTimer >= pInterval) { powerUpSpawnTimer = 0; int puType = isGodMode ? (Math.random() > 0.5 ? Constants.POWERUP_MAGNET : Constants.POWERUP_DOUBLE) : (int)(Math.random() * 4); powerUps.add(new PowerUp(screenW, screenH, puType)); }
    }
    
    private StarDust spawnBiasedStarDust() {
        StarDust sd = new StarDust(screenW, screenH);
        if (Math.random() <= Constants.SPAWN_BIAS_STRENGTH) { float aheadDist = 150f + (float)(Math.random() * 150f); float angleOffset = (float)((Math.random() - 0.5) * Math.toRadians(60)); float finalAngle = spawnBiasAngle + angleOffset; float spawnX = player.getX() + (float) Math.cos(finalAngle) * aheadDist; spawnX = Math.max(20, Math.min(screenW - 20, spawnX)); sd.setPosition(spawnX, sd.getY()); }
        return sd;
    }
    private void spawnStarDustChain() {
        int count = Constants.STARDUST_CHAIN_MIN + (int)(Math.random() * (Constants.STARDUST_CHAIN_MAX - Constants.STARDUST_CHAIN_MIN + 1)); if (isGodMode) count += 3;
        chainTarget = count; chainCounter = 0; chainActive = true; float startX = spawnBiasedStarDust().getX(); float endX = spawnBiasedStarDust().getX();
        for (int i = 0; i < count; i++) { float t = (float) i / (count - 1), cx = startX + (endX - startX) * t, cy = -(i * 100 + 50), wobble = (float)(Math.sin(i * 1.2) * screenW * 0.04); StarDust sd = new StarDust(screenW, screenH); sd.setPosition(cx + wobble, cy); starDusts.add(sd); }
    }
    
    private void updateSpawnWarnings() { Iterator<float[]> it = spawnWarnings.iterator(); while (it.hasNext()) { float[] w = it.next(); w[1]--; if (w[1] <= 0) it.remove(); } }
    private float getEffectiveDifficulty(float speedMult, float gameSpeedMult) { float eff = difficulty * speedMult * gameSpeedMult * getTempoSpeedMult(); if (slowmoActive) eff *= Constants.SLOWMO_FACTOR; return eff; }
    private void moveEntities(float effDiff) {
        for (Asteroid a : asteroids) a.update(effDiff); for (StarDust s : starDusts) s.update(effDiff); for (PowerUp p : powerUps) p.update(effDiff);
        Iterator<Particle> pi = particles.iterator(); while (pi.hasNext()) { Particle p = pi.next(); p.update(); if (!p.isAlive()) pi.remove(); }
    }
    private void applyStarDustMagnet() { float px = player.getX(), py = player.getY(); for (StarDust s : starDusts) s.applyMagnet(px, py, screenW, magnetActive); }

    private void checkCollisions() {
        RectF pb = player.getBounds(); float px = player.getX(), py = player.getY();
        Iterator<StarDust> si = starDusts.iterator(); while (si.hasNext()) { StarDust s = si.next(); if (RectF.intersects(pb, s.getBounds())) { if (!firstStarDustSeen) firstStarDustSeen = true; collectStarDust(s); si.remove(); } }
        Iterator<PowerUp> pui = powerUps.iterator(); while (pui.hasNext()) { PowerUp pu = pui.next(); if (RectF.intersects(pb, pu.getBounds())) { collectPowerUp(pu); pui.remove(); } }

        if (plasmaCore.checkCollection(px, py, player.getCollisionRadius())) {
            spawnCollectBurst(px, py, Color.rgb(60, 180, 255)); popups.add(new ScorePopup(px, py - 30, "⚡ OVERCHARGE!", Color.rgb(80, 200, 255), 1.5f));
            shakeIntensity += 3f; score += 50; if(vibration!=null) vibration.vibrateOvercharge(); if(sound!=null) sound.playOvercharge();
        }

        projectiles.checkHits(asteroids, economy, new ProjectileSystem.HitCallback() {
            @Override public void onAsteroidHit(Asteroid asteroid) { spawnParticles(asteroid.getX(), asteroid.getY(), 3, Color.WHITE); if(sound!=null) sound.playHit(); if(vibration!=null) vibration.vibrateHit(); }
            @Override public void onAsteroidDestroyed(Asteroid asteroid) {
                spawnParticles(asteroid.getX(), asteroid.getY(), 15, Color.rgb(200, 180, 100)); spawnRingBurst(asteroid.getX(), asteroid.getY(), Color.rgb(255, 215, 0)); 
                int cred = economy.isDoubleActive() ? asteroid.getCreditValue() * 2 : asteroid.getCreditValue();
                popups.add(new ScorePopup(asteroid.getX(), asteroid.getY(), "+ " + cred + " ✦", Color.rgb(255, 215, 0), economy.isDoubleActive() ? 1.5f : 1.2f));
                if(sound!=null) sound.playExplode(); if(vibration!=null) vibration.vibrateExplode(); score += 25; 
            }
        });

        Iterator<Asteroid> ai = asteroids.iterator(); while (ai.hasNext()) { if (ai.next().isDead()) ai.remove(); }
        if (player.isShielded() || isGodMode) return;

        for (Asteroid a : asteroids) {
            if (RectF.intersects(pb, a.getBounds())) { onPlayerHit(a, px, py); return; }
            if (nearMissCooldown <= 0) {
                float dx = px - a.getX(), dy = py - a.getY(), dist = (float) Math.sqrt(dx * dx + dy * dy);
                float threshold = (player.getSize() + a.getSize()) * Constants.NEAR_MISS_RANGE, hitDist = (player.getSize() + a.getSize()) * 0.65f;
                if (dist < threshold && dist > hitDist) { onNearMiss(px, py, a.getX(), a.getY()); break; }
            }
        }
    }

    private void spawnRingBurst(float x, float y, int color) { int count = 7; float angleStep = (float)(2 * Math.PI / count); for (int i = 0; i < count; i++) { float angle = angleStep * i + (float)(Math.random() * 0.3); ringBursts.add(new float[]{x, y, (float)Math.cos(angle), (float)Math.sin(angle), 150 + (float)(Math.random() * 80), 1f, color}); } }
    private void spawnCollectBurst(float x, float y, int color) { spawnRingBurst(x, y, color); }
    private void collectStarDust(StarDust s) { hitStallTimer = Constants.HIT_STALL_DURATION; combo++; comboTimer = Constants.COMBO_TIMEOUT; if (combo > maxCombo) maxCombo = combo; int pts = Constants.STARDUST_SCORE, cm = 1 + (int)(combo * Constants.COMBO_MULTIPLIER); if (economy.isDoubleActive() || isGodMode) cm *= 2; if (riskWindowActive) pts = (int)(pts * Constants.RISK_WINDOW_MULT); int fp = pts * cm; score += fp; orbsCollected++; float fuelMult = economy.isDoubleActive() ? 2.0f : 1.0f; fuelSystem.onStarDustCollected(fuelMult); if (chainActive) { chainCounter++; if (chainCounter >= chainTarget) { chainActive = false; int chainBonus = Constants.STARDUST_CHAIN_BONUS * chainTarget; score += chainBonus; popups.add(new ScorePopup(s.getX(), s.getY() - 40, "CHAIN +" + chainBonus + "!", 0xFF00E5FF, 1.5f)); spawnParticles(s.getX(), s.getY(), 20, 0xFF00E5FF); fuelSystem.onChainCompleted(); } } popups.add(ScorePopup.createCollect(s.getX(), s.getY(), fp, cm)); spawnRingBurst(s.getX(), s.getY(), 0xFFFFD740); if(sound!=null) sound.playCollect(); if(vibration!=null) vibration.vibrateCollect(); }
    private void collectPowerUp(PowerUp pu) { activatePowerUp(pu.getType()); popups.add(ScorePopup.createPowerUp(pu.getX(), pu.getY(), pu.getType())); spawnRingBurst(pu.getX(), pu.getY(), PowerUp.getColor(pu.getType())); if(sound!=null) sound.playPowerUp(); if(vibration!=null) vibration.vibratePowerUp(); }
    private void activatePowerUp(int type) { switch (type) { case Constants.POWERUP_MAGNET: magnetActive = true; magnetTimer = Constants.POWERUP_DURATION; break; case Constants.POWERUP_SLOWMO: slowmoActive = true; slowmoTimer = Constants.POWERUP_DURATION; break; case Constants.POWERUP_DOUBLE: economy.setDoubleActive(true); doubleTimer = Constants.POWERUP_DURATION; doubleActive = true; break; case Constants.POWERUP_SHIELD: player.activateShield(Constants.POWERUP_DURATION); break; } }
    
    private void onNearMiss(float px, float py, float ax, float ay) { 
        nearMissCooldown = Constants.NEAR_MISS_COOLDOWN; nearMissCount++; if (!firstNearMiss) firstNearMiss = true; 
        grazeChainCount++; grazeChainTimer = Constants.GRAZE_CHAIN_WINDOW; 
        int grazePoints = Constants.NEAR_MISS_BONUS * (1 << (Math.min(grazeChainCount, 6) - 1)); 
        if (riskWindowActive) grazePoints = (int)(grazePoints * Constants.RISK_WINDOW_MULT); score += grazePoints; 
        int textColor; switch (grazeChainCount) { case 1: textColor = Color.WHITE; break; case 2: textColor = Color.CYAN; break; case 3: textColor = Color.MAGENTA; break; default: textColor = Color.rgb(255, 100, 0); break; } 
        if (grazeChainCount >= 3) popups.add(new ScorePopup(px, py - 70, grazeChainCount >= 5 ? "INSANE!" : "DAREDEVIL!", textColor, 1.6f)); 
        popups.add(new ScorePopup(px, py - 40, "+" + grazePoints, textColor, 1f)); 
        riskWindowActive = true; riskWindowTimer = Constants.RISK_WINDOW_DURATION; 
        nearMissFlashes.add(new float[]{px, py, ax, ay, Constants.NEAR_MISS_FLASH_LIFE}); 
        if(sound!=null) sound.playNearMiss(); 
        if (grazeChainCount > 1 && vibration!=null) vibration.vibrateNearMiss(); // HATA ÇÖZÜMÜ: vibrateNearMiss kullanılıyor (eski kodda patlama veriliyordu)
    }
    
    private void checkOverdrive() { if (!overdriveTriggered && combo >= Constants.OVERDRIVE_COMBO_THRESHOLD && !player.isOverdrive()) { overdriveTriggered = true; player.activateOverdrive(); popups.add(new ScorePopup(player.getX(), player.getY() - 60, "OVERDRIVE!", 0xFFFF6D00, 1.8f)); spawnParticles(player.getX(), player.getY(), 20, 0xFFFF6D00); if(vibration!=null) vibration.vibrateExplosion(); } }
    private void onPlayerHit(Asteroid a, float px, float py) { killerAsteroid = a; spawnParticles(px, py, Constants.EXPLOSION_PARTICLES, 0xFFFF1744); spawnParticles(a.getX(), a.getY(), Constants.EXPLOSION_PARTICLES / 2, 0xFF78909C); if(sound!=null) sound.playDeath(); if(vibration!=null) vibration.vibrateDeath(); shakeIntensity = Constants.SHAKE_INTENSITY; shockwaveActive = true; shockwaveX = px; shockwaveY = py; shockwaveRadius = 0; shockwaveAlpha = 1f; freezeTimer = Constants.FREEZE_FRAMES; }
    private void triggerGameOver() { state = Constants.STATE_GAME_OVER; settings.setHighScore(score); settings.incrementGamesPlayed(); economy.convertSessionScore(score); isGodMode = false; soundToggleClicks = 0; vibToggleClicks = 0; }
    private void updateShockwave() { if (!shockwaveActive) return; shockwaveRadius += 25; float maxR = Math.max(screenW, screenH) * 0.8f; shockwaveAlpha = 1f - (shockwaveRadius / maxR); if (shockwaveAlpha <= 0) { shockwaveActive = false; shockwaveAlpha = 0; } }
    private void checkMilestone() { for (int m : MILESTONES) { if (score >= m && lastMilestone < m) { lastMilestone = m; milestoneText = m >= 10000 ? "★ " + (m/1000) + "K ★" : "★ " + m + " ★"; milestoneTimer = 90; spawnParticles(screenW / 2f, screenH * 0.3f, 25, 0xFFFFD740); if(vibration!=null) vibration.vibrateCollect(); break; } } }
    private void updateDangerLevel() { float minDist = Float.MAX_VALUE; float px = player.getX(), py = player.getY(); for (Asteroid a : asteroids) { float dx = px - a.getX(), dy = py - a.getY(), dist = (float) Math.sqrt(dx * dx + dy * dy) - a.getSize(); if (dist < minDist) minDist = dist; } dangerLevel = Math.max(0, Math.min(1f, 1f - minDist / (screenW * 0.2f))); }
    private void spawnParticles(float px, float py, int count, int color) { for (int i = 0; i < count; i++) { float angle = (float)(Math.random() * Math.PI * 2), spd = (float)(Math.random() * Constants.PARTICLE_MAX_SPEED), vx = (float)(Math.cos(angle) * spd), vy = (float)(Math.sin(angle) * spd), sz = 2f + (float)(Math.random() * 5); int life = Constants.PARTICLE_LIFETIME / 2 + (int)(Math.random() * Constants.PARTICLE_LIFETIME / 2); particles.add(new Particle(px, py, vx, vy, sz, tweakColor(color), life)); } }
    private int tweakColor(int b) { int r = Math.min(255, Math.max(0, ((b >> 16)&0xFF) + (int)(Math.random()*40-20))), g = Math.min(255, Math.max(0, ((b >> 8)&0xFF) + (int)(Math.random()*40-20))), bl = Math.min(255, Math.max(0, (b&0xFF) + (int)(Math.random()*40-20))); return 0xFF000000 | (r << 16) | (g << 8) | bl; }
    private void cleanOffscreen() { Iterator<Asteroid> ai = asteroids.iterator(); while (ai.hasNext()) if (ai.next().isOffScreen(screenH)) ai.remove(); Iterator<StarDust> si = starDusts.iterator(); while (si.hasNext()) if (si.next().isOffScreen(screenH)) si.remove(); Iterator<PowerUp> pi = powerUps.iterator(); while (pi.hasNext()) if (pi.next().isOffScreen(screenH)) pi.remove(); }

    public void handleTap() { if (state == Constants.STATE_GAME_OVER) { state = Constants.STATE_MENU; if(sound!=null) sound.playMenuClick(); if(vibration!=null) vibration.vibrateMenuClick(); } }
    
    // HATA ÇÖZÜMÜ: openSettings eklendi
    public void openSettings() { state = Constants.STATE_SETTINGS; }
    
    public void startGame() {
        state = Constants.STATE_PLAYING; score = 0; difficulty = 1f; frameCount = 0; combo = 0; comboTimer = 0; nearMissCooldown = 0;
        magnetActive = false; slowmoActive = false; doubleActive = false; magnetTimer = 0; slowmoTimer = 0; doubleTimer = 0; economy.setDoubleActive(false);
        powerUpSpawnTimer = 0; overdriveTriggered = false; riskWindowActive = false; riskWindowTimer = 0; freezeTimer = 0; killerAsteroid = null; shockwaveActive = false; shakeIntensity = 0; shakeX = 0; shakeY = 0;
        orbsCollected = 0; nearMissCount = 0; maxCombo = 0; lastMilestone = 0; milestoneText = null; milestoneTimer = 0; dangerLevel = 0; tempoPhase = Constants.TEMPO_CALM; tempoTimer = Constants.TEMPO_CALM_DURATION;
        chainActive = false; chainCounter = 0; chainTarget = 0; firstStarDustSeen = false; firstNearMiss = false; transitionAlpha = 1f; transitioningIn = true; grazeChainCount = 0; grazeChainTimer = 0; hitStallTimer = 0f; spawnBiasAngle = 0f;
        fuelSystem.reset(); startTime = System.currentTimeMillis(); asteroids.clear(); starDusts.clear(); particles.clear(); powerUps.clear(); popups.clear(); spawnWarnings.clear(); nearMissFlashes.clear(); ringBursts.clear(); plasmaCore.reset(); player.reset();
        economy.syncUpgradesToShipData(shipRegistry.getAllShips()); player.setShip(shipRegistry.getSelectedShip()); 
        if (isGodMode) popups.add(new ScorePopup(screenW/2f, screenH/2f, "GOD MODE ENABLED", 0xFFFFD740, 2f));
    }
    public void pauseGame() { if (state == Constants.STATE_PLAYING) state = Constants.STATE_PAUSED; }
    public void resumeGame() { if (state == Constants.STATE_PAUSED) state = Constants.STATE_PLAYING; }
    public void quitToMenu() { state = Constants.STATE_MENU; }
    public void closeSettings() { state = Constants.STATE_MENU; }
    public void cycleDifficulty() { settings.cycleDifficulty(); if(sound!=null) sound.playMenuClick(); if(vibration!=null) vibration.vibrateMenuClick(); }
    public void cycleGameSpeed() { settings.cycleGameSpeed(); if(sound!=null) sound.playMenuClick(); if(vibration!=null) vibration.vibrateMenuClick(); }
    public void toggleSound() { settings.toggleSound(); if(sound!=null) { sound.setEnabled(settings.isSoundEnabled()); if (settings.isSoundEnabled()) sound.playMenuClick(); } if (!isGodMode) { soundToggleClicks++; if (soundToggleClicks >= 10) { isGodMode = true; if(vibration!=null) vibration.vibrateExplosion(); } } }
    public void toggleVibration() { settings.toggleVibration(); if(vibration!=null) { vibration.setEnabled(settings.isVibrationEnabled()); vibration.vibrateMenuClick(); } if (isGodMode) { vibToggleClicks++; if (vibToggleClicks >= 5) { isGodMode = false; soundToggleClicks = 0; vibToggleClicks = 0; if(sound!=null) sound.playMenuClick(); } } }

    public int getState() { return state; } 
    public void setState(int state) { this.state = state; } // Extra garanti
    public int getScore() { return score; } public int getHighScore() { return settings.getHighScore(); } public float getDifficulty() { return difficulty; } public Player getPlayer() { return player; } public List<Asteroid> getAsteroids() { return asteroids; } public List<StarDust> getStarDusts() { return starDusts; } public List<Particle> getParticles() { return particles; } public List<PowerUp> getPowerUps() { return powerUps; } public List<ScorePopup> getPopups() { return popups; } public SettingsManager getSettings() { return settings; } public int getCombo() { return combo; } public boolean isMagnetActive() { return magnetActive; } public boolean isSlowmoActive() { return slowmoActive; } public boolean isDoubleActive() { return doubleActive; } public int getMagnetTimer() { return magnetTimer; } public int getSlowmoTimer() { return slowmoTimer; } public int getDoubleTimer() { return doubleTimer; } public float getShakeX() { return shakeX; } public float getShakeY() { return shakeY; } public int getOrbsCollected() { return orbsCollected; } public int getNearMissCount() { return nearMissCount; } public int getMaxCombo() { return maxCombo; } public long getSurvivalTime() { return startTime == 0 ? 0 : (System.currentTimeMillis() - startTime) / 1000; } public int getTempoPhase() { return tempoPhase; } public boolean isRiskWindowActive() { return riskWindowActive; } public int getRiskWindowTimer() { return riskWindowTimer; } public boolean isFreezing() { return freezeTimer > 0; } public Asteroid getKillerAsteroid() { return killerAsteroid; } public boolean isShockwaveActive() { return shockwaveActive; } public float getShockwaveX() { return shockwaveX; } public float getShockwaveY() { return shockwaveY; } public float getShockwaveRadius() { return shockwaveRadius; } public float getShockwaveAlpha() { return shockwaveAlpha; } public float getDangerLevel() { return dangerLevel; } public float[] getDirectionalDangers() { return directionalDangers; } public String getMilestoneText() { return milestoneText; } public int getMilestoneTimer() { return milestoneTimer; } public List<float[]> getSpawnWarnings() { return spawnWarnings; } public List<float[]> getNearMissFlashes() { return nearMissFlashes; } public List<float[]> getRingBursts() { return ringBursts; } public float getTransitionAlpha() { return transitionAlpha; } public boolean isFirstStarDustSeen() { return firstStarDustSeen; } public boolean isFirstNearMiss() { return firstNearMiss; } public int getFrameCount() { return frameCount; } public float getCosmicBreath() { return cosmicBreathValue; } public void releaseResources() { if (sound != null) sound.release(); }
    public ProjectileSystem getProjectiles() { return projectiles; } public ShipRegistry getShipRegistry() { return shipRegistry; } public EconomyManager getEconomy() { return economy; } 
    public FuelSystem getFuelSystem() { return fuelSystem; } public boolean isGodModeActive() { return isGodMode; } public PlasmaCore getPlasmaCore() { return plasmaCore; }
}
