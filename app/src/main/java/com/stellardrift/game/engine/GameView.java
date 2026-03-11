package com.stellardrift.game.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.stellardrift.game.util.Constants;
import com.stellardrift.game.world.GameWorld;
import com.stellardrift.game.render.SpaceBackground;
import com.stellardrift.game.render.Renderer;
import com.stellardrift.game.render.ShipRenderer;
import com.stellardrift.game.render.UIOverlay;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameLoop gameLoop;
    private GameWorld gameWorld;
    private SpaceBackground background;
    private Renderer renderer;
    private ShipRenderer shipRenderer;
    private UIOverlay uiOverlay;
    private Joystick joystick;

    private int screenW, screenH;

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
        if (shipRenderer == null) shipRenderer = new ShipRenderer();
        if (uiOverlay == null) uiOverlay = new UIOverlay(screenW, screenH);
        if (joystick == null) joystick = new Joystick(screenW);
        startLoop(holder);
    }

    @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int h2) { screenW = w; screenH = h2; }
    @Override public void surfaceDestroyed(SurfaceHolder holder) { stopLoop(); }

    private void startLoop(SurfaceHolder holder) {
        if (gameLoop == null || !gameLoop.isRunning()) { gameLoop = new GameLoop(holder, this); gameLoop.setRunning(true); gameLoop.start(); }
    }
    private void stopLoop() {
        if (gameLoop != null) { gameLoop.setRunning(false); boolean r = true; while (r) { try { gameLoop.join(1000); r = false; } catch (InterruptedException ignored) {} } gameLoop = null; }
    }
    public void resume() {} public void pause() { stopLoop(); }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float ex = event.getX(), ey = event.getY();
        
        // 1. Shop overlay aktifse önce o eventleri yakalar
        if (uiOverlay != null && uiOverlay.isShopVisible()) {
            uiOverlay.handleShopTouch(event.getAction(), ex, ey, gameWorld.getShipRegistry(), gameWorld.getEconomy());
            return true;
        }

        int state = gameWorld != null ? gameWorld.getState() : Constants.STATE_MENU;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (state == Constants.STATE_PLAYING) {
                    joystick.onTouchDown(ex, ey); 
                } else if (state == Constants.STATE_PAUSED) {
                    // handlePauseTap(ex, ey); 
                } else {
                    handleTap(ex, ey);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (state == Constants.STATE_PLAYING) joystick.onTouchMove(ex, ey);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                joystick.onTouchUp();
                break;
        }
        return true;
    }

    private void handleTap(float x, float y) {
        if (gameWorld == null || uiOverlay == null) return;
        int state = gameWorld.getState();
        if (state == Constants.STATE_MENU) {
            if (uiOverlay.isPlayHit(x, y)) { uiOverlay.resetGameOver(); gameWorld.startGame(); }
            else if (uiOverlay.isShopHit(x, y)) { uiOverlay.openShop(); }
            else if (uiOverlay.isSettingsHit(x, y)) gameWorld.openSettings();
        } else if (state == Constants.STATE_SETTINGS) {
            if (uiOverlay.isDiffHit(x, y)) gameWorld.cycleDifficulty();
            else if (uiOverlay.isSpeedHit(x, y)) gameWorld.cycleGameSpeed();
            else if (uiOverlay.isSoundHit(x, y)) gameWorld.toggleSound();
            else if (uiOverlay.isVibHit(x, y)) gameWorld.toggleVibration();
            else if (uiOverlay.isBackHit(x, y)) gameWorld.closeSettings();
        } else if (state == Constants.STATE_GAME_OVER) {
            if (uiOverlay.isRestartHit(x, y)) { uiOverlay.resetGameOver(); gameWorld.startGame(); }
            else { gameWorld.handleTap(); uiOverlay.resetGameOver(); }
        }
    }

    public void updateGame() {
        if (uiOverlay != null) uiOverlay.update(0.016f);
        if (background != null && gameWorld != null) {
            background.update(gameWorld.getDifficulty(), gameWorld.getTempoPhase(), 0.016f);
        }
        if (gameWorld != null) {
            gameWorld.update(joystick.getDirX(), joystick.getDirY(), joystick.getMagnitude());
        }
    }

    public void drawGame(Canvas canvas) {
        if (canvas == null) return;
        canvas.drawColor(Color.parseColor("#050510"));

        float sx = 0, sy = 0;
        if (gameWorld != null) { sx = gameWorld.getShakeX(); sy = gameWorld.getShakeY(); }
        if (sx != 0 || sy != 0) canvas.translate(sx, sy);
        
        if (background != null) background.render(canvas);
        
        if (renderer != null && gameWorld != null && shipRenderer != null) {
            // Renderer'ın içine gemi çizicisini de pass etmek iyi olabilir ama basit tutalım:
            renderer.render(canvas, gameWorld); // background effects, enemies
        }
        
        if (sx != 0 || sy != 0) canvas.translate(-sx, -sy);

        int state = gameWorld != null ? gameWorld.getState() : Constants.STATE_MENU;

        if (state == Constants.STATE_PLAYING) {
            if (joystick != null) joystick.render(canvas);
        }

        if (uiOverlay != null && gameWorld != null) {
            uiOverlay.renderFull(canvas, gameWorld, shipRenderer);
        }
    }
}
