package synth;

import java.awt.*;
import javax.swing.*;

public class GainMeter extends JPanel {
    private int peak = 0;
    private boolean clipping = false;

    public GainMeter() {
        setPreferredSize(new Dimension(20, 100));
        setBackground(Color.BLACK);

        Timer timer = new Timer(30, e -> {
            if (peak > 0) {
                peak -= 2; // Decay
                peak = Math.max(peak, 0);
            }
            clipping = false; // Reset clipping
            repaint();
        });
        timer.start();
    }

    public void updatePeak(int value) {
        peak = Math.min(127, Math.max(peak, value));
    }

    public void markClipping() {
        clipping = true;
    }

    public int getPeak() {
        return peak;
    }

    public boolean isClipping() {
        return clipping;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int h = getHeight();
        int barHeight = (int)((peak / 127.0) * h);

        g.setColor(clipping ? Color.RED : Color.GREEN);
        g.fillRect(0, h - barHeight, getWidth(), barHeight);
    }
}