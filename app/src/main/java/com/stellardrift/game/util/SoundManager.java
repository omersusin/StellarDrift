package com.stellardrift.game.util;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

public class SoundManager {

    private static final int SAMPLE_RATE = 22050;
    private boolean enabled = true;
    private float masterVolume = 0.7f;

    private short[] shootBuffer, hitBuffer, explodeBuffer, collectBuffer;
    private short[] powerUpBuffer, errorBuffer, overchargeBuffer, comboBuffer;
    private short[] nearMissBuffer, deathBuffer, menuClickBuffer, purchaseBuffer;
    private short[] fuelLowBuffer, upgradeBuffer, gameOverBuffer;

    private static final int TRACK_POOL_SIZE = 6;
    private final AudioTrack[] trackPool = new AudioTrack[TRACK_POOL_SIZE];
    private int trackCursor = 0;

    private AudioTrack droneTrack;
    private Thread droneThread;
    private volatile boolean droneRunning = false;
    private volatile float droneFreq = 55f, droneLfoRate = 0.3f, droneLfoDepth = 0.02f, droneVolume = 0.04f;

    public SoundManager() {
        generateAllSounds();
        initTrackPool();
    }

    private void generateAllSounds() {
        shootBuffer = generateShootSound(); hitBuffer = generateHitSound();
        explodeBuffer = generateExplodeSound(); collectBuffer = generateCollectSound();
        powerUpBuffer = generatePowerUpSound(); errorBuffer = generateErrorSound();
        overchargeBuffer = generateOverchargeSound(); comboBuffer = generateComboSound();
        nearMissBuffer = generateNearMissSound(); deathBuffer = generateDeathSound();
        menuClickBuffer = generateMenuClickSound(); purchaseBuffer = generatePurchaseSound();
        fuelLowBuffer = generateFuelLowSound(); upgradeBuffer = generateUpgradeSound();
        gameOverBuffer = generateGameOverSound();
    }

    private short[] generateShootSound() {
        int samples = (int)(SAMPLE_RATE * 0.08f); short[] buf = new short[samples]; double phase = 0;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples; float freq = 1200f * (float) Math.pow(0.15, t);
            double sine = Math.sin(2 * Math.PI * phase), square = sine > 0 ? 1.0 : -1.0;
            double wave = sine * 0.7 + square * 0.3; float envelope = 1f - t; envelope *= envelope;
            buf[i] = (short)(wave * (0.35f * envelope) * Short.MAX_VALUE); phase += freq / SAMPLE_RATE;
        } return buf;
    }

    private short[] generateHitSound() {
        int samples = (int)(SAMPLE_RATE * 0.05f); short[] buf = new short[samples]; double phase = 0;
        for (int i = 0; i < samples; i++) {
            float freq = 400f + (float)(Math.random() * 60 - 30);
            double wave = Math.sin(2 * Math.PI * phase) * 0.75 + (Math.random() * 2 - 1) * 0.25;
            float envelope = (float) Math.exp(-(float)i/samples * 12);
            buf[i] = (short)(wave * 0.3f * envelope * Short.MAX_VALUE); phase += freq / SAMPLE_RATE;
        } return buf;
    }

    private short[] generateExplodeSound() {
        int samples = (int)(SAMPLE_RATE * 0.35f); short[] buf = new short[samples]; double phase = 0;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples; float bassFreq = 60f - t * 30f;
            double bass = Math.sin(2 * Math.PI * phase) * 0.5; phase += bassFreq / SAMPLE_RATE;
            double wave = (Math.random() * 2 - 1) * (1f - t) * 0.6 + bass * 0.4;
            float envelope = t < 0.05f ? t / 0.05f : (float) Math.exp(-(t - 0.05f) * 5);
            buf[i] = (short)(wave * 0.5f * envelope * Short.MAX_VALUE);
        } return buf;
    }

    private short[] generateCollectSound() {
        int samples = (int)(SAMPLE_RATE * 0.15f); short[] buf = new short[samples]; double phase = 0;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples; float freq = 600f + t * 600f;
            double wave = Math.sin(2 * Math.PI * phase) + Math.sin(4 * Math.PI * phase) * 0.2;
            float envelope = t < 0.08f ? t / 0.08f : (float) Math.exp(-(t - 0.08f) * 8);
            buf[i] = (short)(wave * 0.25f * envelope * Short.MAX_VALUE); phase += freq / SAMPLE_RATE;
        } return buf;
    }

    private short[] generatePowerUpSound() {
        int samples = (int)(SAMPLE_RATE * 0.4f); short[] buf = new short[samples]; double p1 = 0, p2 = 0;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples; float f1 = 400f + t * t * 600f, f2 = f1 * 2;
            double wave = (Math.sin(2 * Math.PI * p1) * 0.6 + Math.sin(2 * Math.PI * p2) * 0.25) * (1 + 0.05 * Math.sin(t * 60));
            float env = t < 0.1f ? t / 0.1f : t < 0.6f ? 1f : (1f - t) / 0.4f;
            buf[i] = (short)(wave * 0.3f * env * Short.MAX_VALUE); p1 += f1 / SAMPLE_RATE; p2 += f2 / SAMPLE_RATE;
        } return buf;
    }

    private short[] generateErrorSound() {
        int samples = (int)(SAMPLE_RATE * 0.2f); short[] buf = new short[samples]; double phase = 0;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples;
            double wave = (Math.sin(2 * Math.PI * phase) > 0 ? 1 : -1) + Math.sin(2 * Math.PI * phase * 0.68) * 0.5;
            float env = t < 0.02f ? t / 0.02f : t < 0.5f ? 1f : (1f - t) / 0.5f;
            float chop = (float) Math.sin(t * 30) > 0 ? 1f : 0.2f;
            buf[i] = (short)(wave * 0.25f * env * chop * Short.MAX_VALUE); phase += 125f / SAMPLE_RATE;
        } return buf;
    }

    private short[] generateOverchargeSound() {
        int samples = (int)(SAMPLE_RATE * 0.6f); short[] buf = new short[samples]; double p1 = 0, p2 = 0, p3 = 0;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples; float f1 = 100f + t * 200f, f2 = 300f + t * t * 500f, f3 = 1000f + t * 1000f;
            double wave = Math.sin(2 * Math.PI * p1) * 0.4 + Math.sin(2 * Math.PI * p2) * 0.35 + Math.sin(2 * Math.PI * p3) * 0.15 + (Math.random() * 2 - 1) * 0.1 * (1 - t);
            float env = t < 0.15f ? t / 0.15f : t < 0.5f ? 1f : (1f - t) / 0.5f;
            buf[i] = (short)(wave * 0.35f * env * Short.MAX_VALUE); p1 += f1 / SAMPLE_RATE; p2 += f2 / SAMPLE_RATE; p3 += f3 / SAMPLE_RATE;
        } return buf;
    }

    private short[] generateComboSound() {
        int samples = (int)(SAMPLE_RATE * 0.1f); short[] buf = new short[samples]; double phase = 0;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples;
            double wave = Math.sin(2 * Math.PI * phase) + Math.sin(4 * Math.PI * phase) * 0.3;
            buf[i] = (short)(wave * 0.2f * Math.exp(-t * 15) * Short.MAX_VALUE); phase += 1400f / SAMPLE_RATE;
        } return buf;
    }

    private short[] generateNearMissSound() {
        int samples = (int)(SAMPLE_RATE * 0.12f); short[] buf = new short[samples]; double phase = 0;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples; float freq = t < 0.5f ? 200f + t * 2 * 600f : 800f - (t - 0.5f) * 2 * 600f;
            double wave = Math.sin(2 * Math.PI * phase); float env = (float)(Math.sin(t * Math.PI));
            buf[i] = (short)(wave * 0.2f * env * Short.MAX_VALUE); phase += freq / SAMPLE_RATE;
        } return buf;
    }

    private short[] generateDeathSound() {
        int samples = (int)(SAMPLE_RATE * 0.8f); short[] buf = new short[samples]; double p1 = 0, p2 = 0;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples; float f1 = 500f * (float) Math.pow(0.12, t), f2 = f1 * 1.5f;
            double wave = Math.sin(2 * Math.PI * p1) * 0.5 + Math.sin(2 * Math.PI * p2) * 0.3 + (Math.random() * 2 - 1) * t * 0.4;
            float env = t < 0.05f ? t / 0.05f : (float) Math.exp(-(t - 0.05f) * 2.5);
            buf[i] = (short)(wave * 0.45f * env * Short.MAX_VALUE); p1 += f1 / SAMPLE_RATE; p2 += f2 / SAMPLE_RATE;
        } return buf;
    }

    private short[] generateMenuClickSound() {
        int samples = (int)(SAMPLE_RATE * 0.04f); short[] buf = new short[samples]; double phase = 0;
        for (int i = 0; i < samples; i++) {
            buf[i] = (short)(Math.sin(2 * Math.PI * phase) * 0.2f * Math.exp(-(float)i/samples * 25) * Short.MAX_VALUE); phase += 800f / SAMPLE_RATE;
        } return buf;
    }

    private short[] generatePurchaseSound() {
        int samples = (int)(SAMPLE_RATE * 0.3f); short[] buf = new short[samples]; double phase = 0;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples; float freq = t < 0.4f ? 659f : 784f;
            double wave = Math.sin(2 * Math.PI * phase) + Math.sin(4 * Math.PI * phase) * 0.2;
            float noteT = t < 0.4f ? t / 0.4f : (t - 0.4f) / 0.6f;
            float env = noteT < 0.1f ? noteT / 0.1f : (float) Math.exp(-(noteT - 0.1f) * 5);
            buf[i] = (short)(wave * 0.25f * env * Short.MAX_VALUE); phase += freq / SAMPLE_RATE;
        } return buf;
    }

    private short[] generateFuelLowSound() {
        int samples = (int)(SAMPLE_RATE * 0.4f); short[] buf = new short[samples]; double phase = 0;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples; double wave = Math.sin(2 * Math.PI * phase);
            float env = t < 0.15f ? (float) Math.exp(-(t/0.15f - 0.3f)*(t/0.15f - 0.3f)*20) : (t > 0.2f && t < 0.35f ? (float) Math.exp(-((t-0.2f)/0.15f - 0.3f)*((t-0.2f)/0.15f - 0.3f)*20)*0.6f : 0);
            buf[i] = (short)(wave * 0.35f * env * Short.MAX_VALUE); phase += 70f / SAMPLE_RATE;
        } return buf;
    }

    private short[] generateUpgradeSound() {
        int samples = (int)(SAMPLE_RATE * 0.35f); short[] buf = new short[samples]; double phase = 0; float[] notes = {523.3f, 659.3f, 784f};
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples; int nIdx = Math.min((int)(t * 3), 2);
            double wave = Math.sin(2 * Math.PI * phase) + Math.sin(4 * Math.PI * phase) * 0.15;
            float locT = (t * 3) - nIdx, env = locT < 0.15f ? locT / 0.15f : (float) Math.exp(-(locT - 0.15f) * 6);
            buf[i] = (short)(wave * 0.25f * env * Short.MAX_VALUE); phase += notes[nIdx] / SAMPLE_RATE;
        } return buf;
    }

    private short[] generateGameOverSound() {
        int samples = (int)(SAMPLE_RATE * 1.5f); short[] buf = new short[samples]; double phase = 0;
        float[] notes = {329.63f, 261.63f, 220f, 164.81f}; // E4, C4, A3, E3
        for (int i = 0; i < samples; i++) {
            float t = (float) i / samples; int nIdx = Math.min((int)(t * notes.length), notes.length - 1);
            double wave = Math.sin(2 * Math.PI * phase) * 0.5 + (Math.random() * 2 - 1) * 0.05;
            float locT = (t * notes.length) - nIdx, env = (float) Math.exp(-locT * 3);
            buf[i] = (short)(wave * 0.4f * env * Short.MAX_VALUE); phase += notes[nIdx] / SAMPLE_RATE;
        } return buf;
    }

    private void initTrackPool() {
        int maxLen = gameOverBuffer.length * 2, bufSize = Math.max(AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT), maxLen);
        AudioAttributes attrs = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        AudioFormat format = new AudioFormat.Builder().setSampleRate(SAMPLE_RATE).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build();
        for (int i = 0; i < TRACK_POOL_SIZE; i++) trackPool[i] = new AudioTrack.Builder().setAudioAttributes(attrs).setAudioFormat(format).setBufferSizeInBytes(bufSize).setTransferMode(AudioTrack.MODE_STATIC).build();
    }

    private void playBuffer(final short[] buffer, float volume) {
        if (!enabled || buffer == null) return;
        final float vol = volume * masterVolume; final int idx = trackCursor; trackCursor = (trackCursor + 1) % TRACK_POOL_SIZE;
        new Thread(() -> {
            try {
                AudioTrack track = trackPool[idx];
                if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) track.stop();
                track.reloadStaticData(); track.write(buffer, 0, buffer.length); track.setVolume(vol); track.play();
            } catch (Exception e) {}
        }, "SFX-" + idx).start();
    }

    public void startDrone() {
        if (droneRunning) return; droneRunning = true;
        droneTrack = new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(new AudioFormat.Builder().setSampleRate(SAMPLE_RATE).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)).setTransferMode(AudioTrack.MODE_STREAM).build();
        droneTrack.play();
        droneThread = new Thread(() -> {
            short[] chunk = new short[512]; double phase = 0, lfoPhase = 0;
            while (droneRunning) {
                for (int i = 0; i < chunk.length; i++) {
                    double lfo = 1.0 + droneLfoDepth * Math.sin(2 * Math.PI * lfoPhase); lfoPhase += droneLfoRate / SAMPLE_RATE;
                    double sample = Math.sin(2 * Math.PI * phase) * droneVolume; phase += (droneFreq * lfo) / SAMPLE_RATE;
                    chunk[i] = (short)(Math.max(-0.95, Math.min(0.95, sample)) * Short.MAX_VALUE);
                }
                try { droneTrack.write(chunk, 0, chunk.length); } catch (Exception e) { break; }
            }
        }, "Drone"); droneThread.setPriority(Thread.MIN_PRIORITY + 1); droneThread.start();
    }

    public void setDroneState(int tempoPhase) {
        switch (tempoPhase) {
            case Constants.TEMPO_CALM: droneFreq = 55f; droneLfoRate = 0.3f; droneLfoDepth = 0.02f; droneVolume = 0.03f; break;
            case Constants.TEMPO_PRESSURE: droneFreq = 85f; droneLfoRate = 2.5f; droneLfoDepth = 0.1f; droneVolume = 0.05f; break;
            case Constants.TEMPO_REWARD: droneFreq = 65f; droneLfoRate = 0.2f; droneLfoDepth = 0.01f; droneVolume = 0.025f; break;
        }
    }

    public void stopDrone() { droneRunning = false; if (droneTrack != null) { try { droneTrack.stop(); droneTrack.release(); } catch (Exception ignored) {} } }
    
    public void playShoot() { playBuffer(shootBuffer, 0.5f); } public void playHit() { playBuffer(hitBuffer, 0.6f); } public void playExplode() { playBuffer(explodeBuffer, 0.8f); }
    public void playCollect() { playBuffer(collectBuffer, 0.5f); } public void playPowerUp() { playBuffer(powerUpBuffer, 0.7f); } public void playError() { playBuffer(errorBuffer, 0.5f); }
    public void playOvercharge() { playBuffer(overchargeBuffer, 0.9f); } public void playCombo() { playBuffer(comboBuffer, 0.4f); } public void playNearMiss() { playBuffer(nearMissBuffer, 0.5f); }
    public void playDeath() { playBuffer(deathBuffer, 1.0f); } public void playMenuClick() { playBuffer(menuClickBuffer, 0.4f); } public void playPurchase() { playBuffer(purchaseBuffer, 0.6f); }
    public void playFuelLow() { playBuffer(fuelLowBuffer, 0.6f); } public void playUpgrade() { playBuffer(upgradeBuffer, 0.7f); }
    public void playGameOver() { playBuffer(gameOverBuffer, 0.8f); }

    public void setEnabled(boolean e) { enabled = e; } public boolean isEnabled() { return enabled; }
    public void release() { stopDrone(); for (AudioTrack t : trackPool) { if (t != null) { try { t.stop(); t.release(); } catch (Exception ignored) {} } } }
}
