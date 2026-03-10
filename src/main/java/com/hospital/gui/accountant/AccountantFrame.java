package com.hospital.gui.accountant;

import com.hospital.gui.BaseFrame;
import com.hospital.gui.receptionist.PaymentPanel;
import com.hospital.model.Account;

import javax.swing.*;

public class AccountantFrame extends BaseFrame {

    public AccountantFrame(Account account) {
        super(account, "Kế toán", "💰");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("💰", "Thanh toán", () -> showPanel(new PaymentPanel()));
        addMenuItem("📄", "Hóa đơn", () -> showPanel(new InvoiceListPanel()));

        addSeparator();
        addSectionLabel("Báo cáo");

        addMenuItem("📊", "Dashboard tài chính", () -> showPanel(new FinanceDashboardPanel()));
        addMenuItem("📈", "Báo cáo doanh thu", () -> showPanel(new RevenueReportPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new FinanceDashboardPanel();
    }
}
