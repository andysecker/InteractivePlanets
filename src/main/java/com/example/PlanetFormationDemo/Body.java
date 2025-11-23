package com.example.PlanetFormationDemo;

import java.awt.Color;

/**
 * Mutable simulation entity representing dust, planets, or the sun.
 */
public class Body {
    /** x-position in simulation space. */
    public float x, y;
    /** Velocity components. */
    public float vx, vy;
    /** Mass of the body. */
    public float mass;
    /** Render radius (scaled). */
    public float radius;
    /** Current stage used for coloring. */
    public Stage stage = Stage.ROCK;
    /** Render color for this body. */
    public Color color;
    /** Friendly name once assigned. */
    public String name;
    /** True if this is the sun. */
    public boolean isSun;
    /** True when marked for removal. */
    public boolean removed;
    /** Approximate particle count aggregated into this body. */
    public long particleCount;
    /** True if this body should be rendered with a comet tail. */
    public boolean isComet;
}
