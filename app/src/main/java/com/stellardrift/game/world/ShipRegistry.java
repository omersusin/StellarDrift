package com.stellardrift.game.world;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;

public class ShipRegistry {

    public static final int STRIKER    = 0;
    public static final int JUGGERNAUT = 1;
    public static final int PHANTOM    = 2;

    private final ShipData[] ships = new ShipData[3];
    private int selectedShipId = STRIKER;

    public ShipRegistry() {
        ships[STRIKER] = createStriker();
        ships[JUGGERNAUT] = createJuggernaut();
        ships[PHANTOM] = createPhantom();

        buildStrikerPaths(ships[STRIKER]);
        buildJuggernautPaths(ships[JUGGERNAUT]);
        buildPhantomPaths(ships[PHANTOM]);
    }

    private ShipData createStriker() {
        return new ShipData(
            STRIKER, "THE STRIKER", "Fast and deadly interceptor",
            1.3f, 4.0f, 1, 1.4f, 2, 0,
            new float[]{-10f, 10f}, new float[]{-8f, -8f},
            new float[]{-4f, 4f}, new float[]{26f, 26f}, new float[]{3.5f, 3.5f},
            Color.rgb(60, 85, 110), Color.rgb(35, 55, 80), Color.rgb(45, 70, 100),
            Color.rgb(80, 210, 255), Color.rgb(120, 230, 255), Color.rgb(140, 200, 255),
            Color.rgb(60, 180, 255), Color.rgb(100, 200, 255),
            14f
        );
    }

    private ShipData createJuggernaut() {
        return new ShipData(
            JUGGERNAUT, "THE JUGGERNAUT", "Heavy armored bomber",
            0.75f, 1.8f, 3, 0.8f, 2, 2500,
            new float[]{-16f, 16f}, new float[]{-12f, -12f},
            new float[]{-8f, -3f, 3f, 8f}, new float[]{24f, 24f, 24f, 24f}, new float[]{2.5f, 2f, 2f, 2.5f},
            Color.rgb(75, 65, 70), Color.rgb(50, 42, 48), Color.rgb(85, 70, 60),
            Color.rgb(255, 185, 50), Color.rgb(255, 210, 80), Color.rgb(255, 130, 40),
            Color.rgb(255, 100, 30), Color.rgb(255, 150, 50),
            18f
        );
    }

    private ShipData createPhantom() {
        return new ShipData(
            PHANTOM, "THE PHANTOM", "Alien tech — Triple burst",
            1.0f, 2.8f, 2, 1.15f, 3, 5000,
            new float[]{0f, -18f, 18f}, new float[]{-26f, -4f, -4f},
            new float[]{0f}, new float[]{20f}, new float[]{6f},
            Color.rgb(55, 35, 75), Color.rgb(35, 20, 55), Color.rgb(65, 40, 95),
            Color.rgb(100, 255, 160), Color.rgb(130, 255, 180), Color.rgb(60, 255, 120),
            Color.rgb(50, 230, 100), Color.rgb(80, 255, 130),
            15f
        );
    }

    private void buildStrikerPaths(ShipData ship) {
        ship.hullPath = new Path();
        Path h = ship.hullPath;
        h.moveTo(0, -28); h.lineTo(3, -24); h.lineTo(5, -20); h.lineTo(6.5f, -14);
        h.lineTo(7.5f, -8); h.lineTo(8, -3); h.lineTo(8.5f, 2); h.lineTo(8, 8);
        h.lineTo(7, 14); h.lineTo(6.5f, 18); h.lineTo(6, 22); h.lineTo(6.5f, 25);
        h.lineTo(5, 28); h.lineTo(3, 26); h.lineTo(1, 24); h.lineTo(0, 25);
        h.lineTo(-1, 24); h.lineTo(-3, 26); h.lineTo(-5, 28); h.lineTo(-6.5f, 25);
        h.lineTo(-6, 22); h.lineTo(-6.5f, 18); h.lineTo(-7, 14); h.lineTo(-8, 8);
        h.lineTo(-8.5f, 2); h.lineTo(-8, -3); h.lineTo(-7.5f, -8); h.lineTo(-6.5f, -14);
        h.lineTo(-5, -20); h.lineTo(-3, -24); h.close();

        ship.rightWingPath = new Path(); Path rw = ship.rightWingPath;
        rw.moveTo(8, -3); rw.lineTo(10, -4); rw.lineTo(16, 0); rw.lineTo(22, 5);
        rw.lineTo(25, 8); rw.lineTo(23, 10); rw.lineTo(18, 13); rw.lineTo(12, 12);
        rw.lineTo(8.5f, 8); rw.close();

        ship.leftWingPath = new Path(); Path lw = ship.leftWingPath;
        lw.moveTo(-8, -3); lw.lineTo(-10, -4); lw.lineTo(-16, 0); lw.lineTo(-22, 5);
        lw.lineTo(-25, 8); lw.lineTo(-23, 10); lw.lineTo(-18, 13); lw.lineTo(-12, 12);
        lw.lineTo(-8.5f, 8); lw.close();

        ship.cockpitPath = new Path(); Path cp = ship.cockpitPath;
        cp.moveTo(0, -20); cp.lineTo(4, -12); cp.lineTo(3.5f, -6); cp.lineTo(0, -3);
        cp.lineTo(-3.5f, -6); cp.lineTo(-4, -12); cp.close();

        ship.detailPaths = new Path[3];
        ship.detailPaths[0] = new Path(); ship.detailPaths[0].moveTo(0, -2); ship.detailPaths[0].lineTo(0, 20);
        ship.detailPaths[1] = new Path(); ship.detailPaths[1].moveTo(10, 0); ship.detailPaths[1].lineTo(20, 7);
        ship.detailPaths[2] = new Path(); ship.detailPaths[2].moveTo(-10, 0); ship.detailPaths[2].lineTo(-20, 7);

        ship.accentPath = new Path();
        ship.accentPath.moveTo(0, -28); ship.accentPath.lineTo(2, -23);
        ship.accentPath.lineTo(0, -18); ship.accentPath.lineTo(-2, -23); ship.accentPath.close();
    }

    private void buildJuggernautPaths(ShipData ship) {
        ship.hullPath = new Path(); Path h = ship.hullPath;
        h.moveTo(-11, -24); h.lineTo(-6, -26); h.lineTo(6, -26); h.lineTo(11, -24);
        h.lineTo(13, -20); h.lineTo(14, -14); h.lineTo(15, -6); h.lineTo(15, 0);
        h.lineTo(14.5f, 6); h.lineTo(14, 12); h.lineTo(13, 16); h.lineTo(12, 20);
        h.lineTo(11, 23); h.lineTo(9, 24); h.lineTo(9, 26); h.lineTo(6, 26);
        h.lineTo(5, 24); h.lineTo(4, 26); h.lineTo(1, 26); h.lineTo(0, 24);
        h.lineTo(-1, 26); h.lineTo(-4, 26); h.lineTo(-5, 24); h.lineTo(-6, 26);
        h.lineTo(-9, 26); h.lineTo(-9, 24); h.lineTo(-11, 23); h.lineTo(-12, 20);
        h.lineTo(-13, 16); h.lineTo(-14, 12); h.lineTo(-14.5f, 6); h.lineTo(-15, 0);
        h.lineTo(-14, -6); h.lineTo(-13, -14); h.lineTo(-14, -20); h.close();

        ship.rightWingPath = new Path(); Path rw = ship.rightWingPath;
        rw.moveTo(15, -6); rw.lineTo(18, -8); rw.lineTo(24, -4); rw.lineTo(30, 0);
        rw.lineTo(31, 3); rw.lineTo(29, 6); rw.lineTo(24, 8); rw.lineTo(18, 10);
        rw.lineTo(15, 6); rw.close();

        ship.leftWingPath = new Path(); Path lw = ship.leftWingPath;
        lw.moveTo(-15, -6); lw.lineTo(-18, -8); lw.lineTo(-24, -4); lw.lineTo(-30, 0);
        lw.lineTo(-31, 3); lw.lineTo(-29, 6); lw.lineTo(-24, 8); lw.lineTo(-18, 10);
        lw.lineTo(-15, 6); lw.close();

        ship.cockpitPath = new Path(); Path cp = ship.cockpitPath;
        cp.moveTo(-7, -20); cp.lineTo(7, -20); cp.lineTo(8, -16); cp.lineTo(6, -12);
        cp.lineTo(-6, -12); cp.lineTo(-8, -16); cp.close();

        ship.detailPaths = new Path[4];
        ship.detailPaths[0] = new Path(); ship.detailPaths[0].moveTo(-12, -8); ship.detailPaths[0].lineTo(12, -8);
        ship.detailPaths[1] = new Path(); ship.detailPaths[1].moveTo(-13, 4); ship.detailPaths[1].lineTo(13, 4);
        ship.detailPaths[2] = new Path(); ship.detailPaths[2].moveTo(0, -10); ship.detailPaths[2].lineTo(0, 18);
        ship.detailPaths[3] = new Path(); ship.detailPaths[3].moveTo(-8, -25); ship.detailPaths[3].lineTo(8, -25);

        ship.accentPath = new Path();
        ship.accentPath.moveTo(-10, -24); ship.accentPath.lineTo(10, -24);
        ship.accentPath.lineTo(12, -22); ship.accentPath.lineTo(11, -20);
        ship.accentPath.lineTo(-11, -20); ship.accentPath.lineTo(-12, -22); ship.accentPath.close();
    }

    private void buildPhantomPaths(ShipData ship) {
        ship.hullPath = new Path(); Path h = ship.hullPath;
        h.moveTo(0, -26); h.quadTo(4, -24, 7, -20); h.quadTo(10, -16, 12, -10);
        h.quadTo(14, -4, 13, 2); h.quadTo(12, 8, 10, 12); h.quadTo(8, 16, 6, 18);
        h.quadTo(3, 22, 0, 20); h.quadTo(-3, 22, -6, 18); h.quadTo(-8, 16, -10, 12);
        h.quadTo(-12, 8, -13, 2); h.quadTo(-14, -4, -12, -10); h.quadTo(-10, -16, -7, -20);
        h.quadTo(-4, -24, 0, -26); h.close();

        ship.rightWingPath = new Path(); Path rw = ship.rightWingPath;
        rw.moveTo(12, -10); rw.quadTo(16, -12, 20, -8); rw.quadTo(24, -4, 27, 0);
        rw.quadTo(28, 3, 26, 6); rw.quadTo(22, 10, 16, 8); rw.quadTo(13, 6, 12, 2); rw.close();

        ship.leftWingPath = new Path(); Path lw = ship.leftWingPath;
        lw.moveTo(-12, -10); lw.quadTo(-16, -12, -20, -8); lw.quadTo(-24, -4, -27, 0);
        lw.quadTo(-28, 3, -26, 6); lw.quadTo(-22, 10, -16, 8); lw.quadTo(-13, 6, -12, 2); lw.close();

        ship.cockpitPath = new Path();
        ship.cockpitPath.addOval(new RectF(-5, -16, 5, -6), Path.Direction.CW);

        ship.detailPaths = new Path[4];
        ship.detailPaths[0] = new Path(); ship.detailPaths[0].moveTo(0, -26); ship.detailPaths[0].quadTo(1, -22, 0, -17);
        ship.detailPaths[1] = new Path(); ship.detailPaths[1].moveTo(5, -11); ship.detailPaths[1].quadTo(9, -10, 14, -8);
        ship.detailPaths[2] = new Path(); ship.detailPaths[2].moveTo(-5, -11); ship.detailPaths[2].quadTo(-9, -10, -14, -8);
        ship.detailPaths[3] = new Path(); ship.detailPaths[3].moveTo(0, -5); ship.detailPaths[3].quadTo(2, 6, 0, 16);

        ship.accentPath = new Path();
        ship.accentPath.addOval(new RectF(-7, -18, 7, -4), Path.Direction.CW);
    }

    public ShipData getShip(int id)       { return ships[id]; }
    public ShipData getSelectedShip()     { return ships[selectedShipId]; }
    public int getSelectedId()            { return selectedShipId; }
    public void selectShip(int id)        { selectedShipId = id; }
    public ShipData[] getAllShips()       { return ships; }
    public int getShipCount()             { return ships.length; }
}
