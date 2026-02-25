package com.hospital.bus;

import com.hospital.dao.InvoiceDAO;
import com.hospital.model.Invoice;

import java.util.List;

/**
 * Business logic layer cho hóa đơn.
 */
public class InvoiceBUS extends BaseBUS<Invoice> {

    private final InvoiceDAO invoiceDAO;

    public InvoiceBUS() {
        super(new InvoiceDAO());
        this.invoiceDAO = (InvoiceDAO) dao;
    }

    @Override
    protected boolean validate(Invoice inv) {
        if (inv == null) return false;
        if (inv.getPatientCode() == null || inv.getPatientCode().isEmpty()) return false;
        if (inv.getExamFee() < 0 || inv.getMedicineFee() < 0) return false;
        return true;
    }

    public List<Invoice> getPendingInvoices() {
        return invoiceDAO.findByStatus("Chờ thanh toán");
    }

    public List<Invoice> getPaidInvoices() {
        return invoiceDAO.findByStatus("Đã thanh toán");
    }

    public double getTotalRevenue() {
        return invoiceDAO.getTotalRevenue();
    }

    public boolean markAsPaid(int invoiceId, String method) {
        Invoice inv = invoiceDAO.findById(invoiceId);
        if (inv == null) return false;
        inv.setStatus("Đã thanh toán");
        inv.setPaymentMethod(method);
        return invoiceDAO.update(inv);
    }
}
