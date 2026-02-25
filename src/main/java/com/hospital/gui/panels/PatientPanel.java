package com.hospital.gui.panels;

import com.hospital.bus.PatientBUS;
import com.hospital.exception.BusinessException;
import com.hospital.exception.DataAccessException;
import com.hospital.model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class PatientPanel extends JPanel {

    private JTextField txtName, txtPhone, txtAddress, txtDob;
    private JTable table;
    private DefaultTableModel model;
    private PatientBUS patientBUS;

    public PatientPanel() {

        patientBUS = new PatientBUS();

        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));

        form.add(new JLabel("Tên:"));
        txtName = new JTextField();
        form.add(txtName);

        form.add(new JLabel("SĐT:"));
        txtPhone = new JTextField();
        form.add(txtPhone);

        form.add(new JLabel("Địa chỉ:"));
        txtAddress = new JTextField();
        form.add(txtAddress);

        form.add(new JLabel("Ngày sinh (yyyy-MM-dd):"));
        txtDob = new JTextField();
        form.add(txtDob);

        JButton btnAdd = new JButton("Thêm");
        form.add(btnAdd);

        add(form, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"ID", "Tên", "SĐT", "Địa chỉ", "Ngày sinh"}, 0);

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();

        btnAdd.addActionListener(e -> addPatient());
    }

    private void addPatient() {

        try {
            Patient p = new Patient();
            p.setFullName(txtName.getText());
            p.setPhone(txtPhone.getText());
            p.setAddress(txtAddress.getText());

            if (!txtDob.getText().isEmpty()) {
                p.setDateOfBirth(LocalDate.parse(txtDob.getText()));
            }

            if (patientBUS.insert(p)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công");
                loadData();
                clearForm();
            }

        } catch (BusinessException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ: " + e.getMessage());
        }
    }

    private void loadData() {
        model.setRowCount(0);

        List<Patient> list = patientBUS.findAll();

        for (Patient p : list) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getFullName(),
                    p.getPhone(),
                    p.getAddress(),
                    p.getDateOfBirth()
            });
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtDob.setText("");
    }
}
