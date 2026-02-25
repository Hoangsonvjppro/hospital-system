package com.hospital.bus;

import com.hospital.dao.MedicalRecordDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;

import java.sql.Connection;
import java.util.List;

/**
 * Business logic layer cho benh an.
 * Bao gom: tao benh an + cap nhat chan doan, trieu chung, sinh hieu.
 * Ket hop logic hang doi benh nhan tu PatientDAO.
 */
public class MedicalRecordBUS {

    private MedicalRecordDAO dao;
    private PatientDAO patientDAO;

    public MedicalRecordBUS(Connection connection) {
        this.dao = new MedicalRecordDAO(connection);
        this.patientDAO = new PatientDAO();
    }

    public long createMedicalRecord(long patientId, long doctorId, Long appointmentId) throws Exception {
        if (patientId <= 0) {
            throw new Exception("Patient ID khong hop le");
        }
        if (doctorId <= 0) {
            throw new Exception("Doctor ID khong hop le");
        }
        return dao.createEmptyRecord(patientId, doctorId, appointmentId);
    }

    public boolean updateDiagnosis(long recordId, String diagnosis) throws Exception {
        if (recordId <= 0) {
            throw new Exception("Record ID khong hop le");
        }
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new Exception("Chan doan khong duoc de trong");
        }
        return dao.updateDiagnosis(recordId, diagnosis.trim());
    }

    public boolean updateSymptoms(long recordId, String symptoms) throws Exception {
        if (recordId <= 0) {
            throw new Exception("Record ID khong hop le");
        }
        if (symptoms == null || symptoms.trim().isEmpty()) {
            throw new Exception("Trieu chung khong duoc de trong");
        }
        return dao.updateSymptoms(recordId, symptoms.trim());
    }

    public boolean updateDiagnosisAndSymptoms(long recordId, String diagnosis, String symptoms) throws Exception {
        if (recordId <= 0) {
            throw new Exception("Record ID khong hop le");
        }
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new Exception("Chan doan khong duoc de trong");
        }
        if (symptoms == null || symptoms.trim().isEmpty()) {
            throw new Exception("Trieu chung khong duoc de trong");
        }
        return dao.updateDiagnosisAndSymptoms(recordId, diagnosis.trim(), symptoms.trim());
    }

    public boolean updateVitalSigns(long recordId, double weight, double height,
                                     String bloodPressure, int pulse) throws Exception {
        if (recordId <= 0) {
            throw new Exception("Record ID khong hop le");
        }
        if (weight <= 0) {
            throw new Exception("Can nang phai lon hon 0");
        }
        if (height <= 0) {
            throw new Exception("Chieu cao phai lon hon 0");
        }
        if (pulse <= 0) {
            throw new Exception("Mach phai lon hon 0");
        }
        return dao.updateVitalSigns(recordId, weight, height, bloodPressure, pulse);
    }

    public boolean updateStatus(long recordId, String status) throws Exception {
        if (recordId <= 0) {
            throw new Exception("Record ID khong hop le");
        }
        return dao.updateStatus(recordId, status);
    }

    public List<Patient> getWaitingPatients() {
        return patientDAO.findWaiting();
    }

    public List<Patient> getPatientsByStatus(String status) {
        return patientDAO.findByStatus(status);
    }
}
