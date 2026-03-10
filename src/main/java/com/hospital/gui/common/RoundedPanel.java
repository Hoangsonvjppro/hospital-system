package com.hospital.gui.common;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {

    private final int radius;
    private final boolean drawShadow;

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

        int w = getWidth(), h = getHeight();

        if (drawShadow) {
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(2, 3, w - 4, h - 4, radius, radius);
        }

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, w - 1, h - 2, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}
