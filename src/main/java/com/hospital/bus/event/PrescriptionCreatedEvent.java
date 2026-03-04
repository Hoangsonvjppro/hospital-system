package com.hospital.bus.event;

/**
 * Event: Đơn thuốc mới được tạo.
 */
public class PrescriptionCreatedEvent {
    private final long prescriptionId;

    public PrescriptionCreatedEvent(long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public long getPrescriptionId() {
        return prescriptionId;
    }
}
