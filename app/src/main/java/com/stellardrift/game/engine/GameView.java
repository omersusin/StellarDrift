package com.stellardrift.game.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.stellardrift.game.util.Constants;
import com.stellardrift.game.world.GameWorld;
import com.stellardrift.game.render.SpaceBackground;
import com.stellardrift.game.render.Renderer;
import com.stellardrift.game.render.UIOverlay;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameLoop gameLoop;
    private GameWorld gameWorld;
    private SpaceBackground background;
    private Renderer renderer;
    private UIOverlay uiOverlay;

    private int screenW, screenH;
    private float touchX = -1;
    private boolean touching = false;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenW = getWidth(); screenH = getHeight();
        if (background == null) background = new SpaceBackground(screenW, screenH);
        if (gameWorld == null) gameWorld = new GameWorld(screenW, screenH, getContext());
        if (renderer == null) renderer = new Renderer();
        if (uiOverlay == null) uiOverlay = new UIOverlay(screenW, screenH);
        startLoop(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder h, int f, int w, int h2) {
        screenW = w; screenH = h2;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { stopLoop(); }

    private void startLoop(SurfaceHolder holder) {
        if (gameLoop == null || !gameLoop.isRunning()) {
            gameLoop = new GameLoop(holder, this);
            gameLoop.setRunning(true);
            gameLoop.start();
        }
    }

    private void stopLoop() {
        if (gameLoop != null) {
            gameLoop.setRunning(false);
            boolean retry = true;
            while (retry) {
                try { gameLoop.join(1000); retry = false; }
                catch (InterruptedException ignored) {}
            }
            gameLoop = null;
        }
    }

    public void resume() {}
    public void pause() { stopLoop(); }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float ex = event.getX(), ey = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touching = true; touchX = ex;
                handleTap(ex, ey);
                break;
            case MotionEvent.ACTION_MOVE:
                touchX = ex;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touching = false; touchX = -1;
                break;
        }
        return true;
    }

    private void handleTap(float x, float y) {
        if (gameWorld == null || uiOverlay == null) return;
        int state = gameWorld.getState();

        if (state == Constants.STATE_MENU) {
            if (uiOverlay.isPlayHit(x, y)) gameWorld.startGame();
            else if (uiOverlay.isSettingsHit(x, y)) gameWorld.openSettings();
        } else if (state == Constants.STATE_SETTINGS) {
            if (uiOverlay.isDiffHit(x, y)) gameWorld.cycleDifficulty();
            else if (uiOverlay.isSoundHit(x, y)) gameWorld.toggleSound();
            else if (uiOverlay.isVibHit(x, y)) gameWorld.toggleVibration();
            else if (uiOverlay.isBackHit(x, y)) gameWorld.closeSettings();
        } else if (state == Constants.STATE_GAME_OVER) {
            if (uiOverlay.isRestartHit(x, y)) gameWorld.startGame();
            else gameWorld.handleTap();
        }
    }

    public void updateGame() {
        if (background != null)
            background.update(gameWorld != null ? gameWorld.getDifficulty() : 1f);
        if (gameWorld != null)
            gameWorld.update(touchX, touching);
    }

    public void drawGame(Canvas canvas) {
        if (canvas == null) return;
        canvas.drawColor(Color.parseColor("#050510"));

        // Screen shake
        float sx = 0, sy = 0;
        if (gameWorld != null) {
            sx = gameWorld.getShakeX();
            sy = gameWorld.getShakeY();
        }
        if (sx != 0 || sy != 0) canvas.translate(sx, sy);

        if (background != null) background.render(canvas);
        if (renderer != null && gameWorld != null) renderer.render(canvas, gameWorld);

        if (sx != 0 || sy != 0) canvas.translate(-sx, -sy);

        if (uiOverlay != null && gameWorld != null)
            uiOverlay.renderFull(canvas, gameWorld);
    }
}
