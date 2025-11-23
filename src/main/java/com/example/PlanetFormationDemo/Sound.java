package com.example.PlanetFormationDemo;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Minimal sound helper: generates tiny PCM buffers (whoosh/chime) with no external assets.
 */
public final class Sound {
    /** Audio sample rate for generated PCM buffers. */
    private static final float SAMPLE_RATE = 44100f;

    /** Utility class; do not instantiate. */
    private Sound() {}

    /**
     * Play a noise-based whoosh for a comet.
     */
    public static void playWhoosh(double millis) {
        playBuffer(genNoiseEnvelope(millis));
    }

    /**
     * Play a sine-based chime for planet merges.
     */
    public static void playChime(double millis) {
        playBuffer(genSineEnvelope(new double[]{660, 990}, millis));
    }

    /** Create a short decaying noise envelope. */
    private static byte[] genNoiseEnvelope(double ms) {
        int len = (int) (SAMPLE_RATE * ms / 1000.0);
        byte[] buf = new byte[len];
        for (int i = 0; i < len; i++) {
            double t = i / (double) len;
            // Quick attack, smooth decay
            double env = Math.sin(Math.PI * t) * (1 - t);
            double noise = (Math.random() * 2.0 - 1.0) * env;
            buf[i] = (byte) (Math.max(-1, Math.min(1, noise)) * 70);
        }
        return buf;
    }

    /** Create a multi-frequency sine envelope. */
    private static byte[] genSineEnvelope(double[] freqs, double ms) {
        int len = (int) (SAMPLE_RATE * ms / 1000.0);
        byte[] buf = new byte[len];
        for (int i = 0; i < len; i++) {
            double t = i / SAMPLE_RATE;
            double env = 1.0 - (i / (double) len); // linear decay
            double s = 0;
            for (double f : freqs) {
                s += Math.sin(2 * Math.PI * f * t);
            }
            s /= freqs.length;
            buf[i] = (byte) (s * env * 80);
        }
        return buf;
    }

    /** Play the given PCM buffer on a background thread. */
    private static void playBuffer(byte[] data) {
        new Thread(() -> {
            try {
                AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(fmt);
                line.open(fmt);
                line.start();
                line.write(data, 0, data.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {
                // keep silent if audio unavailable
            }
        }, "SoundThread").start();
    }
}
