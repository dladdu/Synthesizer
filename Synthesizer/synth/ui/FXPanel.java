package synth.ui;

import java.awt.*;
import javax.swing.*;

public class FXPanel extends JPanel {
    public JSlider delayTime, delayMix, reverbAmount;

    public FXPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        delayTime = new JSlider(50, 1000, 300);
        delayTime.setPaintTicks(true);
        delayTime.setPaintLabels(true);
        delayTime.setMajorTickSpacing(200);
        delayTime.setMinorTickSpacing(50);
        delayTime.setPreferredSize(new Dimension(0, 30));
        
        delayMix = new JSlider(0, 100, 40);
        delayMix.setPaintTicks(true);
        delayMix.setPaintLabels(true);
        delayMix.setMajorTickSpacing(20);
        delayMix.setMinorTickSpacing(5);
        delayMix.setPreferredSize(new Dimension(0, 30));
        
        reverbAmount = new JSlider(0, 100, 20);
        reverbAmount.setPaintTicks(true);
        reverbAmount.setPaintLabels(true);
        reverbAmount.setMajorTickSpacing(20);
        reverbAmount.setMinorTickSpacing(5);
        reverbAmount.setPreferredSize(new Dimension(0, 30));

        addRow(gbc, row++, "Delay Time (ms)", delayTime);
        addRow(gbc, row++, "Delay Mix (%)", delayMix);
        addRow(gbc, row++, "Reverb Amount (%)", reverbAmount);
    }

    private void addRow(GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel(label), gbc);
        gbc.gridx = 1;
        add(comp, gbc);
    }
}