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
        super(account, "Kế toán", "money");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("money", "Thanh toán",
                () -> showPanel(new PaymentPanel()));

        addMenuItem("document", "Hóa đơn",
                () -> showPanel(new InvoiceListPanel()));

        addMenuItem("trending_up", "Báo cáo",
                () -> showPanel(new AdminReportPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new PaymentPanel();
    }
}
