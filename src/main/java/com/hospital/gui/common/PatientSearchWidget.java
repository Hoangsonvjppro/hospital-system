package com.hospital.gui.common;

import com.hospital.bus.PatientBUS;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Widget tìm kiếm bệnh nhân dùng chung — theo SĐT / CCCD / Tên.
 */
public class PatientSearchWidget extends JPanel {

    private final JTextField txtSearch;
    private final JButton btnSearch;
    private final PatientBUS patientBUS = new PatientBUS();
    private Consumer<Patient> onPatientSelected;

    public PatientSearchWidget() {
        setLayout(new BorderLayout(8, 0));
        setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập SĐT / CCCD / Tên bệnh nhân...");
        txtSearch.setPreferredSize(new Dimension(300, 38));

        btnSearch = new RoundedButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(120, 38));

        add(txtSearch, BorderLayout.CENTER);
        add(btnSearch, BorderLayout.EAST);

        btnSearch.addActionListener(e -> doSearch());
        txtSearch.addActionListener(e -> doSearch());
    }

    public void setOnPatientSelected(Consumer<Patient> callback) {
        this.onPatientSelected = callback;
    }

    private void doSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) return;

        try {
            List<Patient> results = patientBUS.findAll().stream()
                    .filter(p -> {
                        String kw = keyword.toLowerCase();
                        return (p.getFullName() != null && p.getFullName().toLowerCase().contains(kw))
                            || (p.getPhone() != null && p.getPhone().contains(keyword))
                            || (p.getCccd() != null && p.getCccd().contains(keyword));
                    })
                    .toList();

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy bệnh nhân.", "Kết quả", JOptionPane.INFORMATION_MESSAGE);
            } else if (results.size() == 1) {
                if (onPatientSelected != null) onPatientSelected.accept(results.get(0));
            } else {
                // Show selection dialog
                String[] options = results.stream()
                        .map(p -> p.getFullName() + " - " + p.getPhone())
                        .toArray(String[]::new);
                String selected = (String) JOptionPane.showInputDialog(this,
                        "Tìm thấy " + results.size() + " bệnh nhân. Chọn:", "Chọn bệnh nhân",
                        JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (selected != null) {
                    int idx = java.util.Arrays.asList(options).indexOf(selected);
                    if (idx >= 0 && onPatientSelected != null) {
                        onPatientSelected.accept(results.get(idx));
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clear() {
        txtSearch.setText("");
    }
}
