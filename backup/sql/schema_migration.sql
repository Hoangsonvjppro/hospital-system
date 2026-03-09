-- ============================================================
-- MIGRATION SCRIPT — Bổ sung bảng/cột thiếu
-- So sánh init_database.sql hiện tại với schema đề xuất
-- Chạy trên MySQL 8.0+ sau khi init_database.sql đã chạy
-- ============================================================

USE clinic_management;

-- ============================================================
-- 1. ALTER Patient: thêm patient_type
-- ============================================================
ALTER TABLE Patient
    ADD COLUMN IF NOT EXISTS patient_type ENUM('FIRST_VISIT','REVISIT','EMERGENCY')
    DEFAULT 'FIRST_VISIT' AFTER allergy_note;

-- ============================================================
-- 2. ALTER Medicine: thêm generic_name, dosage_form
-- ============================================================
ALTER TABLE Medicine
    ADD COLUMN IF NOT EXISTS generic_name VARCHAR(200) AFTER medicine_name;

ALTER TABLE Medicine
    ADD COLUMN IF NOT EXISTS dosage_form VARCHAR(100) AFTER unit;

-- ============================================================
-- 3. CREATE Dispensing — Phiếu phát thuốc
-- Theo luồng: Prescription (CONFIRMED) → Dispensing (DISPENSED)
-- ============================================================
CREATE TABLE IF NOT EXISTS Dispensing (
    dispensing_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT        NOT NULL,
    patient_id      BIGINT        NOT NULL,
    dispensed_by    BIGINT,                              -- User (dược sĩ) thực hiện phát
    status          ENUM('PENDING','DISPENSED','PARTIAL') DEFAULT 'PENDING',
    total_amount    DECIMAL(15,2) DEFAULT 0,
    notes           TEXT,
    dispensed_at    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (prescription_id) REFERENCES Prescription(prescription_id),
    FOREIGN KEY (patient_id)      REFERENCES Patient(patient_id),
    FOREIGN KEY (dispensed_by)    REFERENCES `User`(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. CREATE DispensingItem — Chi tiết phát thuốc
-- ============================================================
CREATE TABLE IF NOT EXISTS DispensingItem (
    item_id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispensing_id          BIGINT        NOT NULL,
    prescription_detail_id BIGINT        NOT NULL,       -- FK → PrescriptionDetail
    medicine_id            BIGINT        NOT NULL,
    medicine_name          VARCHAR(200),                  -- Snapshot tên thuốc
    requested_quantity     INT           NOT NULL,        -- Số lượng yêu cầu (từ đơn thuốc)
    dispensed_quantity     INT           NOT NULL,        -- Số lượng thực phát
    unit_price             DECIMAL(15,2),
    subtotal               DECIMAL(15,2) GENERATED ALWAYS AS (dispensed_quantity * unit_price) STORED,
    batch_number           VARCHAR(50),                   -- Số lô thuốc
    created_at             DATETIME      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (dispensing_id)          REFERENCES Dispensing(dispensing_id),
    FOREIGN KEY (prescription_detail_id) REFERENCES PrescriptionDetail(detail_id),
    FOREIGN KEY (medicine_id)            REFERENCES Medicine(medicine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. CREATE FollowUp — Hẹn tái khám (tách riêng khỏi MedicalRecord)
-- ============================================================
CREATE TABLE IF NOT EXISTS FollowUp (
    follow_up_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id     BIGINT       NOT NULL,
    record_id      BIGINT       NOT NULL,                -- FK → MedicalRecord
    follow_up_date DATE         NOT NULL,
    reason         TEXT,
    status         ENUM('SCHEDULED','COMPLETED','MISSED','CANCELLED') DEFAULT 'SCHEDULED',
    reminder_sent  BOOLEAN      DEFAULT FALSE,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id),
    FOREIGN KEY (record_id)  REFERENCES MedicalRecord(record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. INDEXES cho bảng mới
-- ============================================================
CREATE INDEX idx_dispensing_prescription ON Dispensing(prescription_id);
CREATE INDEX idx_dispensing_patient      ON Dispensing(patient_id);
CREATE INDEX idx_dispensing_status       ON Dispensing(status);
CREATE INDEX idx_dispensingitem_disp     ON DispensingItem(dispensing_id);
CREATE INDEX idx_followup_date          ON FollowUp(follow_up_date);
CREATE INDEX idx_followup_patient       ON FollowUp(patient_id);
CREATE INDEX idx_followup_status        ON FollowUp(status, follow_up_date);
CREATE INDEX idx_patient_type           ON Patient(patient_type);

-- ============================================================
-- DONE
-- ============================================================
SELECT '=== MIGRATION HOÀN TẤT ===' AS status;
