package com.hospital.gui.doctor;

import com.hospital.gui.BaseFrame;
import com.hospital.model.Account;

import javax.swing.*;

public class DoctorFrame extends BaseFrame {

    public DoctorFrame(Account account) {
        super(account, "Bác sĩ", "🩺");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("📊", "Dashboard", () -> showPanel(new DoctorDashboardPanel()));
        addMenuItem("🩺", "Khám bệnh", () -> showPanel(new ExaminationPanel()));
        addMenuItem("📜", "Tiền sử BN", () -> showPanel(new PatientHistoryPanel()));

        addSeparator();
        addSectionLabel("Xét nghiệm");

        addMenuItem("🧪", "Yêu cầu XN", () -> showPanel(new LabOrderPanel()));
        addMenuItem("📊", "Kết quả XN", () -> showPanel(new LabResultViewPanel()));

        addSeparator();
        addSectionLabel("Điều trị");

        addMenuItem("💊", "Kê đơn thuốc", () -> showPanel(new PrescriptionPanel()));
        addMenuItem("✅", "Kết thúc khám", () -> showPanel(new CompletionPanel()));
        addMenuItem("📅", "Lịch làm việc", () -> showPanel(new DoctorSchedulePanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new DoctorDashboardPanel();
    }
}
