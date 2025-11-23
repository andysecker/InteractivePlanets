package com.example.PlanetFormationDemo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages current caption from simulation events with fallback rotation.
 */
public class CaptionManager implements SimulationListener {
    private final List<String> fallback = Arrays.asList(
            "Dust bumping dust...",
            "Baby planets are eating nearby dust!",
            "Heavy clumps pull harder.",
            "Orbits are like racetracks around the sun.",
            "Comets bring extra dust to the party.",
            "Planets clear their paths as they grow."
    );
    private volatile String caption = "";
    private volatile long captionUntil = 0L;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "CaptionTimer"));

    public void start() {
        scheduler.scheduleAtFixedRate(this::tick, 5, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    public String currentCaption() {
        return caption;
    }

    @Override
    public void onCaption(String text, int durationMs) {
        caption = text;
        captionUntil = System.currentTimeMillis() + durationMs;
    }

    @Override
    public void onSound(SoundEvent event) {
        // no-op
    }

    private void tick() {
        long now = System.currentTimeMillis();
        if (now > captionUntil) {
            String next = fallback.get((int) (Math.random() * fallback.size()));
            onCaption(next, 4000);
        }
    }
}
