package synth.ui;

import java.awt.*;
import javax.swing.*;

/**
 * Visualizes ADSR envelope shape in real-time based on control panel knobs.
 */
public class ADSRVisualizer extends JPanel {
    private final ControlPanel controls;

    public ADSRVisualizer(ControlPanel controls) {
        this.controls = controls;
        setBorder(BorderFactory.createLoweredBevelBorder());
        new Timer(50, e -> repaint()).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int atkMs = controls.attackKnob.getValue() * 10;
        int decMs = controls.decayKnob.getValue() * 10;
        int susMs = controls.sustainKnob.getValue() * 10;
        int relMs = controls.releaseKnob.getValue() * 10;

        int totalMs = atkMs + decMs + susMs + relMs;
        int displayMs = Math.max(2000, totalMs);
        double pxPerMs = w / (double) displayMs;

        int x0 = 0;
        int x1 = (int)(atkMs * pxPerMs);
        int x2 = x1 + (int)(decMs * pxPerMs);
        int x3 = x2 + (int)(susMs * pxPerMs);
        int x4 = x3 + (int)(relMs * pxPerMs);
        
        int yMax = h - 4;  // Leave some padding
        int yMin = 4;      // Leave some padding
        int sustainVal = controls.sustainKnob.getValue();
        int ySustain = yMax - ((sustainVal * (yMax - yMin)) / 100);

        // Draw time markers
        g2.setColor(UIManager.getColor("Panel.background").darker());
        for (int ms = 0; ms <= displayMs; ms += 500) {
            int x = (int)(ms * pxPerMs);
            g2.drawLine(x, 0, x, h);
            // Only draw time labels if enough space
            if (ms % 1000 == 0) {
                g2.drawString(ms + "ms", x + 2, h - 4);
            }
        }

        // Draw ADSR curve
        g2.setColor(new Color(0, 0, 200));
        g2.setStroke(new BasicStroke(2));
        drawEnvelopePath(g2, x0, x1, x2, x3, x4, yMax, yMin, ySustain);
    }

    private void drawEnvelopePath(Graphics2D g2, int x0, int x1, int x2, int x3, int x4, 
                                int yMax, int yMin, int ySustain) {
        // Attack
        g2.drawLine(x0, yMax, x1, yMin);
        // Decay
        g2.drawLine(x1, yMin, x2, ySustain);
        // Sustain
        g2.drawLine(x2, ySustain, x3, ySustain);
        // Release
        g2.drawLine(x3, ySustain, x4, yMax);
    }

    private void drawCurve(Graphics2D g2, int x0, int y0, int x1, int y1, String type) {
        int steps = 30;
        for (int i = 0; i < steps; i++) {
            double t1 = i / (double) steps;
            double t2 = (i + 1) / (double) steps;

            double ease1 = applyCurve(t1, type);
            double ease2 = applyCurve(t2, type);

            int xA = (int)(x0 + (x1 - x0) * t1);
            int yA = (int)(y0 + (y1 - y0) * ease1);
            int xB = (int)(x0 + (x1 - x0) * t2);
            int yB = (int)(y0 + (y1 - y0) * ease2);

            g2.drawLine(xA, yA, xB, yB);
        }
    }

    private double applyCurve(double t, String type) {
        return switch (type) {
            case "ATTACK" -> 1 - Math.pow(2, -10 * t);
            case "DECAY", "RELEASE" -> 1 - Math.pow(1 - t, 2);
            default -> t;
        };
    }
}