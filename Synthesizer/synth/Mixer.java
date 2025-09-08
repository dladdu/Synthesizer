// ===============================
// Mixer.java
// ===============================
package synth;

import javax.sound.sampled.*;
import synth.ui.ControlPanel;
import synth.ui.SpectrumAnalyzer;

class Mixer {
    private static SourceDataLine line;
    public static final int SAMPLE_RATE = 44100;
    private static volatile boolean running = false;
    private static final int BUFFER_SIZE = 1024;
    private static final int NUM_BUFFERS = 2;
    private static final long BUFFER_DURATION_NS = (long)((BUFFER_SIZE * 1_000_000_000.0) / SAMPLE_RATE);
    private static final long MAX_BUFFER_WRITE_NS = BUFFER_DURATION_NS * 2;
    private static SpectrumAnalyzer spectrumAnalyzer;

    public static void start(VoiceBank voiceBank, ControlPanel controls, SpectrumAnalyzer spectrum) throws LineUnavailableException {
        spectrumAnalyzer = spectrum;
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("No supported audio format found.");
        }

        System.out.println("Using audio format: " + format);
        System.out.println("Sample rate: " + format.getSampleRate());
        System.out.println("Sample size in bits: " + format.getSampleSizeInBits());
        System.out.println("Channels: " + format.getChannels());
        System.out.println("Frame size: " + format.getFrameSize());
        System.out.println("Frame rate: " + format.getFrameRate());
        System.out.println("Big endian: " + format.isBigEndian());

        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format, BUFFER_SIZE * NUM_BUFFERS);
        line.start();

        running = true;

        Thread audioThread = new Thread(() -> {
            long nextBufferTime = System.nanoTime();
            
            while (running) {
                try {
                    long currentTime = System.nanoTime();
                    long timeToNextBuffer = nextBufferTime - currentTime;
                    
                    if (timeToNextBuffer < MAX_BUFFER_WRITE_NS) {
                        byte[] buffer = voiceBank.mixVoices();
                        if (buffer != null) {
                            // Update spectrum analyzer with the exact same buffer that's being played
                            if (spectrumAnalyzer != null) {
                                spectrumAnalyzer.updateSpectrum(buffer);
                            }
                            line.write(buffer, 0, buffer.length);
                        } else {
                            // Write silence if no audio
                            byte[] silence = new byte[BUFFER_SIZE * 2];
                            // Update spectrum analyzer with silence
                            if (spectrumAnalyzer != null) {
                                spectrumAnalyzer.updateSpectrum(silence);
                            }
                            line.write(silence, 0, silence.length);
                        }
                        nextBufferTime += BUFFER_DURATION_NS;
                    }

                    // Sleep until next buffer time
                    long sleepTime = (nextBufferTime - System.nanoTime()) / 1_000_000;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                } catch (Exception e) {
                    System.err.println("Error in audio thread: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        audioThread.setDaemon(true);
        audioThread.setPriority(Thread.MAX_PRIORITY);
        audioThread.start();
    }

    public static void stop() {
        System.out.println("Stopping audio output");
        running = false;
        if (line != null) {
            byte[] silence = new byte[BUFFER_SIZE * 2];
            line.write(silence, 0, silence.length);
            line.drain();
            line.close();
        }
    }
}