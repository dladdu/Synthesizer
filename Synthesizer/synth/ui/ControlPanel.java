package synth.ui;

import java.awt.*;
import javax.swing.*;
import synth.Synth;

public class ControlPanel extends JPanel {
    public Knob osc1Slider, osc2Slider, noiseSlider, masterSlider;
    public Knob detune1Slider, detune2Slider, unisonSlider, trimSlider;
    public Knob attackKnob, decayKnob, sustainKnob, releaseKnob;
    public JComboBox<Synth.Waveform> osc1Waveform, osc2Waveform;
    public Knob osc1Octave, osc2Octave;
    public Knob osc1Voices, osc2Voices;

    public ControlPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);  // Increased initial insets to accommodate all numbers

        // Initialize knobs instead of sliders
        osc1Slider = new Knob("Osc1", 0, 100, 50);
        osc2Slider = new Knob("Osc2", 0, 100, 50);
        noiseSlider = new Knob("Noise", 0, 100, 0);
        masterSlider = new Knob("Master", 0, 100, 25);
        detune1Slider = new Knob("Detune1", 0, 100, 0);
        detune2Slider = new Knob("Detune2", 0, 100, 0);
        unisonSlider = new Knob("Unison", 0, 100, 0);
        trimSlider = new Knob("Trim", 0, 100, 50);
        osc1Octave = new Knob("Octave1", -2, 2, 0);
        osc2Octave = new Knob("Octave2", -2, 2, 0);
        osc1Voices = new Knob("Voices1", 1, 16, 1);
        osc2Voices = new Knob("Voices2", 1, 16, 1);

        // Configure GridBagLayout constraints
        gbc.fill = GridBagConstraints.NONE;  // Don't stretch knobs horizontally
        gbc.weightx = 0.0;  // Don't give extra horizontal space to knobs
        gbc.insets = new Insets(8, 10, 8, 10);  // Increased padding for knobs
        gbc.anchor = GridBagConstraints.CENTER;    // Center the knobs

        // Add some extra spacing for wave selection panels
        gbc.insets = new Insets(6, 8, 6, 8);  // Increased padding for wave panels

        // Initialize waveform ComboBoxes
        osc1Waveform = new JComboBox<>(Synth.Waveform.values());
        osc2Waveform = new JComboBox<>(Synth.Waveform.values());

        // Initialize ADSR knobs (these are now handled in the right panel)
        attackKnob = new Knob("Attack", 0, 1000, 0);
        decayKnob = new Knob("Decay", 0, 1000, 0);
        sustainKnob = new Knob("Sustain", 0, 100, 100);
        releaseKnob = new Knob("Release", 0, 1000, 0);

        // === Two Column Layout ===
        
        // === Left Column - Oscillator 1 ===
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(createCompactLabel("Oscillator 1"), gbc);

        // Waveform selection for Osc1 - now using knobs
        gbc.gridy++;
        add(createCompactLabel("Waveform"), gbc);
        gbc.gridy++;
        add(createWaveformKnob(osc1Waveform, Synth.Waveform.SINE), gbc);

        gbc.gridy++;
        add(createCompactLabel("Volume"), gbc);
        gbc.gridy++;
        add(osc1Slider, gbc);

        gbc.gridy++;
        add(createCompactLabel("Detune"), gbc);
        gbc.gridy++;
        add(detune1Slider, gbc);

        gbc.gridy++;
        add(createCompactLabel("Octave"), gbc);
        gbc.gridy++;
        add(osc1Octave, gbc);

        gbc.gridy++;
        add(createCompactLabel("Voices"), gbc);
        gbc.gridy++;
        add(osc1Voices, gbc);

        // === Right Column - Oscillator 2 ===
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(createCompactLabel("Oscillator 2"), gbc);

        // Waveform selection for Osc2 - now using knobs
        gbc.gridy++;
        add(createCompactLabel("Waveform"), gbc);
        gbc.gridy++;
        add(createWaveformKnob(osc2Waveform, Synth.Waveform.SINE), gbc);

        gbc.gridy++;
        add(createCompactLabel("Volume"), gbc);
        gbc.gridy++;
        add(osc2Slider, gbc);

        gbc.gridy++;
        add(createCompactLabel("Detune"), gbc);
        gbc.gridy++;
        add(detune2Slider, gbc);

        gbc.gridy++;
        add(createCompactLabel("Octave"), gbc);
        gbc.gridy++;
        add(osc2Octave, gbc);

        gbc.gridy++;
        add(createCompactLabel("Voices"), gbc);
        gbc.gridy++;
        add(osc2Voices, gbc);

        // === Third Column - Master Volume ===
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(createCompactLabel("Master"), gbc);

        gbc.gridy++;
        add(createCompactLabel("Volume"), gbc);
        gbc.gridy++;
        add(masterSlider, gbc);

        // Reset grid width for future components
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // ADSR knobs are now handled in the right panel, not here
    }

    private JLabel createCompactLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(11f)); // Smaller font for labels
        return label;
    }

    private Knob createWaveformKnob(JComboBox<Synth.Waveform> waveformCombo, Synth.Waveform defaultWave) {
        // Create a custom knob for waveform selection that shows waveform names
        int defaultIndex = java.util.Arrays.asList(Synth.Waveform.values()).indexOf(defaultWave);
        
        Knob waveformKnob = new Knob("Wave", 0, Synth.Waveform.values().length - 1, defaultIndex) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                
                // Get the current waveform name to display
                int index = getValue();
                String waveformName = Synth.Waveform.values()[index].name();
                
                // Draw the waveform name instead of millisecond value
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(waveformName);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2 + diameter / 2 + fm.getHeight() * 2;
                
                g2.setColor(Color.BLACK);
                g2.drawString(waveformName, cx - textWidth / 2, cy);
            }
        };
        
        // Add listener to update the combo box when knob is turned
        waveformKnob.addPropertyChangeListener(evt -> {
            if ("value".equals(evt.getPropertyName())) {
                int index = (Integer) evt.getNewValue();
                if (index >= 0 && index < Synth.Waveform.values().length) {
                    Synth.Waveform selectedWave = Synth.Waveform.values()[index];
                    waveformCombo.setSelectedItem(selectedWave);
                    waveformKnob.repaint(); // Refresh the display
                }
            }
        });
        
        return waveformKnob;
    }

    public Synth.Waveform osc1Waveform() {
        return (Synth.Waveform) osc1Waveform.getSelectedItem();
    }

    public Synth.Waveform osc2Waveform() {
        return (Synth.Waveform) osc2Waveform.getSelectedItem();
    }
}
