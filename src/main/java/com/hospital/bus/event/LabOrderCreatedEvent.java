package com.hospital.bus.event;

/**
 * Event: Phiếu chỉ định xét nghiệm/dịch vụ mới được tạo.
 */
public class LabOrderCreatedEvent {
    private final long serviceOrderId;

    public LabOrderCreatedEvent(long serviceOrderId) {
        this.serviceOrderId = serviceOrderId;
    }

    public long getServiceOrderId() {
        return serviceOrderId;
    }
}
