package com.hospital.gui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Panel bo góc tùy chỉnh với đổ bóng nhẹ.
 */
public class RoundedPanel extends JPanel {

    private final int radius;
    private Color shadowColor = new Color(0, 0, 0, 18);
    private boolean drawShadow;

    public RoundedPanel(int radius) {
        this(radius, true);
    }

    public RoundedPanel(int radius, boolean drawShadow) {
        this.radius     = radius;
        this.drawShadow = drawShadow;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 0, y = 0, w = getWidth(), h = getHeight();

        // Bóng đổ (shadow)
        if (drawShadow) {
            g2.setColor(shadowColor);
            g2.fillRoundRect(x + 2, y + 3, w - 4, h - 4, radius, radius);
        }

        // Nền panel
        g2.setColor(getBackground());
        g2.fillRoundRect(x, y, w - 1, h - 2, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}
