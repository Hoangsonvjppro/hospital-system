SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS clinic_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE clinic_management;

CREATE TABLE IF NOT EXISTS Role (
    role_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `User` (
    user_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(150) UNIQUE,
    phone         VARCHAR(20),
    role_id       BIGINT       NOT NULL,
    is_active     BOOLEAN      DEFAULT TRUE,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES Role(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Patient (
    patient_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(150) NOT NULL,
    gender        ENUM('MALE','FEMALE','OTHER') NOT NULL,
    date_of_birth DATE         NOT NULL,
    phone         VARCHAR(20),
    id_card       VARCHAR(12)  UNIQUE,
    address       VARCHAR(500),
    allergy_note  VARCHAR(500),
    patient_type  ENUM('FIRST_VISIT','REVISIT','EMERGENCY') DEFAULT 'FIRST_VISIT',
    user_id       BIGINT       UNIQUE,
    is_active     BOOLEAN      DEFAULT TRUE,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `User`(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS PatientAllergy (
    allergy_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id    BIGINT       NOT NULL,
    allergen_name VARCHAR(200) NOT NULL,
    severity      ENUM('MILD','MODERATE','SEVERE') DEFAULT 'MODERATE',
    reaction      VARCHAR(500),
    notes         TEXT,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS QueueEntry (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id    BIGINT       NOT NULL,
    queue_number  INT          NOT NULL,
    priority      ENUM('EMERGENCY','ELDERLY','NORMAL') NOT NULL DEFAULT 'NORMAL',
    status        ENUM('WAITING','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'WAITING',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    called_at     DATETIME     DEFAULT NULL,
    completed_at  DATETIME     DEFAULT NULL,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS Doctor (
    doctor_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL UNIQUE,
    specialty   VARCHAR(150),
    license_no  VARCHAR(50)  UNIQUE,
    is_active   BOOLEAN      DEFAULT TRUE,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `User`(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Service (
    service_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(200)   NOT NULL,
    price        DECIMAL(15,2)  NOT NULL,
    description  VARCHAR(500),
    is_active    BOOLEAN        DEFAULT TRUE,
    created_at   DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Medicine (
    medicine_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_code VARCHAR(20)    UNIQUE,
    medicine_name VARCHAR(200)   NOT NULL,
    generic_name  VARCHAR(200),
    unit          VARCHAR(50)    NOT NULL,
    dosage_form   VARCHAR(100),
    cost_price    DECIMAL(15,2)  NOT NULL,
    sell_price    DECIMAL(15,2)  NOT NULL,
    stock_qty     INT            NOT NULL DEFAULT 0,
    min_threshold INT            NOT NULL DEFAULT 10,
    manufacturer  VARCHAR(200),
    expiry_date   DATE,
    description   VARCHAR(500),
    is_active     BOOLEAN        DEFAULT TRUE,
    created_at    DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS MedicineIngredient (
    ingredient_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id     BIGINT       NOT NULL,
    ingredient_name VARCHAR(200) NOT NULL,
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id),
    UNIQUE KEY uq_medicine_ingredient (medicine_id, ingredient_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS StockTransaction (
    transaction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id      BIGINT       NOT NULL,
    transaction_type ENUM('IMPORT','EXPORT','ADJUSTMENT','RETURN') NOT NULL,
    quantity         INT          NOT NULL,
    stock_before     INT          NOT NULL,
    stock_after      INT          NOT NULL,
    reference_type   VARCHAR(50),
    reference_id     BIGINT,
    notes            TEXT,
    created_by       BIGINT,
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
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

-- Schedule table removed: DoctorSchedulePanel uses AppointmentBUS instead.

CREATE TABLE IF NOT EXISTS Appointment (
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


CREATE TABLE IF NOT EXISTS MedicalRecord (
    record_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id     BIGINT       NOT NULL,
    doctor_id      BIGINT       NOT NULL,
    appointment_id BIGINT       UNIQUE,
    visit_date     DATETIME     NOT NULL,
    blood_pressure VARCHAR(20),
    heart_rate     INT,
    temperature    DECIMAL(4,1),
    weight         DECIMAL(5,1),
    height         DECIMAL(5,1),
    spo2           INT,
    symptoms       TEXT,
    diagnosis      TEXT,
    diagnosis_code VARCHAR(10),
    notes          TEXT,
    queue_status   ENUM('WAITING','EXAMINING','IN_PROGRESS','PRESCRIBED','DISPENSED','COMPLETED','PAID','TRANSFERRED','CANCELLED')
                   DEFAULT NULL,
    priority       ENUM('NORMAL','ELDERLY','EMERGENCY') DEFAULT 'NORMAL',
    queue_number   INT,
    arrival_time   TIME         DEFAULT NULL,
    exam_type      VARCHAR(100) DEFAULT NULL,
    follow_up_date DATE         DEFAULT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)     REFERENCES Patient(patient_id),
    FOREIGN KEY (doctor_id)      REFERENCES Doctor(doctor_id),
    FOREIGN KEY (appointment_id) REFERENCES Appointment(appointment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ServiceOrder (
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

CREATE TABLE IF NOT EXISTS LabResult (
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

CREATE TABLE IF NOT EXISTS LabOrder (
    lab_order_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    examination_id  BIGINT       NOT NULL              ,
    patient_id      BIGINT       NOT NULL              ,
    test_type       ENUM('BLOOD','URINE','XRAY','ULTRASOUND','OTHER') DEFAULT 'OTHER'
                                                       COMMENT 'Loại xét nghiệm',
    test_name       VARCHAR(255) NOT NULL               COMMENT 'Tên xét nghiệm cụ thể',
    status          ENUM('PENDING','IN_PROGRESS','COMPLETED') DEFAULT 'PENDING'
                                                       COMMENT 'Trạng thái xử lý',
    result          TEXT                                COMMENT 'Kết quả xét nghiệm (text)',
    notes           TEXT                                COMMENT 'Ghi chú bổ sung',
    ordered_at      DATETIME     DEFAULT CURRENT_TIMESTAMP
                                                       COMMENT 'Thời điểm yêu cầu',
    completed_at    DATETIME                            COMMENT 'Thời điểm hoàn tất',
    ordered_by      BIGINT                              COMMENT 'FK → User.user_id (bác sĩ chỉ định)',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (examination_id) REFERENCES MedicalRecord(record_id),
    FOREIGN KEY (patient_id)     REFERENCES Patient(patient_id),
    FOREIGN KEY (ordered_by)     REFERENCES `User`(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Prescription (
    prescription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id       BIGINT       NOT NULL,
    status          ENUM('DRAFT','CONFIRMED','DISPENSED','CANCELLED') DEFAULT 'DRAFT',
    total_amount    DECIMAL(15,2) DEFAULT 0,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES MedicalRecord(record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS PrescriptionDetail (
    detail_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT        NOT NULL,
    medicine_id     BIGINT        NOT NULL,
    quantity        INT           NOT NULL,
    dosage          VARCHAR(200),
    instruction     VARCHAR(500),
    frequency       VARCHAR(200)  COMMENT 'Cách dùng: Ngày 3 lần, mỗi lần 1 viên',
    duration        INT DEFAULT 0 COMMENT 'Số ngày dùng thuốc',
    unit_price      DECIMAL(15,2) NOT NULL,
    line_total      DECIMAL(15,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (prescription_id) REFERENCES Prescription(prescription_id),
    FOREIGN KEY (medicine_id)     REFERENCES Medicine(medicine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS Invoice (
    invoice_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id     BIGINT        NOT NULL,
    record_id      BIGINT,
    invoice_date   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exam_fee       DECIMAL(15,2) NOT NULL DEFAULT 0,
    medicine_fee   DECIMAL(15,2) NOT NULL DEFAULT 0,
    other_fee      DECIMAL(15,2) NOT NULL DEFAULT 0,
    discount       DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_amount   DECIMAL(15,2) NOT NULL DEFAULT 0,
    paid_amount    DECIMAL(15,2) NOT NULL DEFAULT 0,
    change_amount  DECIMAL(15,2) NOT NULL DEFAULT 0,
    status         ENUM('PENDING','PAID','CANCELLED') DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    payment_date   DATETIME,
    notes          TEXT,
    created_by     BIGINT,
    created_at     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id),
    FOREIGN KEY (record_id)  REFERENCES MedicalRecord(record_id),
    FOREIGN KEY (created_by) REFERENCES `User`(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS InvoiceServiceDetail (
    detail_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id       BIGINT        NOT NULL,
    service_order_id BIGINT        NOT NULL,
    service_name     VARCHAR(200)  NOT NULL,
    quantity         INT           NOT NULL DEFAULT 1,
    unit_price       DECIMAL(15,2) NOT NULL,
    line_total       DECIMAL(15,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (invoice_id)       REFERENCES Invoice(invoice_id),
    FOREIGN KEY (service_order_id) REFERENCES ServiceOrder(order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS InvoiceMedicineDetail (
    detail_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id             BIGINT        NOT NULL,
    medicine_id            BIGINT        NOT NULL,
    prescription_detail_id BIGINT,
    medicine_name          VARCHAR(200)  NOT NULL,
    quantity               INT           NOT NULL DEFAULT 1,
    unit_price             DECIMAL(15,2) NOT NULL,
    cost_price             DECIMAL(15,2) NOT NULL DEFAULT 0,
    line_total             DECIMAL(15,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    profit_total           DECIMAL(15,2) GENERATED ALWAYS AS (quantity * (unit_price - cost_price)) STORED,
    FOREIGN KEY (invoice_id)             REFERENCES Invoice(invoice_id),
    FOREIGN KEY (medicine_id)            REFERENCES Medicine(medicine_id),
    FOREIGN KEY (prescription_detail_id) REFERENCES PrescriptionDetail(detail_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Dispensing (
    dispensing_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT        NOT NULL,
    patient_id      BIGINT        NOT NULL,
    dispensed_by    BIGINT,
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

CREATE TABLE IF NOT EXISTS DispensingItem (
    item_id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispensing_id          BIGINT        NOT NULL,
    prescription_detail_id BIGINT        NOT NULL,
    medicine_id            BIGINT        NOT NULL,
    medicine_name          VARCHAR(200),
    requested_quantity     INT           NOT NULL,
    dispensed_quantity     INT           NOT NULL,
    unit_price             DECIMAL(15,2),
    subtotal               DECIMAL(15,2) GENERATED ALWAYS AS (dispensed_quantity * unit_price) STORED,
    batch_number           VARCHAR(50),
    created_at             DATETIME      DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (dispensing_id)          REFERENCES Dispensing(dispensing_id),
    FOREIGN KEY (prescription_detail_id) REFERENCES PrescriptionDetail(detail_id),
    FOREIGN KEY (medicine_id)            REFERENCES Medicine(medicine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS FollowUp (
    follow_up_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id     BIGINT       NOT NULL,
    record_id      BIGINT       NOT NULL,
    follow_up_date DATE         NOT NULL,
    reason         TEXT,
    status         ENUM('SCHEDULED','COMPLETED','MISSED','CANCELLED') DEFAULT 'SCHEDULED',
    reminder_sent  BOOLEAN      DEFAULT FALSE,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(patient_id),
    FOREIGN KEY (record_id)  REFERENCES MedicalRecord(record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS ClinicConfig (
    config_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key   VARCHAR(50)  NOT NULL UNIQUE,
    config_value VARCHAR(500) NOT NULL,
    description  VARCHAR(200),
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS DrugInteraction (
    interaction_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id_1   BIGINT       NOT NULL,
    medicine_id_2   BIGINT       NOT NULL,
    severity        ENUM('MINOR','MODERATE','SEVERE','CONTRAINDICATED') NOT NULL DEFAULT 'MODERATE',
    description     TEXT         NOT NULL COMMENT 'Mô tả tương tác',
    recommendation  TEXT         COMMENT 'Khuyến nghị xử trí',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medicine_id_1) REFERENCES Medicine(medicine_id),
    FOREIGN KEY (medicine_id_2) REFERENCES Medicine(medicine_id),
    UNIQUE KEY uq_drug_pair (medicine_id_1, medicine_id_2),
    CHECK (medicine_id_1 < medicine_id_2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Icd10Code (
    icd_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    code      VARCHAR(10)  NOT NULL UNIQUE,
    name_vi   VARCHAR(500) NOT NULL COMMENT 'Tên bệnh tiếng Việt',
    name_en   VARCHAR(500) COMMENT 'Tên bệnh tiếng Anh',
    category  VARCHAR(200) COMMENT 'Nhóm bệnh'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE OR REPLACE VIEW InvoiceSummary AS
SELECT
    i.invoice_id,
    i.patient_id,
    i.record_id,
    i.invoice_date,
    i.exam_fee,
    i.medicine_fee,
    i.other_fee,
    i.discount,
    i.total_amount,
    i.paid_amount,
    i.change_amount,
    i.status,
    i.payment_method,
    i.payment_date,
    i.notes,
    i.created_by,
    i.created_at,
    COALESCE(svc.service_detail_total, 0) AS service_detail_total,
    COALESCE(med.medicine_detail_total, 0) AS medicine_detail_total,
    COALESCE(med.medicine_cost, 0) AS medicine_cost,
    COALESCE(med.medicine_profit, 0) AS medicine_profit,
    p.full_name AS patient_name,
    p.phone AS patient_phone
FROM Invoice i
JOIN Patient p ON i.patient_id = p.patient_id
LEFT JOIN (
    SELECT invoice_id,
           SUM(line_total) AS service_detail_total
    FROM InvoiceServiceDetail
    GROUP BY invoice_id
) svc ON i.invoice_id = svc.invoice_id
LEFT JOIN (
    SELECT invoice_id,
           SUM(line_total) AS medicine_detail_total,
           SUM(quantity * cost_price) AS medicine_cost,
           SUM(profit_total) AS medicine_profit
    FROM InvoiceMedicineDetail
    GROUP BY invoice_id
) med ON i.invoice_id = med.invoice_id;


CREATE INDEX idx_patient_name    ON Patient(full_name);
CREATE INDEX idx_patient_phone   ON Patient(phone);
CREATE INDEX idx_patient_id_card ON Patient(id_card);
CREATE INDEX idx_patient_type    ON Patient(patient_type);

CREATE INDEX idx_allergy_patient ON PatientAllergy(patient_id);

CREATE INDEX idx_queue_date_status ON QueueEntry(created_at, status);
CREATE INDEX idx_queue_patient     ON QueueEntry(patient_id);
CREATE INDEX idx_queue_priority    ON QueueEntry(priority, created_at);
CREATE INDEX idx_ingredient_name ON MedicineIngredient(ingredient_name);

CREATE INDEX idx_laborder_exam      ON LabOrder(examination_id);
CREATE INDEX idx_laborder_patient   ON LabOrder(patient_id);
CREATE INDEX idx_laborder_status    ON LabOrder(status);
CREATE INDEX idx_laborder_orderedby ON LabOrder(ordered_by);

CREATE INDEX idx_record_patient    ON MedicalRecord(patient_id, visit_date);
CREATE INDEX idx_record_doctor     ON MedicalRecord(doctor_id, visit_date);
CREATE INDEX idx_record_queue      ON MedicalRecord(queue_status, visit_date);
CREATE INDEX idx_record_followup   ON MedicalRecord(follow_up_date);
CREATE INDEX idx_record_priority   ON MedicalRecord(priority, queue_number);

CREATE INDEX idx_invoice_date   ON Invoice(invoice_date);
CREATE INDEX idx_invoice_status ON Invoice(status);

CREATE INDEX idx_medicine_stock ON Medicine(stock_qty, min_threshold);
CREATE INDEX idx_stock_medicine ON StockTransaction(medicine_id, created_at);

CREATE INDEX idx_appt_doctor_date ON Appointment(doctor_id, appointment_date);
CREATE INDEX idx_svcorder_record  ON ServiceOrder(record_id);
CREATE INDEX idx_prescription_record ON Prescription(record_id);

CREATE INDEX idx_dispensing_prescription ON Dispensing(prescription_id);
CREATE INDEX idx_dispensing_patient      ON Dispensing(patient_id);
CREATE INDEX idx_dispensing_status       ON Dispensing(status);
CREATE INDEX idx_dispensingitem_disp     ON DispensingItem(dispensing_id);

CREATE INDEX idx_followup_date    ON FollowUp(follow_up_date);
CREATE INDEX idx_followup_patient ON FollowUp(patient_id);
CREATE INDEX idx_followup_status  ON FollowUp(status, follow_up_date);

CREATE INDEX idx_drug_interaction_med1 ON DrugInteraction(medicine_id_1);
CREATE INDEX idx_drug_interaction_med2 ON DrugInteraction(medicine_id_2);
CREATE INDEX idx_icd10_code            ON Icd10Code(code);
CREATE INDEX idx_icd10_name_vi         ON Icd10Code(name_vi);
