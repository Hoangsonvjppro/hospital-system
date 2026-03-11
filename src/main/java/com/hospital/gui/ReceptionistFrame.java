package com.hospital.gui;

import com.hospital.gui.panels.PatientAllergyPanel;
import com.hospital.gui.panels.PatientPanel;
import com.hospital.gui.panels.PaymentPanel;
import com.hospital.gui.panels.ReceptionPanel;
import com.hospital.model.Account;

import javax.swing.*;

/**
 * Frame chính dành cho Lễ tân — kế thừa BaseFrame.
 * Menu: Tiếp nhận BN, Hàng đợi.
 */
public class ReceptionistFrame extends BaseFrame {

    public ReceptionistFrame(Account account) {
        super(account, "Lễ tân", "hospital");
    }

    @Override
    protected void registerMenuItems() {
        addMenuItem("clipboard", "Tiếp nhận",
                () -> showPanel(new ReceptionPanel()));


        addSeparator();
        addSectionLabel("Thu ngân");

        addMenuItem("money", "Thanh toán & Thu ngân",
                () -> showPanel(new PaymentPanel()));

        addSeparator();
        addSectionLabel("Hồ sơ");

        addMenuItem("warning", "Dị ứng bệnh nhân",
                () -> showPanel(new PatientAllergyPanel()));
    }

    @Override
    protected JPanel createDefaultPanel() {
        return new ReceptionPanel();
    }
}