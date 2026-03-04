package com.hospital.bus.event;

import com.hospital.model.Patient;

/**
 * Event: Bệnh nhân mới đã được đăng ký vào hệ thống.
 */
public class PatientRegisteredEvent {
    private final long patientId;
    private final Patient patient;

    public PatientRegisteredEvent(long patientId) {
        this.patientId = patientId;
        this.patient = null;
    }

    public PatientRegisteredEvent(long patientId, Patient patient) {
        this.patientId = patientId;
        this.patient = patient;
    }

    public long getPatientId() {
        return patientId;
    }

    public Patient getPatient() {
        return patient;
    }
}
