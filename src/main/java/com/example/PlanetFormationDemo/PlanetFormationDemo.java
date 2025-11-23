package com.example.PlanetFormationDemo;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

/**
 * Thin UI shell wiring together simulation, rendering, input, captions, and sounds.
 */
public class PlanetFormationDemo {
    private Simulation simulation;
    private CaptionManager captions;
    private SoundManager sounds;
    private final ToolModeHolder toolHolder = new ToolModeHolder();

    public PlanetFormationDemo() {}

    private void showUI() {
        JFrame frame = new JFrame("Planet Formation Demo");
        frame.setLayout(new BorderLayout());
        // Build minimal simulation so the sun renders immediately
        this.simulation = new Simulation(1100, 720, false);
        this.captions = new CaptionManager();
        this.sounds = new SoundManager();
        simulation.addListener(captions);
        simulation.addListener(sounds);
        simulation.start();
        captions.start();

        RenderPanel render = new RenderPanel(simulation, captions, toolHolder::get, simulation::isAutoCometsEnabled);
        render.setPreferredSize(new java.awt.Dimension(1100, 720));
        render.setFocusable(true);
        render.requestFocusInWindow();

        ControlBar bar = buildToolbar();

        frame.add(bar, BorderLayout.NORTH);
        frame.add(render, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        InputBinder inputBinder = new InputBinder(toolHolder, simulation);
        inputBinder.bindKeys(render, () -> {
            captions.stop();
            simulation.stop();
            frame.dispose();
        }, () -> simulation.setAutoCometsEnabled(!simulation.isAutoCometsEnabled()));
        inputBinder.bindMouse(render);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                captions.stop();
                simulation.stop();
            }
        });

        // Start repaint loop
        javax.swing.Timer repaintTimer = new javax.swing.Timer(16, e -> render.repaint());
        repaintTimer.setCoalesce(true);
        repaintTimer.start();

        // Heavy dust seeding in background, then recenter to actual size
        new Thread(() -> {
            // Ensure frame is maximized before seeding
            SwingUtilities.invokeLater(() -> {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.revalidate();
            });
            // Wait briefly for layout to settle
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            int w = render.getWidth() > 0 ? render.getWidth() : frame.getWidth();
            int h = render.getHeight() > 0 ? render.getHeight() : frame.getHeight();
            simulation.rebuildForSize(w, h);
            SwingUtilities.invokeLater(() -> simulation.recenterTo(w, h));
        }).start();
    }

    private ControlBar buildToolbar() {
        ControlBar bar = new ControlBar(
                toolHolder::set,
                simulation::launchComet,
                simulation::shakeDisk,
                simulation::cleanUpDust,
                () -> simulation.setAutoCometsEnabled(!simulation.isAutoCometsEnabled()),
                simulation::isAutoCometsEnabled
        );
        bar.updateCometLabel(simulation.isAutoCometsEnabled());
        return bar;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PlanetFormationDemo().showUI());
    }
}
