package com.hospital.bus.event;

/**
 * Event: Phát thuốc hoàn tất.
 */
public class DispensingCompletedEvent {
    private final long dispensingId;

    public DispensingCompletedEvent(long dispensingId) {
        this.dispensingId = dispensingId;
    }

    public long getDispensingId() {
        return dispensingId;
    }
}
