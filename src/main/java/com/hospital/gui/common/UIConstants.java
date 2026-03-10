package com.hospital.gui.common;

import java.awt.*;

/**
 * Design System — Hằng số giao diện thống nhất toàn ứng dụng.
 */
public class UIConstants {

    // ═══════════════════════════════════════════════════════════
    //  MÀU CHỦ ĐẠO (Primary) — Blue Medical Theme
    // ═══════════════════════════════════════════════════════════
    public static final Color PRIMARY           = new Color(0,   123, 255);
    public static final Color PRIMARY_DARK      = new Color(0,   86,  179);
    public static final Color PRIMARY_LIGHT     = new Color(0,   105, 217);
    public static final Color PRIMARY_BG_SOFT   = new Color(232, 240, 254);

    // ═══════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════════════════
    public static final Color SIDEBAR_BG            = new Color(255, 255, 255);
    public static final Color SIDEBAR_HEADER_BG     = new Color(0,   123, 255);
    public static final Color SIDEBAR_ITEM_HOVER    = new Color(232, 240, 254);
    public static final Color SIDEBAR_ACTIVE        = PRIMARY;
    public static final Color SIDEBAR_TEXT          = new Color(51,  51,  51);
    public static final Color SIDEBAR_TEXT_ACTIVE   = new Color(0,   123, 255);
    public static final Color SIDEBAR_BTN_DEFAULT   = new Color(255, 255, 255);
    public static final Color SIDEBAR_BTN_HOVER     = new Color(232, 240, 254);
    public static final Color SIDEBAR_BTN_ACTIVE    = new Color(232, 240, 254);
    public static final Color SIDEBAR_SEPARATOR     = new Color(226, 232, 240);
    public static final Color SIDEBAR_SECTION_TEXT  = new Color(0,   86,  179);

    // ═══════════════════════════════════════════════════════════
    //  NỀN (Background)
    // ═══════════════════════════════════════════════════════════
    public static final Color CONTENT_BG        = new Color(245, 246, 250);
    public static final Color WHITE             = Color.WHITE;
    public static final Color CARD_BG           = Color.WHITE;

    // ═══════════════════════════════════════════════════════════
    //  VĂN BẢN (Text)
    // ═══════════════════════════════════════════════════════════
    public static final Color TEXT_PRIMARY      = new Color(44,  62,  80);
    public static final Color TEXT_SECONDARY    = new Color(127, 140, 141);
    public static final Color TEXT_MUTED        = new Color(189, 195, 199);

    // ═══════════════════════════════════════════════════════════
    //  TRẠNG THÁI (Status)
    // ═══════════════════════════════════════════════════════════
    public static final Color STATUS_WAITING    = new Color(230, 126, 34);
    public static final Color STATUS_EXAMINING  = new Color(52,  152, 219);
    public static final Color STATUS_DONE       = new Color(39,  174, 96);
    public static final Color STATUS_CANCEL     = new Color(149, 165, 166);

    // ═══════════════════════════════════════════════════════════
    //  NÚT HÀNH ĐỘNG
    // ═══════════════════════════════════════════════════════════
    public static final Color LOGOUT_COLOR      = new Color(0,   123, 255);
    public static final Color LOGOUT_HOVER      = new Color(0,   86,  179);

    // ═══════════════════════════════════════════════════════════
    //  ACCENT BLUE
    // ═══════════════════════════════════════════════════════════
    public static final Color ACCENT_BLUE       = new Color(37,  99,  235);
    public static final Color ACCENT_BLUE_DARK  = new Color(29,  78,  216);
    public static final Color ACCENT_BLUE_LIGHT = new Color(59,  130, 246);
    public static final Color ACCENT_BLUE_SOFT  = new Color(239, 246, 255);

    // ═══════════════════════════════════════════════════════════
    //  FORM / INPUT
    // ═══════════════════════════════════════════════════════════
    public static final Color FIELD_BG          = new Color(245, 247, 250);
    public static final Color FIELD_BORDER      = new Color(221, 226, 232);
    public static final Color ERROR_COLOR       = new Color(229, 57,  53);
    public static final Color ICON_MUTED        = new Color(176, 190, 197);

    // ═══════════════════════════════════════════════════════════
    //  KHÁC
    // ═══════════════════════════════════════════════════════════
    public static final Color BORDER_COLOR      = new Color(220, 221, 226);
    public static final Color TABLE_HEADER_BG   = new Color(248, 249, 250);
    public static final Color TABLE_ROW_ALT     = new Color(252, 252, 252);
    public static final Color SUCCESS_GREEN     = new Color(39,  174, 96);
    public static final Color SUCCESS_GREEN_DARK= new Color(21,  128, 61);
    public static final Color WARNING_ORANGE    = new Color(230, 126, 34);
    public static final Color DANGER_RED        = new Color(220, 53,  69);
    public static final Color DANGER_RED_DARK   = new Color(185, 43,  39);

    // ═══════════════════════════════════════════════════════════
    //  CẢNH BÁO DASHBOARD
    // ═══════════════════════════════════════════════════════════
    public static final Color ALERT_RED_BG      = new Color(254, 242, 242);
    public static final Color ALERT_RED_BORDER  = new Color(252, 165, 165);
    public static final Color ALERT_AMBER_BG    = new Color(255, 251, 235);
    public static final Color ALERT_AMBER_BORDER= new Color(252, 211, 77);
    public static final Color REVENUE_PURPLE    = new Color(124, 58,  237);

    // ═══════════════════════════════════════════════════════════
    //  FONT
    // ═══════════════════════════════════════════════════════════
    public static final String FONT_NAME            = "Segoe UI";

    public static final Font   FONT_TITLE           = new Font(FONT_NAME, Font.BOLD,  26);
    public static final Font   FONT_SUBTITLE        = new Font(FONT_NAME, Font.BOLD,  18);
    public static final Font   FONT_HEADER          = new Font(FONT_NAME, Font.BOLD,  22);
    public static final Font   FONT_SECTION         = new Font(FONT_NAME, Font.BOLD,  19);
    public static final Font   FONT_BODY            = new Font(FONT_NAME, Font.PLAIN, 17);
    public static final Font   FONT_LABEL           = new Font(FONT_NAME, Font.PLAIN, 16);
    public static final Font   FONT_CAPTION         = new Font(FONT_NAME, Font.PLAIN, 14);
    public static final Font   FONT_SMALL           = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font   FONT_OVERLINE        = new Font(FONT_NAME, Font.BOLD,  12);
    public static final Font   FONT_BOLD            = new Font(FONT_NAME, Font.BOLD,  16);
    public static final Font   FONT_BUTTON          = new Font(FONT_NAME, Font.BOLD,  16);
    public static final Font   FONT_NUMBER_BIG      = new Font(FONT_NAME, Font.BOLD,  34);
    public static final Font   FONT_ITALIC          = new Font(FONT_NAME, Font.ITALIC, 14);

    public static final Font   FONT_SIDEBAR         = new Font(FONT_NAME, Font.PLAIN, 16);
    public static final Font   FONT_SIDEBAR_HEADER  = new Font(FONT_NAME, Font.BOLD,  18);
    public static final Font   FONT_SIDEBAR_ROLE    = new Font(FONT_NAME, Font.PLAIN, 14);
    public static final Font   FONT_SIDEBAR_SECTION = new Font(FONT_NAME, Font.BOLD,  13);
    public static final Font   FONT_SIDEBAR_BTN     = new Font(FONT_NAME, Font.BOLD,  17);
    public static final Font   FONT_SIDEBAR_ICON    = new Font("Segoe UI Emoji", Font.PLAIN, 19);

    public static final Font   FONT_ICON            = new Font("SansSerif", Font.PLAIN, 38);

    // ═══════════════════════════════════════════════════════════
    //  KÍCH THƯỚC
    // ═══════════════════════════════════════════════════════════
    public static final int    SIDEBAR_WIDTH    = 240;
    public static final int    TOPBAR_HEIGHT    = 60;
    public static final int    CARD_RADIUS      = 12;
    public static final int    BTN_RADIUS       = 10;
    public static final int    SIDEBAR_BTN_H    = 44;

    private UIConstants() {}
}
