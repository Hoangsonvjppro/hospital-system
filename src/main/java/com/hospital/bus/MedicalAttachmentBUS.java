package com.hospital.bus;

import com.hospital.dao.MedicalAttachmentDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.MedicalAttachment;

import java.util.List;

/**
 * Business logic layer cho tệp đính kèm bệnh án (MedicalAttachment).
 */
public class MedicalAttachmentBUS extends BaseBUS<MedicalAttachment> {

    private final MedicalAttachmentDAO attachmentDAO;

    public MedicalAttachmentBUS() {
        super(new MedicalAttachmentDAO());
        this.attachmentDAO = (MedicalAttachmentDAO) dao;
    }

    @Override
    protected void validate(MedicalAttachment a) {
        if (a == null) throw new BusinessException("Dữ liệu tệp đính kèm không hợp lệ");
        if (a.getRecordId() <= 0)
            throw new BusinessException("Mã bệnh án không hợp lệ");
        if (a.getFileUrl() == null || a.getFileUrl().trim().isEmpty())
            throw new BusinessException("Đường dẫn tệp không được để trống");
    }

    public List<MedicalAttachment> findByRecordId(long recordId) {
        return attachmentDAO.findByRecordId(recordId);
    }

    public List<MedicalAttachment> findByServiceOrderId(long serviceOrderId) {
        return attachmentDAO.findByServiceOrderId(serviceOrderId);
    }
}
