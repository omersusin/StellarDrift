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
import com.stellardrift.game.util.SoundManager;
import com.stellardrift.game.util.VibrationManager;
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
    
    private SoundManager soundManager;
    private VibrationManager vibrationManager;

    private int screenW, screenH;
    private Paint vignettePaint, riskBorderPaint;
    private RadialGradient vignetteNormal, vignetteDanger;
    private float currentDanger;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        vignettePaint = new Paint();
        riskBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG); riskBorderPaint.setStyle(Paint.Style.STROKE); riskBorderPaint.setColor(0xFFFFD740);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenW = getWidth(); screenH = getHeight();
        
        soundManager = new SoundManager();
        vibrationManager = new VibrationManager(getContext());
        
        if (background == null) background = new SpaceBackground(screenW, screenH);
        if (gameWorld == null) gameWorld = new GameWorld(screenW, screenH, getContext());
        if (renderer == null) renderer = new Renderer();
        if (shipRenderer == null) shipRenderer = new ShipRenderer();
        if (uiOverlay == null) {
            uiOverlay = new UIOverlay(screenW, screenH);
            uiOverlay.initPrefs(gameWorld.getSettings(), gameWorld.getShipRegistry());
            uiOverlay.setFuelSystem(gameWorld.getFuelSystem());
        }
        
        soundManager.setEnabled(true);
        vibrationManager.setEnabled(true);
        soundManager.startDrone();
        gameWorld.setAudioEngine(soundManager, vibrationManager);
        
        if (joystick == null) joystick = new Joystick(screenW);
        initVignette();
        startLoop(holder);
    }

    @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int h2) { screenW = w; screenH = h2; }
    
    @Override 
    public void surfaceDestroyed(SurfaceHolder holder) { 
        stopLoop(); 
        if (soundManager != null) soundManager.release();
        if (vibrationManager != null) vibrationManager.cancel();
    }

    private void initVignette() {
        float cx = screenW / 2f, cy = screenH / 2f, r = (float) Math.hypot(cx, cy);
        vignetteNormal = new RadialGradient(cx, cy, r, new int[]{0x00000000, 0x00000000, 0x40000000}, new float[]{0f, 0.6f, 1f}, Shader.TileMode.CLAMP);
        vignetteDanger = new RadialGradient(cx, cy, r, new int[]{0x00000000, 0x00000000, 0x60FF0000}, new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
    }

    private void startLoop(SurfaceHolder holder) {
        if (gameLoop == null || !gameLoop.isRunning()) { gameLoop = new GameLoop(holder, this); gameLoop.setRunning(true); gameLoop.start(); }
    }
    private void stopLoop() {
        if (gameLoop != null) { gameLoop.setRunning(false); boolean r = true; while (r) { try { gameLoop.join(1000); r = false; } catch (InterruptedException ignored) {} } gameLoop = null; }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float ex = event.getX(), ey = event.getY();
        
        if (uiOverlay != null && uiOverlay.isShopVisible()) {
            uiOverlay.handleShopTouch(event.getAction(), ex, ey, gameWorld.getShipRegistry(), gameWorld.getEconomy());
            return true;
        }

        int state = gameWorld != null ? gameWorld.getState() : Constants.STATE_MENU;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (state == Constants.STATE_PLAYING) { joystick.onTouchDown(ex, ey); }
                else if (state == Constants.STATE_SETTINGS) { uiOverlay.handleSettingsTouch(ex, ey, gameWorld); }
                else if (state == Constants.STATE_MENU || state == Constants.STATE_GAME_OVER) { handleTap(ex, ey, state); }
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

    private void handleTap(float x, float y, int state) {
        if (gameWorld == null || uiOverlay == null) return;
        
        if (state == Constants.STATE_MENU) {
            float cx = screenW / 2f, bw = screenW * 0.65f, bh = screenH * 0.075f;
            RectF playBtn = new RectF(cx - bw/2, screenH * 0.40f, cx + bw/2, screenH * 0.40f + bh);
            RectF shopBtn = new RectF(cx - bw/2, screenH * 0.50f, cx + bw/2, screenH * 0.50f + bh);
            RectF settBtn = new RectF(cx - bw/2, screenH * 0.60f, cx + bw/2, screenH * 0.60f + bh);

            if (playBtn.contains(x,y)) { uiOverlay.resetGameOver(); gameWorld.startGame(); }
            else if (shopBtn.contains(x,y)) { uiOverlay.openShop(); }
            else if (settBtn.contains(x,y)) { gameWorld.openSettings(); }
        } else if (state == Constants.STATE_GAME_OVER) {
            float cx = screenW / 2f, bw = screenW * 0.65f, bh = screenH * 0.075f;
            RectF restartBtn = new RectF(cx - bw/2, screenH * 0.76f, cx + bw/2, screenH * 0.76f + bh);
            
            if (restartBtn.contains(x, y)) { uiOverlay.resetGameOver(); gameWorld.startGame(); }
            else { gameWorld.handleTap(); uiOverlay.resetGameOver(); }
        }
    }

    public void updateGame() {
        if (uiOverlay != null) uiOverlay.update(0.016f);
        if (background != null && gameWorld != null) background.update(gameWorld.getDifficulty(), gameWorld.getTempoPhase(), 0.016f);
        if (gameWorld != null) {
            uiOverlay.setState(gameWorld.getState());
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

        if (gameWorld != null && gameWorld.getPlasmaCore() != null) {
            gameWorld.getPlasmaCore().drawOverchargeScreenEffect(canvas, screenW, screenH);
        }
        
        if (renderer != null && gameWorld != null && shipRenderer != null) renderer.render(canvas, gameWorld);

        if (gameWorld != null && gameWorld.getPlasmaCore() != null) {
            gameWorld.getPlasmaCore().draw(canvas);
        }
        
        if (sx != 0 || sy != 0) canvas.translate(-sx, -sy);

        drawVignette(canvas);
        drawRiskBorder(canvas);

        int state = gameWorld != null ? gameWorld.getState() : Constants.STATE_MENU;

        if (state == Constants.STATE_PLAYING) {
            if (joystick != null) joystick.render(canvas);
            if (gameWorld != null && gameWorld.getPlasmaCore() != null) {
                gameWorld.getPlasmaCore().drawOverchargeHUD(canvas, screenW);
            }
        }

        if (uiOverlay != null && gameWorld != null) {
            uiOverlay.renderFull(canvas, gameWorld, shipRenderer);
        }
    }

    private void drawVignette(Canvas canvas) {
        if (gameWorld == null || (gameWorld.getState() != Constants.STATE_PLAYING && gameWorld.getState() != Constants.STATE_PAUSED)) {
            vignettePaint.setShader(vignetteNormal); vignettePaint.setAlpha(255);
            canvas.drawRect(0, 0, screenW, screenH, vignettePaint); return;
        }
        float danger = gameWorld.getDangerLevel();
        currentDanger += (danger - currentDanger) * 0.1f;
        if (currentDanger > 0.3f) { vignettePaint.setShader(vignetteDanger); vignettePaint.setAlpha((int)(currentDanger * 255)); }
        else { vignettePaint.setShader(vignetteNormal); vignettePaint.setAlpha(255); }
        canvas.drawRect(0, 0, screenW, screenH, vignettePaint);
    }

    private void drawRiskBorder(Canvas canvas) {
        if (gameWorld == null || !gameWorld.isRiskWindowActive()) return;
        float timer = gameWorld.getRiskWindowTimer();
        float progress = timer / (float) Constants.RISK_WINDOW_DURATION;
        float blink = progress < 0.25f ? (float)(Math.sin(timer * 0.5) * 0.4 + 0.6) : 1f;
        int alpha = (int)(120 * progress * blink);
        riskBorderPaint.setAlpha(alpha); riskBorderPaint.setStrokeWidth(3f);
        canvas.drawLine(0, 0, screenW, 0, riskBorderPaint); canvas.drawLine(0, screenH, screenW, screenH, riskBorderPaint);
        canvas.drawLine(0, 0, 0, screenH, riskBorderPaint); canvas.drawLine(screenW, 0, screenW, screenH, riskBorderPaint);
    }
}
