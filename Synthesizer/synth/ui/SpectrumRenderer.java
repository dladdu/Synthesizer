package synth.ui;

import java.awt.*;

public class SpectrumRenderer {
    private static final float REFERENCE_LEVEL = 1.0f;  // 1.0 represents 0dB (full scale)
    private static final float MAX_DB = 6.0f;   // +6dB for clipping detection
    private static final float MIN_DB = -60.0f; // -60dB for dynamic range
    private static final float DB_RANGE = MAX_DB - MIN_DB;
    
    public void renderSpectrum(Graphics2D g2, int width, int height, float[] binValues, 
                              int minFreq, int maxFreq, int numBins) {
        // Draw background grid
        g2.setColor(new Color(20, 20, 20));
        for (int i = 0; i < 10; i++) {
            int y = height * i / 10;
            g2.drawLine(0, y, width, y);
        }
        
        // Draw frequency labels
        g2.setColor(Color.GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        int[] freqLabels = {20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000};
        for (int freq : freqLabels) {
            float x = (float) (Math.log(freq/minFreq) / Math.log(maxFreq/minFreq) * width);
            g2.drawString(freq + "Hz", x - 15, height - 5);
        }
        
        // Draw amplitude scale with 0dB as reference
        g2.drawString("+6dB", 5, 15);      // Clipping level
        g2.drawString("0dB", 5, height/2);  // Full scale reference
        g2.drawString("-60dB", 5, height - 5); // Dynamic range bottom
        
        // Draw frequency spectrum with gradient
        float binWidth = (float)width / numBins;
        float barWidth = binWidth * 0.9f; // Make bars fill 90% of bin width for fuller look
        float barSpacing = binWidth * 0.1f; // 10% spacing between bars
        
        // Draw bins
        for (int i = 0; i < numBins; i++) {
            float x = i * binWidth + barSpacing/2; // Center bars within bins
            float magnitude = binValues[i];
            
            // Convert to dB scale where 0dB = full scale (1.0)
            float db = (float)(20 * Math.log10(magnitude / REFERENCE_LEVEL));
            float normalizedDb = Math.max(MIN_DB, Math.min(MAX_DB, db));
            float y = height * (1 - (normalizedDb - MIN_DB) / DB_RANGE);
            
            // Create gradient based on frequency
            float hue = (float)i / numBins * 0.7f;
            Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
            
            // Change color to red if clipping (above 0dB)
            if (db > 0) {
                color = Color.RED;
            }
            
            g2.setColor(color);
            
            // Draw fuller bars with rounded corners effect
            if (barWidth > 2) {
                // Draw main bar
                g2.fillRect((int)x, (int)y, (int)barWidth, height - (int)y);
                
                // Add subtle highlight at top for 3D effect
                Color highlightColor = color.brighter();
                g2.setColor(highlightColor);
                g2.fillRect((int)x, (int)y, (int)barWidth, Math.max(2, (height - (int)y) / 8));
            } else {
                // For very narrow bars, just draw a line
                g2.setStroke(new BasicStroke(barWidth));
                g2.drawLine((int)(x + barWidth/2), (int)y, (int)(x + barWidth/2), height);
            }
        }
    }
}
