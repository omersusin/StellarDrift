package com.stellardrift.game.render;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.stellardrift.game.util.Constants;
import com.stellardrift.game.world.Asteroid;
import com.stellardrift.game.world.GameWorld;
import com.stellardrift.game.world.Particle;
import com.stellardrift.game.world.StarDust;

public class Renderer {

    private Paint particlePaint;

    public Renderer() {
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setStyle(Paint.Style.FILL);
    }

    public void render(Canvas canvas, GameWorld world) {
        int state = world.getState();

        if (state == Constants.STATE_PLAYING ||
            state == Constants.STATE_GAME_OVER) {

            for (Asteroid a : world.getAsteroids())
                a.render(canvas);

            for (StarDust s : world.getStarDusts())
                s.render(canvas);

            for (Particle p : world.getParticles())
                p.render(canvas, particlePaint);

            world.getPlayer().render(canvas);
        }
    }
}
