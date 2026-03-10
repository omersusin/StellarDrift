package com.stellardrift.game.util;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {

    private boolean enabled;
    private ExecutorService executor;
    private short[] collectBuf, explosionBuf, clickBuf, gameOverBuf;
    private static final int SR = 22050;

    public SoundManager() {
        enabled = true;
        executor = Executors.newFixedThreadPool(2);
        collectBuf = genTone(new float[]{880, 1100}, new int[]{60, 80}, 0.25f);
        explosionBuf = genRumble(220, 0.35f);
        clickBuf = genTone(new float[]{1400}, new int[]{35}, 0.15f);
        gameOverBuf = genTone(new float[]{440, 330, 220}, new int[]{120, 120, 200}, 0.3f);
    }

    public void setEnabled(boolean e) { enabled = e; }

    public void playCollect() { play(collectBuf); }
    public void playExplosion() { play(explosionBuf); }
    public void playClick() { play(clickBuf); }
    public void playGameOver() { play(gameOverBuf); }

    private short[] genTone(float[] freqs, int[] durations, float vol) {
        int total = 0;
        for (int d : durations) total += d * SR / 1000;
        short[] buf = new short[total];
        int offset = 0;
        for (int s = 0; s < freqs.length; s++) {
            int n = durations[s] * SR / 1000;
            for (int i = 0; i < n; i++) {
                double t = (double) i / SR;
                double env = Math.pow(1.0 - (double) i / n, 1.5);
                double wave = Math.sin(2 * Math.PI * freqs[s] * t);
                wave += Math.sin(4 * Math.PI * freqs[s] * t) * 0.15;
                buf[offset + i] = (short)(wave * Short.MAX_VALUE * vol * env);
            }
            offset += n;
        }
        return buf;
    }

    private short[] genRumble(int ms, float vol) {
        int n = ms * SR / 1000;
        short[] buf = new short[n];
        for (int i = 0; i < n; i++) {
            double t = (double) i / SR;
            double env = Math.pow(1.0 - (double) i / n, 1.2);
            double freq = 120 - 80.0 * i / n;
            double wave = Math.sin(2 * Math.PI * freq * t);
            double noise = (Math.random() - 0.5) * 0.4;
            buf[i] = (short)((wave + noise) * Short.MAX_VALUE * vol * env * 0.5);
        }
        return buf;
    }

    private void play(final short[] buffer) {
        if (!enabled || buffer == null) return;
        executor.execute(() -> {
            AudioTrack track = null;
            try {
                int size = buffer.length * 2;
                track = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                    .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SR)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                    .setBufferSizeInBytes(Math.max(size, AudioTrack.getMinBufferSize(
                        SR, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT)))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build();
                track.write(buffer, 0, buffer.length);
                track.play();
                Thread.sleep(buffer.length * 1000L / SR + 30);
                track.stop();
                track.release();
            } catch (Exception e) {
                if (track != null) try { track.release(); } catch (Exception x) {}
            }
        });
    }

    public void release() {
        if (executor != null && !executor.isShutdown())
            executor.shutdownNow();
    }
}
