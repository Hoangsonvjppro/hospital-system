package com.hospital.bus;

import com.hospital.bus.event.EventBus;
import com.hospital.bus.event.QueueUpdatedEvent;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.QueueDAO;
import com.hospital.dao.QueueEntryDAO;
import com.hospital.exception.BusinessException;
import com.hospital.model.Patient;
import com.hospital.model.QueueEntry;
import com.hospital.model.QueueEntry.Priority;
import com.hospital.model.QueueEntry.QueueStatus;

import java.util.List;

/**
 * Business logic layer cho hàng đợi khám bệnh.
 * Sử dụng QueueEntryDAO (bảng queue_entries) cho hàng đợi mới,
 * giữ QueueDAO (MedicalRecord) cho tương thích ngược.
 */
public class QueueBUS {

    private final QueueDAO queueDAO;
    private final QueueEntryDAO queueEntryDAO;
    private final PatientDAO patientDAO;

    public QueueBUS() {
        this.queueDAO = new QueueDAO();
        this.queueEntryDAO = new QueueEntryDAO();
        this.patientDAO = new PatientDAO();
    }

    // ══════════════════════════════════════════════════════════
    //  QUEUE ENTRY (bảng queue_entries mới)
    // ══════════════════════════════════════════════════════════

    /**
     * Thêm bệnh nhân vào hàng đợi.
     *
     * @param patientId ID bệnh nhân
     * @param priority  Ưu tiên (EMERGENCY, ELDERLY, NORMAL)
     * @return QueueEntry đã tạo
     */
    public QueueEntry addToQueue(int patientId, Priority priority) {
        if (patientId <= 0) {
            throw new BusinessException("Patient ID không hợp lệ");
        }

        // Kiểm tra xem bệnh nhân đã có trong hàng đợi hôm nay chưa
        if (queueEntryDAO.isPatientInTodayQueue(patientId)) {
            throw new BusinessException("Bệnh nhân đã có trong hàng đợi khám hôm nay. Không thể thêm lại.");
        }

        QueueEntry entry = new QueueEntry();
        entry.setPatientId(patientId);
        entry.setPriority(priority != null ? priority : Priority.NORMAL);

        queueEntryDAO.addToQueue(entry);

        // Fire event
        EventBus.getInstance().publish(new QueueUpdatedEvent(entry.getId(), QueueStatus.WAITING.name()));
        return entry;
    }

    /**
     * Lấy toàn bộ hàng đợi hôm nay.
     */
    public List<QueueEntry> getTodayQueue() {
        return queueEntryDAO.getTodayQueue();
    }

    /**
     * Lấy danh sách đang chờ hôm nay.
     */
    public List<QueueEntry> getWaitingQueue() {
        return queueEntryDAO.getWaitingQueue();
    }

    /**
     * Cập nhật trạng thái hàng đợi.
     */
    public boolean updateQueueEntryStatus(int queueId, QueueStatus status) {
        if (queueId <= 0) {
            throw new BusinessException("Queue ID không hợp lệ");
        }
        boolean result = queueEntryDAO.updateStatus(queueId, status);
        if (result) {
            EventBus.getInstance().publish(new QueueUpdatedEvent(queueId, status.name()));
        }
        return result;
    }

    /**
     * Lấy bệnh nhân tiếp theo cần khám.
     */
    public QueueEntry getNextPatient() {
        return queueEntryDAO.getNextPatient();
    }

    /**
     * Gọi bệnh nhân tiếp theo — cập nhật trạng thái sang IN_PROGRESS.
     */
    public QueueEntry callNextPatient() {
        QueueEntry next = queueEntryDAO.getNextPatient();
        if (next == null) {
            throw new BusinessException("Không còn bệnh nhân trong hàng đợi.");
        }
        queueEntryDAO.updateStatus(next.getId(), QueueStatus.IN_PROGRESS);
        next.setStatus(QueueStatus.IN_PROGRESS);
        EventBus.getInstance().publish(new QueueUpdatedEvent(next.getId(), QueueStatus.IN_PROGRESS.name()));
        return next;
    }

    /**
     * Bỏ qua bệnh nhân — đưa xuống cuối hàng đợi (tạo entry mới với WAITING).
     */
    public void skipPatient(int queueId) {
        QueueEntry entry = queueEntryDAO.findById(queueId);
        if (entry == null) {
            throw new BusinessException("Không tìm thấy bệnh nhân trong hàng đợi.");
        }
        // Cancel entry cũ
        queueEntryDAO.updateStatus(queueId, QueueStatus.CANCELLED);
        // Tạo entry mới ở cuối
        QueueEntry newEntry = new QueueEntry();
        newEntry.setPatientId(entry.getPatientId());
        newEntry.setPriority(entry.getPriority());
        queueEntryDAO.addToQueue(newEntry);
        EventBus.getInstance().publish(new QueueUpdatedEvent(newEntry.getId(), QueueStatus.WAITING.name()));
    }

    /**
     * Hủy bệnh nhân khỏi hàng đợi.
     */
    public void cancelQueueEntry(int queueId) {
        queueEntryDAO.updateStatus(queueId, QueueStatus.CANCELLED);
        EventBus.getInstance().publish(new QueueUpdatedEvent(queueId, QueueStatus.CANCELLED.name()));
    }

    /**
     * Đếm số đang chờ hôm nay.
     */
    public int countTodayWaiting() {
        return queueEntryDAO.countTodayWaiting();
    }

    // ══════════════════════════════════════════════════════════
    //  LEGACY (QueueDAO — MedicalRecord-based) — giữ tương thích
    // ══════════════════════════════════════════════════════════

    /**
     * Đưa bệnh nhân vào hàng đợi khám (legacy — MedicalRecord).
     */
    public long enqueue(long patientId, long doctorId, String examType) {
        if (patientId <= 0) {
            throw new BusinessException("Patient ID không hợp lệ");
        }
        if (doctorId <= 0) {
            throw new BusinessException("Doctor ID không hợp lệ");
        }
        return queueDAO.enqueue(patientId, doctorId, examType);
    }

    /**
     * Lấy danh sách bệnh nhân đang chờ khám + đang khám (ngày hôm nay).
     */
    public List<Patient> getWaitingPatients() {
        // Đọc từ QueueEntry (hệ thống mới) - lấy cả WAITING và IN_PROGRESS (đang khám)
        List<QueueEntry> queueEntries = queueEntryDAO.getTodayQueue();
        List<Patient> patients = new java.util.ArrayList<>();
        
        for (QueueEntry entry : queueEntries) {
            // Only include WAITING and IN_PROGRESS, skip COMPLETED and CANCELLED
            if (entry.getStatus() == QueueEntry.QueueStatus.COMPLETED || 
                entry.getStatus() == QueueEntry.QueueStatus.CANCELLED) {
                continue;
            }
            
            try {
                Patient p = patientDAO.findById(entry.getPatientId());
                if (p != null) {
                    // Set transient fields for display
                    p.setPatientCode(p.getPatientCode()); // BN00X format
                    p.setStatus(entry.getStatus() != null ? entry.getStatus().name() : "WAITING");
                    p.setArrivalTime(entry.getCreatedAt() != null ? 
                        entry.getCreatedAt().toLocalTime().toString() : "");
                    p.setExamType("Khám tổng quát");
                    patients.add(p);
                }
            } catch (Exception e) {
                // Skip invalid entries
            }
        }
        
        return patients;
    }

    /**
     * Lấy danh sách bệnh nhân theo trạng thái hàng đợi.
     */
    public List<Patient> getPatientsByStatus(String... statuses) {
        return queueDAO.findByQueueStatus(statuses);
    }

    /**
     * Cập nhật trạng thái hàng đợi (legacy).
     */
    public boolean updateQueueStatus(long recordId, String newStatus) {
        if (recordId <= 0) {
            throw new BusinessException("Record ID không hợp lệ");
        }
        return queueDAO.updateQueueStatus(recordId, newStatus);
    }

    /**
     * Đếm tổng số bệnh nhân trong hàng đợi hôm nay (legacy).
     */
    public int countToday() {
        return queueDAO.countToday();
    }

    /**
     * Đếm bệnh nhân theo trạng thái (hôm nay) (legacy).
     */
    public int countByStatus(String status) {
        return queueDAO.countByStatus(status);
    }
}
