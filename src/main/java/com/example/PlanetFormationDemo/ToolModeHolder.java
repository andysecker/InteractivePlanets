package com.example.PlanetFormationDemo;

/**
 * Shared mutable holder for the current tool mode.
 */
public class ToolModeHolder {
    private volatile ToolMode tool = ToolMode.STAR_WAND;

    /** @return current tool mode. */
    public ToolMode get() {
        return tool;
    }

    /** Update current tool mode. */
    public void set(ToolMode tool) {
        this.tool = tool;
    }
}
