// Synth.java
package synth;

/**
 * Utility for waveform generation.
 */
public class Synth {
    public enum Waveform {
        SINE,
        SQUARE,
        SAW,
        TRIANGLE
    }

    public static final double SAMPLE_RATE = 44100.0;
    public static final int BUFFER_SIZE = 4096;
    private static final double NYQUIST = SAMPLE_RATE / 2.0;
    private static final double TWO_PI = 2.0 * Math.PI;

    /**
     * Generate one buffer of raw waveform samples.
     */
    public static byte[] generateBuffer(double freq, Waveform waveform, float volume, long sampleIndex) {
        byte[] buffer = new byte[BUFFER_SIZE * 2]; // 16-bit audio
        
        // Ensure frequency is below Nyquist
        freq = Math.min(freq, NYQUIST);
        
        double phaseIncrement = (freq * TWO_PI) / SAMPLE_RATE;
        double currentPhase = (sampleIndex * phaseIncrement) % TWO_PI;

        for (int i = 0; i < BUFFER_SIZE; i++) {
            // Generate clean sine wave
            double val = Math.sin(currentPhase);
            currentPhase += phaseIncrement;
            if (currentPhase >= TWO_PI) {
                currentPhase -= TWO_PI;
            }

            // Convert to 16-bit PCM with proper scaling
            int pcm = (int)(val * volume * Short.MAX_VALUE);
            pcm = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, pcm));
            
            // Write 16-bit PCM (little-endian)
            int outIndex = i * 2;
            buffer[outIndex] = (byte)(pcm & 0xFF);
            buffer[outIndex + 1] = (byte)((pcm >> 8) & 0xFF);
        }
        return buffer;
    }
    
    public static double oscillatorValue(Waveform waveform, double phase) {
        return Math.sin(phase);
    }

    /** Mutable flag to signal release */
    public static class BooleanWrapper { public volatile boolean value = false; }

    public static byte[] generateUnisonBuffer(double freq, Waveform waveform, float volume, long offset, int unison) {
        byte[] buffer = new byte[BUFFER_SIZE * 2]; // 16-bit audio
        double[] sum = new double[BUFFER_SIZE];
        double detuneAmount = 0.0005; // Reduced detune amount

        // Ensure frequency is below Nyquist
        freq = Math.min(freq, NYQUIST * 0.8);

        for (int i = 0; i < unison; i++) {
            double detuned = freq * (1 + (i - unison / 2.0) * detuneAmount);
            detuned = Math.min(detuned, NYQUIST * 0.8);
            
            double phaseIncrement = (detuned * TWO_PI) / SAMPLE_RATE;
            double currentPhase = (offset * phaseIncrement) % TWO_PI;

            for (int j = 0; j < BUFFER_SIZE; j++) {
                sum[j] += Math.sin(currentPhase);
                currentPhase += phaseIncrement;
                if (currentPhase >= TWO_PI) {
                    currentPhase -= TWO_PI;
                }
            }
        }

        // Normalize and convert to 16-bit PCM
        double scale = 1.0 / Math.sqrt(unison);
        for (int i = 0; i < BUFFER_SIZE; i++) {
            double sample = (sum[i] * scale) * volume;
            sample = Math.max(-1.0, Math.min(1.0, sample));
            
            int pcm = (int)(sample * Short.MAX_VALUE);
            int outIndex = i * 2;
            buffer[outIndex] = (byte)(pcm & 0xFF);
            buffer[outIndex + 1] = (byte)((pcm >> 8) & 0xFF);
        }

        return buffer;
    }
}