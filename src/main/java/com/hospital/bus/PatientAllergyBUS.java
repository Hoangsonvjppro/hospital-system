package com.hospital.bus;

import com.hospital.dao.PatientAllergyDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.PatientAllergy;

import java.util.List;
import java.util.logging.Logger;

/**
 * BUS tiền sử dị ứng bệnh nhân.
 */
public class PatientAllergyBUS {

    private static final Logger LOGGER = Logger.getLogger(PatientAllergyBUS.class.getName());

    private final PatientAllergyDAO dao;

    public PatientAllergyBUS() {
        this.dao = new PatientAllergyDAO();
    }

    public PatientAllergyBUS(PatientAllergyDAO dao) {
        this.dao = dao;
    }

    // ── Queries ─────────────────────────────────────────────────────────────

    public PatientAllergy findById(int id) {
        return dao.findById(id);
    }

    public List<PatientAllergy> findAll() {
        return dao.findAll();
    }

    public List<PatientAllergy> findByPatientId(long patientId) {
        return dao.findByPatientId(patientId);
    }

    // ── CUD ─────────────────────────────────────────────────────────────────

    public boolean insert(PatientAllergy a) {
        validate(a);
        return dao.insert(a);
    }

    public boolean update(PatientAllergy a) {
        validate(a);
        return dao.update(a);
    }

    public boolean delete(int id) {
        return dao.delete(id);
    }

    // ── Validation ──────────────────────────────────────────────────────────

    protected void validate(PatientAllergy a) {
        if (a == null) throw new BusinessException("Dị ứng không được null");
        if (a.getPatientId() <= 0) throw new BusinessException("Mã bệnh nhân không hợp lệ");
        if (a.getAllergenName() == null || a.getAllergenName().isBlank())
            throw new BusinessException("Tên chất dị ứng không được để trống");
        if (a.getSeverity() == null || a.getSeverity().isBlank())
            throw new BusinessException("Mức độ dị ứng không được để trống");
    }
}
