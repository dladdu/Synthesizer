package synth.ui;

import java.awt.*;
import javax.swing.*;

public class FilterPanel extends JPanel {
    public JComboBox<String> filterType;
    public JSlider cutoffSlider, resonanceSlider;

    public FilterPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        filterType = new JComboBox<>(new String[] { "Low-pass", "High-pass", "Band-pass" });
        cutoffSlider = new JSlider(20, 20000, 1000); // default 1kHz
        resonanceSlider = new JSlider(1, 100, 10);   // 0.1 – 10.0 range (x10 for int)

        addRow(gbc, row++, "Filter Type", filterType);
        addRow(gbc, row++, "Cutoff (Hz)", cutoffSlider);
        addRow(gbc, row++, "Resonance (x10)", resonanceSlider);
    }

    private void addRow(GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel(label), gbc);
        gbc.gridx = 1;
        add(comp, gbc);
    }
}