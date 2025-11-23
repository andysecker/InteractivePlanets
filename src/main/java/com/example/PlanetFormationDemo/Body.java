package com.example.PlanetFormationDemo;

import java.awt.Color;

/**
 * Mutable simulation entity representing dust, planets, or the sun.
 */
public class Body {
    public float x, y;
    public float vx, vy;
    public float mass;
    public float radius;
    public Stage stage = Stage.ROCK;
    public Color color;
    public String name;
    public boolean isSun;
    public boolean removed;
    public long particleCount;
    public boolean isComet;
}
