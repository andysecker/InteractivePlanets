package com.example.PlanetFormationDemo;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

/**
 * Thin UI shell wiring together simulation, rendering, input, captions, and sounds.
 */
public class PlanetFormationDemo {
    private final Simulation simulation;
    private final CaptionManager captions;
    private final SoundManager sounds;
    private final ToolModeHolder toolHolder = new ToolModeHolder();

    public PlanetFormationDemo() {
        this.simulation = new Simulation(1100, 720);
        this.captions = new CaptionManager();
        this.sounds = new SoundManager();
        simulation.addListener(captions);
        simulation.addListener(sounds);
        captions.start();
        simulation.start();
    }

    private void showUI() {
        JFrame frame = new JFrame("Planet Formation Demo");
        RenderPanel render = new RenderPanel(simulation, captions, toolHolder::get, simulation::isAutoCometsEnabled);
        render.setPreferredSize(new java.awt.Dimension(1100, 720));
        render.setFocusable(true);
        render.requestFocusInWindow();
        InputBinder inputBinder = new InputBinder(toolHolder, simulation);
        inputBinder.bindKeys(render, () -> {
            captions.stop();
            simulation.stop();
            frame.dispose();
        }, () -> {
            simulation.setAutoCometsEnabled(!simulation.isAutoCometsEnabled());
        });
        inputBinder.bindMouse(render);

        ControlBar bar = buildToolbar();

        frame.setLayout(new BorderLayout());
        frame.add(bar, BorderLayout.NORTH);
        frame.add(render, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        SwingUtilities.invokeLater(() -> simulation.rebuildForSize(render.getWidth(), render.getHeight()));
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                captions.stop();
                simulation.stop();
            }
        });

        // Repaint loop
        javax.swing.Timer repaintTimer = new javax.swing.Timer(16, e -> render.repaint());
        repaintTimer.setCoalesce(true);
        repaintTimer.start();
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
