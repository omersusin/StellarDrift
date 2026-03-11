package com.stellardrift.game.world;

import android.graphics.Path;

public class ShipData {

    public final int id;
    public final String name;
    public final String description;

    public final float speedMultiplier;
    public final float fireRate;            
    public final int damage;                
    public final float projectileSpeed;     
    public final int projectileCount;       

    public final int price;                 
    public boolean unlocked;

    public final float[] weaponX;
    public final float[] weaponY;
    public final float[] engineX;
    public final float[] engineY;
    public final float[] engineRadius;

    public final int hullColor;
    public final int hullDarkColor;
    public final int wingColor;
    public final int cockpitColor;
    public final int cockpitGlowColor;
    public final int engineGlowColor;
    public final int trailColor;
    public final int projectileColor;

    public Path hullPath;
    public Path cockpitPath;
    public Path leftWingPath;
    public Path rightWingPath;
    public Path[] detailPaths;     
    public Path accentPath;        

    public final float collisionRadius;

    public ShipData(int id, String name, String description,
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
        this.description = description;
        this.speedMultiplier = speed;
        this.fireRate = fireRate;
        this.damage = damage;
        this.projectileSpeed = projSpeed;
        this.projectileCount = projCount;
        this.price = price;
        this.unlocked = (id == 0); 
        this.weaponX = wx;
        this.weaponY = wy;
        this.engineX = ex;
        this.engineY = ey;
        this.engineRadius = er;
        this.hullColor = hull;
        this.hullDarkColor = hullDark;
        this.wingColor = wing;
        this.cockpitColor = cockpit;
        this.cockpitGlowColor = cockpitGlow;
        this.engineGlowColor = engineGlow;
        this.trailColor = trail;
        this.projectileColor = projectile;
        this.collisionRadius = collR;
    }
}
