-- ============================================================
--  PHÒNG MẠCH TƯ — SCHEMA HOÀN CHỈNH v4.0
--  Kết hợp ưu điểm của schema_v3 và clinic_management
--
--  THÊM MỚI SO VỚI v3:
--  (+) MedicineIngredient  — thành phần thuốc, kiểm tra dị ứng chính xác
--  (+) Dispensing + DispensingItem — phát thuốc, hỗ trợ phát thiếu (PARTIAL)
--  (+) Schedule            — lịch làm việc bác sĩ theo ca
--  (+) QueueEntry          — hàng đợi standalone cho màn hình real-time
--  (+) ClinicConfig        — cấu hình phòng mạch dạng key-value
--  (+) FollowUp.reminder_sent — track đã gửi nhắc lịch hẹn chưa
--  (+) Icd10Code.category  — nhóm bệnh để lọc/tìm kiếm
--  (+) LabResult: result_value, normal_range, unit — cấu trúc hơn
--  (+) InvoiceMedicineDetail: cost_price, profit_total — phân tích lợi nhuận
--  (+) DrugInteraction: CHECK (id_1 < id_2), CONTRAINDICATED — chuẩn hơn
--  (+) StockTransaction: CHECK constraint tính nhất quán số dư
--
--  SỬA LỖI:
--  (-) Bỏ LabOrder              → trùng ServiceOrder
--  (-) Bỏ Patient.patient_type  → sai chỗ, chuyển thành visit_type trong MedicalRecord
--  (-) Bỏ Patient.allergy_note  → trùng PatientAllergy
--  (-) Bỏ Medicine.expiry_date / cost_price / sell_price / stock_qty → quản lý theo lô
--  (-) Bỏ MedicalRecord.follow_up_date → trùng FollowUp
--  (✔) MedicalRecord.doctor_id: cho phép NULL (lễ tân tạo trước, gán bác sĩ sau)
--  (✔) UNIQUE Invoice(record_id) — 1 lượt khám = 1 hoá đơn
--  (✔) UNIQUE Prescription(record_id) — 1 lượt khám = 1 đơn thuốc
--  (✔) FK thật MedicalRecord.diagnosis_code → Icd10Code
--
--  ⚠ GHI CHÚ TẦNG ỨNG DỤNG (BUS LAYER):
--  1. Xuất/nhập kho: dùng DB Transaction để UPDATE MedicineBatch.current_qty
--     VÀ INSERT StockTransaction cùng lúc (tránh lệch số dư).
--  2. QueueEntry.status và MedicalRecord.queue_status phải đồng bộ qua Transaction.
--  3. Kiểm tra chồng lấn lịch hẹn (Schedule/Appointment overlap) tại tầng ứng dụng.
--  4. Kiểm tra current_qty > 0 và expiry_date > NOW() trước khi kê đơn / phát thuốc.
--  5. GoodsReceipt.total_amount cần validate = SUM(import_price * initial_qty).
--  6. Kiểm tra dị ứng: JOIN MedicineIngredient với PatientAllergy trước khi kê đơn.
-- ============================================================

CREATE DATABASE IF NOT EXISTS clinic_v4
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clinic_v4;

-- ============================================================
-- [A] PHÂN QUYỀN & TÀI KHOẢN
-- ============================================================

CREATE TABLE Role (
    role_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255)
    -- Giá trị: ADMIN, DOCTOR, RECEPTIONIST, PHARMACIST, CASHIER
) ENGINE=InnoDB;

CREATE TABLE `User` (
    user_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(150) NOT NULL,
    email      VARCHAR(150) UNIQUE,
    phone      VARCHAR(20),
    role_id    BIGINT NOT NULL,
    is_active  BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES Role(role_id)
) ENGINE=InnoDB;

-- ============================================================
-- [B] CHUYÊN KHOA & BÁC SĨ
-- ============================================================

CREATE TABLE Specialty (
    specialty_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    specialty_name VARCHAR(150) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE Doctor (
    doctor_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL UNIQUE,
    specialty_id BIGINT,
    license_no   VARCHAR(50) UNIQUE,
    is_active    BOOLEAN DEFAULT TRUE,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)      REFERENCES `User`(user_id),
    FOREIGN KEY (specialty_id) REFERENCES Specialty(specialty_id)
) ENGINE=InnoDB;

-- Ca làm việc của bác sĩ
-- ⚠ UNIQUE chỉ ngăn trùng start_time; kiểm tra overlap khoảng thời gian tại tầng app
CREATE TABLE Schedule (
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id   BIGINT NOT NULL,
    work_date   DATE   NOT NULL,
    start_time  TIME   NOT NULL,
    end_time    TIME   NOT NULL,
    notes       VARCHAR(500),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_doctor_schedule (doctor_id, work_date, start_time),
    FOREIGN KEY (doctor_id) REFERENCES Doctor(doctor_id)
) ENGINE=InnoDB;

-- ============================================================
-- [C] DANH MỤC ICD-10 & CẤU HÌNH HỆ THỐNG
-- ============================================================

CREATE TABLE Icd10Code (
    icd_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    code     VARCHAR(10)  NOT NULL UNIQUE,
    name_vi  VARCHAR(500) NOT NULL,
    name_en  VARCHAR(500),
    category VARCHAR(200)       -- Nhóm bệnh: Bệnh nhiễm trùng, Tim mạch...
) ENGINE=InnoDB;

-- Cấu hình phòng mạch dạng key-value
CREATE TABLE ClinicConfig (
    config_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key   VARCHAR(50)  NOT NULL UNIQUE,
    config_value VARCHAR(500) NOT NULL,
    description  VARCHAR(200),
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    -- VD: clinic_name, exam_fee_default, logo_url, sms_template_reminder...
) ENGINE=InnoDB;

-- ============================================================
-- [D] BỆNH NHÂN & HỒ SƠ SỨC KHOẺ
-- ============================================================

CREATE TABLE Patient (
    patient_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(150) NOT NULL,
    gender        ENUM('MALE','FEMALE','OTHER') NOT NULL,
    date_of_birth DATE NOT NULL,
    phone         VARCHAR(20),
    id_card       VARCHAR(20) UNIQUE,
    address       VARCHAR(500),
    avatar_url    VARCHAR(500),
    -- ✔ Bỏ allergy_note (dùng PatientAllergy có cấu trúc)
    -- ✔ Bỏ patient_type (chuyển thành visit_type trong MedicalRecord)
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tiền sử dị ứng (có cấu trúc — JOIN với MedicineIngredient để cảnh báo)
CREATE TABLE PatientAllergy (
    allergy_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id    BIGINT NOT NULL,
    allergen_name VARCHAR(200) NOT NULL,
    severity      ENUM('MILD','MODERATE','SEVERE') DEFAULT 'MODERATE',
    reaction      VARCHAR(500),
    notes         TEXT,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)
) ENGINE=InnoDB;

-- Bệnh nền / bệnh mãn tính
CREATE TABLE PatientChronicDisease (
    chronic_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id   BIGINT NOT NULL,
    icd10_code   VARCHAR(10) NOT NULL,
    diagnosed_at DATE,
    is_active    BOOLEAN DEFAULT TRUE,
    note         VARCHAR(500),
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id),
    FOREIGN KEY (icd10_code) REFERENCES Icd10Code(code)
) ENGINE=InnoDB;

-- ============================================================
-- [E] HÀNG ĐỢI KHÁM (standalone, real-time display)
-- ============================================================

-- ⚠ Đồng bộ QueueEntry.status ↔ MedicalRecord.queue_status qua Transaction tại backend
CREATE TABLE QueueEntry (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id   BIGINT NOT NULL,
    queue_number INT    NOT NULL,
    priority     ENUM('EMERGENCY','ELDERLY','NORMAL') NOT NULL DEFAULT 'NORMAL',
    status       ENUM('WAITING','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'WAITING',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    called_at    DATETIME DEFAULT NULL,
    completed_at DATETIME DEFAULT NULL,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)
) ENGINE=InnoDB;

-- ============================================================
-- [F] LỊCH HẸN KHÁM
-- ============================================================

-- ⚠ UNIQUE chỉ ngăn trùng start_time; kiểm tra overlap tại tầng app
CREATE TABLE Appointment (
    appointment_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id       BIGINT NOT NULL,
    doctor_id        BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time       TIME NOT NULL,
    end_time         TIME NOT NULL,
    status           ENUM('SCHEDULED','CHECKED_IN','COMPLETED','CANCELLED') DEFAULT 'SCHEDULED',
    reason           VARCHAR(500),
    created_by       BIGINT,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_doctor_appointment (doctor_id, appointment_date, start_time),
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id),
    FOREIGN KEY (doctor_id)  REFERENCES Doctor(doctor_id),
    FOREIGN KEY (created_by) REFERENCES `User`(user_id)
) ENGINE=InnoDB;

-- ============================================================
-- [G] LƯỢT KHÁM — TRUNG TÂM HỆ THỐNG
-- ============================================================

CREATE TABLE MedicalRecord (
    record_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id     BIGINT NOT NULL,
    doctor_id      BIGINT,                       -- ✔ NULL khi lễ tân tiếp nhận, gán bác sĩ sau
    appointment_id BIGINT UNIQUE,
    visit_date     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- ✔ Phân loại lượt khám (chuyển từ Patient.patient_type về đây)
    visit_type     ENUM('FIRST_VISIT','REVISIT','EMERGENCY') NOT NULL DEFAULT 'FIRST_VISIT',

    -- Hàng đợi (⚠ đồng bộ với QueueEntry qua Transaction)
    queue_number   INT,
    priority       ENUM('NORMAL','ELDERLY','EMERGENCY') DEFAULT 'NORMAL',
    queue_status   ENUM(
                       'WAITING',
                       'EXAMINING',
                       'WAITING_LAB',
                       'PRESCRIBING',
                       'WAITING_PAYMENT',
                       'COMPLETED',
                       'TRANSFERRED',
                       'CANCELLED'
                   ) DEFAULT 'WAITING',

    -- Sinh hiệu
    blood_pressure VARCHAR(20),
    heart_rate     INT,
    temperature    DECIMAL(4,1),
    weight         DECIMAL(5,1),
    height         DECIMAL(5,1),
    spo2           INT,                           -- SpO2 (%)

    -- Chẩn đoán
    symptoms       TEXT,
    diagnosis      TEXT,
    diagnosis_code VARCHAR(10),                   -- ✔ FK thật đến Icd10Code
    referral_note  TEXT,
    notes          TEXT,

    -- ✔ Bỏ follow_up_date (dùng bảng FollowUp)
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (patient_id)     REFERENCES Patient(patient_id),
    FOREIGN KEY (doctor_id)      REFERENCES Doctor(doctor_id),
    FOREIGN KEY (appointment_id) REFERENCES Appointment(appointment_id),
    FOREIGN KEY (diagnosis_code) REFERENCES Icd10Code(code)
) ENGINE=InnoDB;

-- ============================================================
-- [H] XÉT NGHIỆM / CẬN LÂM SÀNG
-- ============================================================

CREATE TABLE Service (
    service_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(200) NOT NULL,
    service_type ENUM('EXAMINATION','LAB_TEST','IMAGING','PROCEDURE') NOT NULL,
    price        DECIMAL(15,2) NOT NULL,
    description  VARCHAR(500),
    is_active    BOOLEAN DEFAULT TRUE,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ✔ Bỏ LabOrder (trùng ServiceOrder); gộp vào 1 bảng duy nhất
CREATE TABLE ServiceOrder (
    order_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id    BIGINT NOT NULL,
    service_id   BIGINT NOT NULL,
    ordered_by   BIGINT,
    ordered_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,
    status       ENUM('ORDERED','IN_PROGRESS','COMPLETED','CANCELLED') DEFAULT 'ORDERED',
    notes        TEXT,
    FOREIGN KEY (record_id)  REFERENCES MedicalRecord(record_id),
    FOREIGN KEY (service_id) REFERENCES Service(service_id)
) ENGINE=InnoDB;

-- Kết quả xét nghiệm (cấu trúc rõ ràng: result_value, normal_range, unit)
CREATE TABLE LabResult (
    lab_result_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id        BIGINT NOT NULL,
    service_order_id BIGINT UNIQUE,
    test_name        VARCHAR(200) NOT NULL,
    result_value     VARCHAR(500),                -- Giá trị đo được
    normal_range     VARCHAR(200),                -- Khoảng bình thường (VD: 70-100 mg/dL)
    unit             VARCHAR(50),                 -- Đơn vị (mg/dL, mmol/L, %)
    result_text      TEXT,                        -- Nhận xét / kết luận tự do
    performed_by     BIGINT,
    test_date        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes            TEXT,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id)        REFERENCES MedicalRecord(record_id),
    FOREIGN KEY (service_order_id) REFERENCES ServiceOrder(order_id)
) ENGINE=InnoDB;

CREATE TABLE MedicalAttachment (
    attachment_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id        BIGINT NOT NULL,
    service_order_id BIGINT,
    file_url         VARCHAR(500) NOT NULL,
    file_type        VARCHAR(50),
    description      VARCHAR(255),
    uploaded_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id)        REFERENCES MedicalRecord(record_id),
    FOREIGN KEY (service_order_id) REFERENCES ServiceOrder(order_id)
) ENGINE=InnoDB;

-- ============================================================
-- [I] THUỐC & DANH MỤC
-- ============================================================

-- Danh mục thuốc (không lưu giá, tồn kho, hạn dùng — quản lý theo lô)
CREATE TABLE Medicine (
    medicine_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_code VARCHAR(20)  UNIQUE,
    medicine_name VARCHAR(200) NOT NULL,
    generic_name  VARCHAR(200),
    unit          VARCHAR(50)  NOT NULL,
    dosage_form   VARCHAR(100),                   -- Viên, siro, tiêm, bột...
    manufacturer  VARCHAR(200),
    description   VARCHAR(500),
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    -- ✔ Bỏ cost_price, sell_price, stock_qty, expiry_date → trong MedicineBatch
) ENGINE=InnoDB;

-- Thành phần / hoạt chất của từng thuốc (dùng kiểm tra dị ứng)
-- Logic: nếu ingredient_name nào khớp với PatientAllergy.allergen_name → CẢNH BÁO
CREATE TABLE MedicineIngredient (
    ingredient_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id     BIGINT       NOT NULL,
    ingredient_name VARCHAR(200) NOT NULL,
    UNIQUE KEY uq_medicine_ingredient (medicine_id, ingredient_name),
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id)
) ENGINE=InnoDB;

-- Tương tác thuốc nguy hiểm
-- ✔ CHECK (medicine_id_1 < medicine_id_2): đảm bảo không lưu trùng 2 chiều (A,B) và (B,A)
CREATE TABLE DrugInteraction (
    interaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id_1  BIGINT NOT NULL,
    medicine_id_2  BIGINT NOT NULL,
    severity       ENUM('MINOR','MODERATE','SEVERE','CONTRAINDICATED') NOT NULL DEFAULT 'MODERATE',
    description    TEXT NOT NULL,
    recommendation TEXT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_drug_pair (medicine_id_1, medicine_id_2),
    CHECK (medicine_id_1 < medicine_id_2),
    FOREIGN KEY (medicine_id_1) REFERENCES Medicine(medicine_id),
    FOREIGN KEY (medicine_id_2) REFERENCES Medicine(medicine_id)
) ENGINE=InnoDB;

-- ============================================================
-- [J] KHO DƯỢC — NHẬP HÀNG THEO LÔ
-- ============================================================

CREATE TABLE Supplier (
    supplier_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(200) NOT NULL,
    contact_name  VARCHAR(150),
    phone         VARCHAR(20),
    address       VARCHAR(500),
    is_active     BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE GoodsReceipt (
    receipt_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_code VARCHAR(50) UNIQUE NOT NULL,
    supplier_id  BIGINT,
    import_date  DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(15,2) DEFAULT 0,         -- ⚠ Validate = SUM(import_price * initial_qty)
    note         TEXT,
    created_by   BIGINT,
    status       ENUM('DRAFT','COMPLETED','CANCELLED') DEFAULT 'DRAFT',
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id),
    FOREIGN KEY (created_by)  REFERENCES `User`(user_id)
) ENGINE=InnoDB;

-- Lô thuốc — đơn vị quản lý hạn dùng và giá
CREATE TABLE MedicineBatch (
    batch_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_id       BIGINT NOT NULL,
    medicine_id      BIGINT NOT NULL,
    batch_number     VARCHAR(100) NOT NULL,
    manufacture_date DATE,
    expiry_date      DATE NOT NULL,               -- ⚠ Hạn dùng — cột quan trọng nhất
    import_price     DECIMAL(15,2) NOT NULL,
    sell_price       DECIMAL(15,2) NOT NULL,
    initial_qty      INT NOT NULL,
    current_qty      INT NOT NULL,
    min_threshold    INT NOT NULL DEFAULT 10,     -- Ngưỡng cảnh báo sắp hết hàng
    FOREIGN KEY (receipt_id)  REFERENCES GoodsReceipt(receipt_id),
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id)
) ENGINE=InnoDB;

-- Audit trail kho — không xoá, chỉ INSERT
-- ✔ CHECK constraint đảm bảo stock_after = stock_before ± quantity
CREATE TABLE StockTransaction (
    transaction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id      BIGINT NOT NULL,
    batch_id         BIGINT NOT NULL,
    transaction_type ENUM(
                         'IMPORT',
                         'EXPORT_PRESCRIPTION',
                         'ADJUSTMENT',
                         'RETURN_TO_SUPPLIER',
                         'EXPIRED_DISPOSAL'
                     ) NOT NULL,
    quantity         INT NOT NULL CHECK (quantity > 0),  -- Luôn dương
    stock_before     INT NOT NULL,
    stock_after      INT NOT NULL,
    reference_id     BIGINT COMMENT 'GoodsReceipt.receipt_id hoặc Invoice.invoice_id',
    notes            TEXT,
    created_by       BIGINT,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_stock_consistency CHECK (
        stock_after = stock_before + CASE
            WHEN transaction_type = 'IMPORT' THEN quantity
            ELSE -quantity
        END
    ),
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id),
    FOREIGN KEY (batch_id)    REFERENCES MedicineBatch(batch_id)
) ENGINE=InnoDB;

-- ============================================================
-- [K] KÊ ĐƠN THUỐC
-- ============================================================

-- ✔ UNIQUE(record_id): 1 lượt khám = tối đa 1 đơn thuốc
CREATE TABLE Prescription (
    prescription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id       BIGINT NOT NULL UNIQUE,
    status          ENUM('DRAFT','CONFIRMED','DISPENSED','CANCELLED') DEFAULT 'DRAFT',
    total_amount    DECIMAL(15,2) DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES MedicalRecord(record_id)
) ENGINE=InnoDB;

CREATE TABLE PrescriptionDetail (
    detail_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    medicine_id     BIGINT NOT NULL,
    batch_id        BIGINT,                       -- Lô xuất (FEFO: hết hạn gần nhất xuất trước)
    quantity        INT NOT NULL,
    dosage          VARCHAR(200),                 -- Liều: '1 viên × 3 lần/ngày'
    instruction     VARCHAR(500),                 -- Cách dùng: 'Uống sau ăn'
    frequency       VARCHAR(200),
    duration_days   INT DEFAULT 0,                -- Số ngày dùng thuốc
    unit_price      DECIMAL(15,2) NOT NULL,
    line_total      DECIMAL(15,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (prescription_id) REFERENCES Prescription(prescription_id),
    FOREIGN KEY (medicine_id)     REFERENCES Medicine(medicine_id),
    FOREIGN KEY (batch_id)        REFERENCES MedicineBatch(batch_id)
) ENGINE=InnoDB;

-- ============================================================
-- [L] PHÁT THUỐC (hỗ trợ phát thiếu PARTIAL)
-- ============================================================

CREATE TABLE Dispensing (
    dispensing_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    patient_id      BIGINT NOT NULL,
    dispensed_by    BIGINT,
    status          ENUM('PENDING','DISPENSED','PARTIAL') DEFAULT 'PENDING',
    total_amount    DECIMAL(15,2) DEFAULT 0,
    notes           TEXT,
    dispensed_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (prescription_id) REFERENCES Prescription(prescription_id),
    FOREIGN KEY (patient_id)      REFERENCES Patient(patient_id),
    FOREIGN KEY (dispensed_by)    REFERENCES `User`(user_id)
) ENGINE=InnoDB;

-- requested_quantity (bác sĩ kê) vs dispensed_quantity (thực tế phát)
-- → Phát thiếu: dispensed_quantity < requested_quantity, status = PARTIAL
CREATE TABLE DispensingItem (
    item_id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispensing_id          BIGINT NOT NULL,
    prescription_detail_id BIGINT NOT NULL,
    medicine_id            BIGINT NOT NULL,
    batch_id               BIGINT,
    medicine_name          VARCHAR(200),
    requested_quantity     INT NOT NULL,
    dispensed_quantity     INT NOT NULL,
    unit_price             DECIMAL(15,2),
    subtotal               DECIMAL(15,2) GENERATED ALWAYS AS (dispensed_quantity * unit_price) STORED,
    created_at             DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (dispensing_id)          REFERENCES Dispensing(dispensing_id),
    FOREIGN KEY (prescription_detail_id) REFERENCES PrescriptionDetail(detail_id),
    FOREIGN KEY (medicine_id)            REFERENCES Medicine(medicine_id),
    FOREIGN KEY (batch_id)               REFERENCES MedicineBatch(batch_id)
) ENGINE=InnoDB;

-- ============================================================
-- [M] HOÁ ĐƠN & THANH TOÁN
-- ============================================================

-- ✔ UNIQUE(record_id): 1 lượt khám = đúng 1 hoá đơn
CREATE TABLE Invoice (
    invoice_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id     BIGINT NOT NULL,
    record_id      BIGINT NOT NULL UNIQUE,
    invoice_date   DATETIME DEFAULT CURRENT_TIMESTAMP,
    exam_fee       DECIMAL(15,2) NOT NULL DEFAULT 0,
    service_fee    DECIMAL(15,2) NOT NULL DEFAULT 0,
    medicine_fee   DECIMAL(15,2) NOT NULL DEFAULT 0,
    other_fee      DECIMAL(15,2) NOT NULL DEFAULT 0,
    discount       DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_amount   DECIMAL(15,2) NOT NULL DEFAULT 0,
    paid_amount    DECIMAL(15,2) NOT NULL DEFAULT 0,
    change_amount  DECIMAL(15,2) NOT NULL DEFAULT 0,
    payment_method ENUM('CASH','TRANSFER','CARD'),
    payment_date   DATETIME,
    status         ENUM('PENDING','PAID','PARTIALLY_PAID','CANCELLED') DEFAULT 'PENDING',
    cashier_id     BIGINT,
    notes          TEXT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id),
    FOREIGN KEY (record_id)  REFERENCES MedicalRecord(record_id),
    FOREIGN KEY (cashier_id) REFERENCES `User`(user_id)
) ENGINE=InnoDB;

-- Chi tiết hoá đơn: DỊCH VỤ / XÉT NGHIỆM
CREATE TABLE InvoiceServiceDetail (
    detail_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id       BIGINT NOT NULL,
    service_order_id BIGINT NOT NULL,
    service_name     VARCHAR(200) NOT NULL,
    quantity         INT NOT NULL DEFAULT 1,
    unit_price       DECIMAL(15,2) NOT NULL,
    line_total       DECIMAL(15,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (invoice_id)       REFERENCES Invoice(invoice_id),
    FOREIGN KEY (service_order_id) REFERENCES ServiceOrder(order_id)
) ENGINE=InnoDB;

-- Chi tiết hoá đơn: THUỐC (có cost_price và profit_total để phân tích)
CREATE TABLE InvoiceMedicineDetail (
    detail_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id             BIGINT NOT NULL,
    medicine_id            BIGINT NOT NULL,
    prescription_detail_id BIGINT,
    batch_id               BIGINT,
    medicine_name          VARCHAR(200) NOT NULL,
    quantity               INT NOT NULL DEFAULT 1,
    unit_price             DECIMAL(15,2) NOT NULL,
    cost_price             DECIMAL(15,2) NOT NULL DEFAULT 0,
    line_total             DECIMAL(15,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    profit_total           DECIMAL(15,2) GENERATED ALWAYS AS (quantity * (unit_price - cost_price)) STORED,
    FOREIGN KEY (invoice_id)             REFERENCES Invoice(invoice_id),
    FOREIGN KEY (medicine_id)            REFERENCES Medicine(medicine_id),
    FOREIGN KEY (prescription_detail_id) REFERENCES PrescriptionDetail(detail_id),
    FOREIGN KEY (batch_id)               REFERENCES MedicineBatch(batch_id)
) ENGINE=InnoDB;

-- ============================================================
-- [N] HẸN TÁI KHÁM
-- ============================================================

CREATE TABLE FollowUp (
    follow_up_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id    BIGINT NOT NULL,
    record_id     BIGINT NOT NULL,
    doctor_id     BIGINT,
    follow_up_date DATE NOT NULL,
    reason        TEXT,
    status        ENUM('SCHEDULED','COMPLETED','MISSED','CANCELLED') DEFAULT 'SCHEDULED',
    reminder_sent BOOLEAN DEFAULT FALSE,          -- ✔ Track đã gửi SMS/Zalo nhắc lịch chưa
    notes         TEXT,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id),
    FOREIGN KEY (record_id)  REFERENCES MedicalRecord(record_id),
    FOREIGN KEY (doctor_id)  REFERENCES Doctor(doctor_id)
) ENGINE=InnoDB;

-- ============================================================
-- [O] INDEX TỐI ƯU TRUY VẤN
-- ============================================================

CREATE INDEX idx_patient_name       ON Patient(full_name);
CREATE INDEX idx_patient_phone      ON Patient(phone);
CREATE INDEX idx_patient_id_card    ON Patient(id_card);

CREATE INDEX idx_record_patient     ON MedicalRecord(patient_id, visit_date);
CREATE INDEX idx_record_doctor      ON MedicalRecord(doctor_id, visit_date);
CREATE INDEX idx_record_status      ON MedicalRecord(queue_status, visit_date);
CREATE INDEX idx_record_priority    ON MedicalRecord(priority, queue_number);

CREATE INDEX idx_queue_date_status  ON QueueEntry(created_at, status);
CREATE INDEX idx_queue_priority     ON QueueEntry(priority, created_at);

CREATE INDEX idx_appt_doctor_date   ON Appointment(doctor_id, appointment_date);
CREATE INDEX idx_appt_patient       ON Appointment(patient_id, appointment_date);

CREATE INDEX idx_schedule_doctor    ON Schedule(doctor_id, work_date);

CREATE INDEX idx_batch_expiry       ON MedicineBatch(expiry_date);
CREATE INDEX idx_batch_medicine     ON MedicineBatch(medicine_id, expiry_date);
CREATE INDEX idx_batch_threshold    ON MedicineBatch(current_qty, min_threshold);
CREATE INDEX idx_stock_batch        ON StockTransaction(batch_id, created_at);

CREATE INDEX idx_ingredient_name    ON MedicineIngredient(ingredient_name);
CREATE INDEX idx_allergy_patient    ON PatientAllergy(patient_id);
CREATE INDEX idx_drug_interact_med1 ON DrugInteraction(medicine_id_1);
CREATE INDEX idx_drug_interact_med2 ON DrugInteraction(medicine_id_2);

CREATE INDEX idx_svcorder_record    ON ServiceOrder(record_id);
CREATE INDEX idx_labresult_record   ON LabResult(record_id);

CREATE INDEX idx_prescription_record ON Prescription(record_id);
CREATE INDEX idx_dispensing_status   ON Dispensing(status);
CREATE INDEX idx_dispensing_pres     ON Dispensing(prescription_id);

CREATE INDEX idx_invoice_date       ON Invoice(invoice_date);
CREATE INDEX idx_invoice_status     ON Invoice(status);
CREATE INDEX idx_invoice_patient    ON Invoice(patient_id);

CREATE INDEX idx_followup_date      ON FollowUp(follow_up_date, status);
CREATE INDEX idx_followup_patient   ON FollowUp(patient_id);
CREATE INDEX idx_followup_reminder  ON FollowUp(reminder_sent, follow_up_date);

CREATE INDEX idx_icd10_code         ON Icd10Code(code);
CREATE INDEX idx_icd10_category     ON Icd10Code(category);

-- ============================================================
-- [P] VIEW TIỆN ÍCH
-- ============================================================

-- Tổng tồn kho theo thuốc (chỉ lô còn hạn dùng)
CREATE OR REPLACE VIEW v_medicine_stock AS
SELECT
    m.medicine_id,
    m.medicine_name,
    m.medicine_code,
    m.unit,
    COALESCE(SUM(mb.current_qty), 0)                                           AS total_stock,
    MIN(mb.expiry_date)                                                        AS nearest_expiry,
    SUM(CASE WHEN mb.current_qty <= mb.min_threshold THEN 1 ELSE 0 END)       AS low_stock_batches
FROM Medicine m
LEFT JOIN MedicineBatch mb
    ON m.medicine_id = mb.medicine_id
    AND mb.current_qty > 0
    AND mb.expiry_date > CURDATE()
GROUP BY m.medicine_id, m.medicine_name, m.medicine_code, m.unit;

-- Hàng đợi khám hôm nay
CREATE OR REPLACE VIEW v_today_queue AS
SELECT
    mr.record_id,
    mr.queue_number,
    mr.priority,
    mr.queue_status,
    mr.visit_type,
    p.full_name   AS patient_name,
    p.phone,
    u.full_name   AS doctor_name,
    mr.visit_date
FROM MedicalRecord mr
JOIN Patient p       ON mr.patient_id = p.patient_id
LEFT JOIN Doctor d   ON mr.doctor_id  = d.doctor_id
LEFT JOIN `User` u   ON d.user_id     = u.user_id
WHERE DATE(mr.visit_date) = CURDATE()
  AND mr.queue_status NOT IN ('COMPLETED','CANCELLED','TRANSFERRED')
ORDER BY FIELD(mr.priority,'EMERGENCY','ELDERLY','NORMAL'), mr.queue_number;

-- Lô thuốc sắp hết hạn trong 30 ngày tới
CREATE OR REPLACE VIEW v_expiring_batches AS
SELECT
    mb.batch_id,
    m.medicine_name,
    m.medicine_code,
    mb.batch_number,
    mb.expiry_date,
    mb.current_qty,
    DATEDIFF(mb.expiry_date, CURDATE()) AS days_until_expiry
FROM MedicineBatch mb
JOIN Medicine m ON mb.medicine_id = m.medicine_id
WHERE mb.current_qty > 0
  AND mb.expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)
ORDER BY mb.expiry_date ASC;

-- Doanh thu & lợi nhuận theo tháng
CREATE OR REPLACE VIEW v_monthly_revenue AS
SELECT
    DATE_FORMAT(i.payment_date, '%Y-%m')  AS month,
    COUNT(DISTINCT i.invoice_id)          AS total_invoices,
    SUM(i.exam_fee)                       AS exam_revenue,
    SUM(i.service_fee)                    AS service_revenue,
    SUM(i.medicine_fee)                   AS medicine_revenue,
    SUM(i.total_amount)                   AS total_revenue,
    COALESCE(SUM(med.profit), 0)          AS medicine_profit
FROM Invoice i
LEFT JOIN (
    SELECT invoice_id, SUM(profit_total) AS profit
    FROM InvoiceMedicineDetail
    GROUP BY invoice_id
) med ON i.invoice_id = med.invoice_id
WHERE i.status = 'PAID'
GROUP BY DATE_FORMAT(i.payment_date, '%Y-%m')
ORDER BY month DESC;

-- Tóm tắt hoá đơn đầy đủ (màn hình thu ngân)
CREATE OR REPLACE VIEW v_invoice_summary AS
SELECT
    i.invoice_id,
    i.record_id,
    i.invoice_date,
    i.exam_fee,
    i.service_fee,
    i.medicine_fee,
    i.discount,
    i.total_amount,
    i.paid_amount,
    i.change_amount,
    i.status,
    i.payment_method,
    i.payment_date,
    p.full_name   AS patient_name,
    p.phone       AS patient_phone,
    u.full_name   AS cashier_name,
    COALESCE(svc.svc_total,0)   AS service_detail_total,
    COALESCE(med.med_total,0)   AS medicine_detail_total,
    COALESCE(med.med_profit,0)  AS medicine_profit
FROM Invoice i
JOIN Patient p ON i.patient_id = p.patient_id
LEFT JOIN `User` u ON i.cashier_id = u.user_id
LEFT JOIN (
    SELECT invoice_id, SUM(line_total) AS svc_total
    FROM InvoiceServiceDetail GROUP BY invoice_id
) svc ON i.invoice_id = svc.invoice_id
LEFT JOIN (
    SELECT invoice_id,
           SUM(line_total)   AS med_total,
           SUM(profit_total) AS med_profit
    FROM InvoiceMedicineDetail GROUP BY invoice_id
) med ON i.invoice_id = med.invoice_id;