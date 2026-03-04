package com.hospital.bus.event;

/**
 * Event: Khám bệnh hoàn tất (bệnh án đã xong).
 */
public class ExaminationCompletedEvent {
    private final long recordId;

    public ExaminationCompletedEvent(long recordId) {
        this.recordId = recordId;
    }

    public long getRecordId() {
        return recordId;
    }
}
