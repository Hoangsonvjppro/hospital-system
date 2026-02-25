package com.hospital.bus;

import com.hospital.dao.PatientDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Patient;
import com.hospital.util.AppUtils;

import java.util.List;

/**
 * BUS benh nhan — validation + quan ly hang doi kham.
 */
public class PatientBUS extends BaseBUS<Patient> {

    private final PatientDAO patientDAO;

    public PatientBUS() {
        super(new PatientDAO());
        this.patientDAO = (PatientDAO) dao;
    }

    @Override
    protected boolean validate(Patient entity) {

        if (AppUtils.isNullOrEmpty(entity.getFullName())) {
            throw new BusinessException("Tên bệnh nhân không được để trống.");
        }

        if (AppUtils.isNullOrEmpty(entity.getPhone())) {
            throw new BusinessException("SĐT không được để trống.");
        }

        if (!entity.getPhone().matches("\\d{10}")) {
            throw new BusinessException("SĐT phải đủ 10 chữ số.");
        }

        return true;
    }

    // -- Doctor workflow methods --

    public void addToQueue(int patientId, String examType) {
        patientDAO.addToQueue(patientId, examType);
    }

    public List<Patient> getWaitingPatients() {
        return patientDAO.findWaiting();
    }

    public List<Patient> getPatientsByStatus(String status) {
        return patientDAO.findByStatus(status);
    }

    public boolean updateStatus(int patientId, String newStatus) {
        return patientDAO.updateStatus(patientId, newStatus);
    }

    public int countToday() {
        return patientDAO.countToday();
    }
}
