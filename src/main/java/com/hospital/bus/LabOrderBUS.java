package com.hospital.bus;

import com.hospital.bus.event.EventBus;
import com.hospital.bus.event.LabOrderCreatedEvent;
import com.hospital.bus.event.LabResultReadyEvent;
import com.hospital.dao.LabOrderDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.LabOrder;
import com.hospital.model.LabOrder.LabStatus;

import java.util.List;
import java.util.logging.Logger;

/**
 * Business logic cho phiếu yêu cầu xét nghiệm (LabOrder).
 */
public class LabOrderBUS {

    private static final Logger LOGGER = Logger.getLogger(LabOrderBUS.class.getName());

    private final LabOrderDAO dao;

    public LabOrderBUS() {
        this.dao = new LabOrderDAO();
    }

    public LabOrderBUS(LabOrderDAO dao) {
        this.dao = dao;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public LabOrder findById(int id) {
        return dao.findById(id);
    }

    public List<LabOrder> findAll() {
        return dao.findAll();
    }

    public List<LabOrder> getByExaminationId(long examId) {
        return dao.getByExaminationId(examId);
    }

    public List<LabOrder> getByPatientId(long patientId) {
        return dao.getByPatientId(patientId);
    }

    public List<LabOrder> getPendingOrders() {
        return dao.getPendingOrders();
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    /**
     * Tạo phiếu yêu cầu xét nghiệm mới (status = PENDING).
     * Fire LabOrderCreatedEvent sau khi tạo thành công.
     */
    public long createLabOrder(LabOrder order) {
        validate(order);
        order.setStatus(LabStatus.PENDING);
        long id = dao.createLabOrder(order);
        if (id > 0) {
            EventBus.getInstance().publish(new LabOrderCreatedEvent(id));
        }
        return id;
    }

    /**
     * Bắt đầu xét nghiệm (PENDING → IN_PROGRESS).
     */
    public boolean startOrder(int id) {
        LabOrder order = dao.findById(id);
        if (order == null) {
            throw new BusinessException("Không tìm thấy phiếu xét nghiệm #" + id);
        }
        if (order.getStatus() != LabStatus.PENDING) {
            throw new BusinessException("Chỉ có thể bắt đầu xét nghiệm đang ở trạng thái CHỜ XỬ LÝ");
        }
        return dao.updateStatus(id, LabStatus.IN_PROGRESS);
    }

    /**
     * Cập nhật kết quả xét nghiệm (chưa hoàn tất).
     */
    public boolean updateResult(int id, String result) {
        if (result == null || result.trim().isEmpty()) {
            throw new BusinessException("Kết quả xét nghiệm không được để trống");
        }
        return dao.updateResult(id, result.trim());
    }

    /**
     * Hoàn tất xét nghiệm: cập nhật kết quả + status = COMPLETED.
     * Fire LabResultReadyEvent để bác sĩ biết.
     */
    public boolean completeOrder(int id, String result) {
        if (result == null || result.trim().isEmpty()) {
            throw new BusinessException("Kết quả xét nghiệm không được để trống");
        }
        LabOrder order = dao.findById(id);
        if (order == null) {
            throw new BusinessException("Không tìm thấy phiếu xét nghiệm #" + id);
        }
        if (order.getStatus() == LabStatus.COMPLETED) {
            throw new BusinessException("Phiếu xét nghiệm đã hoàn tất trước đó");
        }

        boolean ok = dao.completeOrder(id, result.trim());
        if (ok) {
            EventBus.getInstance().publish(
                    new LabResultReadyEvent(id, order.getExaminationId()));
        }
        return ok;
    }

    /**
     * Cập nhật trạng thái tùy ý.
     */
    public boolean updateStatus(int id, LabStatus status) {
        return dao.updateStatus(id, status);
    }

    // ── Validation ──────────────────────────────────────────────────────────

    protected void validate(LabOrder order) {
        if (order == null) throw new BusinessException("Phiếu xét nghiệm không được null");
        if (order.getExaminationId() <= 0) throw new BusinessException("Mã lần khám không hợp lệ");
        if (order.getPatientId() <= 0) throw new BusinessException("Mã bệnh nhân không hợp lệ");
        if (order.getTestType() == null) throw new BusinessException("Vui lòng chọn loại xét nghiệm");
        if (order.getTestName() == null || order.getTestName().isBlank())
            throw new BusinessException("Tên xét nghiệm không được để trống");
    }
}
