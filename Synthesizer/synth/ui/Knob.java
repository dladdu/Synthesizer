// Knob.java
package synth.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.*;

/**
 * A rotary knob control with 0–100 mapping, showing percent outside on drag, ms beneath label.
 * 0 at 225° (Q3), 50 at 90° (up, Q1), 100 at 315° (Q4).
 */
public class Knob extends JComponent {
    private final int min, max;
    private int value;
    private final String label;
    protected final int diameter = 34; // Changed from private to protected for subclass access
    private boolean dragging = false;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private static final double START_ANGLE = 225;
    private static final double SWEEP_ANGLE = 270;

    public Knob(String label, int min, int max, int init) {
        this.label = label;
        this.min   = min;
        this.max   = max;
        this.value = init;
        // Set appropriately sized preferred size for smaller knobs
        setPreferredSize(new Dimension(60, 80));
        setMinimumSize(new Dimension(60, 80));
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragging = true; updateValue(e); }
            @Override public void mouseDragged(MouseEvent e) { updateValue(e); }
            @Override public void mouseReleased(MouseEvent e) { dragging = false; repaint(); }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private void updateValue(MouseEvent e) {
        int cx = getWidth()/2;
        int cy = getHeight()/2 - 10;
        double dx = e.getX() - cx;
        double dy = cy - e.getY();
        double ang = Math.toDegrees(Math.atan2(dy, dx));
        if (ang < 0) ang += 360;
        double delta = (START_ANGLE - ang + 360) % 360;
        if (delta <= SWEEP_ANGLE) {
            int newV = min + (int)Math.round(delta / SWEEP_ANGLE * (max - min));
            if (newV != value) { 
                int oldValue = value;
                value = newV; 
                pcs.firePropertyChange("value", oldValue, newV);
                repaint(); 
            }
        }
    }

    public int getValue() { return value; }
    
    public void setValue(int newValue) {
        if (newValue != value && newValue >= min && newValue <= max) {
            int oldValue = value;
            value = newValue;
            pcs.firePropertyChange("value", oldValue, newValue);
            repaint();
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int cx = getWidth()/2;
        int cy = getHeight()/2 - 10;
        // draw knob
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillOval(cx - diameter/2, cy - diameter/2, diameter, diameter);
        g2.setColor(Color.DARK_GRAY);
        g2.drawOval(cx - diameter/2, cy - diameter/2, diameter, diameter);
        // draw pointer
        double frac = (value - min) / (double)(max - min);
        double angDeg = (START_ANGLE - frac * SWEEP_ANGLE + 360) % 360;
        double rad = Math.toRadians(angDeg);
        int lx = cx + (int)(Math.cos(rad) * (diameter/2 - 5));
        int ly = cy - (int)(Math.sin(rad) * (diameter/2 - 5));
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.BLACK);
        g2.drawLine(cx, cy, lx, ly);
        // percent outside when dragging
        if (dragging) {
            String pctTxt = String.valueOf(value);
            FontMetrics fm = g2.getFontMetrics();
            int ptw = fm.stringWidth(pctTxt);
            int offset = diameter/2 + 15;
            int tx = cx + (int)(Math.cos(rad) * offset) - ptw/2;
            int ty = cy - (int)(Math.sin(rad) * offset) + fm.getAscent()/2;
            g2.setColor(Color.BLUE);
            g2.drawString(pctTxt, tx, ty);
        }
        // knob label beneath
        FontMetrics fm = g2.getFontMetrics();
        int lw = fm.stringWidth(label);
        g2.setColor(Color.BLACK);
        g2.drawString(label, cx - lw/2, cy + diameter/2 + fm.getHeight());
        // ms value beneath label
        int ms = value * 10;
        String msTxt = ms + " ms";
        int mtw = fm.stringWidth(msTxt);
        g2.drawString(msTxt, cx - mtw/2, cy + diameter/2 + fm.getHeight() * 2);
    }
}