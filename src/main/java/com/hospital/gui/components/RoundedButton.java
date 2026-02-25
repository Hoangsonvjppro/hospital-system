package com.hospital.gui.components;

import com.hospital.gui.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Nút bo góc với hiệu ứng hover.
 */
public class RoundedButton extends JButton {

    private Color normalColor;
    private Color hoverColor;
    private Color pressColor;
    private Color currentColor;
    private final int radius;

    public RoundedButton(String text) {
        this(text, UIConstants.PRIMARY_RED, UIConstants.PRIMARY_RED_DARK, 8);
    }

    public RoundedButton(String text, Color normal, Color hover, int radius) {
        super(text);
        this.normalColor  = normal;
        this.hoverColor   = hover;
        this.pressColor   = hover.darker();
        this.currentColor = normal;
        this.radius       = radius;

        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setForeground(Color.WHITE);
        setFont(UIConstants.FONT_BUTTON);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                currentColor = hoverColor; repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                currentColor = normalColor; repaint();
            }
            @Override public void mousePressed(MouseEvent e) {
                currentColor = pressColor; repaint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                currentColor = hoverColor; repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(currentColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }

    public void setColors(Color normal, Color hover) {
        this.normalColor  = normal;
        this.hoverColor   = hover;
        this.pressColor   = hover.darker();
        this.currentColor = normal;
        repaint();
    }
}
