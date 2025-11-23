package com.example.PlanetFormationDemo;

/**
 * Shared mutable holder for the current tool mode.
 */
public class ToolModeHolder {
    private volatile ToolMode tool = ToolMode.STAR_WAND;

    public ToolMode get() {
        return tool;
    }

    public void set(ToolMode tool) {
        this.tool = tool;
    }
}
