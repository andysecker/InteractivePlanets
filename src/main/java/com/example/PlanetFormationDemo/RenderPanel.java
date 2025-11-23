package com.example.PlanetFormationDemo;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Renders simulation snapshots with HUD and view-only zoom.
 */
public class RenderPanel extends JPanel {
    /** Source simulation to sample snapshots from. */
    private final Simulation simulation;
    /** Provides rotating story captions. */
    private final CaptionManager captions;
    /** Supplies the currently active tool for HUD display. */
    private final ToolModeProvider toolProvider;
    /** Supplies HUD state (auto-comet flag). */
    private final HudInfoProvider hudProvider;
    /** Base HUD font. */
    private final Font hudFont = new Font("SansSerif", Font.BOLD, 14);
    /** View zoom factor (render-only). */
    private float zoomFactor = 1.0f;

    /** Clamp helper. */
    private float clamp(float val, float min, float max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    /** Accessor for the active tool. */
    public interface ToolModeProvider { ToolMode currentTool(); }
    /** Accessor for HUD booleans. */
    public interface HudInfoProvider { boolean autoComets(); }

    /**
     * Construct a render panel bound to a simulation and HUD providers.
     */
    public RenderPanel(Simulation simulation, CaptionManager captions,
                       ToolModeProvider toolProvider, HudInfoProvider hudProvider) {
        this.simulation = simulation;
        this.captions = captions;
        this.toolProvider = toolProvider;
        this.hudProvider = hudProvider;
        setBackground(Color.black);
        setDoubleBuffered(true);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                simulation.recenterTo(getWidth(), getHeight());
            }
        });
        addMouseWheelListener(e -> {
            float factor = e.getPreciseWheelRotation() < 0 ? 1.1f : 0.9f;
            zoomFactor = clamp(zoomFactor * factor, 0.4f, 3.0f);
            repaint();
        });
    }

    /**
     * Draw the current simulation snapshot with HUD overlays and zoom transform.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        SimulationSnapshot snap = simulation.snapshot();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // background
        g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(5, 8, 20),
                0, getHeight(), new Color(10, 12, 35)));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // apply zoom around sun
        java.awt.geom.AffineTransform original = g2.getTransform();
        SimulationSnapshot.BodyView sun = snap.bodies().get(0);
        float cx = sun.x();
        float cy = sun.y();
        g2.translate(cx * (1 - zoomFactor), cy * (1 - zoomFactor));
        g2.scale(zoomFactor, zoomFactor);

        // rings centered on sun
        g2.setColor(new Color(255, 255, 255, 12));
        g2.setStroke(new BasicStroke(1f));
        for (int r = 80; r < Math.min(getWidth(), getHeight()) * 0.45; r += 70) {
            g2.drawOval((int) (cx - r), (int) (cy - r), r * 2, r * 2);
        }

        // bodies
        for (int i = snap.bodies().size() - 1; i >= 0; i--) {
            SimulationSnapshot.BodyView b = snap.bodies().get(i);
            if (b.sun()) {
                g2.setColor(b.color());
                g2.fillOval((int) (b.x() - b.radius() * 1.6f), (int) (b.y() - b.radius() * 1.6f),
                        (int) (b.radius() * 3.2f), (int) (b.radius() * 3.2f));
                g2.setColor(new Color(255, 210, 70, 200));
                g2.fillOval((int) (b.x() - b.radius()), (int) (b.y() - b.radius()),
                        (int) (b.radius() * 2), (int) (b.radius() * 2));
            } else {
                // Tail for fast movers (comets): proportional to speed, opposite velocity
                if (b.comet()) {
                    float speed = (float) Math.sqrt(b.vx() * b.vx() + b.vy() * b.vy());
                    float tailLen = clamp(speed * 12f, 24f, 180f);
                    float nx = -b.vx() / (speed + 1e-5f);
                    float ny = -b.vy() / (speed + 1e-5f);
                    int x1 = (int) b.x();
                    int y1 = (int) b.y();
                    int x2 = (int) (b.x() + nx * tailLen);
                    int y2 = (int) (b.y() + ny * tailLen);
                    g2.setColor(new Color(b.color().getRed(), b.color().getGreen(), b.color().getBlue(), 120));
                    g2.setStroke(new BasicStroke(Math.max(2f, b.radius() * 0.45f)));
                    g2.drawLine(x1, y1, x2, y2);
                }
                g2.setColor(b.color());
                g2.fillOval((int) (b.x() - b.radius()), (int) (b.y() - b.radius()),
                        (int) (b.radius() * 2), (int) (b.radius() * 2));
                if (b.name() != null) {
                    String label = b.name() + " — " + FormatUtil.formatCount(b.particles());
                    int lx = (int) (b.x() - b.radius());
                    int ly = (int) (b.y() - b.radius() - 14);
                    g2.setFont(hudFont.deriveFont(Font.BOLD, 13f));
                    g2.setColor(new Color(0, 0, 0, 190));
                    g2.drawString(label, lx + 1, ly + 1);
                    g2.setColor(new Color(255, 255, 255, 240));
                    g2.drawString(label, lx, ly);
                    g2.setFont(hudFont);
                }
            }
        }

        // restore before HUD
        g2.setTransform(original);

        // HUD
        g2.setFont(hudFont);
        g2.setColor(new Color(240, 240, 255, 230));
        int line = 22;
        long totalParticles = 0;
        for (int i = 1; i < snap.bodies().size(); i++) {
            totalParticles += snap.bodies().get(i).particles();
        }
        g2.drawString("Captain's Log: " + captions.currentCaption(), 16, line);
        g2.drawString("Bodies: " + (snap.bodies().size() - 1) + "  |  Aggregated dust: " + totalParticles, 16, line + 18);
        g2.drawString("Current Tool [1/2/3]: " + toolProvider.currentTool(), 16, line + 36);
        g2.drawString("Auto comets [C]: " + (hudProvider.autoComets() ? "ON" : "OFF"), 16, line + 54);
        g2.drawString("Zoom (wheel): " + String.format("%.1fx", zoomFactor), 16, line + 72);

        g2.dispose();
    }
}
