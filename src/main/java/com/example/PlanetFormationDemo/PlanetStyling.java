package com.example.PlanetFormationDemo;

import java.awt.Color;

/**
 * Colors and names for planets.
 */
public final class PlanetStyling {
    /** Child-friendly planet name pool (shuffled each run). */
    private static final String[] PLANET_NAMES = {
            "Pebble", "Dot", "Glimmer", "Sprout", "Nova", "Pip", "Luna", "Bouncy", "Marble",
            "Breeze", "Twirl", "Spark", "Glow", "Flicker", "Comet", "Mossy", "Sunny", "Skippy",
            "Flare", "Mellow", "Zippy", "Blossom", "Whirl", "Shimmer", "Ripple",
            "Starlight", "Buttercup", "Daisy", "Cuddles", "Twinkle", "Giggles", "Rainbow",
            "Polka", "Poppy", "Gummy", "Bubbles", "Jellybean", "Sprinkle", "Honey", "Pixie"
    };
    /** Remaining names available for this session. */
    private static final java.util.List<String> pool = new java.util.ArrayList<>();
    /** Random source for shuffling the pool. */
    private static final java.util.Random RNG = new java.util.Random();

    static {
        resetPool();
    }

    /** Utility class; do not instantiate. */
    private PlanetStyling() {}

    /**
     * Pull the next unique planet name, reshuffling when exhausted.
     */
    public static String nextName() {
        if (pool.isEmpty()) {
            resetPool();
        }
        int idx = RNG.nextInt(pool.size());
        String name = pool.remove(idx);
        return name;
    }

    /**
     * Reset the name pool so names can be reused in a new session.
     */
    public static void resetPool() {
        pool.clear();
        pool.addAll(java.util.Arrays.asList(PLANET_NAMES));
    }

    /**
     * Color palette per growth stage.
     */
    public static Color colorForStage(Stage stage) {
        return switch (stage) {
            case ROCK -> new Color(195, 180, 160);
            case OCEAN -> new Color(120, 170, 225);
            case GARDEN -> new Color(120, 200, 160);
            case MYSTERY -> new Color(185, 140, 220);
        };
    }
}
