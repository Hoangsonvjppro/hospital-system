package com.hospital.bus.event;

/**
 * Event: Bệnh nhân mới đã được đăng ký vào hệ thống.
 */
public class PatientRegisteredEvent {
    private final long patientId;

    public PatientRegisteredEvent(long patientId) {
        this.patientId = patientId;
    }

    public long getPatientId() {
        return patientId;
    }
}
