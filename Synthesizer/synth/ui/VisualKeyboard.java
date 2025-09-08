// ===============================
// VisualKeyboard.java
// ===============================
package synth.ui;

import java.awt.*;
import javax.swing.*;
import synth.VoiceBank;

public class VisualKeyboard extends JPanel {
    private final VoiceBank voiceBank;
    private final int startMidiNote;
    private final int endMidiNote;

    private static final int KEY_WIDTH = 29;
    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public VisualKeyboard(VoiceBank voiceBank, int startMidiNote, int endMidiNote) {
        this.voiceBank = voiceBank;
        this.startMidiNote = startMidiNote;
        this.endMidiNote = endMidiNote;

        int whiteKeyCount = 0;
        for (int i = startMidiNote; i <= endMidiNote; i++) {
            if (!isBlackKey(i)) {
                whiteKeyCount++;
            }
        }

        setPreferredSize(new Dimension(whiteKeyCount * KEY_WIDTH, 120));
        setBackground(Color.LIGHT_GRAY);
        setLayout(new BorderLayout());
        
        // Add padding to maintain the same height
        setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    }

    private boolean isBlackKey(int midiNote) {
        int mod = midiNote % 12;
        return mod == 1 || mod == 3 || mod == 6 || mod == 8 || mod == 10;
    }

    private String getNoteName(int midiNote) {
        return NOTE_NAMES[midiNote % 12] + (midiNote / 12 - 1); // MIDI octave starts at -1
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int whiteKeyHeight = getHeight() - 2;
        int blackKeyHeight = (int) (whiteKeyHeight * 0.6);
        int currentX = 0;

        java.util.Map<Integer, Integer> whiteKeyXMap = new java.util.HashMap<>();
        for (int i = startMidiNote; i <= endMidiNote; i++) {
            if (!isBlackKey(i)) {
                whiteKeyXMap.put(i, currentX);
                g2.setColor(Color.WHITE);
                g2.fillRect(currentX, 0, KEY_WIDTH, whiteKeyHeight);
                g2.setColor(Color.BLACK);
                g2.drawRect(currentX, 0, KEY_WIDTH, whiteKeyHeight);

                String note = getNoteName(i);
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(note);
                g2.drawString(note, currentX + (KEY_WIDTH - textWidth) / 2, whiteKeyHeight - 5);

                currentX += KEY_WIDTH;
            }
        }

        for (int i = startMidiNote; i <= endMidiNote; i++) {
            if (isBlackKey(i)) {
                int leftWhite = -1;
                switch (i % 12) {
                    case 1: leftWhite = i - 1; break;
                    case 3: leftWhite = i - 1; break;
                    case 6: leftWhite = i - 1; break;
                    case 8: leftWhite = i - 1; break;
                    case 10: leftWhite = i - 1; break;
                }
                if (whiteKeyXMap.containsKey(leftWhite)) {
                    int x = whiteKeyXMap.get(leftWhite) + (int)(KEY_WIDTH * 0.75);
                    g2.setColor(Color.BLACK);
                    g2.fillRect(x, 0, KEY_WIDTH / 2, blackKeyHeight);
                }
            }
        }
    }
}