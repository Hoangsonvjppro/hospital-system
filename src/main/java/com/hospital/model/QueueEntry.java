package com.hospital.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Entity hàng đợi khám bệnh — ánh xạ bảng queue_entries trong CSDL.
 * Implement Comparable để sắp xếp theo priority rồi theo createdAt.
 */
public class QueueEntry implements Comparable<QueueEntry> {

    // ── Enum ưu tiên ──────────────────────────────────────────
    public enum Priority {
        EMERGENCY(1, "Cấp cứu"),
        ELDERLY(2, "Người cao tuổi"),
        NORMAL(3, "Bình thường");

        private final int level;
        private final String displayName;

        Priority(int level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }

        public int getLevel() { return level; }
        public String getDisplayName() { return displayName; }

        @Override
        public String toString() { return displayName; }
    }

    // ── Enum trạng thái ───────────────────────────────────────
    public enum QueueStatus {
        WAITING("Đang chờ"),
        IN_PROGRESS("Đang khám"),
        COMPLETED("Hoàn thành"),
        CANCELLED("Đã hủy");

        private final String displayName;

        QueueStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }

        @Override
        public String toString() { return displayName; }
    }

    // ── Fields ────────────────────────────────────────────────
    private int id;
    private int patientId;
    private int queueNumber;
    private Priority priority;
    private QueueStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;

    // ── Transient display fields (populated by DAO JOIN) ──────
    private String patientName;
    private String patientCode;
    private String patientPhone;

    // ── Constructors ──────────────────────────────────────────

    public QueueEntry() {
        this.priority = Priority.NORMAL;
        this.status = QueueStatus.WAITING;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getQueueNumber() { return queueNumber; }
    public void setQueueNumber(int queueNumber) { this.queueNumber = queueNumber; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public QueueStatus getStatus() { return status; }
    public void setStatus(QueueStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCalledAt() { return calledAt; }
    public void setCalledAt(LocalDateTime calledAt) { this.calledAt = calledAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientCode() {
        if (patientCode == null || patientCode.isEmpty()) {
            return String.format("BN%03d", patientId);
        }
        return patientCode;
    }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    // ── Display helpers ───────────────────────────────────────

    /**
     * Hiển thị ưu tiên tiếng Việt.
     */
    public String getPriorityDisplay() {
        return priority != null ? priority.getDisplayName() : "Bình thường";
    }

    /**
     * Hiển thị trạng thái tiếng Việt.
     */
    public String getStatusDisplay() {
        return status != null ? status.getDisplayName() : "";
    }

    /**
     * Tính thời gian chờ (phút) từ createdAt đến bây giờ.
     */
    public long getWaitingMinutes() {
        if (createdAt == null) return 0;
        long mins = java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        return Math.max(mins, 0);
    }

    /**
     * Hiển thị thời gian chờ dạng đọc được.
     */
    public String getWaitingTimeDisplay() {
        long mins = getWaitingMinutes();
        if (mins < 60) {
            return mins + " phút";
        }
        return (mins / 60) + "h " + (mins % 60) + "m";
    }

    // ── Comparable (sắp xếp theo priority rồi createdAt) ─────

    @Override
    public int compareTo(QueueEntry other) {
        // Priority level thấp hơn = ưu tiên cao hơn
        int cmp = Integer.compare(
                this.priority != null ? this.priority.getLevel() : 3,
                other.priority != null ? other.priority.getLevel() : 3);
        if (cmp != 0) return cmp;
        // Cùng priority → ai đến trước (createdAt sớm hơn) thì đứng trước
        if (this.createdAt != null && other.createdAt != null) {
            return this.createdAt.compareTo(other.createdAt);
        }
        return 0;
    }

    // ── equals / hashCode (dựa trên id) ──────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueEntry that = (QueueEntry) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "QueueEntry{" +
                "id=" + id +
                ", queueNumber=" + queueNumber +
                ", patientName='" + patientName + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                '}';
    }
}
