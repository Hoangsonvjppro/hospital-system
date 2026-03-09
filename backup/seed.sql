-- ============================================================
-- SEED DATA — Phòng Mạch Tư
-- Chạy sau schema.sql
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE clinic_management;

-- ============================================================
-- 1. VAI TRÒ (Roles)
-- ============================================================

INSERT INTO Role (role_name, description) VALUES
    ('ADMIN',        'Quản trị hệ thống'),
    ('DOCTOR',       'Bác sĩ'),
    ('NURSE',        'Y tá'),
    ('RECEPTIONIST', 'Lễ tân'),
    ('ACCOUNTANT',   'Kế toán'),
    ('PATIENT',      'Bệnh nhân (xem lịch sử khám)'),
    ('PHARMACIST',   'Dược sĩ / Kho dược');

-- ============================================================
-- 2. TÀI KHOẢN (Users)
-- Password cho tất cả: "password"
-- ============================================================

INSERT INTO `User` (username, password_hash, full_name, email, phone, role_id) VALUES
    ('admin',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Quản trị viên',      'admin@clinic.local',    NULL,         1),
    ('doctor',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BS. Nguyễn Văn A',    'doctor@clinic.local',   NULL,         2),
    ('letan',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Thị B',            'letan@clinic.local',    NULL,         4),
    ('ketoan',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trần Thị C',          'ketoan@clinic.local',   NULL,         5),
    ('duocsi',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Phạm Văn D',          'duocsi@clinic.local',   NULL,         7),
    ('doctor2',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BS. Trần Thị Mai',    'doctor2@clinic.local',  '0901234568', 2),
    ('nurse1',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'YT. Phạm Thị Hồng',  'nurse1@clinic.local',   '0901234569', 3);

-- ============================================================
-- 3. BÁC SĨ (Doctors)
-- ============================================================

INSERT INTO Doctor (user_id, specialty, license_no) VALUES
    (2, 'Nội tổng quát', 'BS-2024-001'),
    (6, 'Nhi khoa',      'BS-2024-002');

-- ============================================================
-- 4. DỊCH VỤ KHÁM (Services)
-- ============================================================

INSERT INTO Service (service_name, price, description) VALUES
    ('Khám tổng quát',          150000, 'Khám bệnh tổng quát'),
    ('Xét nghiệm máu',         200000, 'Xét nghiệm công thức máu'),
    ('Xét nghiệm nước tiểu',   100000, 'Xét nghiệm nước tiểu thường quy'),
    ('Siêu âm bụng',           300000, 'Siêu âm bụng tổng quát'),
    ('Đo điện tim (ECG)',       250000, 'Đo điện tim');

-- ============================================================
-- 5. KHO THUỐC (20 loại)
-- ============================================================

INSERT INTO Medicine (medicine_code, medicine_name, generic_name, unit, dosage_form, cost_price, sell_price, stock_qty, min_threshold, manufacturer, expiry_date, description) VALUES
    ('MED001', 'Paracetamol 500mg',     'Paracetamol',     'Viên', 'Viên nén',          1200, 2000,  500, 50,  'Công ty Dược Hậu Giang',   '2027-12-31', 'Giảm đau, hạ sốt'),
    ('MED002', 'Amoxicillin 500mg',     'Amoxicillin',     'Viên', 'Viên nang',         1800, 3000,  300, 30,  'Công ty Dược Domesco',     '2027-12-31', 'Kháng sinh phổ rộng'),
    ('MED003', 'Omeprazole 20mg',       'Omeprazole',      'Viên', 'Viên nang cứng',    3000, 5000,  200, 20,  'Công ty Dược Imexpharm',   '2027-12-31', 'Ức chế bơm proton, điều trị dạ dày'),
    ('MED004', 'Loratadine 10mg',       'Loratadine',      'Viên', 'Viên nén',          2500, 4000,  150, 15,  'Công ty Dược OPV',         '2027-12-31', 'Kháng histamin, chống dị ứng'),
    ('MED005', 'Vitamin C 1000mg',      'Ascorbic Acid',   'Viên', 'Viên sủi',           900, 1500, 1000, 100, 'Công ty Dược Traphaco',    '2027-12-31', 'Bổ sung vitamin C'),
    ('MED006', 'Ibuprofen 400mg',       'Ibuprofen',       'Viên', 'Viên nén',          2000, 3500,  400, 40,  'Công ty Dược Hậu Giang',   '2027-06-30', 'Giảm đau, kháng viêm không steroid'),
    ('MED007', 'Cetirizine 10mg',       'Cetirizine',      'Viên', 'Viên nén',          1500, 2500,  300, 30,  'Công ty Dược OPV',         '2027-12-15', 'Kháng histamin thế hệ 2'),
    ('MED008', 'Metformin 500mg',       'Metformin',       'Viên', 'Viên nén',          2200, 3800,  250, 25,  'Công ty Dược Domesco',     '2027-09-20', 'Điều trị đái tháo đường type 2'),
    ('MED009', 'Amlodipine 5mg',        'Amlodipine',      'Viên', 'Viên nén',          3500, 5500,  200, 20,  'Công ty Dược Imexpharm',   '2027-08-10', 'Hạ huyết áp, thuốc chẹn kênh calci'),
    ('MED010', 'Azithromycin 250mg',    'Azithromycin',    'Viên', 'Viên nén',          5000, 8000,  150, 15,  'Công ty Dược Pymepharco',  '2027-03-25', 'Kháng sinh nhóm macrolid'),
    ('MED011', 'Dexamethasone 0.5mg',   'Dexamethasone',   'Viên', 'Viên nén',          1000, 1800,  500, 50,  'Công ty Dược Traphaco',    '2027-11-30', 'Corticosteroid kháng viêm'),
    ('MED012', 'Salbutamol 2mg',        'Salbutamol',      'Viên', 'Viên nén',          1200, 2000,  350, 35,  'Công ty Dược Mekophar',    '2027-07-15', 'Giãn phế quản, điều trị hen'),
    ('MED013', 'Pantoprazole 40mg',     'Pantoprazole',    'Viên', 'Viên nén',          4000, 6500,  180, 18,  'Công ty Dược Imexpharm',   '2027-10-20', 'Ức chế bơm proton'),
    ('MED014', 'Diclofenac 50mg',       'Diclofenac',      'Viên', 'Viên nén',          1800, 3000,  280, 28,  'Công ty Dược Hậu Giang',  '2027-05-10', 'Giảm đau kháng viêm NSAID'),
    ('MED015', 'Ciprofloxacin 500mg',   'Ciprofloxacin',   'Viên', 'Viên nén',          3200, 5200,  220, 22,  'Công ty Dược Domesco',     '2027-04-30', 'Kháng sinh nhóm fluoroquinolone'),
    ('MED016', 'Metoclopramid 10mg',    'Metoclopramide',  'Viên', 'Viên nén',          1500, 2500,  300, 30,  'Công ty Dược Hậu Giang',  '2027-12-31', 'Chống nôn, kích thích nhu động dạ dày'),
    ('MED017', 'Bisoprolol 5mg',        'Bisoprolol',      'Viên', 'Viên nén bao phim', 3000, 5000,  200, 20,  'Công ty Dược Domesco',     '2027-10-15', 'Chẹn beta, điều trị tăng huyết áp'),
    ('MED018', 'Atorvastatin 20mg',     'Atorvastatin',    'Viên', 'Viên nén bao phim', 4500, 7500,  180, 18,  'Công ty Dược Imexpharm',   '2027-09-20', 'Hạ mỡ máu nhóm statin'),
    ('MED019', 'Loperamide 2mg',        'Loperamide',      'Viên', 'Viên nang',         1200, 2000,  400, 40,  'Công ty Dược OPV',         '2027-11-30', 'Chống tiêu chảy'),
    ('MED020', 'Doxycycline 100mg',     'Doxycycline',     'Viên', 'Viên nang',         2000, 3500,  250, 25,  'Công ty Dược Pymepharco',  '2027-08-10', 'Kháng sinh nhóm tetracycline');

-- ============================================================
-- 6. THÀNH PHẦN THUỐC (phục vụ cảnh báo dị ứng)
-- ============================================================

INSERT INTO MedicineIngredient (medicine_id, ingredient_name) VALUES
    (1, 'Paracetamol'),
    (2, 'Amoxicillin'),
    (2, 'Penicillin'),
    (3, 'Omeprazole'),
    (4, 'Loratadine'),
    (5, 'Ascorbic Acid'),
    (6, 'Ibuprofen'),
    (7, 'Cetirizine'),
    (8, 'Metformin'),
    (9, 'Amlodipine'),
    (10, 'Azithromycin'),
    (11, 'Dexamethasone'),
    (12, 'Salbutamol'),
    (13, 'Pantoprazole'),
    (14, 'Diclofenac'),
    (15, 'Ciprofloxacin'),
    (16, 'Metoclopramide'),
    (17, 'Bisoprolol'),
    (18, 'Atorvastatin'),
    (19, 'Loperamide'),
    (20, 'Doxycycline'),
    (20, 'Tetracycline');

-- ============================================================
-- 7. CẤU HÌNH PHÒNG KHÁM (ClinicConfig)
-- ============================================================

INSERT INTO ClinicConfig (config_key, config_value, description) VALUES
    ('clinic_name',              'Phòng Khám Đa Khoa ABC',                 'Tên phòng khám'),
    ('clinic_address',           '123 Đường Nguyễn Văn Linh, Q.7, TP.HCM', 'Địa chỉ'),
    ('clinic_phone',             '028-1234-5678',                           'Số điện thoại'),
    ('clinic_email',             'info@phongkhamabc.vn',                    'Email liên hệ'),
    ('default_exam_fee',         '150000',                                  'Phí khám mặc định (VNĐ)'),
    ('working_hours',            '07:30 - 17:00',                           'Giờ làm việc'),
    ('invoice_prefix',           'HD',                                      'Tiền tố mã hóa đơn'),
    ('max_queue_per_day',        '50',                                      'Số lượng BN tối đa mỗi ngày'),
    ('follow_up_reminder_days',  '1',                                       'Số ngày trước tái khám gửi nhắc nhở'),
    ('prescription_validity_days','7',                                      'Số ngày hiệu lực đơn thuốc'),
    ('allow_partial_dispensing', 'true',                                     'Cho phép phát thuốc một phần');

-- ============================================================
-- 8. BỆNH NHÂN (10 bệnh nhân mẫu)
-- ============================================================

INSERT INTO Patient (full_name, gender, date_of_birth, phone, id_card, address, allergy_note, patient_type) VALUES
    ('Nguyễn Văn Hùng',     'MALE',   '1985-03-15', '0912345001', '079185001001', '123 Lê Lợi, Q.1, TP.HCM',                   'Dị ứng Penicillin',    'REVISIT'),
    ('Trần Thị Lan',        'FEMALE', '1990-07-22', '0912345002', '079190002002', '45 Nguyễn Huệ, Q.1, TP.HCM',                 NULL,                   'REVISIT'),
    ('Lê Minh Tuấn',        'MALE',   '1978-11-08', '0912345003', '079178003003', '78 Trần Hưng Đạo, Q.5, TP.HCM',             'Dị ứng Aspirin',       'REVISIT'),
    ('Phạm Thị Hoa',        'FEMALE', '1995-01-30', '0912345004', '079195004004', '12 Hai Bà Trưng, Q.3, TP.HCM',               NULL,                   'FIRST_VISIT'),
    ('Hoàng Văn Đức',       'MALE',   '1960-06-12', '0912345005', '079160005005', '90 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM',     'Dị ứng Paracetamol',   'REVISIT'),
    ('Vũ Thị Mai Anh',      'FEMALE', '2000-09-05', '0912345006', '079200006006', '34 Lý Tự Trọng, Q.1, TP.HCM',               NULL,                   'REVISIT'),
    ('Đặng Quốc Bảo',       'MALE',   '1988-04-17', '0912345007', '079188007007', '56 Pasteur, Q.3, TP.HCM',                    'Dị ứng Sulfonamide',   'FIRST_VISIT'),
    ('Bùi Thị Ngọc Trinh',  'FEMALE', '1972-12-25', '0912345008', '079172008008', '67 Cách Mạng Tháng 8, Q.10, TP.HCM',         'Dị ứng Ibuprofen',     'FIRST_VISIT'),
    ('Ngô Thanh Tùng',      'MALE',   '1992-08-03', '0912345009', '079192009009', '23 Võ Văn Tần, Q.3, TP.HCM',                NULL,                   'FIRST_VISIT'),
    ('Lý Thị Kim Ngân',     'FEMALE', '1983-05-20', '0912345010', '079183010010', '89 Nam Kỳ Khởi Nghĩa, Q.1, TP.HCM',         NULL,                   'FIRST_VISIT');

-- ============================================================
-- 9. TIỀN SỬ DỊ ỨNG
-- ============================================================

INSERT INTO PatientAllergy (patient_id, allergen_name, severity, reaction) VALUES
    (1, 'Penicillin',    'SEVERE',   'Khó thở, phù mặt'),
    (3, 'Aspirin',       'MODERATE', 'Nổi mẩn đỏ'),
    (5, 'Paracetamol',   'MILD',     'Ngứa nhẹ'),
    (7, 'Sulfonamide',   'SEVERE',   'Sốc phản vệ'),
    (8, 'Ibuprofen',     'MODERATE', 'Đau bụng, buồn nôn');

-- ============================================================
-- 10. LỊCH LÀM VIỆC BÁC SĨ
-- ============================================================

INSERT INTO Schedule (doctor_id, work_date, start_time, end_time, notes) VALUES
    (1, CURDATE(), '08:00:00', '12:00:00', 'Ca sáng'),
    (1, CURDATE(), '14:00:00', '17:00:00', 'Ca chiều'),
    (1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '12:00:00', 'Ca sáng'),
    (1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', '17:00:00', 'Cả ngày'),
    (2, CURDATE(), '08:00:00', '17:00:00', 'Cả ngày');

-- ============================================================
-- 11. LỊCH HẸN KHÁM
-- ============================================================

INSERT INTO Appointment (patient_id, doctor_id, appointment_date, start_time, end_time, status, reason) VALUES
    (1, 1, CURDATE(), '08:30:00', '09:00:00', 'SCHEDULED',  'Khám tổng quát định kỳ'),
    (2, 1, CURDATE(), '09:00:00', '09:30:00', 'SCHEDULED',  'Đau đầu kéo dài'),
    (3, 1, CURDATE(), '09:30:00', '10:00:00', 'SCHEDULED',  'Tái khám huyết áp'),
    (4, 1, CURDATE(), '10:00:00', '10:30:00', 'CHECKED_IN', 'Ho và sốt nhẹ'),
    (5, 1, CURDATE(), '10:30:00', '11:00:00', 'SCHEDULED',  'Đau khớp gối');

-- ============================================================
-- 12. BỆNH ÁN
-- ============================================================

-- Bệnh án đã hoàn thành
INSERT INTO MedicalRecord (patient_id, doctor_id, appointment_id, visit_date, blood_pressure, heart_rate, temperature, weight, height, spo2, symptoms, diagnosis, diagnosis_code, notes, queue_status, priority, queue_number, follow_up_date) VALUES
    (1, 1, NULL, DATE_SUB(NOW(), INTERVAL 7 DAY),
     '130/85', 78, 36.8, 72.0, 170.0, 98,
     'Đau đầu, chóng mặt, mệt mỏi',
     'Tăng huyết áp độ 1', 'I10',
     'Cần theo dõi huyết áp hàng ngày. Tái khám sau 2 tuần.',
     'COMPLETED', 'NORMAL', 1, DATE_ADD(CURDATE(), INTERVAL 7 DAY)),

    (3, 1, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY),
     '150/95', 82, 37.0, 80.5, 168.0, 97,
     'Đau đầu, hoa mắt, tê tay chân',
     'Tăng huyết áp độ 2', 'I11',
     'Kê đơn thuốc hạ áp. Kiểm tra lại sau 1 tuần.',
     'COMPLETED', 'ELDERLY', 2, DATE_ADD(CURDATE(), INTERVAL 3 DAY)),

    (6, 1, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY),
     '120/80', 72, 38.5, 55.0, 160.0, 96,
     'Ho, sổ mũi, đau họng, sốt nhẹ 38.5°C',
     'Viêm họng cấp', 'J02.9',
     'Nghỉ ngơi, uống nhiều nước ấm. Tái khám nếu sốt cao.',
     'COMPLETED', 'NORMAL', 3, NULL);

-- Bệnh án đang chờ
INSERT INTO MedicalRecord (patient_id, doctor_id, appointment_id, visit_date, queue_status, priority, queue_number) VALUES
    (4, 1, 4, NOW(), 'EXAMINING', 'NORMAL', 1),
    (2, 1, NULL, NOW(), 'WAITING', 'NORMAL', 2),
    (5, 1, NULL, NOW(), 'WAITING', 'ELDERLY', 3);

-- ============================================================
-- 13. ĐƠN THUỐC
-- ============================================================

INSERT INTO Prescription (record_id, status, total_amount) VALUES
    (1, 'DISPENSED', 0),
    (2, 'DISPENSED', 0),
    (3, 'CONFIRMED', 0);

INSERT INTO PrescriptionDetail (prescription_id, medicine_id, quantity, dosage, instruction, unit_price) VALUES
    (1, 9,  30, '1 viên/ngày',          'Uống buổi sáng sau ăn',    5500),
    (1, 5,  10, '1 viên/ngày',          'Uống buổi tối',            1500),
    (2, 9,  30, '1 viên/ngày',          'Uống buổi sáng sau ăn',    5500),
    (2, 1,  20, '2 viên x 2 lần/ngày',  'Uống khi đau đầu',         2000),
    (3, 10, 6,  '1 viên/ngày x 3 ngày', 'Uống sau ăn 1 giờ',        8000),
    (3, 1,  10, '2 viên x 3 lần/ngày',  'Uống khi sốt > 38.5°C',    2000),
    (3, 5,  10, '1 viên/ngày',           'Uống buổi sáng',           1500);

-- Cập nhật total_amount
UPDATE Prescription SET total_amount = (SELECT COALESCE(SUM(line_total), 0) FROM PrescriptionDetail WHERE prescription_id = 1) WHERE prescription_id = 1;
UPDATE Prescription SET total_amount = (SELECT COALESCE(SUM(line_total), 0) FROM PrescriptionDetail WHERE prescription_id = 2) WHERE prescription_id = 2;
UPDATE Prescription SET total_amount = (SELECT COALESCE(SUM(line_total), 0) FROM PrescriptionDetail WHERE prescription_id = 3) WHERE prescription_id = 3;

-- ============================================================
-- 14. GIAO DỊCH KHO
-- ============================================================

INSERT INTO StockTransaction (medicine_id, transaction_type, quantity, stock_before, stock_after, reference_type, reference_id, notes) VALUES
    (9,  'EXPORT', -30, 200, 170,  'PRESCRIPTION', 1, 'Xuất Amlodipine cho đơn thuốc #1'),
    (5,  'EXPORT', -10, 1000, 990, 'PRESCRIPTION', 1, 'Xuất Vitamin C cho đơn thuốc #1'),
    (9,  'EXPORT', -30, 170, 140,  'PRESCRIPTION', 2, 'Xuất Amlodipine cho đơn thuốc #2'),
    (1,  'EXPORT', -20, 500, 480,  'PRESCRIPTION', 2, 'Xuất Paracetamol cho đơn thuốc #2');

UPDATE Medicine SET stock_qty = 140 WHERE medicine_id = 9;
UPDATE Medicine SET stock_qty = 480 WHERE medicine_id = 1;
UPDATE Medicine SET stock_qty = 990 WHERE medicine_id = 5;

-- ============================================================
-- 15. HÓA ĐƠN
-- ============================================================

INSERT INTO Invoice (patient_id, record_id, invoice_date, exam_fee, medicine_fee, total_amount, paid_amount, change_amount, status, payment_method, payment_date, notes) VALUES
    (1, 1, DATE_SUB(NOW(), INTERVAL 7 DAY), 150000, 180000, 330000, 350000, 20000, 'PAID', 'CASH',     DATE_SUB(NOW(), INTERVAL 7 DAY), 'Thanh toán tiền mặt'),
    (3, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), 150000, 205000, 355000, 355000, 0,     'PAID', 'TRANSFER', DATE_SUB(NOW(), INTERVAL 5 DAY), 'Chuyển khoản ngân hàng');

INSERT INTO InvoiceMedicineDetail (invoice_id, medicine_id, medicine_name, quantity, unit_price, cost_price) VALUES
    (1, 9,  'Amlodipine 5mg',    30, 5500, 3500),
    (1, 5,  'Vitamin C 1000mg',  10, 1500, 900),
    (2, 9,  'Amlodipine 5mg',    30, 5500, 3500),
    (2, 1,  'Paracetamol 500mg', 20, 2000, 1200);

-- ============================================================
-- DONE
-- ============================================================
SELECT '=== SEED HOÀN TẤT ===' AS status;
SELECT CONCAT('Bệnh nhân: ', COUNT(*)) AS info FROM Patient
UNION ALL
SELECT CONCAT('Bác sĩ: ', COUNT(*)) FROM Doctor
UNION ALL
SELECT CONCAT('Thuốc: ', COUNT(*)) FROM Medicine
UNION ALL
SELECT CONCAT('Bệnh án: ', COUNT(*)) FROM MedicalRecord
UNION ALL
SELECT CONCAT('Lịch hẹn hôm nay: ', COUNT(*)) FROM Appointment WHERE appointment_date = CURDATE();
