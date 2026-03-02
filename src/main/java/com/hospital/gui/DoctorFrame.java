package com.hospital.gui;

import com.hospital.gui.panels.DoctorDashboardPanel;
import com.hospital.gui.panels.DoctorSchedulePanel;
import com.hospital.gui.panels.DoctorWorkstationPanel;
import com.hospital.gui.panels.ExaminationPanel;
import com.hospital.gui.panels.LabResultPanel;
import com.hospital.gui.panels.PatientAllergyPanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Bác sĩ — kế thừa BaseFrame.
 * Menu: Danh sách chờ khám, Khám bệnh, Kê đơn.
 */
public class DoctorFrame extends BaseFrame {

    public DoctorFrame(Account account) {
        super(account, "Bác sĩ", "🩺");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("📋", "Danh sách chờ khám",
                () -> showPanel(new DoctorDashboardPanel()));

        addMenuItem("🩺", "Khám bệnh",
                () -> showPanel(new DoctorWorkstationPanel()));

        addMenuItem("📅", "Lịch khám",
                () -> showPanel(new DoctorSchedulePanel()));

        addMenuItem("📝", "Kê đơn",
                () -> showPanel(new DoctorWorkstationPanel()));

        addMenuItem("🔬", "Kết quả xét nghiệm",
                () -> showPanel(new LabResultPanel()));

        addSeparator();
        addSectionLabel("Hồ sơ");

        addMenuItem("⚠", "Dị ứng bệnh nhân",
                () -> showPanel(new PatientAllergyPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new DoctorDashboardPanel();
    }
}
