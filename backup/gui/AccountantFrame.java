package com.hospital.gui;

import com.hospital.gui.panels.AdminReportPanel;
import com.hospital.gui.panels.InvoiceListPanel;
import com.hospital.gui.panels.PaymentPanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Kế toán — kế thừa BaseFrame.
 * Menu: Thanh toán, Hóa đơn.
 */
public class AccountantFrame extends BaseFrame {

    public AccountantFrame(Account account) {
        super(account, "Kế toán", "💰");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("💰", "Thanh toán",
                () -> showPanel(new PaymentPanel()));

        addMenuItem("📄", "Hóa đơn",
                () -> showPanel(new InvoiceListPanel()));

        addMenuItem("📈", "Báo cáo",
                () -> showPanel(new AdminReportPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new PaymentPanel();
    }
}
