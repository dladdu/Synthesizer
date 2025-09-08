package synth.ui;

import javax.swing.*;
import java.awt.*;
import javax.swing.Timer;
import synth.GainMeter;

public class GainVisualizer extends JPanel {
    private static final int SEGMENTS = 30;
    private int displayPeak = 0;
    private boolean clipping = false;
    private final GainMeter gainMeter;

    public GainVisualizer(GainMeter gainMeter) {
        this.gainMeter = gainMeter;
        setPreferredSize(new Dimension(20, 0));
        new Timer(50, e -> {
            clipping = gainMeter.isClipping();
            int peak = gainMeter.getPeak();
            int smoothed = (displayPeak * 4 + peak) / 5;
            displayPeak = Math.max(peak, smoothed);
            repaint();
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight();
        int segH = h / SEGMENTS;
        int lit = (int)Math.ceil((displayPeak / 127.0) * SEGMENTS);
        for (int i = 0; i < SEGMENTS; i++) {
            int y = h - (i + 1) * segH;
            g.setColor(new Color(80, 80, 80));
            g.fillRect(0, y, w, segH - 2);
            if (i < lit) {
                if (clipping) g.setColor(Color.RED);
                else if (i < SEGMENTS * 0.7) g.setColor(Color.GREEN);
                else if (i < SEGMENTS * 0.9) g.setColor(Color.YELLOW);
                else g.setColor(Color.ORANGE);
                g.fillRect(0, y, w, segH - 2);
            }
        }
    }
}