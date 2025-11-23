package com.example.PlanetFormationDemo;

import java.awt.Color;

/**
 * Colors and names for planets.
 */
public final class PlanetStyling {
    private static final String[] PLANET_NAMES = {
            "Pebble", "Dot", "Glimmer", "Sprout", "Nova", "Pip", "Luna", "Bouncy", "Marble",
            "Breeze", "Twirl", "Spark", "Glow", "Flicker", "Comet", "Mossy", "Sunny", "Skippy",
            "Flare", "Mellow", "Zippy", "Blossom", "Whirl", "Shimmer", "Ripple",
            "Starlight", "Buttercup", "Daisy", "Cuddles", "Twinkle", "Giggles", "Rainbow",
            "Polka", "Poppy", "Gummy", "Bubbles", "Jellybean", "Sprinkle", "Honey", "Pixie"
    };
    private static final java.util.List<String> pool = new java.util.ArrayList<>();
    private static final java.util.Random RNG = new java.util.Random();

    static {
        resetPool();
    }

    private PlanetStyling() {}

    public static String nextName() {
        if (pool.isEmpty()) {
            resetPool();
        }
        int idx = RNG.nextInt(pool.size());
        String name = pool.remove(idx);
        return name;
    }

    public static void resetPool() {
        pool.clear();
        pool.addAll(java.util.Arrays.asList(PLANET_NAMES));
    }

    public static Color colorForStage(Stage stage) {
        return switch (stage) {
            case ROCK -> new Color(195, 180, 160);
            case OCEAN -> new Color(120, 170, 225);
            case GARDEN -> new Color(120, 200, 160);
            case MYSTERY -> new Color(185, 140, 220);
        };
    }
}
