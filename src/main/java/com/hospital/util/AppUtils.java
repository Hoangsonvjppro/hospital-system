package com.hospital.util;

import javax.swing.*;
import java.awt.*;

/**
 * Các hàm tiện ích dùng chung cho ứng dụng.
 * Common utility functions for the application.
 */
public class AppUtils {

    /**
     * Hiển thị hộp thoại thông báo.
     */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Hiển thị hộp thoại lỗi.
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Hiển thị hộp thoại xác nhận.
     */
    public static boolean showConfirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Xác nhận", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Kiểm tra chuỗi rỗng hoặc null.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
