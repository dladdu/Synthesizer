package synth.ui;

public class FFTProcessor {
    private static final int FFT_SIZE = 512;
    private static final int SAMPLE_RATE = 44100;
    private static final float SMOOTHING_FACTOR = 0.3f;
    
    private float[] spectrum;
    private float[] smoothedSpectrum;
    private static final int NUM_BINS = 100;
    private static final int MIN_FREQ = 20;
    private static final int MAX_FREQ = 20000;
    
    public FFTProcessor() {
        spectrum = new float[FFT_SIZE/2];
        smoothedSpectrum = new float[FFT_SIZE/2];
    }
    
    public float[] processAudio(byte[] audioData) {
        // Convert byte array to float array with proper signed conversion
        float[] audio = new float[FFT_SIZE];
        for (int i = 0; i < Math.min(audioData.length/2, FFT_SIZE); i++) {
            // Convert signed 16-bit PCM to float (-1.0 to 1.0)
            short sample = (short)((audioData[i*2] & 0xFF) | (audioData[i*2 + 1] << 8));
            audio[i] = sample / (float)Short.MAX_VALUE;
        }

        // Apply Hanning window
        for (int i = 0; i < FFT_SIZE; i++) {
            audio[i] *= 0.5 * (1 - Math.cos(2*Math.PI*i / (FFT_SIZE-1)));
        }

        // Compute FFT
        computeFFT(audio);
        
        // Apply responsive smoothing
        for (int i = 0; i < spectrum.length; i++) {
            float current = spectrum[i];
            float smoothed = smoothedSpectrum[i];
            // Use maximum of current and smoothed value for rising edges
            if (current > smoothed) {
                smoothedSpectrum[i] = current;
            } else {
                // Apply smoothing only for falling edges
                smoothedSpectrum[i] = smoothed * (1 - SMOOTHING_FACTOR) + current * SMOOTHING_FACTOR;
            }
        }
        
        return getBinValues();
    }
    
    private void computeFFT(float[] audio) {
        // Simple FFT implementation
        float[] real = new float[FFT_SIZE];
        float[] imag = new float[FFT_SIZE];
        System.arraycopy(audio, 0, real, 0, FFT_SIZE);

        // Perform FFT
        fft(real, imag);

        // Compute magnitude spectrum with proper normalization for dB scale
        for (int i = 0; i < FFT_SIZE/2; i++) {
            float magnitude = (float) Math.sqrt(real[i]*real[i] + imag[i]*imag[i]);
            // Normalize by FFT size and apply window correction factor
            // This will give us proper dB values where 0dB represents full scale
            spectrum[i] = magnitude / (FFT_SIZE * 0.5f);
        }
    }
    
    private void fft(float[] real, float[] imag) {
        int n = real.length;
        if (n == 1) return;

        float[] realEven = new float[n/2];
        float[] imagEven = new float[n/2];
        float[] realOdd = new float[n/2];
        float[] imagOdd = new float[n/2];

        for (int i = 0; i < n/2; i++) {
            realEven[i] = real[2*i];
            imagEven[i] = imag[2*i];
            realOdd[i] = real[2*i+1];
            imagOdd[i] = imag[2*i+1];
        }

        fft(realEven, imagEven);
        fft(realOdd, imagOdd);

        for (int k = 0; k < n/2; k++) {
            double theta = -2 * Math.PI * k / n;
            float c = (float) Math.cos(theta);
            float s = (float) Math.sin(theta);
            float tReal = c * realOdd[k] - s * imagOdd[k];
            float tImag = s * realOdd[k] + c * imagOdd[k];
            real[k] = realEven[k] + tReal;
            imag[k] = imagEven[k] + tImag;
            real[k + n/2] = realEven[k] - tReal;
            imag[k + n/2] = imagEven[k] - tImag;
        }
    }
    
    private float[] getBinValues() {
        float[] binValues = new float[NUM_BINS];
        
        // Calculate bin values with improved frequency mapping
        for (int i = 0; i < NUM_BINS; i++) {
            float startFreq = (float)(MIN_FREQ * Math.pow(MAX_FREQ/MIN_FREQ, (float)i/NUM_BINS));
            float endFreq = (float)(MIN_FREQ * Math.pow(MAX_FREQ/MIN_FREQ, (float)(i+1)/NUM_BINS));
            
            float max = 0;
            
            for (int j = 0; j < smoothedSpectrum.length; j++) {
                float freq = j * SAMPLE_RATE / FFT_SIZE;
                if (freq >= startFreq && freq < endFreq) {
                    max = Math.max(max, smoothedSpectrum[j]);
                }
            }
            
            binValues[i] = max;
        }
        
        return binValues;
    }
    
    // Getters for technical analysis
    public float[] getRawSpectrum() {
        return spectrum.clone();
    }
    
    public float[] getSmoothedSpectrum() {
        return smoothedSpectrum.clone();
    }
    
    public int getFFTSize() {
        return FFT_SIZE;
    }
    
    public int getSampleRate() {
        return SAMPLE_RATE;
    }
    
    public int getMinFreq() {
        return MIN_FREQ;
    }
    
    public int getMaxFreq() {
        return MAX_FREQ;
    }
    
    public int getNumBins() {
        return NUM_BINS;
    }
}
