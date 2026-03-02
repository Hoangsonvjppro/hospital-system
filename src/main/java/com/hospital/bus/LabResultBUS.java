package com.hospital.bus;

import com.hospital.dao.LabResultDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.LabResult;

import java.util.List;
import java.util.logging.Logger;

/**
 * BUS kết quả xét nghiệm.
 */
public class LabResultBUS {

    private static final Logger LOGGER = Logger.getLogger(LabResultBUS.class.getName());

    private final LabResultDAO dao;

    public LabResultBUS() {
        this.dao = new LabResultDAO();
    }

    public LabResultBUS(LabResultDAO dao) {
        this.dao = dao;
    }

    // ── Queries ─────────────────────────────────────────────────────────────

    public LabResult findById(int id) {
        return dao.findById(id);
    }

    public List<LabResult> findAll() {
        return dao.findAll();
    }

    public List<LabResult> findByRecordId(long recordId) {
        return dao.findByRecordId(recordId);
    }

    public List<LabResult> findByServiceOrderId(long serviceOrderId) {
        return dao.findByServiceOrderId(serviceOrderId);
    }

    // ── CUD ─────────────────────────────────────────────────────────────────

    public boolean insert(LabResult r) {
        validate(r);
        return dao.insert(r);
    }

    public boolean update(LabResult r) {
        validate(r);
        return dao.update(r);
    }

    public boolean delete(int id) {
        return dao.delete(id);
    }

    // ── Validation ──────────────────────────────────────────────────────────

    protected void validate(LabResult r) {
        if (r == null) throw new BusinessException("Kết quả xét nghiệm không được null");
        if (r.getRecordId() <= 0) throw new BusinessException("Mã bệnh án không hợp lệ");
        if (r.getTestName() == null || r.getTestName().isBlank())
            throw new BusinessException("Tên xét nghiệm không được để trống");
        if (r.getTestDate() == null)
            throw new BusinessException("Ngày xét nghiệm không được để trống");
    }
}
