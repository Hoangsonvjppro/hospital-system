package com.hospital.bus;

import com.hospital.dao.MedicalRecordDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.MedicalRecord;

public class MedicalRecordBUS {

    private final MedicalRecordDAO dao = new MedicalRecordDAO();

    public void updateVitalSigns(long recordId,
                                 double temperature,
                                 String bloodPressure,
                                 int pulse,
                                 double weight,
                                 double height) {

        if (temperature < 35 || temperature > 42)
            throw new BusinessException("Nhiệt độ phải từ 35-42°C");

        if (!bloodPressure.matches("\\d{2,3}/\\d{2,3}"))
            throw new BusinessException("Huyết áp phải dạng 120/80");

        if (pulse < 30 || pulse > 200)
            throw new BusinessException("Mạch không hợp lệ");

        if (weight <= 0 || height <= 0)
            throw new BusinessException("Cân nặng / chiều cao không hợp lệ");

        dao.updateVitalSigns(recordId, temperature, bloodPressure, pulse, weight, height);
    }

    public void updateDiagnosisAndSymptoms(long recordId,
                                           String diagnosis,
                                           String symptoms) {

        if (diagnosis == null || diagnosis.isBlank())
            throw new BusinessException("Chẩn đoán không được để trống");

        dao.updateDiagnosisAndSymptoms(recordId, diagnosis, symptoms);
    }

    public void completeExamination(MedicalRecord record) {

        if (record == null)
            throw new BusinessException("Hồ sơ không hợp lệ");

        record.setStatus("PRESCRIBED");

        dao.updateExamination(record);
    }
}
