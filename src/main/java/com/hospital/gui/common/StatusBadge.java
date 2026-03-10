package com.hospital.gui.common;

import javax.swing.*;
import java.awt.*;

public class StatusBadge extends JLabel {

    private Color bgColor = UIConstants.STATUS_WAITING;

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

    private void applyStyle(String s) {
        bgColor = switch (s) {
            case "WAITING", "CHỜ KHÁM", "Chờ thanh toán", "PENDING", "ORDERED" -> UIConstants.STATUS_WAITING;
            case "IN_PROGRESS", "ĐANG KHÁM", "Mới", "IN PROGRESS" -> UIConstants.STATUS_EXAMINING;
            case "COMPLETED", "XONG", "Đã xác nhận", "Đã khám", "Đã thanh toán", "PAID", "DISPENSED", "Hoạt động" -> UIConstants.STATUS_DONE;
            case "CANCELLED", "Hủy", "Vô hiệu" -> UIConstants.STATUS_CANCEL;
            default -> UIConstants.TEXT_SECONDARY;
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
