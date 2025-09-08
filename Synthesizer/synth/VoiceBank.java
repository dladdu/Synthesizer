package synth;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import synth.ui.*;

public class VoiceBank {
    private final List<Voice> voices = new CopyOnWriteArrayList<>();
    private final Map<Double, Voice> activeVoices = new ConcurrentHashMap<>();
    private final ControlPanel controls;
    private final GainMeter gainMeter;
    private final ModulationPanel mod;
    private long lastMixTime = 0;
    private static final long MAX_GAP_MS = 50;
    private static final int MAX_VOICE_AGE = 1000; // Maximum age in milliseconds before removing a voice
    private static final double MAX_VOLUME = 1.0; // Increased from 0.8 to 1.0

    public VoiceBank(ControlPanel controls, GainMeter gainMeter, ModulationPanel modPanel) {
        this.controls = controls;
        this.gainMeter = gainMeter;
        this.mod = modPanel;
        if (modPanel != null) {
            modPanel.lfoEnabled.setSelected(false);
            modPanel.filterEnabled.setSelected(false);
        }
    }

    public void noteOn(double freq) {
        if (activeVoices.containsKey(freq)) {
            // If note is already playing, restart it
            Voice existingVoice = activeVoices.get(freq);
            voices.remove(existingVoice);
            activeVoices.remove(freq);
        }

        int atk = controls.attackKnob.getValue() * 10;
        int dec = controls.decayKnob.getValue() * 10;
        int sus = controls.sustainKnob.getValue() * 10;
        int rel = controls.releaseKnob.getValue() * 10;

        Synth.Waveform wave1 = controls.osc1Waveform();
        Synth.Waveform wave2 = controls.osc2Waveform();
        if (wave1 == null) wave1 = Synth.Waveform.SINE;
        if (wave2 == null) wave2 = Synth.Waveform.SINE;

        Voice voice = new Voice(freq, wave1, freq, wave2, controls, atk, dec, sus, rel, gainMeter, mod);
        voices.add(voice);
        activeVoices.put(freq, voice);
    }

    public void noteOff(double freq) {
        Voice voice = activeVoices.get(freq);
        if (voice != null) {
            voice.noteOff();
            activeVoices.remove(freq);
        }
    }

    public byte[] mixVoices() {
        byte[] mixed = new byte[Synth.BUFFER_SIZE * 2];
        boolean hasActiveVoices = false;

        // Mix all active voices
        for (Voice voice : voices) {
            if (voice.isActive()) {
                byte[] buffer = voice.nextBuffer();
                if (buffer != null) {
                    hasActiveVoices = true;
                    for (int i = 0; i < buffer.length; i += 2) {
                        // Convert bytes to 16-bit PCM
                        int sample1 = (buffer[i] & 0xFF) | (buffer[i + 1] << 8);
                        int sample2 = (mixed[i] & 0xFF) | (mixed[i + 1] << 8);
                        
                        // Mix without volume scaling
                        int mixedSample = sample1 + sample2;
                        
                        // Clamp to 16-bit range
                        mixedSample = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, mixedSample));
                        
                        // Convert back to bytes
                        mixed[i] = (byte)(mixedSample & 0xFF);
                        mixed[i + 1] = (byte)((mixedSample >> 8) & 0xFF);
                    }
                }
            }
        }

        // Remove finished voices
        voices.removeIf(voice -> !voice.isActive() && voice.getAge() > MAX_VOICE_AGE);

        // Update gain meter
        if (hasActiveVoices) {
            gainMeter.repaint();
        }

        return mixed;
    }

    public int getActiveVoiceCount() {
        return activeVoices.size();
    }
}