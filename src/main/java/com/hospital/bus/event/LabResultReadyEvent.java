package com.hospital.bus.event;

/**
 * Event: Kết quả xét nghiệm đã sẵn sàng.
 */
public class LabResultReadyEvent {
    private final long labResultId;
    private final long recordId;

    public LabResultReadyEvent(long labResultId, long recordId) {
        this.labResultId = labResultId;
        this.recordId = recordId;
    }

    public long getLabResultId() {
        return labResultId;
    }

    public long getRecordId() {
        return recordId;
    }
}
