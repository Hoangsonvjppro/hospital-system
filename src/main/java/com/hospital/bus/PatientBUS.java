package com.hospital.bus;

import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;

import java.util.List;

/**
 * Business logic layer cho bệnh nhân.
 */
public class PatientBUS extends BaseBUS<Patient> {

    private final PatientDAO patientDAO;

    public PatientBUS() {
        super(new PatientDAO());
        this.patientDAO = (PatientDAO) dao;
    }

    @Override
    protected boolean validate(Patient p) {
        if (p == null) return false;
        if (p.getFullName() == null || p.getFullName().trim().isEmpty()) return false;
        if (p.getPhone() == null || p.getPhone().trim().isEmpty()) return false;
        return true;
    }

    public List<Patient> getWaitingPatients() {
        return patientDAO.findWaiting();
    }

    public List<Patient> getPatientsByStatus(String status) {
        return patientDAO.findByStatus(status);
    }

    public int countToday() {
        return patientDAO.countToday();
    }

    public boolean updateStatus(int patientId, String newStatus) {
        Patient p = patientDAO.findById(patientId);
        if (p == null) return false;
        p.setStatus(newStatus);
        return patientDAO.update(p);
    }
}
