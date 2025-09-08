package synth.ui;

import java.awt.*;
import javax.swing.*;

public class SpectrumAnalyzer extends JPanel {
    private FFTProcessor fftProcessor;
    private SpectrumRenderer renderer;
    private ControlPanel controls;
    private float[] currentBinValues;

    public SpectrumAnalyzer(ControlPanel controls) {
        this.controls = controls;
        this.fftProcessor = new FFTProcessor();
        this.renderer = new SpectrumRenderer();
        this.currentBinValues = new float[fftProcessor.getNumBins()];
        
        setPreferredSize(new Dimension(400, 200));
        setBackground(Color.BLACK);
    }

    public void updateSpectrum(byte[] audioData) {
        // Process audio data through FFT processor
        currentBinValues = fftProcessor.processAudio(audioData);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();
        
        // Render the spectrum using the renderer
        renderer.renderSpectrum(g2, width, height, currentBinValues, 
                               fftProcessor.getMinFreq(), fftProcessor.getMaxFreq(), 
                               fftProcessor.getNumBins());
    }
    
    // Public methods for technical analysis (can be extended without affecting visuals)
    public FFTProcessor getFFTProcessor() {
        return fftProcessor;
    }
    
    public float[] getCurrentBinValues() {
        return currentBinValues.clone();
    }
    
    public float[] getRawSpectrum() {
        return fftProcessor.getRawSpectrum();
    }
    
    public float[] getSmoothedSpectrum() {
        return fftProcessor.getSmoothedSpectrum();
    }
} 