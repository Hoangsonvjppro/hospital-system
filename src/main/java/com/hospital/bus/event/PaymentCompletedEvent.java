package com.hospital.bus.event;

/**
 * Event: Thanh toán hóa đơn hoàn tất.
 */
public class PaymentCompletedEvent {
    private final long invoiceId;

    public PaymentCompletedEvent(long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public long getInvoiceId() {
        return invoiceId;
    }
}
