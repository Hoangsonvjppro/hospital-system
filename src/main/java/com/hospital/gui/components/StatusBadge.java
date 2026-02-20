package com.hospital.gui.components;

import com.hospital.gui.UIConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Badge trạng thái (CHỜ KHÁM, ĐANG KHÁM, XONG…).
 */
public class StatusBadge extends JLabel {

    public StatusBadge(String status) {
        super(status, SwingConstants.CENTER);
        setFont(new Font(UIConstants.FONT_NAME, Font.BOLD, 11));
        setForeground(Color.WHITE);
        setOpaque(false);
        applyStyle(status);
    }

    public void setStatus(String status) {
        setText(status);
        applyStyle(status);
        repaint();
    }

    private Color bgColor = UIConstants.STATUS_WAITING;

    private void applyStyle(String s) {
        bgColor = switch (s) {
            case "CHỜ KHÁM"    -> UIConstants.STATUS_WAITING;
            case "ĐANG KHÁM"   -> UIConstants.STATUS_EXAMINING;
            case "XONG"        -> UIConstants.STATUS_DONE;
            case "Đã xác nhận" -> UIConstants.STATUS_DONE;
            case "Đã khám"     -> UIConstants.STATUS_DONE;
            case "Hủy"         -> UIConstants.STATUS_CANCEL;
            case "Mới"         -> UIConstants.STATUS_EXAMINING;
            case "Đã thanh toán" -> UIConstants.STATUS_DONE;
            case "Chờ thanh toán"-> UIConstants.STATUS_WAITING;
            default            -> UIConstants.TEXT_SECONDARY;
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 16, d.height + 6);
    }
}
