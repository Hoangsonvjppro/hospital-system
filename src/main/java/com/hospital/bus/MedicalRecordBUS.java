package com.hospital.bus;

import com.hospital.dao.MedicalRecordDAO;

import java.sql.Connection;

public class MedicalRecordBUS {

    private MedicalRecordDAO dao;

    public MedicalRecordBUS(Connection connection) {
        this.dao = new MedicalRecordDAO(connection);
    }

    public long createMedicalRecord(long patientId, long doctorId, Long appointmentId) throws Exception {

        if (patientId <= 0) {
            throw new Exception("Patient ID không hợp lệ");
        }

        if (doctorId <= 0) {
            throw new Exception("Doctor ID không hợp lệ");
        }

        return dao.createEmptyRecord(patientId, doctorId, appointmentId);
    }
}