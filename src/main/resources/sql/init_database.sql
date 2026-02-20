-- ============================================================
-- HỆ THỐNG QUẢN LÝ PHÒNG MẠCH TƯ — DATABASE INITIALIZATION
-- Database: clinic_management
-- Engine:   MySQL 8.0+
-- Charset:  UTF-8 (utf8mb4)
-- Version:  4.0 (Round 3 — 20 bảng + 1 VIEW)
-- ============================================================
--
-- ⚠ GHI CHÚ QUAN TRỌNG CHO TẦNG JAVA (BUS LAYER):
--
-- 1. ĐỒNG BỘ KHO (stock_qty ↔ StockTransaction):
--    Medicine.stock_qty là "cache" để query nhanh.
--    StockTransaction là "audit trail" (nguồn sự thật).
--    → Khi xuất/nhập kho, PHẢI dùng DB Transaction (BEGIN...COMMIT)
--      để UPDATE Medicine.stock_qty VÀ INSERT StockTransaction cùng lúc.
--    → Nếu cần đối soát: SELECT SUM(quantity) FROM StockTransaction
--      WHERE medicine_id = ? phải bằng Medicine.stock_qty.
--
-- 2. CHỒNG LẤN LỊCH HẸN:
--    UNIQUE KEY chỉ ngăn trùng start_time, không ngăn overlap.
--    → Java phải query kiểm tra trước khi INSERT:
--    SELECT COUNT(*) FROM Appointment
--    WHERE doctor_id=? AND appointment_date=? AND status!='CANCELLED'
--      AND start_time < :new_end AND end_time > :new_start
--
-- ============================================================

CREATE DATABASE IF NOT EXISTS clinic_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE clinic_management;

-- ============================================================
-- A. PHÂN QUYỀN & BẢO MẬT
-- ============================================================

-- 1. Bảng Role — Vai trò trong hệ thống
CREATE TABLE Role (
    role_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng User — Tài khoản đăng nhập
CREATE TABLE `User` (
    user_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,              -- BCrypt hash, KHÔNG lưu plaintext
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(150) UNIQUE,
    phone         VARCHAR(20),
    role_id       BIGINT       NOT NULL,
    is_active     BOOLEAN      DEFAULT TRUE,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES Role(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- B. HỒ SƠ BỆNH ÁN ĐIỆN TỬ (EMR)
-- ============================================================

-- 3. Bảng Patient — Hồ sơ bệnh nhân
CREATE TABLE Patient (
    patient_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(150) NOT NULL,
    gender        ENUM('MALE','FEMALE','OTHER') NOT NULL,
    date_of_birth DATE         NOT NULL,
    phone         VARCHAR(20),
    address       VARCHAR(500),
    user_id       BIGINT       UNIQUE,                -- Nullable: BN có tài khoản xem lịch sử
    is_active     BOOLEAN      DEFAULT TRUE,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `User`(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng PatientAllergy — Tiền sử dị ứng (cảnh báo tự động khi kê đơn)
CREATE TABLE PatientAllergy (
    allergy_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id    BIGINT       NOT NULL,
    allergen_name VARCHAR(200) NOT NULL,              -- Tên chất/thuốc gây dị ứng
    severity      ENUM('MILD','MODERATE','SEVERE') DEFAULT 'MODERATE',
    reaction      VARCHAR(500),                       -- Phản ứng: "Nổi mẩn", "Khó thở"…
    notes         TEXT,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng Doctor — Thông tin bác sĩ (liên kết 1-1 với User)
CREATE TABLE Doctor (
    doctor_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL UNIQUE,
    specialty   VARCHAR(150),                         -- Chuyên khoa
    license_no  VARCHAR(50)  UNIQUE,                  -- Số giấy phép hành nghề
    is_active   BOOLEAN      DEFAULT TRUE,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `User`(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- C. TÀI CHÍNH & KHO DƯỢC
-- ============================================================

-- 6. Bảng Service — Danh mục dịch vụ khám
CREATE TABLE Service (
    service_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(200)   NOT NULL,
    price        DECIMAL(15,2)  NOT NULL,
    description  VARCHAR(500),
    is_active    BOOLEAN        DEFAULT TRUE,
    created_at   DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bảng Medicine — Kho thuốc
CREATE TABLE Medicine (
    medicine_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_name VARCHAR(200)   NOT NULL,
    unit          VARCHAR(50)    NOT NULL,             -- Viên, chai, gói, ống…
    cost_price    DECIMAL(15,2)  NOT NULL,             -- Giá nhập (giá vốn)
    sell_price    DECIMAL(15,2)  NOT NULL,             -- Giá bán cho bệnh nhân
    stock_qty     INT            NOT NULL DEFAULT 0,   -- Cache — nguồn sự thật là StockTransaction
    min_threshold INT            NOT NULL DEFAULT 10,  -- Ngưỡng cảnh báo hết thuốc
    expiry_date   DATE,
    description   VARCHAR(500),
    is_active     BOOLEAN        DEFAULT TRUE,
    created_at    DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bảng MedicineIngredient — Thành phần thuốc (phục vụ cảnh báo dị ứng)
-- Join PatientAllergy.allergen_name với ingredient_name để cảnh báo khi kê đơn
CREATE TABLE MedicineIngredient (
    ingredient_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id     BIGINT       NOT NULL,
    ingredient_name VARCHAR(200) NOT NULL,             -- Tên hoạt chất: "Amoxicillin", "Paracetamol"…
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id),
    UNIQUE KEY uq_medicine_ingredient (medicine_id, ingredient_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Bảng StockTransaction — Lịch sử nhập/xuất kho (audit trail)
-- ⚠ stock_qty trong Medicine là cache. Bảng này là nguồn sự thật.
--   Java BUS PHẢI dùng DB Transaction: UPDATE stock_qty + INSERT StockTransaction cùng lúc.
CREATE TABLE StockTransaction (
    transaction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id      BIGINT       NOT NULL,
    transaction_type ENUM('IMPORT','EXPORT','ADJUSTMENT','RETURN') NOT NULL,
    quantity         INT          NOT NULL,
    stock_before     INT          NOT NULL,            -- Tồn kho trước giao dịch
    stock_after      INT          NOT NULL,            -- Tồn kho sau giao dịch
    reference_type   VARCHAR(50),                      -- 'PRESCRIPTION', 'MANUAL_IMPORT', 'RETURN'…
    reference_id     BIGINT,                           -- ID phiếu liên quan
    notes            TEXT,
    created_by       BIGINT,
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    -- [R3] CHECK constraints đảm bảo logic nhất quán
    CONSTRAINT chk_import_positive CHECK (
        transaction_type != 'IMPORT' OR quantity > 0
    ),
    CONSTRAINT chk_export_negative CHECK (
        transaction_type != 'EXPORT' OR quantity < 0
    ),
    CONSTRAINT chk_stock_after CHECK (
        stock_after = stock_before + quantity
    ),
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id),
    FOREIGN KEY (created_by)  REFERENCES `User`(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- D. LỊCH LÀM VIỆC & ĐẶT LỊCH
-- ============================================================

-- 10. Bảng Schedule — Ca trực / lịch làm việc bác sĩ
CREATE TABLE Schedule (
    schedule_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id    BIGINT       NOT NULL,
    work_date    DATE         NOT NULL,
    start_time   TIME         NOT NULL,
    end_time     TIME         NOT NULL,
    notes        VARCHAR(500),
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES Doctor(doctor_id),
    UNIQUE KEY uq_doctor_schedule (doctor_id, work_date, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Bảng Appointment — Lịch hẹn khám
-- ⚠ UNIQUE KEY chỉ ngăn trùng start_time, KHÔNG ngăn overlap → xử lý ở Java BUS
CREATE TABLE Appointment (
    appointment_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id      BIGINT       NOT NULL,
    doctor_id       BIGINT       NOT NULL,
    appointment_date DATE        NOT NULL,
    start_time      TIME         NOT NULL,
    end_time        TIME         NOT NULL,
    status          ENUM('SCHEDULED','CHECKED_IN','COMPLETED','CANCELLED') DEFAULT 'SCHEDULED',
    reason          VARCHAR(500),
    is_active       BOOLEAN      DEFAULT TRUE,
    created_by      BIGINT,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id),
    FOREIGN KEY (doctor_id)  REFERENCES Doctor(doctor_id),
    FOREIGN KEY (created_by) REFERENCES `User`(user_id),
    UNIQUE KEY uq_doctor_appointment (doctor_id, appointment_date, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- B (tiếp). HỒ SƠ BỆNH ÁN — Các bảng phụ thuộc Appointment
-- ============================================================

-- 12. Bảng MedicalRecord — Hồ sơ bệnh án
CREATE TABLE MedicalRecord (
    record_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id     BIGINT       NOT NULL,
    doctor_id      BIGINT       NOT NULL,
    appointment_id BIGINT       UNIQUE,               -- [R3] UNIQUE → đảm bảo 1 Appointment : 1 Record
    visit_date     DATETIME     NOT NULL,
    -- Sinh hiệu (Vitals)
    blood_pressure VARCHAR(20),                       -- Huyết áp: "120/80"
    heart_rate     INT,                                -- Nhịp tim (bpm)
    temperature    DECIMAL(4,1),                       -- Nhiệt độ (°C)
    weight         DECIMAL(5,1),                       -- Cân nặng (kg)
    height         DECIMAL(5,1),                       -- Chiều cao (cm)
    spo2           INT,                                -- SpO2 (%)
    -- Khám lâm sàng
    symptoms       TEXT,
    diagnosis      TEXT,
    notes          TEXT,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)     REFERENCES Patient(patient_id),
    FOREIGN KEY (doctor_id)      REFERENCES Doctor(doctor_id),
    FOREIGN KEY (appointment_id) REFERENCES Appointment(appointment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Bảng ServiceOrder — Phiếu chỉ định dịch vụ / cận lâm sàng
CREATE TABLE ServiceOrder (
    order_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id    BIGINT       NOT NULL,
    service_id   BIGINT       NOT NULL,
    status       ENUM('ORDERED','IN_PROGRESS','COMPLETED','CANCELLED') DEFAULT 'ORDERED',
    ordered_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,
    notes        TEXT,
    FOREIGN KEY (record_id)  REFERENCES MedicalRecord(record_id),
    FOREIGN KEY (service_id) REFERENCES Service(service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Bảng LabResult — Kết quả xét nghiệm
CREATE TABLE LabResult (
    lab_result_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id        BIGINT       NOT NULL,
    service_order_id BIGINT,
    test_name        VARCHAR(200) NOT NULL,
    result_value     VARCHAR(500),
    normal_range     VARCHAR(200),
    unit             VARCHAR(50),
    test_date        DATETIME     NOT NULL,
    notes            TEXT,
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id)        REFERENCES MedicalRecord(record_id),
    FOREIGN KEY (service_order_id) REFERENCES ServiceOrder(order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Bảng Prescription — Đơn thuốc (header, N:1 với MedicalRecord)
CREATE TABLE Prescription (
    prescription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id       BIGINT       NOT NULL,            -- N:1 cho phép nhiều đơn/bệnh án
    status          ENUM('DRAFT','CONFIRMED','DISPENSED','CANCELLED') DEFAULT 'DRAFT',
    total_amount    DECIMAL(15,2) DEFAULT 0,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES MedicalRecord(record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Bảng PrescriptionDetail — Chi tiết đơn thuốc
CREATE TABLE PrescriptionDetail (
    detail_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT        NOT NULL,
    medicine_id     BIGINT        NOT NULL,
    quantity        INT           NOT NULL,
    dosage          VARCHAR(200),                      -- "2 viên x 3 lần/ngày"
    instruction     VARCHAR(500),                      -- "Uống sau ăn"
    unit_price      DECIMAL(15,2) NOT NULL,            -- Đơn giá tại thời điểm kê
    line_total      DECIMAL(15,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (prescription_id) REFERENCES Prescription(prescription_id),
    FOREIGN KEY (medicine_id)     REFERENCES Medicine(medicine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- C (tiếp). HÓA ĐƠN
-- [R3] Bỏ total_amount khỏi Invoice → dùng VIEW tính tổng tự động
-- ============================================================

-- 17. Bảng Invoice — Hóa đơn thanh toán
CREATE TABLE Invoice (
    invoice_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id     BIGINT        NOT NULL,
    record_id      BIGINT,
    invoice_date   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status         ENUM('PENDING','PAID','CANCELLED') DEFAULT 'PENDING',
    payment_method VARCHAR(50),                       -- 'CASH','CARD','TRANSFER'
    notes          TEXT,
    created_by     BIGINT,
    created_at     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id),
    FOREIGN KEY (record_id)  REFERENCES MedicalRecord(record_id),
    FOREIGN KEY (created_by) REFERENCES `User`(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. Bảng InvoiceServiceDetail — Chi tiết hóa đơn: DỊCH VỤ
CREATE TABLE InvoiceServiceDetail (
    detail_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id       BIGINT        NOT NULL,
    service_order_id BIGINT        NOT NULL,           -- FK cứng → ServiceOrder
    service_name     VARCHAR(200)  NOT NULL,            -- Snapshot
    quantity         INT           NOT NULL DEFAULT 1,
    unit_price       DECIMAL(15,2) NOT NULL,
    line_total       DECIMAL(15,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (invoice_id)       REFERENCES Invoice(invoice_id),
    FOREIGN KEY (service_order_id) REFERENCES ServiceOrder(order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. Bảng InvoiceMedicineDetail — Chi tiết hóa đơn: THUỐC
-- [R3] Thêm FK → PrescriptionDetail để truy vết đơn thuốc gốc
CREATE TABLE InvoiceMedicineDetail (
    detail_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id             BIGINT        NOT NULL,
    medicine_id            BIGINT        NOT NULL,     -- FK cứng → Medicine
    prescription_detail_id BIGINT,                     -- [R3] FK → PrescriptionDetail (nullable: bán lẻ)
    medicine_name          VARCHAR(200)  NOT NULL,      -- Snapshot
    quantity               INT           NOT NULL DEFAULT 1,
    unit_price             DECIMAL(15,2) NOT NULL,      -- Giá bán snapshot
    cost_price             DECIMAL(15,2) NOT NULL DEFAULT 0, -- Giá vốn snapshot
    line_total             DECIMAL(15,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    profit_total           DECIMAL(15,2) GENERATED ALWAYS AS (quantity * (unit_price - cost_price)) STORED,
    FOREIGN KEY (invoice_id)             REFERENCES Invoice(invoice_id),
    FOREIGN KEY (medicine_id)            REFERENCES Medicine(medicine_id),
    FOREIGN KEY (prescription_detail_id) REFERENCES PrescriptionDetail(detail_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- VIEW — Tổng tiền hóa đơn tự động (thay cho Invoice.total_amount)
-- [R3] Không lưu total_amount vật lý → tránh sai lệch khi Java quên cập nhật
-- ============================================================

-- 20. VIEW InvoiceSummary — Tổng hợp hóa đơn
CREATE OR REPLACE VIEW InvoiceSummary AS
SELECT
    i.invoice_id,
    i.patient_id,
    i.record_id,
    i.invoice_date,
    i.status,
    i.payment_method,
    i.notes,
    i.created_by,
    i.created_at,
    COALESCE(svc.service_total, 0) AS service_total,
    COALESCE(med.medicine_total, 0) AS medicine_total,
    COALESCE(med.medicine_cost, 0) AS medicine_cost,
    COALESCE(med.medicine_profit, 0) AS medicine_profit,
    COALESCE(svc.service_total, 0) + COALESCE(med.medicine_total, 0) AS total_amount
FROM Invoice i
LEFT JOIN (
    SELECT invoice_id,
           SUM(line_total) AS service_total
    FROM InvoiceServiceDetail
    GROUP BY invoice_id
) svc ON i.invoice_id = svc.invoice_id
LEFT JOIN (
    SELECT invoice_id,
           SUM(line_total) AS medicine_total,
           SUM(quantity * cost_price) AS medicine_cost,
           SUM(profit_total) AS medicine_profit
    FROM InvoiceMedicineDetail
    GROUP BY invoice_id
) med ON i.invoice_id = med.invoice_id;

-- ============================================================
-- INDEXES
-- ============================================================

-- Bệnh nhân
CREATE INDEX idx_patient_name  ON Patient(full_name);
CREATE INDEX idx_patient_phone ON Patient(phone);

-- Dị ứng
CREATE INDEX idx_allergy_patient ON PatientAllergy(patient_id);

-- Thành phần thuốc (join với PatientAllergy để cảnh báo)
CREATE INDEX idx_ingredient_name ON MedicineIngredient(ingredient_name);

-- Lịch sử khám
CREATE INDEX idx_record_patient ON MedicalRecord(patient_id, visit_date);
CREATE INDEX idx_record_doctor  ON MedicalRecord(doctor_id, visit_date);

-- Doanh thu
CREATE INDEX idx_invoice_date   ON Invoice(invoice_date);
CREATE INDEX idx_invoice_status ON Invoice(status);

-- Kho thuốc
CREATE INDEX idx_medicine_stock ON Medicine(stock_qty, min_threshold);
CREATE INDEX idx_stock_medicine ON StockTransaction(medicine_id, created_at);

-- Lịch hẹn
CREATE INDEX idx_appt_doctor_date ON Appointment(doctor_id, appointment_date);

-- Phiếu chỉ định
CREATE INDEX idx_svcorder_record ON ServiceOrder(record_id);

-- Đơn thuốc
CREATE INDEX idx_prescription_record ON Prescription(record_id);

-- ============================================================
-- SEED DATA
-- ============================================================

INSERT INTO Role (role_name, description) VALUES
    ('ADMIN',        'Quản trị hệ thống'),
    ('DOCTOR',       'Bác sĩ'),
    ('NURSE',        'Y tá'),
    ('RECEPTIONIST', 'Lễ tân'),
    ('ACCOUNTANT',   'Kế toán'),
    ('PATIENT',      'Bệnh nhân (xem lịch sử khám)');

INSERT INTO `User` (username, password_hash, full_name, email, role_id) VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Quản trị viên', 'admin@clinic.local', 1);

INSERT INTO Service (service_name, price, description) VALUES
    ('Khám tổng quát',          150000, 'Khám bệnh tổng quát'),
    ('Xét nghiệm máu',         200000, 'Xét nghiệm công thức máu'),
    ('Xét nghiệm nước tiểu',   100000, 'Xét nghiệm nước tiểu thường quy'),
    ('Siêu âm bụng',           300000, 'Siêu âm bụng tổng quát'),
    ('Đo điện tim (ECG)',       250000, 'Đo điện tim');

INSERT INTO Medicine (medicine_name, unit, cost_price, sell_price, stock_qty, min_threshold, description) VALUES
    ('Paracetamol 500mg',    'Viên',  1200, 2000,  500, 50,  'Giảm đau, hạ sốt'),
    ('Amoxicillin 500mg',    'Viên',  1800, 3000,  300, 30,  'Kháng sinh phổ rộng'),
    ('Omeprazole 20mg',      'Viên',  3000, 5000,  200, 20,  'Ức chế bơm proton, điều trị dạ dày'),
    ('Loratadine 10mg',      'Viên',  2500, 4000,  150, 15,  'Kháng histamin, chống dị ứng'),
    ('Vitamin C 1000mg',     'Viên',   900, 1500, 1000, 100, 'Bổ sung vitamin C');

-- Thành phần thuốc mẫu (phục vụ cảnh báo dị ứng)
INSERT INTO MedicineIngredient (medicine_id, ingredient_name) VALUES
    (1, 'Paracetamol'),
    (2, 'Amoxicillin'),
    (2, 'Penicillin'),           -- Amoxicillin thuộc nhóm Penicillin
    (3, 'Omeprazole'),
    (4, 'Loratadine'),
    (5, 'Ascorbic Acid');
