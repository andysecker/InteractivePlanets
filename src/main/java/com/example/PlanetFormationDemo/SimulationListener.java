package com.example.PlanetFormationDemo;

/**
 * Event sink for simulation notifications (captions, sounds).
 */
public interface SimulationListener {
    /**
     * Called when a caption should be shown to the user.
     *
     * @param text       caption contents
     * @param durationMs display duration in milliseconds
     */
    void onCaption(String text, int durationMs);

    /**
     * Called when a simulation sound cue should play.
     *
     * @param event specific sound event type
     */
    void onSound(SoundEvent event);
}
