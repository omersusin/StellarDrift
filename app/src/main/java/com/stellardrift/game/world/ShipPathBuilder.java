package com.stellardrift.game.world;

import android.graphics.Path;
import android.graphics.RectF;

public class ShipPathBuilder {

    public static void buildStrikerPaths(ShipData ship) {
        ship.hullPath = new Path(); Path h = ship.hullPath;
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
        ship.detailPaths[0] = makeLine(0, -2, 0, 20);
        ship.detailPaths[1] = makeLine(10, 0, 20, 7);
        ship.detailPaths[2] = makeLine(-10, 0, -20, 7);

        ship.accentPath = new Path();
        ship.accentPath.moveTo(0, -28); ship.accentPath.lineTo(2, -23);
        ship.accentPath.lineTo(0, -18); ship.accentPath.lineTo(-2, -23); ship.accentPath.close();
    }

    public static void buildJuggernautPaths(ShipData ship) {
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
        ship.detailPaths[0] = makeLine(-12, -8, 12, -8);
        ship.detailPaths[1] = makeLine(-13, 4, 13, 4);
        ship.detailPaths[2] = makeLine(0, -10, 0, 18);
        ship.detailPaths[3] = makeLine(-8, -25, 8, -25);

        ship.accentPath = new Path();
        ship.accentPath.moveTo(-10, -24); ship.accentPath.lineTo(10, -24);
        ship.accentPath.lineTo(12, -22); ship.accentPath.lineTo(11, -20);
        ship.accentPath.lineTo(-11, -20); ship.accentPath.lineTo(-12, -22); ship.accentPath.close();
    }

    public static void buildPhantomPaths(ShipData ship) {
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

    public static void buildSwarmPaths(ShipData ship) {
        ship.hullPath = new Path(); Path h = ship.hullPath;
        h.moveTo(-12, -24); h.lineTo(-14, -20); h.lineTo(-15, -14); h.lineTo(-18, -4);
        h.lineTo(-20, 4); h.lineTo(-18, 12); h.lineTo(-15, 18); h.lineTo(-10, 22);
        h.lineTo(-5, 24); h.lineTo(0, 23); h.lineTo(5, 24); h.lineTo(10, 22);
        h.lineTo(15, 18); h.lineTo(18, 12); h.lineTo(20, 4); h.lineTo(18, -4);
        h.lineTo(15, -14); h.lineTo(14, -20); h.lineTo(12, -24); h.lineTo(9, -20);
        h.lineTo(7, -14); h.lineTo(5, -7); h.lineTo(-5, -7); h.lineTo(-7, -14);
        h.lineTo(-9, -20); h.close();

        ship.rightWingPath = new Path(); Path rw = ship.rightWingPath;
        rw.moveTo(20, 2); rw.lineTo(24, 0); rw.lineTo(30, 4); rw.lineTo(33, 8);
        rw.lineTo(31, 11); rw.lineTo(26, 12); rw.lineTo(20, 10); rw.close();

        ship.leftWingPath = new Path(); Path lw = ship.leftWingPath;
        lw.moveTo(-20, 2); lw.lineTo(-24, 0); lw.lineTo(-30, 4); lw.lineTo(-33, 8);
        lw.lineTo(-31, 11); lw.lineTo(-26, 12); lw.lineTo(-20, 10); lw.close();

        ship.cockpitPath = new Path(); Path cp = ship.cockpitPath;
        cp.moveTo(0, -6); cp.lineTo(3.5f, -2); cp.lineTo(3, 3); cp.lineTo(0, 5);
        cp.lineTo(-3, 3); cp.lineTo(-3.5f, -2); cp.close();

        ship.detailPaths = new Path[5];
        ship.detailPaths[0] = makeLine(-11, -22, -12, -10);
        ship.detailPaths[1] = makeLine(11, -22, 12, -10);
        ship.detailPaths[2] = makeLine(-4, -3, 4, -3);
        ship.detailPaths[3] = makeLine(-14, 4, -10, 18);
        ship.detailPaths[4] = makeLine(14, 4, 10, 18);

        ship.accentPath = new Path();
        ship.accentPath.moveTo(-5, -7); ship.accentPath.lineTo(5, -7);
        ship.accentPath.lineTo(4, -4); ship.accentPath.lineTo(-4, -4); ship.accentPath.close();
    }

    public static void buildEclipsePaths(ShipData ship) {
        ship.hullPath = new Path(); Path h = ship.hullPath;
        h.moveTo(0, -22); h.lineTo(4, -18); h.lineTo(8, -14); h.lineTo(14, -8);
        h.lineTo(20, -3); h.lineTo(26, 0); h.lineTo(30, 3); h.lineTo(31, 5);
        h.lineTo(28, 6); h.lineTo(24, 5); h.lineTo(18, 6); h.lineTo(14, 8);
        h.lineTo(10, 6); h.lineTo(6, 8); h.lineTo(3, 12); h.lineTo(1.5f, 16);
        h.lineTo(0, 17); h.lineTo(-1.5f, 16); h.lineTo(-3, 12); h.lineTo(-6, 8);
        h.lineTo(-10, 6); h.lineTo(-14, 8); h.lineTo(-18, 6); h.lineTo(-24, 5);
        h.lineTo(-28, 6); h.lineTo(-31, 5); h.lineTo(-30, 3); h.lineTo(-26, 0);
        h.lineTo(-20, -3); h.lineTo(-14, -8); h.lineTo(-8, -14); h.lineTo(-4, -18);
        h.close();

        ship.rightWingPath = new Path(); Path rw = ship.rightWingPath;
        rw.moveTo(30, 3); rw.lineTo(33, 1); rw.lineTo(34, 4); rw.lineTo(32, 7); rw.lineTo(29, 6); rw.close();

        ship.leftWingPath = new Path(); Path lw = ship.leftWingPath;
        lw.moveTo(-30, 3); lw.lineTo(-33, 1); lw.lineTo(-34, 4); lw.lineTo(-32, 7); lw.lineTo(-29, 6); lw.close();

        ship.cockpitPath = new Path(); Path cp = ship.cockpitPath;
        cp.moveTo(0, -17); cp.lineTo(2.5f, -12); cp.lineTo(2, -7); cp.lineTo(0, -5);
        cp.lineTo(-2, -7); cp.lineTo(-2.5f, -12); cp.close();

        ship.detailPaths = new Path[5];
        ship.detailPaths[0] = makeLine(0, -4, 0, 14);
        ship.detailPaths[1] = makeLine(8, -10, 22, -1);
        ship.detailPaths[2] = makeLine(-8, -10, -22, -1);
        ship.detailPaths[3] = makeLine(12, 0, 24, 5);
        ship.detailPaths[4] = makeLine(-12, 0, -24, 5);

        ship.accentPath = new Path();
        ship.accentPath.moveTo(0, -22); ship.accentPath.lineTo(3, -17);
        ship.accentPath.lineTo(2, -14); ship.accentPath.lineTo(0, -13);
        ship.accentPath.lineTo(-2, -14); ship.accentPath.lineTo(-3, -17); ship.accentPath.close();
    }

    public static void buildZenithPaths(ShipData ship) {
        ship.hullPath = new Path(); Path h = ship.hullPath;
        h.moveTo(0, -30); h.lineTo(2, -27); h.lineTo(4, -23); h.lineTo(6, -18);
        h.lineTo(8, -12); h.lineTo(9.5f, -5); h.lineTo(10, 0); h.lineTo(10, 5);
        h.lineTo(9.5f, 10); h.lineTo(8, 15); h.lineTo(6, 19); h.lineTo(4, 22);
        h.lineTo(2, 25); h.lineTo(0, 26); h.lineTo(-2, 25); h.lineTo(-4, 22);
        h.lineTo(-6, 19); h.lineTo(-8, 15); h.lineTo(-9.5f, 10); h.lineTo(-10, 5);
        h.lineTo(-10, 0); h.lineTo(-9.5f, -5); h.lineTo(-8, -12); h.lineTo(-6, -18);
        h.lineTo(-4, -23); h.lineTo(-2, -27); h.close();

        ship.rightWingPath = new Path(); Path rw = ship.rightWingPath;
        rw.moveTo(9.5f, -5); rw.quadTo(14, -10, 18, -6); rw.quadTo(22, -2, 23, 2);
        rw.quadTo(23, 6, 21, 9); rw.quadTo(17, 13, 12, 10); rw.quadTo(10, 8, 10, 5); rw.close();

        ship.leftWingPath = new Path(); Path lw = ship.leftWingPath;
        lw.moveTo(-9.5f, -5); lw.quadTo(-14, -10, -18, -6); lw.quadTo(-22, -2, -23, 2);
        lw.quadTo(-23, 6, -21, 9); lw.quadTo(-17, 13, -12, 10); lw.quadTo(-10, 8, -10, 5); lw.close();

        ship.cockpitPath = new Path(); Path cp = ship.cockpitPath;
        cp.moveTo(0, -22); cp.quadTo(3.5f, -17, 3, -11); cp.quadTo(2.5f, -7, 0, -5);
        cp.quadTo(-2.5f, -7, -3, -11); cp.quadTo(-3.5f, -17, 0, -22); cp.close();

        ship.detailPaths = new Path[6];
        ship.detailPaths[0] = makeLine(0, -30, 0, -23);
        ship.detailPaths[1] = makeLine(0, -4, 0, 22);

        ship.detailPaths[2] = new Path(); Path rod = ship.detailPaths[2];
        rod.moveTo(18, -4); rod.quadTo(22, -6, 26, -2); rod.quadTo(28, 2, 26, 6); rod.quadTo(22, 10, 18, 7); rod.close();

        ship.detailPaths[3] = new Path(); Path lod = ship.detailPaths[3];
        lod.moveTo(-18, -4); lod.quadTo(-22, -6, -26, -2); lod.quadTo(-28, 2, -26, 6); lod.quadTo(-22, 10, -18, 7); lod.close();

        ship.detailPaths[4] = new Path(); ship.detailPaths[4].moveTo(10, -2); ship.detailPaths[4].quadTo(15, -4, 20, 0);
        ship.detailPaths[5] = new Path(); ship.detailPaths[5].moveTo(-10, -2); ship.detailPaths[5].quadTo(-15, -4, -20, 0);

        ship.accentPath = new Path(); Path a = ship.accentPath;
        a.moveTo(0, -30); a.lineTo(3, -27); a.quadTo(4.5f, -25, 3, -23);
        a.lineTo(1.5f, -24); a.lineTo(0, -25); a.lineTo(-1.5f, -24);
        a.lineTo(-3, -23); a.quadTo(-4.5f, -25, -3, -27); a.close();
    }

    private static Path makeLine(float x1, float y1, float x2, float y2) {
        Path p = new Path(); p.moveTo(x1, y1); p.lineTo(x2, y2); return p;
    }
}
