package com.stellardrift.game.util;

import android.content.Context;
import android.content.SharedPreferences;

public class EconomyManager {

    private final SharedPreferences prefs;
    private int totalCredits;
    private final boolean[] unlockedShips;
    private int selectedShipId;

    private float displayedCredits;
    private float creditFlashTimer = 0f;

    public EconomyManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        totalCredits = prefs.getInt("credits", 0);
        selectedShipId = prefs.getInt("selected_ship", 0);

        unlockedShips = new boolean[3];
        unlockedShips[0] = true; 
        unlockedShips[1] = prefs.getBoolean("ship_1_unlocked", false);
        unlockedShips[2] = prefs.getBoolean("ship_2_unlocked", false);

        displayedCredits = totalCredits;
    }

    public void addCredits(int amount) {
        totalCredits += amount;
        creditFlashTimer = 0.3f; 
        saveAll();
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
        prefs.edit()
            .putInt("credits", totalCredits)
            .putInt("selected_ship", selectedShipId)
            .putBoolean("ship_1_unlocked", unlockedShips[1])
            .putBoolean("ship_2_unlocked", unlockedShips[2])
            .apply();
    }

    public void convertSessionScore(int sessionScore) {
        int earned = sessionScore / 10; 
        if (earned > 0) {
            addCredits(earned);
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

    public int getCredits()                { return totalCredits; }
    public int getDisplayedCredits()       { return (int) displayedCredits; }
    public float getCreditFlash()          { return Math.max(0, creditFlashTimer); }
    public boolean isShipUnlocked(int id)  { return unlockedShips[id]; }
    public int getSelectedShipId()         { return selectedShipId; }
    public void selectShip(int id)         { selectedShipId = id; saveAll(); }
}
