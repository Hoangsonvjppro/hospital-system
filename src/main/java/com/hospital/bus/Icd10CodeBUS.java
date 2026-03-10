package com.hospital.bus;

import com.hospital.dao.Icd10CodeDAO;
import com.hospital.model.Icd10Code;

import java.util.List;

/**
 * Business logic layer cho danh mục mã ICD-10.
 * Chủ yếu là tra cứu (read-only), không cần validate cho insert/update thường xuyên.
 */
public class Icd10CodeBUS {

    private final Icd10CodeDAO icd10CodeDAO = new Icd10CodeDAO();

    public List<Icd10Code> findAll() {
        return icd10CodeDAO.findAll();
    }

    public Icd10Code findByCode(String code) {
        return icd10CodeDAO.findByCode(code);
    }

    public List<Icd10Code> search(String keyword) {
        return icd10CodeDAO.search(keyword);
    }
}
