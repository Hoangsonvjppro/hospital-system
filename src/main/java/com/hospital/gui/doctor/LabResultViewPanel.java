package com.hospital.gui.doctor;

import com.hospital.bus.LabResultBUS;
import com.hospital.gui.common.*;
import com.hospital.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ④ Xem kết quả xét nghiệm — bác sĩ xem kết quả XN cho bệnh án đang khám.
 */
public class LabResultViewPanel extends JPanel {

    private final LabResultBUS labResultBUS = new LabResultBUS();

    private JTextField txtRecordId;
    private DefaultTableModel resultModel;

    public LabResultViewPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);

        JLabel title = new JLabel("📊 Kết quả xét nghiệm");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel idBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        idBar.setOpaque(false);
        idBar.add(new JLabel("Mã bệnh án:"));
        txtRecordId = new JTextField(12);
        txtRecordId.setFont(UIConstants.FONT_BODY);
        idBar.add(txtRecordId);

        RoundedButton btnLoad = new RoundedButton("Xem kết quả");
        btnLoad.setBackground(UIConstants.PRIMARY);
        btnLoad.setForeground(Color.WHITE);
        btnLoad.addActionListener(e -> loadResults());
        idBar.add(btnLoad);

        header.add(title, BorderLayout.NORTH);
        header.add(idBar, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createBody() {
        RoundedPanel card = new RoundedPanel(UIConstants.CARD_RADIUS);
        card.setBackground(UIConstants.CARD_BG);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("Kết quả xét nghiệm");
        lbl.setFont(UIConstants.FONT_SUBTITLE);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        card.add(lbl, BorderLayout.NORTH);

        resultModel = new DefaultTableModel(
                new String[]{"ID", "Tên xét nghiệm", "Kết quả", "Khoảng bình thường", "Đơn vị", "Ngày XN", "Ghi chú"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable resultTable = new JTable(resultModel);
        resultTable.setRowHeight(34);
        resultTable.setFont(UIConstants.FONT_LABEL);
        resultTable.getTableHeader().setFont(UIConstants.FONT_BOLD);
        resultTable.getTableHeader().setBackground(UIConstants.TABLE_HEADER_BG);

        JScrollPane scroll = new JScrollPane(resultTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void loadResults() {
        String idText = txtRecordId.getText().trim();
        if (idText.isEmpty()) return;

        resultModel.setRowCount(0);
        try {
            long recordId = Long.parseLong(idText);
            List<LabResult> results = labResultBUS.findByRecordId(recordId);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa có kết quả XN cho bệnh án #" + idText);
                return;
            }

            for (LabResult r : results) {
                resultModel.addRow(new Object[]{
                        r.getId(),
                        r.getTestName() != null ? r.getTestName() : "",
                        r.getResultValue() != null ? r.getResultValue() : "",
                        r.getNormalRange() != null ? r.getNormalRange() : "",
                        r.getUnit() != null ? r.getUnit() : "",
                        r.getTestDate() != null ? r.getTestDate().toLocalDate().toString() : "",
                        r.getNotes() != null ? r.getNotes() : ""
                });
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mã bệnh án không hợp lệ.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải kết quả: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
