package com.hospital.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Quản lý icon PNG cho GUI.
 * Load từ /imageicon/ trong resources, cache lại để tái sử dụng.
 */
public final class IconManager {

    private static final String ICON_DIR = "/imageicon/";
    private static final Map<String, ImageIcon> cache = new ConcurrentHashMap<>();

    private IconManager() {}

    /**
     * Tô màu đen cho icon (giữ nguyên alpha).
     */
    private static BufferedImage tintBlack(Image src) {
        int w = src.getWidth(null);
        int h = src.getHeight(null);
        if (w <= 0 || h <= 0) return null;
        BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = buf.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = buf.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                // giữ alpha, đổi RGB thành đen (0,0,0)
                buf.setRGB(x, y, (alpha << 24));
            }
        }
        return buf;
    }

    /**
     * Lấy icon theo tên file (không cần extension).
     * Ví dụ: getIcon("dashboard") → load /imageicon/dashboard.png
     */
    public static ImageIcon getIcon(String name) {
        ImageIcon cached = cache.get(name);
        if (cached != null) return cached;
        URL url = IconManager.class.getResource(ICON_DIR + name + ".png");
        ImageIcon icon;
        if (url != null) {
            Image src = new ImageIcon(url).getImage();
            BufferedImage tinted = tintBlack(src);
            icon = tinted != null ? new ImageIcon(tinted) : new ImageIcon(url);
        } else {
            System.err.println("Icon not found: " + name);
            icon = new ImageIcon();
        }
        cache.put(name, icon);
        return icon;
    }

    /**
     * Lấy icon với kích thước tùy chỉnh (màu đen).
     */
    public static ImageIcon getIcon(String name, int width, int height) {
        String key = name + "_" + width + "x" + height;
        ImageIcon cached = cache.get(key);
        if (cached != null) return cached;
        ImageIcon original = getIcon(name);
        ImageIcon scaled;
        if (original.getImage() == null) {
            scaled = original;
        } else {
            scaled = new ImageIcon(original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }
        cache.put(key, scaled);
        return scaled;
    }

    /**
     * Lấy icon GỐC (màu trắng) — dùng cho sidebar nền tối.
     */
    public static ImageIcon getWhiteIcon(String name) {
        String key = name + "_white";
        ImageIcon cached = cache.get(key);
        if (cached != null) return cached;
        URL url = IconManager.class.getResource(ICON_DIR + name + ".png");
        ImageIcon icon;
        if (url != null) {
            icon = new ImageIcon(url);
        } else {
            icon = new ImageIcon();
        }
        cache.put(key, icon);
        return icon;
    }

    /**
     * Lấy icon GỐC (màu trắng) với kích thước tùy chỉnh — dùng cho sidebar nền tối.
     */
    public static ImageIcon getWhiteIcon(String name, int width, int height) {
        String key = name + "_white_" + width + "x" + height;
        ImageIcon cached = cache.get(key);
        if (cached != null) return cached;
        ImageIcon original = getWhiteIcon(name);
        ImageIcon scaled;
        if (original.getImage() == null) {
            scaled = original;
        } else {
            scaled = new ImageIcon(original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }
        cache.put(key, scaled);
        return scaled;
    }
}
