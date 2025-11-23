package com.example.PlanetFormationDemo;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Binds keyboard/mouse interactions to simulation tool actions.
 */
public class InputBinder {
    /** Shared tool state holder. */
    private final ToolModeHolder toolHolder;
    /** Target simulation to command. */
    private final Simulation sim;

    /**
     * Create a binder for the given tool state and simulation.
     */
    public InputBinder(ToolModeHolder holder, Simulation sim) {
        this.toolHolder = holder;
        this.sim = sim;
    }

    /**
     * Install keyboard shortcuts onto a panel.
     *
     * @param panel          component receiving focus and key events
     * @param onClose        callback to close the app
     * @param onToggleComets callback to toggle auto-comets
     */
    public void bindKeys(JPanel panel, Runnable onClose, Runnable onToggleComets) {
        panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('1'), "toolStar");
        panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('2'), "toolWind");
        panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('3'), "toolGlove");
        panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('C'), "toggleComets");

        panel.getActionMap().put("close", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { onClose.run(); }
        });
        panel.getActionMap().put("toolStar", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { toolHolder.set(ToolMode.STAR_WAND); }
        });
        panel.getActionMap().put("toolWind", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { toolHolder.set(ToolMode.WIND); }
        });
        panel.getActionMap().put("toolGlove", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { toolHolder.set(ToolMode.GRAVITY_GLOVE); }
        });
        panel.getActionMap().put("toggleComets", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { onToggleComets.run(); }
        });
    }

    /**
     * Bind mouse press/drag to tool actions.
     */
    public void bindMouse(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                applyTool(e.getX(), e.getY(), true);
            }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                applyTool(e.getX(), e.getY(), false);
            }
        });
    }

    /**
     * Route the current tool to the appropriate simulation effect.
     */
    private void applyTool(int x, int y, boolean press) {
        switch (toolHolder.get()) {
            case STAR_WAND -> sim.sprinkleDust(x, y, press ? 30 : 18);
            case WIND -> sim.applyWind(x, y, press ? 1.0f : 1.4f);
            case GRAVITY_GLOVE -> sim.applyGravityGlove(x, y, press ? 1.0f : 1.5f, press ? 10 : 3);
        }
    }
}
