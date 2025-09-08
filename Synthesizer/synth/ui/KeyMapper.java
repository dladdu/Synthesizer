package synth.ui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import synth.*;

public class KeyMapper implements KeyEventDispatcher {
    private final VoiceBank voiceBank;
    private final Map<Character, Double> keyToFreq = new HashMap<>();
    private final Map<Character, Boolean> keyState = new HashMap<>();

    public KeyMapper(VoiceBank voiceBank) {
        this.voiceBank = voiceBank;
        initMap();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
    }

    private void initMap() {
        keyToFreq.put('a', 261.63);
        keyToFreq.put('w', 277.18);
        keyToFreq.put('s', 293.66);
        keyToFreq.put('e', 311.13);
        keyToFreq.put('d', 329.63);
        keyToFreq.put('f', 349.23);
        keyToFreq.put('t', 369.99);
        keyToFreq.put('g', 392.00);
        keyToFreq.put('y', 415.30);
        keyToFreq.put('h', 440.00);
        keyToFreq.put('u', 466.16);
        keyToFreq.put('j', 493.88);
        keyToFreq.put('k', 523.25);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            char key = Character.toLowerCase(e.getKeyChar());
            if (keyToFreq.containsKey(key) && !Boolean.TRUE.equals(keyState.get(key))) {

                voiceBank.noteOn(keyToFreq.get(key));
                keyState.put(key, true);
            }
        } else if (e.getID() == KeyEvent.KEY_RELEASED) {
            char key = Character.toLowerCase(e.getKeyChar());
            if (keyToFreq.containsKey(key)) {
  
                voiceBank.noteOff(keyToFreq.get(key));
                keyState.put(key, false);
            }
        }
        return false; // Let event propagate
    }
}