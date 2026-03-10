package com.stellardrift.game.engine;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import com.stellardrift.game.util.Constants;

public class GameLoop extends Thread {

    private final SurfaceHolder holder;
    private final GameView view;
    private volatile boolean running;

    public GameLoop(SurfaceHolder holder, GameView view) {
        super("GameLoop");
        this.holder = holder;
        this.view = view;
        this.running = false;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        long start, elapsed, sleep;

        while (running) {
            Canvas canvas = null;
            start = System.currentTimeMillis();

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    synchronized (holder) {
                        view.updateGame();
                        view.drawGame(canvas);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas);
                    } catch (Exception ignored) {}
                }
            }

            elapsed = System.currentTimeMillis() - start;
            sleep = Constants.FRAME_PERIOD - elapsed;

            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
