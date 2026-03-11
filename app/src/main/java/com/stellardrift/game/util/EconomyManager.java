package com.stellardrift.game.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.stellardrift.game.world.ShipRegistry;

public class EconomyManager {
    private final SharedPreferences prefs;
    private int totalCredits;
    private final boolean[] unlockedShips;
    private int selectedShipId;
    private float displayedCredits;
    private float creditFlashTimer = 0f;

    public EconomyManager(Context context) {
        prefs = context.getSharedPreferences("stellar_drift_save", Context.MODE_PRIVATE);
        totalCredits = prefs.getInt("credits", 0);
        selectedShipId = prefs.getInt("selected_ship", 0);

        unlockedShips = new boolean[ShipRegistry.SHIP_COUNT];
        unlockedShips[0] = true; 
        for (int i = 1; i < ShipRegistry.SHIP_COUNT; i++) {
            unlockedShips[i] = prefs.getBoolean("ship_" + i + "_unlocked", false);
        }
        displayedCredits = totalCredits;
    }

    public boolean purchaseShip(int shipId, int price) {
        if (totalCredits >= price && !unlockedShips[shipId]) {
            totalCredits -= price;
            unlockedShips[shipId] = true;
            saveAll();
            return true;
        }
        return false;
    }

    public void saveAll() {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt("credits", totalCredits);
        ed.putInt("selected_ship", selectedShipId);
        for (int i = 1; i < ShipRegistry.SHIP_COUNT; i++) {
            ed.putBoolean("ship_" + i + "_unlocked", unlockedShips[i]);
        }
        ed.apply();
    }

    public void addCredits(int amount) {
        totalCredits += amount;
        creditFlashTimer = 0.3f;
    }

    public void convertSessionScore(int score) {
        int earned = score / 10;
        if (earned > 0) {
            addCredits(earned);
            saveAll();
        }
    }

    public void update(float dt) {
        if (displayedCredits < totalCredits) {
            float diff = totalCredits - displayedCredits;
            float step = diff * 6f * dt;
            if (step < 1f) step = 1f;
            displayedCredits = Math.min(displayedCredits + step, totalCredits);
        } else {
            displayedCredits = totalCredits;
        }
        if (creditFlashTimer > 0) creditFlashTimer -= dt;
    }

    public int getCredits()               { return totalCredits; }
    public int getDisplayedCredits()      { return (int) displayedCredits; }
    public float getCreditFlash()         { return Math.max(0, creditFlashTimer); }
    public boolean isShipUnlocked(int id) { return unlockedShips[id]; }
    public int getSelectedShipId()        { return selectedShipId; }
    public void selectShip(int id)        { selectedShipId = id; saveAll(); }
}
