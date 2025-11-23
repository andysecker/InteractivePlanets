package com.example.PlanetFormationDemo;

/**
 * Plays synthesized sound cues for simulation events.
 */
public class SoundManager implements SimulationListener {
    /** Whether sound playback is currently enabled. */
    private volatile boolean enabled = true;

    /** Enable or disable audio output. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Captions are ignored for sound playback. */
    @Override
    public void onCaption(String text, int durationMs) {
        // ignore
    }

    @Override
    public void onSound(SoundEvent event) {
        if (!enabled) return;
        switch (event) {
            case COMET -> Sound.playWhoosh(400);
            case MERGE -> Sound.playChime(220);
        }
    }
}
