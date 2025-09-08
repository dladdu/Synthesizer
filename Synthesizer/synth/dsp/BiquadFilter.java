package synth.dsp;

public class BiquadFilter {
    public enum Type { LOWPASS, HIGHPASS, BANDPASS }

    private Type type;
    private double a0, a1, a2, b1, b2;
    private double z1 = 0, z2 = 0;
    private static final double MIN_Q = 0.1;
    private static final double MAX_Q = 10.0;
    private static final double MIN_FREQ = 20.0;
    private static final double MAX_FREQ = 20000.0;
    
    // Smoothing parameters
    private static final double SMOOTHING_FACTOR = 0.0005; // Reduced from 0.002 for smoother transitions
    private double targetA0, targetA1, targetA2, targetB1, targetB2;
    private double currentA0, currentA1, currentA2, currentB1, currentB2;

    public BiquadFilter(Type type, double sampleRate, double freq, double q) {
        this.type = type;
        calculateCoefficients(sampleRate, freq, q);
        // Initialize current coefficients to target values
        currentA0 = targetA0 = a0;
        currentA1 = targetA1 = a1;
        currentA2 = targetA2 = a2;
        currentB1 = targetB1 = b1;
        currentB2 = targetB2 = b2;
    }

    public void update(Type type, double sampleRate, double freq, double q) {
        this.type = type;
        calculateCoefficients(sampleRate, freq, q);
        // Update target coefficients
        targetA0 = a0;
        targetA1 = a1;
        targetA2 = a2;
        targetB1 = b1;
        targetB2 = b2;
    }

    private void calculateCoefficients(double sr, double freq, double q) {
        // Clamp frequency and Q to safe ranges
        freq = Math.max(MIN_FREQ, Math.min(MAX_FREQ, freq));
        q = Math.max(MIN_Q, Math.min(MAX_Q, q));

        double omega = 2 * Math.PI * freq / sr;
        double sinw = Math.sin(omega);
        double cosw = Math.cos(omega);
        double alpha = sinw / (2 * q);

        switch (type) {
            case LOWPASS:
                a0 = (1 - cosw) / 2;
                a1 = 1 - cosw;
                a2 = a0;
                b1 = -2 * cosw;
                b2 = 1 - alpha;
                break;
            case HIGHPASS:
                a0 = (1 + cosw) / 2;
                a1 = -(1 + cosw);
                a2 = a0;
                b1 = -2 * cosw;
                b2 = 1 - alpha;
                break;
            case BANDPASS:
                a0 = alpha;
                a1 = 0;
                a2 = -alpha;
                b1 = -2 * cosw;
                b2 = 1 - alpha;
                break;
        }

        // Normalize coefficients
        double norm = 1 / (1 + alpha);
        a0 *= norm;
        a1 *= norm;
        a2 *= norm;
        b1 *= norm;
        b2 *= norm;

        // Simple stability check
        if (Math.abs(b1) > 2.0 || Math.abs(b2) > 1.0) {
            // If unstable, use safer coefficients
            a0 = 1.0;
            a1 = 0.0;
            a2 = 0.0;
            b1 = 0.0;
            b2 = 0.0;
        }
    }

    public double process(double input) {
        // Smoothly interpolate coefficients
        currentA0 += (targetA0 - currentA0) * SMOOTHING_FACTOR;
        currentA1 += (targetA1 - currentA1) * SMOOTHING_FACTOR;
        currentA2 += (targetA2 - currentA2) * SMOOTHING_FACTOR;
        currentB1 += (targetB1 - currentB1) * SMOOTHING_FACTOR;
        currentB2 += (targetB2 - currentB2) * SMOOTHING_FACTOR;
        
        double output = currentA0 * input + currentA1 * z1 + currentA2 * z2 - currentB1 * z1 - currentB2 * z2;
        
        // Clamp output to prevent overflow
        output = Math.max(-1.0, Math.min(1.0, output));
        
        z2 = z1;
        z1 = output;
        return output;
    }

    public void reset() {
        z1 = z2 = 0;
        // Reset current coefficients to target values
        currentA0 = targetA0;
        currentA1 = targetA1;
        currentA2 = targetA2;
        currentB1 = targetB1;
        currentB2 = targetB2;
    }
}