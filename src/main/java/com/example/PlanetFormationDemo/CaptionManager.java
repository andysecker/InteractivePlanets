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
    /** Default captions that cycle when no new events arrive. */
    private final List<String> fallback = Arrays.asList(
            "Dust bumping dust...",
            "Baby planets are eating nearby dust!",
            "Heavy clumps pull harder.",
            "Orbits are like racetracks around the sun.",
            "Comets bring extra dust to the party.",
            "Planets clear their paths as they grow."
    );
    /** Current caption text. */
    private volatile String caption = "";
    /** Time until caption expires (ms epoch). */
    private volatile long captionUntil = 0L;
    /** Scheduler for fallback captions. */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "CaptionTimer"));

    /** Begin rotating fallback captions. */
    public void start() {
        scheduler.scheduleAtFixedRate(this::tick, 5, 5, TimeUnit.SECONDS);
    }

    /** Stop the caption scheduler. */
    public void stop() {
        scheduler.shutdownNow();
    }

    /** @return current caption text. */
    public String currentCaption() {
        return caption;
    }

    /** Receive caption events from the simulation. */
    @Override
    public void onCaption(String text, int durationMs) {
        caption = text;
        captionUntil = System.currentTimeMillis() + durationMs;
    }

    /** Sound events are ignored by the caption manager. */
    @Override
    public void onSound(SoundEvent event) {
        // no-op
    }

    /** Periodic tick to refresh captions when expired. */
    private void tick() {
        long now = System.currentTimeMillis();
        if (now > captionUntil) {
            String next = fallback.get((int) (Math.random() * fallback.size()));
            onCaption(next, 4000);
        }
    }
}
