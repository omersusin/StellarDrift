package com.stellardrift.game.world;

import android.graphics.Color;
import android.graphics.Path;

public class ShipData {

    public final int id;
    public final String name;
    public final String description;

    // EKSIK OLAN FILEDLAR: Player ve ProjectileSystem tarafindan dogrudan cagiriliyor
    public final float speedMultiplier;
    public final float fireRate;
    public final int damage;
    public final float projectileSpeed;
    
    public final float baseSpeed;
    public final float baseFireRate;
    public final int baseDamage;
    public final float baseProjectileSpeed;
    
    public final int projectileCount;
    public final int price;
    public boolean unlocked;

    public static final int MAX_UPGRADE_LEVEL = 5;
    public static final int STAT_SPEED = 0;
    public static final int STAT_FIRE_RATE = 1;
    public static final int STAT_DAMAGE = 2;
    public static final int STAT_COUNT = 3;

    private final int[] upgradeLevels = new int[STAT_COUNT];
    public static final int[] UPGRADE_PRICES = {500, 1000, 2000, 3500, 5000};

    private static final float SPEED_PER_LEVEL = 0.06f;
    private static final float FIRE_RATE_PER_LEVEL = 0.12f;
    private static final float DAMAGE_PER_LEVEL = 0.20f;

    public final float[] weaponX;
    public final float[] weaponY;
    public final float[] engineX;
    public final float[] engineY;
    public final float[] engineRadius;

    public final int hullColor, hullDarkColor, wingColor;
    public final int cockpitColor, cockpitGlowColor, engineGlowColor;
    public final int trailColor, projectileColor;

    public Path hullPath, cockpitPath, leftWingPath, rightWingPath;
    public Path[] detailPaths;
    public Path accentPath;

    public final float halfWidth, halfHeight, collisionRadius;

    public ShipData(int id, String name, String desc,
                    float speed, float fireRate, int damage,
                    float projSpeed, int projCount, int price,
                    float[] wx, float[] wy,
                    float[] ex, float[] ey, float[] er,
                    int hull, int hullDark, int wing,
                    int cockpit, int cockpitGlow, int engineGlow,
                    int trail, int projectile,
                    float collR) {
        this.id = id;
        this.name = name;
        this.description = desc;
        
        // Base stat atamalari
        this.baseSpeed = speed;
        this.baseFireRate = fireRate;
        this.baseDamage = damage;
        this.baseProjectileSpeed = projSpeed;
        
        // EKSIK OLAN FIELD'LARIN ATAMALARI (Geriye Uyumluluk)
        this.speedMultiplier = speed;
        this.fireRate = fireRate;
        this.damage = damage;
        this.projectileSpeed = projSpeed;
        
        this.projectileCount = projCount;
        this.price = price;
        this.unlocked = (id == 0);
        this.weaponX = wx; this.weaponY = wy;
        this.engineX = ex; this.engineY = ey; this.engineRadius = er;
        this.hullColor = hull; this.hullDarkColor = hullDark; this.wingColor = wing;
        this.cockpitColor = cockpit; this.cockpitGlowColor = cockpitGlow; this.engineGlowColor = engineGlow;
        this.trailColor = trail; this.projectileColor = projectile;
        this.halfWidth = 30f; this.halfHeight = 30f; this.collisionRadius = collR;
    }

    public float getEffectiveSpeed() { return baseSpeed * (1f + upgradeLevels[STAT_SPEED] * SPEED_PER_LEVEL); }
    public float getEffectiveFireRate() { return baseFireRate * (1f + upgradeLevels[STAT_FIRE_RATE] * FIRE_RATE_PER_LEVEL); }
    public int getEffectiveDamage() { return Math.max(1, Math.round(baseDamage * (1f + upgradeLevels[STAT_DAMAGE] * DAMAGE_PER_LEVEL))); }

    public int getUpgradeLevel(int statIndex) { return upgradeLevels[statIndex]; }
    public void setUpgradeLevel(int statIndex, int level) { upgradeLevels[statIndex] = Math.max(0, Math.min(MAX_UPGRADE_LEVEL, level)); }
    public boolean canUpgrade(int statIndex) { return upgradeLevels[statIndex] < MAX_UPGRADE_LEVEL; }
    public int getUpgradeCost(int statIndex) { return upgradeLevels[statIndex] >= MAX_UPGRADE_LEVEL ? -1 : UPGRADE_PRICES[upgradeLevels[statIndex]]; }
    public boolean applyUpgrade(int statIndex) { if (!canUpgrade(statIndex)) return false; upgradeLevels[statIndex]++; return true; }

    public float getStatBarRatio(int statIndex) {
        switch (statIndex) {
            case STAT_SPEED: return Math.min(1f, getEffectiveSpeed() / 1.6f);
            case STAT_FIRE_RATE: return Math.min(1f, getEffectiveFireRate() / 7f);
            case STAT_DAMAGE: return Math.min(1f, getEffectiveDamage() / 6f);
            default: return 0f;
        }
    }

    public String getStatName(int statIndex) {
        switch (statIndex) {
            case STAT_SPEED: return "SPD";
            case STAT_FIRE_RATE: return "FRT";
            case STAT_DAMAGE: return "DMG";
            default: return "???";
        }
    }

    public int getStatColor(int statIndex) {
        switch (statIndex) {
            case STAT_SPEED: return Color.rgb(80, 200, 255);
            case STAT_FIRE_RATE: return Color.rgb(255, 180, 50);
            case STAT_DAMAGE: return Color.rgb(255, 80, 60);
            default: return Color.WHITE;
        }
    }
}
