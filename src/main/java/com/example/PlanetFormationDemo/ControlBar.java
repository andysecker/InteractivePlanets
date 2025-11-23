package com.example.PlanetFormationDemo;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.util.function.Consumer;
import java.util.function.BooleanSupplier;

/**
 * Toolbar of buttons/toggles for simulation controls.
 */
public class ControlBar extends JPanel {
    private final JButton toggleComets;

    public ControlBar(Consumer<ToolMode> onTool, Runnable onComet, Runnable onShake,
                      Runnable onCleanup, Runnable onToggleComets, BooleanSupplier autoState) {
        super(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton star = new JButton("Star wand");
        JButton wind = new JButton("Wind");
        JButton glove = new JButton("Gravity glove");
        JButton comet = new JButton("Make a comet");
        JButton shake = new JButton("Shake disk");
        JButton cleanup = new JButton("Clean up dust");
        toggleComets = new JButton("Auto comets: ON");

        star.addActionListener(e -> onTool.accept(ToolMode.STAR_WAND));
        wind.addActionListener(e -> onTool.accept(ToolMode.WIND));
        glove.addActionListener(e -> onTool.accept(ToolMode.GRAVITY_GLOVE));
        comet.addActionListener(e -> onComet.run());
        shake.addActionListener(e -> onShake.run());
        cleanup.addActionListener(e -> onCleanup.run());
        toggleComets.addActionListener(e -> {
            onToggleComets.run();
            updateCometLabel(autoState.getAsBoolean());
        });

        add(star);
        add(wind);
        add(glove);
        add(comet);
        add(shake);
        add(cleanup);
        add(toggleComets);
    }

    public void updateCometLabel(boolean enabled) {
        toggleComets.setText("Auto comets: " + (enabled ? "ON" : "OFF"));
    }
}
