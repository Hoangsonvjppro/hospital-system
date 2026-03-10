package com.hospital.bus;

import com.hospital.dao.PatientChronicDiseaseDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.PatientChronicDisease;

import java.util.List;

/**
 * Business logic layer cho bệnh mãn tính của bệnh nhân (PatientChronicDisease).
 */
public class PatientChronicDiseaseBUS extends BaseBUS<PatientChronicDisease> {

    private final PatientChronicDiseaseDAO diseaseDAO;

    public PatientChronicDiseaseBUS() {
        super(new PatientChronicDiseaseDAO());
        this.diseaseDAO = (PatientChronicDiseaseDAO) dao;
    }

    @Override
    protected void validate(PatientChronicDisease d) {
        if (d == null) throw new BusinessException("Dữ liệu bệnh mãn tính không hợp lệ");
        if (d.getPatientId() <= 0)
            throw new BusinessException("Mã bệnh nhân không hợp lệ");
        if (d.getIcd10Code() == null || d.getIcd10Code().trim().isEmpty())
            throw new BusinessException("Mã ICD-10 không được để trống");
    }

    public List<PatientChronicDisease> findByPatientId(long patientId) {
        return diseaseDAO.findByPatientId(patientId);
    }

    public List<PatientChronicDisease> findActiveByPatientId(long patientId) {
        return diseaseDAO.findActiveByPatientId(patientId);
    }
}
