package com.hospital.gui;

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

        addDisabledMenuItem("📄", "Hóa đơn");
        addDisabledMenuItem("📈", "Báo cáo");
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new PaymentPanel();
    }
}
