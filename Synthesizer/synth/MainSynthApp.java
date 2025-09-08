package synth;

import java.awt.*;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import synth.ui.*;

public class MainSynthApp {
    public static void main(String[] args) throws LineUnavailableException {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Java Synthesizer");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());

                // Panels
                ControlPanel controls = new ControlPanel();
                ModulationPanel mod = new ModulationPanel();
                FXPanel fx = new FXPanel();
                GainMeter gainMeter = new GainMeter();
                VoiceBank voiceBank = new VoiceBank(controls, gainMeter, mod);
                VisualKeyboard keyboard = new VisualKeyboard(voiceBank, 48, 72); // C3 to C5
                GainVisualizer gain = new GainVisualizer(gainMeter);
                SpectrumAnalyzer spectrum = new SpectrumAnalyzer(controls);

                // === Tabs ===
                JTabbedPane tabs = new JTabbedPane();
                tabs.addTab("Oscillators", controls);
                tabs.addTab("Modulation", mod);
                tabs.addTab("FX", fx);

                // === Right Panel - Only ADSR knobs now ===
                JPanel rightPanel = new JPanel(new BorderLayout());
                rightPanel.setPreferredSize(new Dimension(250, 0));
                rightPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                
                // Add ADSR knobs to the right panel
                JPanel knobs = new JPanel(new GridLayout(2, 2, 5, 5)); // 2x2 grid with 5px spacing
                knobs.setBorder(BorderFactory.createTitledBorder("ADSR"));
                knobs.add(controls.attackKnob);    // Top left
                knobs.add(controls.decayKnob);     // Top right
                knobs.add(controls.sustainKnob);   // Bottom left
                knobs.add(controls.releaseKnob);   // Bottom right
                rightPanel.add(knobs, BorderLayout.NORTH);

                // Main layout
                frame.setLayout(new BorderLayout());
                frame.add(rightPanel, BorderLayout.EAST);

                // Add keyboard and spectrum analyzer to main area
                JPanel mainArea = new JPanel(new BorderLayout());
                mainArea.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                
                // Add spectrum analyzer at the top
                JPanel spectrumWrapper = new JPanel(new BorderLayout());
                spectrumWrapper.setBorder(BorderFactory.createTitledBorder("Frequency Spectrum"));
                spectrumWrapper.setPreferredSize(new Dimension(0, 140));
                spectrumWrapper.add(spectrum, BorderLayout.CENTER);
                mainArea.add(spectrumWrapper, BorderLayout.NORTH);
                
                // Add oscillator, modulation, and fx panels in the middle
                JPanel controlsWrapper = new JPanel(new BorderLayout());
                controlsWrapper.setBorder(BorderFactory.createTitledBorder("Synthesizer Controls"));
                controlsWrapper.setPreferredSize(new Dimension(0, 200));
                controlsWrapper.add(tabs, BorderLayout.CENTER);
                mainArea.add(controlsWrapper, BorderLayout.CENTER);
                
                // Add keyboard at the bottom
                JPanel keyboardWrapper = new JPanel(new BorderLayout());
                keyboardWrapper.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                JPanel keyboardBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                keyboardBottom.add(keyboard);
                keyboardWrapper.add(keyboardBottom, BorderLayout.SOUTH);
                mainArea.add(keyboardWrapper, BorderLayout.SOUTH);
                
                frame.add(mainArea, BorderLayout.CENTER);

                new KeyMapper(voiceBank);
                frame.setPreferredSize(new Dimension(800, 1000));
                frame.setMinimumSize(new Dimension(800, 1000));
                frame.pack();
                frame.setVisible(true);

                Mixer.start(voiceBank, controls, spectrum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}