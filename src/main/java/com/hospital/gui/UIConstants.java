package com.hospital.gui;

import java.awt.*;

/**
 * Hằng số giao diện: màu sắc, font chữ, kích thước.
 */
public class UIConstants {

    // ─── Màu chủ đạo ──────────────────────────────────────────────────────────
    public static final Color PRIMARY_RED      = new Color(192, 57,  43);   // #C0392B
    public static final Color PRIMARY_RED_DARK = new Color(150, 40,  27);   // hover
    public static final Color PRIMARY_RED_LIGHT= new Color(231, 76,  60);   // #E74C3C accent
    public static final Color RED_BG_SOFT      = new Color(253, 237, 236);  // nền mềm đỏ nhạt

    // ─── Sidebar ──────────────────────────────────────────────────────────────
    public static final Color SIDEBAR_BG       = new Color(28,  35,  49);   // #1C2331
    public static final Color SIDEBAR_ITEM_HOVER = new Color(40,  50,  68);
    public static final Color SIDEBAR_ACTIVE   = PRIMARY_RED;
    public static final Color SIDEBAR_TEXT     = new Color(189, 195, 199);  // #BDC3C7
    public static final Color SIDEBAR_TEXT_ACTIVE = Color.WHITE;

    // ─── Nền ─────────────────────────────────────────────────────────────────
    public static final Color CONTENT_BG       = new Color(245, 246, 250);  // #F5F6FA
    public static final Color WHITE            = Color.WHITE;
    public static final Color CARD_BG          = Color.WHITE;

    // ─── Văn bản ──────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY     = new Color(44,  62,  80);   // #2C3E50
    public static final Color TEXT_SECONDARY   = new Color(127, 140, 141);  // #7F8C8D
    public static final Color TEXT_MUTED       = new Color(189, 195, 199);

    // ─── Trạng thái ───────────────────────────────────────────────────────────
    public static final Color STATUS_WAITING   = new Color(230, 126, 34);   // cam
    public static final Color STATUS_EXAMINING = new Color(52,  152, 219);  // xanh
    public static final Color STATUS_DONE      = new Color(39,  174, 96);   // xanh lá
    public static final Color STATUS_CANCEL    = new Color(149, 165, 166);  // xám

    // ─── Khác ────────────────────────────────────────────────────────────────
    public static final Color BORDER_COLOR     = new Color(220, 221, 226);
    public static final Color TABLE_HEADER_BG  = new Color(248, 249, 250);
    public static final Color TABLE_ROW_ALT    = new Color(252, 252, 252);
    public static final Color SUCCESS_GREEN    = new Color(39,  174, 96);
    public static final Color WARNING_ORANGE   = new Color(230, 126, 34);

    // ─── Font ────────────────────────────────────────────────────────────────
    public static final String FONT_NAME       = "Segoe UI";
    public static final Font   FONT_TITLE      = new Font(FONT_NAME, Font.BOLD,  22);
    public static final Font   FONT_SUBTITLE   = new Font(FONT_NAME, Font.BOLD,  15);
    public static final Font   FONT_LABEL      = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font   FONT_SMALL      = new Font(FONT_NAME, Font.PLAIN, 11);
    public static final Font   FONT_BOLD       = new Font(FONT_NAME, Font.BOLD,  13);
    public static final Font   FONT_BUTTON     = new Font(FONT_NAME, Font.BOLD,  13);
    public static final Font   FONT_SIDEBAR    = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font   FONT_NUMBER_BIG = new Font(FONT_NAME, Font.BOLD,  28);

    // ─── Kích thước ──────────────────────────────────────────────────────────
    public static final int    SIDEBAR_WIDTH   = 210;
    public static final int    TOPBAR_HEIGHT   = 60;
    public static final int    CARD_RADIUS     = 12;

    private UIConstants() {}
}
