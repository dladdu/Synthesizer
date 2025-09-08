package synth;

public class Oscillator {
    private final Synth.Waveform waveform;
    private double frequency;
    private double phase;
    private static final double TWO_PI = 2.0 * Math.PI;
    private static final double SAMPLE_RATE = Synth.SAMPLE_RATE;
    private static final double NYQUIST = SAMPLE_RATE / 2.0;

    public Oscillator(double frequency, Synth.Waveform waveform) {
        this.frequency = frequency;
        this.waveform = waveform;
        this.phase = 0.0;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public double[] nextBuffer(double modFreq, float volume, long sampleIndex, int unison, double detuneAmount) {
        double[] buffer = new double[Synth.BUFFER_SIZE];
        
        // Ensure frequency is below Nyquist
        modFreq = Math.min(modFreq, NYQUIST);
        
        // If unison is 1, use the original single oscillator approach
        if (unison == 1) {
            double phaseIncrement = (modFreq * TWO_PI) / SAMPLE_RATE;
            double currentPhase = phase;
            
            for (int i = 0; i < buffer.length; i++) {
                double value;
                switch (waveform) {
                    case SINE:
                        value = Math.sin(currentPhase);
                        break;
                    case SQUARE:
                        value = Math.sin(currentPhase) >= 0 ? 1.0 : -1.0;
                        break;
                    case SAW:
                        value = (currentPhase / Math.PI) - 1.0;
                        break;
                    case TRIANGLE:
                        value = Math.abs((currentPhase / Math.PI) - 1.0) * 2.0 - 1.0;
                        break;
                    default:
                        value = Math.sin(currentPhase);
                }
                
                buffer[i] = value * volume;
                currentPhase += phaseIncrement;
                if (currentPhase >= TWO_PI) {
                    currentPhase -= TWO_PI;
                }
            }
            
            phase = currentPhase;
        } else {
            // Serum-like unison - generate multiple detuned voices
            double[] sum = new double[Synth.BUFFER_SIZE];
            double unisonDetune = detuneAmount * 50; // Reduced to 0-50 cents range for smoother sound
            
            for (int voice = 0; voice < unison; voice++) {
                double detunedFreq = modFreq;
                
                // Only apply detuning if detune knob > 0
                if (unisonDetune > 0) {
                    // Calculate voice position (-1 to 1 for spreading)
                    double voicePos = (voice - (unison - 1) / 2.0) / Math.max(1, unison - 1);
                    
                    // Calculate detuned frequency for this voice with smoothing
                    double detuneCents = voicePos * unisonDetune;
                    double detuneRatio = Math.pow(2.0, detuneCents / 1200.0);
                    detunedFreq = modFreq * detuneRatio;
                    
                    // Apply anti-aliasing and frequency limiting
                    detunedFreq = Math.min(detunedFreq, NYQUIST * 0.9);
                    detunedFreq = Math.max(detunedFreq, 20.0); // Prevent sub-audio frequencies
                }
                
                // Generate the waveform for this voice with phase randomization
                double phaseIncrement = (detunedFreq * TWO_PI) / SAMPLE_RATE;
                // Add slight phase offset to break up beating patterns
                double phaseOffset = (voice * 0.618033988749) * TWO_PI; // Golden ratio
                double currentPhase = ((sampleIndex * phaseIncrement) + phaseOffset) % TWO_PI;
                
                for (int i = 0; i < buffer.length; i++) {
                    double value;
                    switch (waveform) {
                        case SINE:
                            value = Math.sin(currentPhase);
                            break;
                        case SQUARE:
                            // Softened square wave to reduce artifacts
                            value = Math.tanh(Math.sin(currentPhase) * 3.0);
                            break;
                        case SAW:
                            // Band-limited sawtooth to reduce aliasing
                            value = (currentPhase / Math.PI) - 1.0;
                            // Apply soft clipping
                            value = Math.tanh(value * 0.8);
                            break;
                        case TRIANGLE:
                            // Smoother triangle wave
                            double triPhase = currentPhase % TWO_PI;
                            if (triPhase < Math.PI) {
                                value = (triPhase / Math.PI) * 2.0 - 1.0;
                            } else {
                                value = 3.0 - (triPhase / Math.PI) * 2.0;
                            }
                            // Apply soft clipping
                            value = Math.tanh(value * 0.9);
                            break;
                        default:
                            value = Math.sin(currentPhase);
                    }
                    
                    sum[i] += value;
                    currentPhase += phaseIncrement;
                    if (currentPhase >= TWO_PI) {
                        currentPhase -= TWO_PI;
                    }
                }
            }
            
            // Simple normalization
            double scale = 1.0 / unison;
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = (sum[i] * scale) * volume;
            }
        }
        
        return buffer;
    }
    
    // Backward compatibility method for single voice
    public double[] nextBuffer(double modFreq, float volume, long sampleIndex, int unison) {
        return nextBuffer(modFreq, volume, sampleIndex, unison, 0.0);
    }
} 