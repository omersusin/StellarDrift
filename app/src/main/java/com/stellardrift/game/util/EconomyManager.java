package com.stellardrift.game.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.stellardrift.game.world.ShipData;

public class EconomyManager {

    private final SharedPreferences prefs;
    private int totalCredits;
    private int selectedShipId;
    private float displayedCredits;
    private float creditFlashTimer = 0f;

    private boolean doubleActive = false;
    private final int shipCount;
    private final boolean[] unlockedShips;
    private final int[][] upgradeLevels;

    public EconomyManager(Context context, int shipCount) {
        this.shipCount = shipCount;
        prefs = context.getSharedPreferences("stellar_drift_eco", Context.MODE_PRIVATE);

        totalCredits = prefs.getInt("credits", 0);
        selectedShipId = prefs.getInt("selected_ship", 0);
        displayedCredits = totalCredits;

        unlockedShips = new boolean[shipCount];
        unlockedShips[0] = true; 

        for (int i = 1; i < shipCount; i++) {
            unlockedShips[i] = prefs.getBoolean("ship_unlocked_" + i, false);
        }

        upgradeLevels = new int[shipCount][ShipData.STAT_COUNT];
        for (int s = 0; s < shipCount; s++) {
            for (int st = 0; st < ShipData.STAT_COUNT; st++) {
                upgradeLevels[s][st] = prefs.getInt("ship_" + s + "_stat_" + st, 0);
            }
        }
    }

    public void saveAll() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("credits", totalCredits);
        editor.putInt("selected_ship", selectedShipId);

        for (int i = 0; i < shipCount; i++) {
            editor.putBoolean("ship_unlocked_" + i, unlockedShips[i]);
            for (int st = 0; st < ShipData.STAT_COUNT; st++) {
                editor.putInt("ship_" + i + "_stat_" + st, upgradeLevels[i][st]);
            }
        }
        editor.commit(); // BUG FIX: Synchronous save ensures data isn't lost
    }

    public boolean purchaseShip(int shipId, int price) {
        if (shipId < 0 || shipId >= shipCount) return false;
        if (unlockedShips[shipId]) return false;           
        if (totalCredits < price) return false;             

        totalCredits -= price;
        unlockedShips[shipId] = true;
        saveAll();
        return true;
    }

    public boolean purchaseUpgrade(int shipId, int statIndex) {
        if (shipId < 0 || shipId >= shipCount || !unlockedShips[shipId] || statIndex < 0 || statIndex >= ShipData.STAT_COUNT) return false;

        int currentLevel = upgradeLevels[shipId][statIndex];
        if (currentLevel >= ShipData.MAX_UPGRADE_LEVEL) return false;

        int cost = ShipData.UPGRADE_PRICES[currentLevel];
        if (totalCredits < cost) return false;

        totalCredits -= cost;
        upgradeLevels[shipId][statIndex] = currentLevel + 1;
        saveAll();
        return true;
    }

    public void addCredits(int amount) {
        int finalAmount = doubleActive ? amount * 2 : amount;
        totalCredits += finalAmount;
        creditFlashTimer = 0.3f;
    }

    public void syncUpgradesToShipData(ShipData[] ships) {
        for (int s = 0; s < Math.min(shipCount, ships.length); s++) {
            ships[s].unlocked = unlockedShips[s];
            for (int st = 0; st < ShipData.STAT_COUNT; st++) {
                ships[s].setUpgradeLevel(st, upgradeLevels[s][st]);
            }
        }
    }

    public void setDoubleActive(boolean active) { doubleActive = active; }
    public boolean isDoubleActive() { return doubleActive; }

    public void convertSessionScore(int score) {
        int earned = score / 10;
        addCredits(earned);
        saveAll();
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
    public boolean isShipUnlocked(int id)  { return id >= 0 && id < shipCount && unlockedShips[id]; }
    public int getSelectedShipId()         { return selectedShipId; }
    public int getUpgradeLevel(int ship, int stat) { return upgradeLevels[ship][stat]; }

    public void selectShip(int id) {
        if (id >= 0 && id < shipCount && unlockedShips[id]) {
            selectedShipId = id;
            saveAll();
        }
    }
}
