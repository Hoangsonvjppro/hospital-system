package com.hospital.bus.event;

/**
 * Event: Trạng thái hàng đợi thay đổi (WAITING → EXAMINING → COMPLETED...).
 */
public class QueueUpdatedEvent {
    private final long recordId;
    private final String queueStatus;

    public QueueUpdatedEvent(long recordId, String queueStatus) {
        this.recordId = recordId;
        this.queueStatus = queueStatus;
    }

    public long getRecordId() {
        return recordId;
    }

    public String getQueueStatus() {
        return queueStatus;
    }
}
