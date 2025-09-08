package synth.dsp;

import synth.Synth;

public class LFOEngine {
    private double phase = 0.0;
    private static final double SAMPLE_RATE = Synth.SAMPLE_RATE;
    private double prevSample = 0.0;
    private static final double SMOOTHING = 0.99;
    private static final double TWO_PI = 2.0 * Math.PI;
    private boolean isEnabled = false;

    public LFOEngine() {
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        if (!enabled) {
            reset();
        }
    }

    public double nextSample(double rateHz, double depth) {
        if (!isEnabled) {
            return 0.0;
        }

        // Ensure rate is reasonable
        rateHz = Math.min(rateHz, 10.0); // Reduced max rate to 10Hz
        
        // Calculate phase increment
        double phaseIncrement = TWO_PI * rateHz / SAMPLE_RATE;
        phase += phaseIncrement;
        
        // Wrap phase
        while (phase >= TWO_PI) {
            phase -= TWO_PI;
        }
        while (phase < 0) {
            phase += TWO_PI;
        }
        
        // Calculate new sample with proper scaling
        double newSample = Math.sin(phase) * depth * 0.25; // Further reduced depth impact
        
        // Smooth transition between samples
        double smoothed = (prevSample * SMOOTHING + newSample * (1.0 - SMOOTHING));
        prevSample = smoothed;
        
        return smoothed;
    }

    public void reset() {
        phase = 0.0;
        prevSample = 0.0;
    }
}