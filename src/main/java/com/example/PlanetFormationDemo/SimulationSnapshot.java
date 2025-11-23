package com.example.PlanetFormationDemo;

import java.awt.Color;
import java.util.List;

/**
 * Immutable view of simulation state for rendering.
 */
public record SimulationSnapshot(List<BodyView> bodies, boolean autoCometsEnabled) {
    /**
     * Immutable projection of a single body for rendering.
     */
    public record BodyView(float x, float y, float vx, float vy, float radius,
                           Color color, boolean sun, String name, long particles, boolean comet) {}
}
