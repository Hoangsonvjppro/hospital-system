package com.hospital.bus.event;

/**
 * Event: Bắt đầu khám bệnh cho bệnh nhân.
 */
public class ExaminationStartedEvent {
    private final long recordId;
    private final long patientId;

    public ExaminationStartedEvent(long recordId, long patientId) {
        this.recordId = recordId;
        this.patientId = patientId;
    }

    public long getRecordId() {
        return recordId;
    }

    public long getPatientId() {
        return patientId;
    }
}
