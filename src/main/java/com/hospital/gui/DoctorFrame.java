package com.hospital.gui;

import com.hospital.gui.panels.DoctorDashboardPanel;
import com.hospital.gui.panels.DoctorSchedulePanel;
import com.hospital.gui.panels.DoctorWorkstationPanel;
import com.hospital.gui.panels.ExaminationPanel;
import com.hospital.gui.panels.LabOrderPanel;
import com.hospital.gui.panels.LabProcessingPanel;
import com.hospital.gui.panels.LabResultPanel;
import com.hospital.gui.panels.PatientAllergyPanel;
import com.hospital.gui.panels.PrescriptionPanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Bác sĩ — kế thừa BaseFrame.
 * Menu: Danh sách chờ khám, Khám bệnh, Kê đơn, Xét nghiệm.
 */
public class DoctorFrame extends BaseFrame {

    public DoctorFrame(Account account) {
        super(account, "Bác sĩ", "stethoscope");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("clipboard", "Danh sách chờ khám",
                () -> showPanel(new DoctorDashboardPanel()));

        addMenuItem("stethoscope", "Khám bệnh",
                () -> showPanel(new DoctorWorkstationPanel()));

        addMenuItem("calendar", "Lịch khám",
                () -> showPanel(new DoctorSchedulePanel()));

        addMenuItem("edit", "Kê đơn thuốc",
                () -> showPanel(new PrescriptionPanel()));

        addSeparator();
        addSectionLabel("Xét nghiệm");

        addMenuItem("lab", "Yêu cầu xét nghiệm",
                () -> showPanel(new LabOrderPanel()));

        addMenuItem("microscope", "Xử lý xét nghiệm",
                () -> showPanel(new LabProcessingPanel()));

        addMenuItem("dashboard", "Kết quả xét nghiệm",
                () -> showPanel(new LabResultPanel()));

        addSeparator();
        addSectionLabel("Hồ sơ");

        addMenuItem("warning", "Dị ứng bệnh nhân",
                () -> showPanel(new PatientAllergyPanel()));

        addSeparator();
        addSectionLabel("Hoàn tất");

        addMenuItem("check", "Kết thúc khám",
                () -> showPanel(new com.hospital.gui.panels.CompletionPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new DoctorDashboardPanel();
    }
}
