package com.example.PlanetFormationDemo;

/**
 * Event sink for simulation notifications (captions, sounds).
 */
public interface SimulationListener {
    void onCaption(String text, int durationMs);
    void onSound(SoundEvent event);
}
