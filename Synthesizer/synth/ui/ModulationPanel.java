package synth.ui;

import java.awt.*;
import javax.swing.*;
import synth.dsp.LFOEngine;

public class ModulationPanel extends JPanel {
    public JComboBox<String> filterType;
    public JSlider cutoffSlider, resonanceSlider;
    public JSlider lfoRateSlider, lfoDepthSlider;
    public JComboBox<String> lfoTarget;
    public JCheckBox filterEnabled, lfoEnabled;
    public final LFOEngine lfo;

    public ModulationPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 1, 1, 1);

        // Initialize LFO
        lfo = new LFOEngine();

        // Filter controls
        filterType = new JComboBox<>(new String[]{"Low-pass", "High-pass", "Band-pass"});
        cutoffSlider = new JSlider(0, 100, 50);  // Changed to percentage for logarithmic mapping
        cutoffSlider.setPaintTicks(true);
        cutoffSlider.setPaintLabels(true);
        cutoffSlider.setMajorTickSpacing(20);
        cutoffSlider.setMinorTickSpacing(5);
        cutoffSlider.setPreferredSize(new Dimension(0, 30));
        
        resonanceSlider = new JSlider(0, 100, 0);  // Changed default from 50 to 0
        resonanceSlider.setPaintTicks(true);
        resonanceSlider.setPaintLabels(true);
        resonanceSlider.setMajorTickSpacing(20);
        resonanceSlider.setMinorTickSpacing(5);
        resonanceSlider.setPreferredSize(new Dimension(0, 30));
        
        filterEnabled = new JCheckBox("Enable Filter");

        // LFO controls
        lfoRateSlider = new JSlider(0, 20, 5);
        lfoRateSlider.setPaintTicks(true);
        lfoRateSlider.setPaintLabels(true);
        lfoRateSlider.setMajorTickSpacing(5);
        lfoRateSlider.setMinorTickSpacing(1);
        lfoRateSlider.setPreferredSize(new Dimension(0, 30));
        
        lfoDepthSlider = new JSlider(0, 100, 50);
        lfoDepthSlider.setPaintTicks(true);
        lfoDepthSlider.setPaintLabels(true);
        lfoDepthSlider.setMajorTickSpacing(20);
        lfoDepthSlider.setMinorTickSpacing(5);
        lfoDepthSlider.setPreferredSize(new Dimension(0, 30));
        
        lfoTarget = new JComboBox<>(new String[]{"Pitch", "Volume"});
        lfoEnabled = new JCheckBox("Enable LFO");
        lfoEnabled.setSelected(false); // Start with LFO disabled
        lfo.setEnabled(false); // Ensure LFO is disabled initially

        // Add LFO enable listener
        lfoEnabled.addActionListener(e -> {
            boolean enabled = lfoEnabled.isSelected();
            lfo.setEnabled(enabled);
        });

        // Add components
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Filter Type:"), gbc);
        gbc.gridx = 1;
        add(filterType, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Cutoff:"), gbc);
        gbc.gridx = 1;
        add(cutoffSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Resonance:"), gbc);
        gbc.gridx = 1;
        add(resonanceSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(filterEnabled, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("LFO Rate:"), gbc);
        gbc.gridx = 1;
        add(lfoRateSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        add(new JLabel("LFO Depth:"), gbc);
        gbc.gridx = 1;
        add(lfoDepthSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        add(new JLabel("LFO Target:"), gbc);
        gbc.gridx = 1;
        add(lfoTarget, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        add(lfoEnabled, gbc);
    }
}