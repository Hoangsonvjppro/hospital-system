package com.hospital.gui;

import java.awt.*;

/**
 * Design System — Hằng số giao diện thống nhất toàn ứng dụng.
 * Tất cả Frame/Panel/Component phải dùng các hằng số ở đây,
 * KHÔNG được tự khai báo màu/font cục bộ.
 */
public class UIConstants {

    // ═══════════════════════════════════════════════════════════════════════════
    //  MÀU CHỦ ĐẠO (Primary)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final Color PRIMARY           = new Color(192, 57,  43);   // #C0392B
    public static final Color PRIMARY_DARK      = new Color(150, 40,  27);   // hover / pressed
    public static final Color PRIMARY_LIGHT     = new Color(231, 76,  60);   // #E74C3C accent
    public static final Color PRIMARY_BG_SOFT   = new Color(253, 237, 236);  // nền mềm đỏ nhạt

    /** @deprecated Dùng {@link #PRIMARY} thay thế */
    @Deprecated public static final Color PRIMARY_RED       = PRIMARY;
    /** @deprecated Dùng {@link #PRIMARY_DARK} thay thế */
    @Deprecated public static final Color PRIMARY_RED_DARK  = PRIMARY_DARK;
    /** @deprecated Dùng {@link #PRIMARY_LIGHT} thay thế */
    @Deprecated public static final Color PRIMARY_RED_LIGHT = PRIMARY_LIGHT;
    /** @deprecated Dùng {@link #PRIMARY_BG_SOFT} thay thế */
    @Deprecated public static final Color RED_BG_SOFT       = PRIMARY_BG_SOFT;

    // ═══════════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════════════════════════════════
    public static final Color SIDEBAR_BG            = new Color(28,  35,  49);  // #1C2331
    public static final Color SIDEBAR_HEADER_BG     = new Color(21,  34,  56);  // #152238
    public static final Color SIDEBAR_ITEM_HOVER    = new Color(40,  50,  68);
    public static final Color SIDEBAR_ACTIVE        = PRIMARY;
    public static final Color SIDEBAR_TEXT          = new Color(189, 195, 199); // #BDC3C7
    public static final Color SIDEBAR_TEXT_ACTIVE   = Color.WHITE;

    // Sidebar button states
    public static final Color SIDEBAR_BTN_DEFAULT   = new Color(34,  53,  88);  // #223558
    public static final Color SIDEBAR_BTN_HOVER     = new Color(42,  66, 112);  // #2A4270
    public static final Color SIDEBAR_BTN_ACTIVE    = PRIMARY;
    public static final Color SIDEBAR_SEPARATOR     = new Color(42,  66, 112);

    // ═══════════════════════════════════════════════════════════════════════════
    //  NỀN (Background)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final Color CONTENT_BG        = new Color(245, 246, 250);  // #F5F6FA
    public static final Color WHITE             = Color.WHITE;
    public static final Color CARD_BG           = Color.WHITE;

    // ═══════════════════════════════════════════════════════════════════════════
    //  VĂN BẢN (Text)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final Color TEXT_PRIMARY      = new Color(44,  62,  80);   // #2C3E50
    public static final Color TEXT_SECONDARY    = new Color(127, 140, 141);  // #7F8C8D
    public static final Color TEXT_MUTED        = new Color(189, 195, 199);

    // ═══════════════════════════════════════════════════════════════════════════
    //  TRẠNG THÁI (Status)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final Color STATUS_WAITING    = new Color(230, 126, 34);   // cam
    public static final Color STATUS_EXAMINING  = new Color(52,  152, 219);  // xanh dương
    public static final Color STATUS_DONE       = new Color(39,  174, 96);   // xanh lá
    public static final Color STATUS_CANCEL     = new Color(149, 165, 166);  // xám

    // ═══════════════════════════════════════════════════════════════════════════
    //  NÚT HÀNH ĐỘNG (Action buttons)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final Color LOGOUT_COLOR      = new Color(231, 76,  60);   // #E74C3C
    public static final Color LOGOUT_HOVER      = new Color(192, 57,  43);   // #C0392B

    // ═══════════════════════════════════════════════════════════════════════════
    //  ACCENT BLUE (Doctor Workstation & clinical context)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final Color ACCENT_BLUE       = new Color(37,  99,  235);  // #2563EB
    public static final Color ACCENT_BLUE_DARK  = new Color(29,  78,  216);  // #1D4ED8
    public static final Color ACCENT_BLUE_LIGHT = new Color(59,  130, 246);  // #3B82F6
    public static final Color ACCENT_BLUE_SOFT  = new Color(239, 246, 255);  // #EFF6FF

    // ═══════════════════════════════════════════════════════════════════════════
    //  FORM / INPUT
    // ═══════════════════════════════════════════════════════════════════════════
    public static final Color FIELD_BG          = new Color(245, 247, 250);  // #F5F7FA
    public static final Color FIELD_BORDER      = new Color(221, 226, 232);  // #DDE2E8
    public static final Color ERROR_COLOR       = new Color(229, 57,  53);   // #E53935
    public static final Color ICON_MUTED        = new Color(176, 190, 197);  // #B0BEC5

    // ═══════════════════════════════════════════════════════════════════════════
    //  KHÁC (Miscellaneous)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final Color BORDER_COLOR      = new Color(220, 221, 226);
    public static final Color TABLE_HEADER_BG   = new Color(248, 249, 250);
    public static final Color TABLE_ROW_ALT     = new Color(252, 252, 252);
    public static final Color SUCCESS_GREEN     = new Color(39,  174, 96);
    public static final Color SUCCESS_GREEN_DARK= new Color(21,  128, 61);   // #15803D
    public static final Color WARNING_ORANGE    = new Color(230, 126, 34);

    // ═══════════════════════════════════════════════════════════════════════════
    //  FONT
    // ═══════════════════════════════════════════════════════════════════════════
    public static final String FONT_NAME            = "Segoe UI";

    // Tiêu đề & nội dung
    public static final Font   FONT_TITLE           = new Font(FONT_NAME, Font.BOLD,  22);
    public static final Font   FONT_SUBTITLE        = new Font(FONT_NAME, Font.BOLD,  15);
    public static final Font   FONT_HEADER          = new Font(FONT_NAME, Font.BOLD,  18);
    public static final Font   FONT_SECTION         = new Font(FONT_NAME, Font.BOLD,  16);
    public static final Font   FONT_BODY            = new Font(FONT_NAME, Font.PLAIN, 14);
    public static final Font   FONT_LABEL           = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font   FONT_CAPTION         = new Font(FONT_NAME, Font.PLAIN, 12);
    public static final Font   FONT_SMALL           = new Font(FONT_NAME, Font.PLAIN, 11);
    public static final Font   FONT_OVERLINE        = new Font(FONT_NAME, Font.BOLD,  10);
    public static final Font   FONT_BOLD            = new Font(FONT_NAME, Font.BOLD,  13);
    public static final Font   FONT_BUTTON          = new Font(FONT_NAME, Font.BOLD,  13);
    public static final Font   FONT_NUMBER_BIG      = new Font(FONT_NAME, Font.BOLD,  28);
    public static final Font   FONT_ITALIC          = new Font(FONT_NAME, Font.ITALIC, 12);

    // Sidebar
    public static final Font   FONT_SIDEBAR         = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font   FONT_SIDEBAR_HEADER  = new Font(FONT_NAME, Font.BOLD,  15);
    public static final Font   FONT_SIDEBAR_ROLE    = new Font(FONT_NAME, Font.PLAIN, 12);
    public static final Font   FONT_SIDEBAR_SECTION = new Font(FONT_NAME, Font.BOLD,  10);
    public static final Font   FONT_SIDEBAR_BTN     = new Font(FONT_NAME, Font.PLAIN, 14);

    // Icon (emoji fallback)
    public static final Font   FONT_ICON            = new Font("SansSerif", Font.PLAIN, 32);

    // ═══════════════════════════════════════════════════════════════════════════
    //  KÍCH THƯỚC (Dimensions)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final int    SIDEBAR_WIDTH    = 240;
    public static final int    TOPBAR_HEIGHT    = 60;
    public static final int    CARD_RADIUS      = 12;
    public static final int    BTN_RADIUS       = 10;
    public static final int    SIDEBAR_BTN_H    = 44;

    private UIConstants() {}
}
