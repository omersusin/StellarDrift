package com.stellardrift.game.world;

import android.graphics.Color;

public class ShipRegistry {

    public static final int STRIKER    = 0;
    public static final int JUGGERNAUT = 1;
    public static final int PHANTOM    = 2;
    public static final int SWARM      = 3;
    public static final int ECLIPSE    = 4;
    public static final int ZENITH     = 5;

    public static final int SHIP_COUNT = 6;

    private final ShipData[] ships = new ShipData[SHIP_COUNT];
    private int selectedShipId = STRIKER;

    public ShipRegistry() {
        ships[STRIKER]    = createStriker();
        ships[JUGGERNAUT] = createJuggernaut();
        ships[PHANTOM]    = createPhantom();
        ships[SWARM]      = createSwarm();
        ships[ECLIPSE]    = createEclipse();
        ships[ZENITH]     = createZenith();

        ShipPathBuilder.buildStrikerPaths(ships[STRIKER]);
        ShipPathBuilder.buildJuggernautPaths(ships[JUGGERNAUT]);
        ShipPathBuilder.buildPhantomPaths(ships[PHANTOM]);
        ShipPathBuilder.buildSwarmPaths(ships[SWARM]);
        ShipPathBuilder.buildEclipsePaths(ships[ECLIPSE]);
        ShipPathBuilder.buildZenithPaths(ships[ZENITH]);
    }

    private ShipData createStriker() {
        return new ShipData(
            STRIKER, "THE STRIKER", "Fast and deadly interceptor",
            1.12f, 4.0f, 1, 1.4f, 2, 0,
            new float[]{-22f, 22f}, new float[]{-5f, -5f},
            new float[]{-4f, 4f}, new float[]{26f, 26f}, new float[]{3.5f, 3.5f},
            Color.rgb(60, 85, 110), Color.rgb(35, 55, 80), Color.rgb(45, 70, 100),
            Color.rgb(80, 210, 255), Color.rgb(120, 230, 255), Color.rgb(140, 200, 255),
            Color.rgb(60, 180, 255), Color.rgb(100, 200, 255), 14f
        );
    }

    private ShipData createJuggernaut() {
        return new ShipData(
            JUGGERNAUT, "THE JUGGERNAUT", "Heavy armored bomber",
            0.92f, 1.8f, 3, 0.9f, 2, 2500,
            new float[]{-28f, 28f}, new float[]{-6f, -6f},
            new float[]{-8f, -3f, 3f, 8f}, new float[]{24f, 24f, 24f, 24f}, new float[]{2.5f, 2f, 2f, 2.5f},
            Color.rgb(75, 65, 70), Color.rgb(50, 42, 48), Color.rgb(85, 70, 60),
            Color.rgb(255, 185, 50), Color.rgb(255, 210, 80), Color.rgb(255, 130, 40),
            Color.rgb(255, 100, 30), Color.rgb(255, 150, 50), 18f
        );
    }

    private ShipData createPhantom() {
        return new ShipData(
            PHANTOM, "THE PHANTOM", "Alien tech — Triple burst",
            1.00f, 2.8f, 2, 1.15f, 3, 5000,
            new float[]{0f, -24f, 24f}, new float[]{-26f, -2f, -2f},
            new float[]{0f}, new float[]{20f}, new float[]{6f},
            Color.rgb(55, 35, 75), Color.rgb(35, 20, 55), Color.rgb(65, 40, 95),
            Color.rgb(100, 255, 160), Color.rgb(130, 255, 180), Color.rgb(60, 255, 120),
            Color.rgb(50, 230, 100), Color.rgb(80, 255, 130), 15f
        );
    }

    private ShipData createSwarm() {
        return new ShipData(
            SWARM, "THE SWARM", "Twin-hull carrier — Bullet rain",
            0.88f, 5.0f, 1, 1.0f, 4, 8000,
            new float[]{-12f, 12f, -20f, 20f}, new float[]{-24f, -24f, 0f, 0f},
            new float[]{-12f, -5f, 5f, 12f}, new float[]{22f, 23f, 23f, 22f}, new float[]{2.5f, 2f, 2f, 2.5f},
            Color.rgb(60, 75, 55), Color.rgb(38, 50, 32), Color.rgb(50, 65, 45),
            Color.rgb(140, 255, 90), Color.rgb(170, 255, 120), Color.rgb(90, 255, 70),
            Color.rgb(70, 200, 50), Color.rgb(110, 255, 70), 16f
        );
    }

    private ShipData createEclipse() {
        return new ShipData(
            ECLIPSE, "THE ECLIPSE", "Shadow hunter — Fast & piercing",
            1.18f, 3.2f, 2, 1.6f, 2, 12000,
            new float[]{-14f, 14f}, new float[]{-10f, -10f},
            new float[]{-6f, 6f}, new float[]{12f, 12f}, new float[]{3f, 3f},
            Color.rgb(45, 18, 25), Color.rgb(28, 10, 14), Color.rgb(55, 22, 30),
            Color.rgb(255, 45, 35), Color.rgb(255, 85, 65), Color.rgb(255, 55, 25),
            Color.rgb(210, 35, 20), Color.rgb(255, 65, 40), 13f
        );
    }

    private ShipData createZenith() {
        return new ShipData(
            ZENITH, "THE ZENITH", "Divine vessel — Penta spread",
            1.05f, 3.0f, 2, 1.2f, 5, 20000,
            new float[]{0f, -10f, 10f, -22f, 22f}, new float[]{-30f, -12f, -12f, -2f, -2f},
            new float[]{0f, -6f, 6f}, new float[]{24f, 22f, 22f}, new float[]{4f, 2.5f, 2.5f},
            Color.rgb(82, 72, 48), Color.rgb(55, 48, 28), Color.rgb(92, 82, 52),
            Color.rgb(255, 248, 195), Color.rgb(255, 255, 225), Color.rgb(255, 228, 135),
            Color.rgb(255, 210, 95), Color.rgb(255, 232, 115), 16f
        );
    }

    public ShipData getShip(int id) { return ships[id]; }
    public ShipData getSelectedShip() { return ships[selectedShipId]; }
    public int getSelectedId() { return selectedShipId; }
    public void selectShip(int id) { selectedShipId = id; }
    public ShipData[] getAllShips() { return ships; }
    public int getShipCount() { return SHIP_COUNT; }
}
