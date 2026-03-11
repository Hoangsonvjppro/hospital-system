package com.hospital.bus;

import com.hospital.dao.Icd10CodeDAO;
import com.hospital.model.Icd10Code;

import java.util.List;

/**
 * BUS danh mục ICD-10 — wrapper cho Icd10CodeDAO.
 */
public class Icd10CodeBUS {

    private final Icd10CodeDAO dao = new Icd10CodeDAO();

    public List<Icd10Code> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        return dao.search(keyword.trim());
    }

    public Icd10Code findByCode(String code) {
        return dao.findByCode(code);
    }

    public List<Icd10Code> findAll() {
        return dao.findAll();
    }
}
