package com.example.PlanetFormationDemo;

/**
 * Small formatting helpers.
 */
public final class FormatUtil {
    /** Utility class; do not instantiate. */
    private FormatUtil() {}

    /**
     * Human-friendly counter formatting (k/M).
     */
    public static String formatCount(long n) {
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 10_000) return String.format("%.1fk", n / 1_000.0);
        if (n >= 1_000) return String.format("%.1fk", n / 1_000.0);
        return Long.toString(n);
    }
}
