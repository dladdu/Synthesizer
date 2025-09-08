package synth;

import synth.ui.ControlPanel;
import synth.ui.ModulationPanel;

public class Voice {
    private final Oscillator osc1;
    private final Oscillator osc2;
    private final ControlPanel controls;
    private final GainMeter gainMeter;
    private final ModulationPanel mod;
    private long startTime;
    private boolean active;
    private long lastBufferTime;
    private long sampleIndex = 0;
    private static final double MAX_VOLUME = 0.25;

    // Add filter
    private synth.dsp.BiquadFilter filter;

    // ADSR parameters
    private int attackSamples;
    private int decaySamples;
    private int sustainSamples;
    private int releaseSamples;
    private double sustainLevel;
    private int stage = 0; // 0=attack, 1=decay, 2=sustain, 3=release
    private boolean noteOff = false;
    private long releaseStartIndex;
    private double envReleaseStart;
    private double prevEnv = 0.0;
    private static final double ENVELOPE_SMOOTHING = 0.99;

    public Voice(double freq1, Synth.Waveform wave1, double freq2, Synth.Waveform wave2,
                ControlPanel controls, int atk, int dec, int sus, int rel,
                GainMeter gainMeter, ModulationPanel mod) {
        this.osc1 = new Oscillator(freq1, wave1);
        this.osc2 = new Oscillator(freq2, wave2);
        this.controls = controls;
        this.gainMeter = gainMeter;
        this.mod = mod;
        this.startTime = System.nanoTime();
        this.active = true;
        this.lastBufferTime = startTime;

        // Initialize filter
        this.filter = new synth.dsp.BiquadFilter(
            synth.dsp.BiquadFilter.Type.LOWPASS,
            Synth.SAMPLE_RATE,
            1000,  // Default cutoff
            0.0    // Default Q (resonance)
        );

        // Convert ADSR times to samples
        this.attackSamples = (int)(atk * Synth.SAMPLE_RATE / 1000.0);
        this.decaySamples = (int)(dec * Synth.SAMPLE_RATE / 1000.0);
        this.sustainSamples = (int)(sus * Synth.SAMPLE_RATE / 1000.0);
        this.releaseSamples = (int)(rel * Synth.SAMPLE_RATE / 1000.0);
        this.sustainLevel = controls.sustainKnob.getValue() / 100.0;
    }

    public boolean isActive() {
        return active;
    }

    public long getAge() {
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    public void noteOff() {
        if (!noteOff) {
            noteOff = true;
            stage = 3;
            releaseStartIndex = sampleIndex;
            envReleaseStart = prevEnv;
        }
    }

    private double getEnvelope() {
        long idx = sampleIndex;
        double env;
        
        if (stage == 0) { // Attack
            if (idx < attackSamples) {
                env = idx / (double)attackSamples;
            } else {
                stage = 1;
                env = 1.0;
            }
        } else if (stage == 1) { // Decay
            long relIdx = idx - attackSamples;
            if (relIdx < decaySamples) {
                env = 1.0 - (1.0 - sustainLevel) * (relIdx / (double)decaySamples);
            } else {
                stage = 2;
                env = sustainLevel;
            }
        } else if (stage == 2) { // Sustain
            if (noteOff) {
                stage = 3;
                releaseStartIndex = idx;
                envReleaseStart = prevEnv;
                env = sustainLevel;
            } else {
                env = sustainLevel;
            }
        } else { // Release
            long rel = idx - releaseStartIndex;
            if (rel < releaseSamples) {
                // Exponential release for smoother fade out
                double t = rel / (double)releaseSamples;
                env = envReleaseStart * Math.exp(-5.0 * t);
            } else {
                env = 0.0;
                active = false;
            }
        }

        // Smooth envelope transitions
        double smoothedEnv = (prevEnv * ENVELOPE_SMOOTHING + env * (1.0 - ENVELOPE_SMOOTHING));
        prevEnv = smoothedEnv;
        return smoothedEnv;
    }

    public byte[] nextBuffer() {
        if (!active) {
            return null;
        }

        float vol1 = controls.osc1Slider.getValue() / 100f;
        float vol2 = controls.osc2Slider.getValue() / 100f;
        float master = controls.masterSlider.getValue() / 100f;

        // Calculate frequency with detune and octave adjustment
        double detune1 = controls.detune1Slider.getValue() / 100.0; // Convert to 0.0 to 1.0 range
        double detune2 = controls.detune2Slider.getValue() / 100.0; // Convert to 0.0 to 1.0 range
        double octave1 = controls.osc1Octave.getValue(); // -2 to 2 range
        double octave2 = controls.osc2Octave.getValue(); // -2 to 2 range
        int voices1 = controls.osc1Voices.getValue(); // 1 to 16 range
        int voices2 = controls.osc2Voices.getValue(); // 1 to 16 range
        
        double baseFreq1 = osc1.getFrequency() * Math.pow(2, octave1);
        double baseFreq2 = osc2.getFrequency() * Math.pow(2, octave2);

        byte[] out = new byte[Synth.BUFFER_SIZE * 2];
        double[] filteredSamples = new double[Synth.BUFFER_SIZE];

        // Generate clean sine waves with multiple voices and detune control
        double[] osc1Buffer = osc1.nextBuffer(baseFreq1, vol1, sampleIndex, voices1, detune1);
        double[] osc2Buffer = osc2.nextBuffer(baseFreq2, vol2, sampleIndex, voices2, detune2);

        // Update filter parameters if enabled
        if (mod != null && mod.filterEnabled.isSelected()) {
            // Convert filter type
            synth.dsp.BiquadFilter.Type type;
            switch (mod.filterType.getSelectedIndex()) {
                case 1: type = synth.dsp.BiquadFilter.Type.HIGHPASS; break;
                case 2: type = synth.dsp.BiquadFilter.Type.BANDPASS; break;
                default: type = synth.dsp.BiquadFilter.Type.LOWPASS; break;
            }
            
            // Update filter parameters
            double cutoff = mod.cutoffSlider.getValue();
            // Map cutoff slider (0-100) to frequency (20Hz - 20000Hz) logarithmically
            cutoff = 20.0 * Math.pow(1000.0, cutoff / 100.0);  // This gives us 20Hz to 20kHz range
            
            // Clamp cutoff frequency to prevent distortion
            cutoff = Math.max(20.0, Math.min(20000.0, cutoff));
            
            double resonance = mod.resonanceSlider.getValue() / 10.0;  // Convert to 0-10 range
            // Clamp resonance to prevent instability
            resonance = Math.max(0.1, Math.min(10.0, resonance));
            
            filter.update(type, Synth.SAMPLE_RATE, cutoff, resonance);
        }

        for (int i = 0; i < Synth.BUFFER_SIZE; i++) {
            // Get envelope value
            double env = getEnvelope();
            
            // Mix oscillators with envelope
            double sample = (osc1Buffer[i] * vol1 + osc2Buffer[i] * vol2);
            sample *= env * MAX_VOLUME;
            
            // Apply filter if enabled
            if (mod != null && mod.filterEnabled.isSelected()) {
                filteredSamples[i] = filter.process(sample);
            } else {
                filteredSamples[i] = sample;
            }
            
            // Apply master volume
            filteredSamples[i] *= master;

            // Convert to 16-bit PCM
            int pcm = (int)(filteredSamples[i] * Short.MAX_VALUE);
            pcm = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, pcm));
            
            // Write 16-bit PCM (little-endian)
            int outIndex = i * 2;
            out[outIndex] = (byte)(pcm & 0xFF);
            out[outIndex + 1] = (byte)((pcm >> 8) & 0xFF);
        }

        sampleIndex += Synth.BUFFER_SIZE;
        return out;
    }
}