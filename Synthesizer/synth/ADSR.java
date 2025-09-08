package synth;

public class ADSR {
    private final int attackSamples;
    private final int decaySamples;
    private final int sustainSamples;
    private final int releaseSamples;
    private final double sustainLevel;
    private boolean noteOff;
    private long sampleIndex;
    private long releaseStartIndex;
    private static final double SAMPLE_RATE = Synth.SAMPLE_RATE;
    private static final double ENVELOPE_SMOOTHING = 0.95;
    private static final double TWO_PI = 2.0 * Math.PI;

    public ADSR(int attackMs, int decayMs, int sustainMs, int releaseMs) {
        this.attackSamples = Math.max(1, (int)(attackMs * SAMPLE_RATE / 1000.0));
        this.decaySamples = Math.max(1, (int)(decayMs * SAMPLE_RATE / 1000.0));
        this.sustainSamples = Math.max(1, (int)(sustainMs * SAMPLE_RATE / 1000.0));
        this.releaseSamples = Math.max(1, (int)(releaseMs * SAMPLE_RATE / 1000.0));
        this.sustainLevel = 0.5;
        this.sampleIndex = 0;
        this.noteOff = false;
    }

    public void noteOff() {
        if (!noteOff) {
            noteOff = true;
            releaseStartIndex = sampleIndex;
        } 
    }

    public boolean isFinished() {
        if (!noteOff) return false;
        return (sampleIndex - releaseStartIndex) >= releaseSamples;
    }

    public double[] nextBuffer() {
        double[] buffer = new double[Synth.BUFFER_SIZE];
        double prevEnv = 0.0;

        for (int i = 0; i < buffer.length; i++) {
            long idx = sampleIndex + i;
            double env;

            if (noteOff) {
                long relIdx = idx - releaseStartIndex;
                if (relIdx >= releaseSamples) {
                    env = 0.0;
                } else {
                    double t = (double)relIdx / releaseSamples;
                    env = sustainLevel * Math.exp(-5.0 * t);
                }
            } else {
                if (idx < attackSamples) {
                    double t = idx / (double)attackSamples;
                    env = 1.0 - Math.exp(-5.0 * t);
                } else if (idx < attackSamples + decaySamples) {
                    double t = (idx - attackSamples) / (double)decaySamples;
                    env = sustainLevel + (1.0 - sustainLevel) * Math.exp(-5.0 * t);
                } else if (idx < attackSamples + decaySamples + sustainSamples) {
                    env = sustainLevel;
                } else {
                    env = 0.0;
                }
            }

            // Smooth envelope transitions
            double smoothedEnv = (prevEnv * ENVELOPE_SMOOTHING + env * (1.0 - ENVELOPE_SMOOTHING));
            prevEnv = smoothedEnv;
            buffer[i] = smoothedEnv;
        }

        sampleIndex += buffer.length;
        return buffer;
    }
} 