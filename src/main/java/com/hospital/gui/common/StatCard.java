package com.hospital.gui.common;

import javax.swing.*;
import java.awt.*;

public class StatCard extends RoundedPanel {

    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private final Color  accentColor;

    public StatCard(String title, String value, String subtitle, String icon, Color accentColor) {
        super(UIConstants.CARD_RADIUS);
        this.accentColor = accentColor;

        setBackground(UIConstants.CARD_BG);
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Icon background
        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        lblIcon.setForeground(accentColor);
        lblIcon.setPreferredSize(new Dimension(56, 56));

        JPanel iconWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(lighten(accentColor, 0.88f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        iconWrapper.setOpaque(false);
        iconWrapper.setPreferredSize(new Dimension(56, 56));
        iconWrapper.add(lblIcon, BorderLayout.CENTER);

        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(UIConstants.FONT_SMALL);
        lblTitle.setForeground(UIConstants.TEXT_SECONDARY);

        lblValue = new JLabel(value);
        lblValue.setFont(UIConstants.FONT_NUMBER_BIG);
        lblValue.setForeground(UIConstants.TEXT_PRIMARY);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UIConstants.FONT_SMALL);
        lblSubtitle.setForeground(accentColor);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(lblValue);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblSubtitle);

        add(textPanel, BorderLayout.CENTER);
        add(iconWrapper, BorderLayout.EAST);
    }

    public void updateValue(String value) {
        lblValue.setText(value);
        repaint();
    }

    public void updateSubtitle(String sub) {
        lblSubtitle.setText(sub);
        repaint();
    }

    private static Color lighten(Color c, float factor) {
        int r = (int) (c.getRed()   + (255 - c.getRed())   * factor);
        int g = (int) (c.getGreen() + (255 - c.getGreen()) * factor);
        int b = (int) (c.getBlue()  + (255 - c.getBlue())  * factor);
        return new Color(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }
}
