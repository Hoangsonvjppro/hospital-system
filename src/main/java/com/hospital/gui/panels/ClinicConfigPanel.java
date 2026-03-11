package com.hospital.gui.panels;

import com.hospital.bus.ClinicConfigBUS;
import com.hospital.gui.UIConstants;
import com.hospital.gui.components.RoundedButton;
import com.hospital.gui.components.RoundedPanel;
import com.hospital.model.ClinicConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Panel cấu hình phòng khám — dành cho Admin.
 * <p>
 * Hiển thị form cho phép chỉnh sửa tên phòng khám, địa chỉ, SĐT,
 * email, phí khám mặc định, giờ làm việc, prefix hóa đơn.
 */
public class ClinicConfigPanel extends JPanel {

    private final ClinicConfigBUS configBUS = new ClinicConfigBUS();
    private final NumberFormat moneyFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    // ── Form fields ───────────────────────────────────────────
    private JTextField txtClinicName;
    private JTextField txtAddress;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtExamFee;
    private JTextField txtWorkingHours;
    private JTextField txtInvoicePrefix;

    public ClinicConfigPanel() {
        setBackground(UIConstants.CONTENT_BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(20, 24, 20, 24));
        initComponents();
        loadConfig();
    }

    // ══════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════

    private void initComponents() {
        // ── Header ──
        JLabel title = new JLabel("  Cấu hình Phòng khám");
        title.setIcon(com.hospital.gui.IconManager.getIcon("settings", 20, 20));
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        add(title, BorderLayout.NORTH);

        // ── Form card ──
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Tên phòng khám
        txtClinicName = addFormRow(form, gbc, row++, "Tên phòng khám:", 40);
        // Địa chỉ
        txtAddress = addFormRow(form, gbc, row++, "Địa chỉ:", 60);
        // Số điện thoại
        txtPhone = addFormRow(form, gbc, row++, "Số điện thoại:", 20);
        // Email
        txtEmail = addFormRow(form, gbc, row++, "Email:", 40);
        // Phí khám mặc định
        txtExamFee = addFormRow(form, gbc, row++, "Phí khám mặc định (VNĐ):", 15);
        // Giờ làm việc
        txtWorkingHours = addFormRow(form, gbc, row++, "Giờ làm việc:", 20);
        // Prefix hóa đơn
        txtInvoicePrefix = addFormRow(form, gbc, row++, "Prefix hóa đơn:", 10);

        // spacer
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        form.add(Box.createVerticalGlue(), gbc);

        card.add(form, BorderLayout.CENTER);

        // ── Button bar ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        RoundedButton btnReload = new RoundedButton("Tải lại");
        btnReload.setIcon(com.hospital.gui.IconManager.getIcon("refresh", 14, 14));
        btnReload.setBackground(UIConstants.FIELD_BG);
        btnReload.setForeground(UIConstants.TEXT_PRIMARY);
        btnReload.addActionListener(e -> loadConfig());

        RoundedButton btnSave = new RoundedButton("Lưu cấu hình");
        btnSave.setIcon(com.hospital.gui.IconManager.getIcon("save", 14, 14));
        btnSave.setBackground(UIConstants.SUCCESS_GREEN);
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveConfig());

        btnPanel.add(btnReload);
        btnPanel.add(btnSave);

        card.add(btnPanel, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    private JTextField addFormRow(JPanel form, GridBagConstraints gbc, int row,
                                   String label, int columns) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weighty = 0;

        // Label
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY.deriveFont(Font.BOLD));
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        form.add(lbl, gbc);

        // TextField
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField tf = new JTextField(columns);
        tf.setFont(UIConstants.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.FIELD_BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        form.add(tf, gbc);

        return tf;
    }

    // ══════════════════════════════════════════════════════════
    //  LOAD / SAVE
    // ══════════════════════════════════════════════════════════

    private void loadConfig() {
        try {
            configBUS.invalidateCache();
            ClinicConfig cfg = configBUS.getConfig();

            txtClinicName.setText(cfg.getClinicName());
            txtAddress.setText(cfg.getClinicAddress());
            txtPhone.setText(cfg.getClinicPhone());
            txtEmail.setText(cfg.getClinicEmail());
            txtExamFee.setText(moneyFmt.format(cfg.getDefaultExamFee()));
            txtWorkingHours.setText(cfg.getWorkingHours());
            txtInvoicePrefix.setText(cfg.getInvoicePrefix());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải cấu hình:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveConfig() {
        try {
            ClinicConfig cfg = new ClinicConfig();
            cfg.setClinicName(txtClinicName.getText().trim());
            cfg.setClinicAddress(txtAddress.getText().trim());
            cfg.setClinicPhone(txtPhone.getText().trim());
            cfg.setClinicEmail(txtEmail.getText().trim());
            cfg.setWorkingHours(txtWorkingHours.getText().trim());
            cfg.setInvoicePrefix(txtInvoicePrefix.getText().trim());

            // Parse phí khám — bỏ dấu chấm phân nhóm, bỏ ký tự không phải số
            String feeText = txtExamFee.getText().trim()
                    .replaceAll("[^0-9]", "");
            if (feeText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Phí khám không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cfg.setDefaultExamFee(Double.parseDouble(feeText));

            // Validate
            if (cfg.getClinicName().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Tên phòng khám không được để trống!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            configBUS.saveConfig(cfg);
            JOptionPane.showMessageDialog(this,
                    "✅ Đã lưu cấu hình thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadConfig();   // refresh UI
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi lưu cấu hình:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

}
