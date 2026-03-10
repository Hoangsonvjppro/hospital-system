package com.hospital.gui.admin;

import com.hospital.bus.ClinicConfigBUS;
import com.hospital.gui.common.*;
import com.hospital.exception.BusinessException;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Cấu hình phòng khám — chỉnh sửa thông tin phòng khám, phí khám, giờ làm việc.
 */
public class ClinicConfigPanel extends JPanel {

    private final ClinicConfigBUS configBUS = new ClinicConfigBUS();

    private JTextField txtName, txtAddress, txtPhone, txtEmail, txtExamFee, txtWorkingHours, txtInvoicePrefix;

    public ClinicConfigPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        add(createActionBar(), BorderLayout.SOUTH);

        loadConfig();
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("⚙ Cấu hình phòng khám");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel createBody() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        txtName = addField(form, gbc, row++, "Tên phòng khám:", 30);
        txtAddress = addField(form, gbc, row++, "Địa chỉ:", 40);
        txtPhone = addField(form, gbc, row++, "Số điện thoại:", 15);
        txtEmail = addField(form, gbc, row++, "Email:", 25);
        txtExamFee = addField(form, gbc, row++, "Phí khám mặc định (VNĐ):", 15);
        txtWorkingHours = addField(form, gbc, row++, "Giờ làm việc:", 25);
        txtInvoicePrefix = addField(form, gbc, row++, "Tiền tố hóa đơn:", 10);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bar.setOpaque(false);

        RoundedButton btnReset = new RoundedButton("🔄 Tải lại");
        btnReset.setBackground(UIConstants.ACCENT_BLUE);
        btnReset.setForeground(Color.WHITE);
        btnReset.addActionListener(e -> loadConfig());

        RoundedButton btnSave = new RoundedButton("💾 Lưu cấu hình");
        btnSave.setBackground(UIConstants.SUCCESS_GREEN);
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveConfig());

        bar.add(btnReset);
        bar.add(btnSave);
        return bar;
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label, int cols) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setPreferredSize(new Dimension(200, 28));
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JTextField txt = new JTextField(cols);
        txt.setFont(UIConstants.FONT_BODY);
        form.add(txt, gbc);
        return txt;
    }

    private void loadConfig() {
        try {
            ClinicConfig config = configBUS.getConfig();
            if (config != null) {
                txtName.setText(config.getClinicName() != null ? config.getClinicName() : "");
                txtAddress.setText(config.getClinicAddress() != null ? config.getClinicAddress() : "");
                txtPhone.setText(config.getClinicPhone() != null ? config.getClinicPhone() : "");
                txtEmail.setText(config.getClinicEmail() != null ? config.getClinicEmail() : "");
                txtExamFee.setText(String.format("%.0f", config.getDefaultExamFee()));
                txtWorkingHours.setText(config.getWorkingHours() != null ? config.getWorkingHours() : "");
                txtInvoicePrefix.setText(config.getInvoicePrefix() != null ? config.getInvoicePrefix() : "HD");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải cấu hình: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveConfig() {
        try {
            ClinicConfig config = new ClinicConfig();
            config.setClinicName(txtName.getText().trim());
            config.setClinicAddress(txtAddress.getText().trim());
            config.setClinicPhone(txtPhone.getText().trim());
            config.setClinicEmail(txtEmail.getText().trim());
            config.setWorkingHours(txtWorkingHours.getText().trim());
            config.setInvoicePrefix(txtInvoicePrefix.getText().trim());

            try {
                config.setDefaultExamFee(Double.parseDouble(txtExamFee.getText().trim().replace(",", "")));
            } catch (NumberFormatException e) {
                config.setDefaultExamFee(150000);
            }

            configBUS.saveConfig(config);
            JOptionPane.showMessageDialog(this, "Lưu cấu hình thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
