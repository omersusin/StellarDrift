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

    private Paint pauseDimPaint, pauseTitlePaint, pauseBtnPaint, pauseBtnOutPaint, pauseBtnTextPaint;
    private RectF pauseResumeBtn, pauseQuitBtn, pauseBtn;

    private Paint transitionPaint;
    private Paint tutorialPaint, tutorialBgPaint;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        vignettePaint = new Paint();
        riskBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG); riskBorderPaint.setStyle(Paint.Style.STROKE); riskBorderPaint.setColor(0xFFFFD740);
        transitionPaint = new Paint(); transitionPaint.setColor(Color.BLACK);

        pauseDimPaint = new Paint(); pauseDimPaint.setColor(0xCC050510);
        pauseTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG); pauseTitlePaint.setColor(0xFF00E5FF); pauseTitlePaint.setTextAlign(Paint.Align.CENTER); pauseTitlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        pauseBtnPaint = new Paint(Paint.ANTI_ALIAS_FLAG); pauseBtnPaint.setStyle(Paint.Style.FILL); pauseBtnPaint.setColor(0x33FFFFFF);
        pauseBtnOutPaint = new Paint(Paint.ANTI_ALIAS_FLAG); pauseBtnOutPaint.setStyle(Paint.Style.STROKE); pauseBtnOutPaint.setStrokeWidth(2f); pauseBtnOutPaint.setColor(0xFF00E5FF); pauseBtnOutPaint.setAlpha(120);
        pauseBtnTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG); pauseBtnTextPaint.setColor(Color.WHITE); pauseBtnTextPaint.setTextAlign(Paint.Align.CENTER); pauseBtnTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        tutorialPaint = new Paint(Paint.ANTI_ALIAS_FLAG); tutorialPaint.setTextAlign(Paint.Align.CENTER);
        tutorialBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG); tutorialBgPaint.setColor(0x60000000);
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
            uiOverlay.initPrefs(gameWorld.getSettings(), gameWorld.getShipRegistry(), gameWorld.getEconomy(), shipRenderer, soundManager, vibrationManager);
            uiOverlay.setFuelSystem(gameWorld.getFuelSystem());
        }
        
        soundManager.setEnabled(uiOverlay.isSoundEnabled());
        vibrationManager.setEnabled(uiOverlay.isVibrationEnabled());
        if (soundManager.isEnabled()) soundManager.startDrone();
        
        gameWorld.setAudioEngine(soundManager, vibrationManager);
        
        if (joystick == null) joystick = new Joystick(screenW);
        initVignette();
        initPauseUI();
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

    private void initPauseUI() {
        float cx = screenW / 2f, bw = screenW * 0.5f, bh = screenH * 0.06f;
        pauseBtn = new RectF(screenW - screenW * 0.14f, screenH * 0.015f, screenW - screenW * 0.02f, screenH * 0.06f);
        pauseResumeBtn = new RectF(cx - bw/2, screenH * 0.48f, cx + bw/2, screenH * 0.48f + bh);
        pauseQuitBtn = new RectF(cx - bw/2, screenH * 0.57f, cx + bw/2, screenH * 0.57f + bh);
        pauseTitlePaint.setTextSize(screenW * 0.07f);
        pauseBtnTextPaint.setTextSize(screenW * 0.04f);
    }

    private void startLoop(SurfaceHolder holder) {
        if (gameLoop == null || !gameLoop.isRunning()) { gameLoop = new GameLoop(holder, this); gameLoop.setRunning(true); gameLoop.start(); }
    }
    private void stopLoop() {
        if (gameLoop != null) { gameLoop.setRunning(false); boolean r = true; while (r) { try { gameLoop.join(1000); r = false; } catch (InterruptedException ignored) {} } gameLoop = null; }
    }
    
    // HATA ÇÖZÜMÜ: EXPLICIT OLARAK RESUME/PAUSE METODLARI
    public void resume() { if (soundManager != null && soundManager.isEnabled()) soundManager.startDrone(); } 
    public void pause() { stopLoop(); if (soundManager != null) soundManager.stopDrone(); }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float ex = event.getX(), ey = event.getY();
        
        if (uiOverlay != null && uiOverlay.isShopVisible()) {
            uiOverlay.handleShopTouch(event.getAction(), ex, ey, gameWorld.getShipRegistry(), gameWorld.getEconomy(), soundManager, vibrationManager);
            return true;
        }

        int state = gameWorld != null ? gameWorld.getState() : Constants.STATE_MENU;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (state == Constants.STATE_PLAYING) {
                    if (pauseBtn != null && pauseBtn.contains(ex, ey)) { gameWorld.pauseGame(); if(soundManager!=null) soundManager.playMenuClick(); } 
                    else { joystick.onTouchDown(ex, ey); }
                } else if (state == Constants.STATE_PAUSED) {
                    handlePauseTap(ex, ey);
                } else if (state == Constants.STATE_SETTINGS) { 
                    uiOverlay.handleSettingsTouch(ex, ey, gameWorld, soundManager, vibrationManager); 
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

    private void handlePauseTap(float x, float y) {
        if (gameWorld == null) return;
        if (pauseResumeBtn != null && pauseResumeBtn.contains(x, y)) { gameWorld.resumeGame(); if(soundManager!=null) soundManager.playMenuClick(); } 
        else if (pauseQuitBtn != null && pauseQuitBtn.contains(x, y)) { gameWorld.quitToMenu(); if(soundManager!=null) soundManager.playMenuClick(); }
    }

    private void handleTap(float x, float y) {
        if (gameWorld == null || uiOverlay == null) return;
        int state = gameWorld.getState();
        if (state == Constants.STATE_MENU) {
            if (uiOverlay.isPlayHit(x, y)) { uiOverlay.resetGameOver(); gameWorld.startGame(); if(soundManager!=null) soundManager.playMenuClick(); }
            else if (uiOverlay.isShopHit(x, y)) { uiOverlay.openShop(); if(soundManager!=null) soundManager.playMenuClick(); }
            else if (uiOverlay.isSettingsHit(x, y)) { gameWorld.openSettings(); if(soundManager!=null) soundManager.playMenuClick(); }
        } else if (state == Constants.STATE_GAME_OVER) {
            if (uiOverlay.isRestartHit(x, y)) { uiOverlay.resetGameOver(); gameWorld.startGame(); if(soundManager!=null) soundManager.playMenuClick(); }
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
            drawPauseButton(canvas);
            drawTutorial(canvas);
        }

        if (state == Constants.STATE_PAUSED) {
            drawPauseScreen(canvas);
        }

        if (uiOverlay != null && gameWorld != null) {
            uiOverlay.renderFull(canvas, gameWorld, shipRenderer);
        }

        drawTransition(canvas);
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

    private void drawPauseButton(Canvas canvas) {
        pauseBtnPaint.setColor(0x25FFFFFF);
        canvas.drawRoundRect(pauseBtn, 8, 8, pauseBtnPaint);
        pauseBtnTextPaint.setTextSize(screenW * 0.03f); pauseBtnTextPaint.setAlpha(150);
        canvas.drawText("II", pauseBtn.centerX(), pauseBtn.centerY() + screenW * 0.01f, pauseBtnTextPaint);
    }

    private void drawPauseScreen(Canvas canvas) {
        canvas.drawRect(0, 0, screenW, screenH, pauseDimPaint);
        canvas.drawText("PAUSED", screenW / 2f, screenH * 0.35f, pauseTitlePaint);

        float rad = pauseResumeBtn.height() * 0.45f;
        canvas.drawRoundRect(pauseResumeBtn, rad, rad, pauseBtnPaint);
        pauseBtnOutPaint.setColor(0xFF00E5FF); canvas.drawRoundRect(pauseResumeBtn, rad, rad, pauseBtnOutPaint);
        pauseBtnTextPaint.setAlpha(255); pauseBtnTextPaint.setTextSize(screenW * 0.04f);
        canvas.drawText("RESUME", pauseResumeBtn.centerX(), pauseResumeBtn.centerY() + screenW * 0.014f, pauseBtnTextPaint);

        canvas.drawRoundRect(pauseQuitBtn, rad, rad, pauseBtnPaint);
        pauseBtnOutPaint.setColor(0xFF7C4DFF); canvas.drawRoundRect(pauseQuitBtn, rad, rad, pauseBtnOutPaint);
        canvas.drawText("QUIT", pauseQuitBtn.centerX(), pauseQuitBtn.centerY() + screenW * 0.014f, pauseBtnTextPaint);
    }

    private void drawTutorial(Canvas canvas) {
        if (gameWorld == null || !gameWorld.getSettings().shouldShowTutorial()) return;
        int frame = gameWorld.getFrameCount();

        if (frame < 180) {
            float alpha = frame < 30 ? frame / 30f : frame > 150 ? (180 - frame) / 30f : 1f;
            drawTutorialText(canvas, "Drag to move your ship", screenH * 0.65f, alpha);
        }

        if (gameWorld.isFirstStarDustSeen() && frame < 400 && frame > 120) {
            float alpha = Math.min(1f, Math.max(0, (frame - 120) / 30f));
            if (frame > 350) alpha = (400 - frame) / 50f;
            drawTutorialText(canvas, "Collect stardust for fuel & points!", screenH * 0.5f, alpha);
        }

        if (gameWorld.isFirstNearMiss() && frame > 200) {
            float alpha = Math.min(1f, 1f);
            if (frame > 350) alpha = 0;
            else alpha = 1f;
            drawTutorialText(canvas, "Close pass = bonus points!", screenH * 0.45f, alpha > 0 ? 0.8f : 0);
        }
    }

    private void drawTutorialText(Canvas canvas, String text, float y, float alpha) {
        if (alpha <= 0.01f) return;
        tutorialPaint.setColor(Color.WHITE); tutorialPaint.setTextSize(screenW * 0.035f);
        tutorialPaint.setAlpha((int)(200 * alpha));

        float tw = tutorialPaint.measureText(text);
        float pad = screenW * 0.03f;
        tutorialBgPaint.setAlpha((int)(100 * alpha));
        canvas.drawRoundRect(screenW/2f - tw/2 - pad, y - screenW*0.035f - pad/2,
            screenW/2f + tw/2 + pad, y + pad/2, 12, 12, tutorialBgPaint);

        canvas.drawText(text, screenW / 2f, y, tutorialPaint);
    }

    private void drawTransition(Canvas canvas) {
        if (gameWorld == null) return;
        float alpha = gameWorld.getTransitionAlpha();
        if (alpha > 0.01f) {
            transitionPaint.setAlpha((int)(alpha * 255));
            canvas.drawRect(0, 0, screenW, screenH, transitionPaint);
        }
    }
}
