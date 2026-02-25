-- ============================================================
-- SEED DỮ LIỆU TEST — Phòng Mạch Tư
-- Chạy sau khi init_database.sql đã tạo schema & seed cơ bản.
-- ============================================================

USE clinic_management;

-- ============================================================
-- 1. THÊM USER & BÁC SĨ PHỤ
-- ============================================================

-- Thêm bác sĩ thứ 2 và 1 y tá (password = "password")
INSERT IGNORE INTO `User` (username, password_hash, full_name, email, phone, role_id) VALUES
    ('doctor2',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BS. Trần Thị Mai',   'doctor2@clinic.local',  '0901234568', 2),
    ('nurse1',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'YT. Phạm Thị Hồng',  'nurse1@clinic.local',   '0901234569', 3);

-- Tạo Doctor record cho bác sĩ thứ 2
INSERT IGNORE INTO Doctor (user_id, specialty, license_no)
SELECT user_id, 'Nhi khoa', 'BS-2024-002'
FROM `User` WHERE username = 'doctor2';

-- ============================================================
-- 2. BỆNH NHÂN (10 bệnh nhân mẫu)
-- ============================================================

INSERT INTO Patient (full_name, gender, date_of_birth, phone, address) VALUES
    ('Nguyễn Văn Hùng',     'MALE',   '1985-03-15', '0912345001', '123 Lê Lợi, Q.1, TP.HCM'),
    ('Trần Thị Lan',        'FEMALE', '1990-07-22', '0912345002', '45 Nguyễn Huệ, Q.1, TP.HCM'),
    ('Lê Minh Tuấn',        'MALE',   '1978-11-08', '0912345003', '78 Trần Hưng Đạo, Q.5, TP.HCM'),
    ('Phạm Thị Hoa',        'FEMALE', '1995-01-30', '0912345004', '12 Hai Bà Trưng, Q.3, TP.HCM'),
    ('Hoàng Văn Đức',       'MALE',   '1960-06-12', '0912345005', '90 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM'),
    ('Vũ Thị Mai Anh',      'FEMALE', '2000-09-05', '0912345006', '34 Lý Tự Trọng, Q.1, TP.HCM'),
    ('Đặng Quốc Bảo',       'MALE',   '1988-04-17', '0912345007', '56 Pasteur, Q.3, TP.HCM'),
    ('Bùi Thị Ngọc Trinh',  'FEMALE', '1972-12-25', '0912345008', '67 Cách Mạng Tháng 8, Q.10, TP.HCM'),
    ('Ngô Thanh Tùng',      'MALE',   '1992-08-03', '0912345009', '23 Võ Văn Tần, Q.3, TP.HCM'),
    ('Lý Thị Kim Ngân',     'FEMALE', '1983-05-20', '0912345010', '89 Nam Kỳ Khởi Nghĩa, Q.1, TP.HCM');

-- ============================================================
-- 3. TIỀN SỬ DỊ ỨNG (một số bệnh nhân có dị ứng)
-- ============================================================

INSERT INTO PatientAllergy (patient_id, allergen_name, severity, reaction) VALUES
    (1, 'Penicillin',    'SEVERE',   'Khó thở, phù mặt'),
    (3, 'Aspirin',       'MODERATE', 'Nổi mẩn đỏ'),
    (5, 'Paracetamol',   'MILD',     'Ngứa nhẹ'),
    (7, 'Sulfonamide',   'SEVERE',   'Sốc phản vệ'),
    (8, 'Ibuprofen',     'MODERATE', 'Đau bụng, buồn nôn');

-- ============================================================
-- 4. THÊM THUỐC (bổ sung thêm 10 loại thuốc)
-- ============================================================

INSERT INTO Medicine (medicine_name, unit, cost_price, sell_price, stock_qty, min_threshold, expiry_date, description) VALUES
    ('Ibuprofen 400mg',       'Viên',  2000, 3500,  400, 40, '2027-06-30', 'Giảm đau, kháng viêm không steroid'),
    ('Cetirizine 10mg',       'Viên',  1500, 2500,  300, 30, '2027-12-15', 'Kháng histamin thế hệ 2'),
    ('Metformin 500mg',       'Viên',  2200, 3800,  250, 25, '2027-09-20', 'Điều trị đái tháo đường type 2'),
    ('Amlodipine 5mg',        'Viên',  3500, 5500,  200, 20, '2027-08-10', 'Hạ huyết áp, thuốc chẹn kênh calci'),
    ('Azithromycin 250mg',    'Viên',  5000, 8000,  150, 15, '2027-03-25', 'Kháng sinh nhóm macrolid'),
    ('Dexamethasone 0.5mg',   'Viên',  1000, 1800,  500, 50, '2027-11-30', 'Corticosteroid kháng viêm'),
    ('Salbutamol 2mg',        'Viên',  1200, 2000,  350, 35, '2027-07-15', 'Giãn phế quản, điều trị hen'),
    ('Pantoprazole 40mg',     'Viên',  4000, 6500,  180, 18, '2027-10-20', 'Ức chế bơm proton'),
    ('Diclofenac 50mg',       'Viên',  1800, 3000,  280, 28, '2027-05-10', 'Giảm đau kháng viêm NSAID'),
    ('Ciprofloxacin 500mg',   'Viên',  3200, 5200,  220, 22, '2027-04-30', 'Kháng sinh nhóm fluoroquinolone');

-- Thành phần thuốc mới (phục vụ cảnh báo dị ứng)
INSERT INTO MedicineIngredient (medicine_id, ingredient_name) VALUES
    (6, 'Ibuprofen'),
    (7, 'Cetirizine'),
    (8, 'Metformin'),
    (9, 'Amlodipine'),
    (10, 'Azithromycin'),
    (11, 'Dexamethasone'),
    (12, 'Salbutamol'),
    (13, 'Pantoprazole'),
    (14, 'Diclofenac'),
    (15, 'Ciprofloxacin');

-- ============================================================
-- 5. LỊCH LÀM VIỆC BÁC SĨ (tuần này)
-- ============================================================

INSERT INTO Schedule (doctor_id, work_date, start_time, end_time, notes) VALUES
    -- Bác sĩ 1: Nguyễn Văn A
    (1, CURDATE(), '08:00:00', '12:00:00', 'Ca sáng'),
    (1, CURDATE(), '14:00:00', '17:00:00', 'Ca chiều'),
    (1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '12:00:00', 'Ca sáng'),
    (1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', '17:00:00', 'Cả ngày');

-- Schedule cho bác sĩ 2 (nếu tồn tại)
INSERT IGNORE INTO Schedule (doctor_id, work_date, start_time, end_time, notes)
SELECT 2, CURDATE(), '08:00:00', '17:00:00', 'Cả ngày'
FROM Doctor WHERE doctor_id = 2;

-- ============================================================
-- 6. LỊCH HẸN KHÁM (một số bệnh nhân có lịch hẹn hôm nay)
-- ============================================================

INSERT INTO Appointment (patient_id, doctor_id, appointment_date, start_time, end_time, status, reason) VALUES
    (1, 1, CURDATE(), '08:30:00', '09:00:00', 'SCHEDULED',  'Khám tổng quát định kỳ'),
    (2, 1, CURDATE(), '09:00:00', '09:30:00', 'SCHEDULED',  'Đau đầu kéo dài'),
    (3, 1, CURDATE(), '09:30:00', '10:00:00', 'SCHEDULED',  'Tái khám huyết áp'),
    (4, 1, CURDATE(), '10:00:00', '10:30:00', 'CHECKED_IN', 'Ho và sốt nhẹ'),
    (5, 1, CURDATE(), '10:30:00', '11:00:00', 'SCHEDULED',  'Đau khớp gối');

-- ============================================================
-- 7. BỆNH ÁN (3 bệnh án đã hoàn thành + 2 đang chờ khám)
-- ============================================================

-- Bệnh án đã hoàn thành (có triệu chứng + chẩn đoán)
INSERT INTO MedicalRecord (patient_id, doctor_id, appointment_id, visit_date, blood_pressure, heart_rate, temperature, weight, height, spo2, symptoms, diagnosis, notes) VALUES
    (1, 1, NULL, DATE_SUB(NOW(), INTERVAL 7 DAY),
     '130/85', 78, 36.8, 72.0, 170.0, 98,
     'Đau đầu, chóng mặt, mệt mỏi',
     'Tăng huyết áp độ 1',
     'Cần theo dõi huyết áp hàng ngày. Tái khám sau 2 tuần.'),

    (3, 1, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY),
     '150/95', 82, 37.0, 80.5, 168.0, 97,
     'Đau đầu, hoa mắt, tê tay chân',
     'Tăng huyết áp độ 2',
     'Kê đơn thuốc hạ áp. Kiểm tra lại sau 1 tuần.'),

    (6, 1, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY),
     '120/80', 72, 38.5, 55.0, 160.0, 96,
     'Ho, sổ mũi, đau họng, sốt nhẹ 38.5°C',
     'Viêm họng cấp',
     'Nghỉ ngơi, uống nhiều nước ấm. Tái khám nếu sốt cao.');

-- Bệnh án mới — đang chờ bác sĩ khám (chưa có triệu chứng & chẩn đoán)
INSERT INTO MedicalRecord (patient_id, doctor_id, appointment_id, visit_date) VALUES
    (4, 1, 4, NOW()),
    (2, 1, NULL, NOW());

-- ============================================================
-- 8. ĐƠN THUỐC (cho 3 bệnh án đã hoàn thành)
-- ============================================================

-- Đơn thuốc cho bệnh án 1 (BN Nguyễn Văn Hùng — Tăng huyết áp)
INSERT INTO Prescription (record_id, status, total_amount) VALUES
    (1, 'DISPENSED', 0);

INSERT INTO PrescriptionDetail (prescription_id, medicine_id, quantity, dosage, instruction, unit_price) VALUES
    (1, 9,  30, '1 viên/ngày',       'Uống buổi sáng sau ăn',   5500),    -- Amlodipine
    (1, 5,  10, '1 viên/ngày',       'Uống buổi tối',           1500);    -- Vitamin C

-- Cập nhật total_amount
UPDATE Prescription SET total_amount = (SELECT SUM(line_total) FROM PrescriptionDetail WHERE prescription_id = 1) WHERE prescription_id = 1;

-- Đơn thuốc cho bệnh án 2 (BN Lê Minh Tuấn — Tăng huyết áp)
INSERT INTO Prescription (record_id, status, total_amount) VALUES
    (2, 'DISPENSED', 0);

INSERT INTO PrescriptionDetail (prescription_id, medicine_id, quantity, dosage, instruction, unit_price) VALUES
    (2, 9,  30, '1 viên/ngày',       'Uống buổi sáng sau ăn',   5500),    -- Amlodipine
    (2, 1,  20, '2 viên x 2 lần/ngày', 'Uống khi đau đầu',      2000);    -- Paracetamol

UPDATE Prescription SET total_amount = (SELECT SUM(line_total) FROM PrescriptionDetail WHERE prescription_id = 2) WHERE prescription_id = 2;

-- Đơn thuốc cho bệnh án 3 (BN Vũ Thị Mai Anh — Viêm họng)
INSERT INTO Prescription (record_id, status, total_amount) VALUES
    (3, 'CONFIRMED', 0);

INSERT INTO PrescriptionDetail (prescription_id, medicine_id, quantity, dosage, instruction, unit_price) VALUES
    (3, 10, 6,  '1 viên/ngày x 3 ngày', 'Uống sau ăn 1 giờ',    8000),    -- Azithromycin
    (3, 1,  10, '2 viên x 3 lần/ngày',  'Uống khi sốt > 38.5°C', 2000),   -- Paracetamol
    (3, 5,  10, '1 viên/ngày',           'Uống buổi sáng',        1500);    -- Vitamin C

UPDATE Prescription SET total_amount = (SELECT SUM(line_total) FROM PrescriptionDetail WHERE prescription_id = 3) WHERE prescription_id = 3;

-- ============================================================
-- 9. GIAO DỊCH KHO (StockTransaction cho thuốc đã xuất)
-- ============================================================

-- Xuất kho cho đơn thuốc 1
INSERT INTO StockTransaction (medicine_id, transaction_type, quantity, stock_before, stock_after, reference_type, reference_id, notes) VALUES
    (9,  'EXPORT', -30, 200, 170, 'PRESCRIPTION', 1, 'Xuất Amlodipine cho đơn thuốc #1'),
    (5,  'EXPORT', -10, 1000, 990, 'PRESCRIPTION', 1, 'Xuất Vitamin C cho đơn thuốc #1');

-- Xuất kho cho đơn thuốc 2
INSERT INTO StockTransaction (medicine_id, transaction_type, quantity, stock_before, stock_after, reference_type, reference_id, notes) VALUES
    (9,  'EXPORT', -30, 170, 140, 'PRESCRIPTION', 2, 'Xuất Amlodipine cho đơn thuốc #2'),
    (1,  'EXPORT', -20, 500, 480, 'PRESCRIPTION', 2, 'Xuất Paracetamol cho đơn thuốc #2');

-- Cập nhật stock_qty trong Medicine
UPDATE Medicine SET stock_qty = 140 WHERE medicine_id = 9;   -- Amlodipine
UPDATE Medicine SET stock_qty = 480 WHERE medicine_id = 1;   -- Paracetamol
UPDATE Medicine SET stock_qty = 990 WHERE medicine_id = 5;   -- Vitamin C

-- ============================================================
-- 10. HÓA ĐƠN (cho 2 bệnh án đã phát thuốc)
-- ============================================================

INSERT INTO Invoice (patient_id, record_id, invoice_date, status, payment_method, notes) VALUES
    (1, 1, DATE_SUB(NOW(), INTERVAL 7 DAY), 'PAID', 'CASH', 'Thanh toán tiền mặt'),
    (3, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), 'PAID', 'TRANSFER', 'Chuyển khoản ngân hàng');

-- Chi tiết hóa đơn — tiền thuốc
INSERT INTO InvoiceMedicineDetail (invoice_id, medicine_id, medicine_name, quantity, unit_price, cost_price) VALUES
    (1, 9,  'Amlodipine 5mg',    30, 5500, 3500),
    (1, 5,  'Vitamin C 1000mg',  10, 1500, 900),
    (2, 9,  'Amlodipine 5mg',    30, 5500, 3500),
    (2, 1,  'Paracetamol 500mg', 20, 2000, 1200);

-- ============================================================
-- DONE! Tóm tắt dữ liệu đã seed:
-- ============================================================
-- ✅ 2 user mới (doctor2, nurse1)
-- ✅ 2 bác sĩ (1 Nội tổng quát, 1 Nhi khoa)
-- ✅ 10 bệnh nhân với tên & địa chỉ tiếng Việt
-- ✅ 5 dị ứng (cảnh báo khi kê đơn)
-- ✅ 15 loại thuốc (5 cũ + 10 mới)
-- ✅ Lịch làm việc bác sĩ (hôm nay + 2 ngày tới)
-- ✅ 5 lịch hẹn khám hôm nay
-- ✅ 5 bệnh án (3 hoàn thành + 2 đang chờ)
-- ✅ 3 đơn thuốc
-- ✅ Giao dịch kho thuốc
-- ✅ 2 hóa đơn đã thanh toán

SELECT '=== SEED TEST DATA HOÀN TẤT ===' AS status;
SELECT CONCAT('Bệnh nhân: ', COUNT(*)) AS info FROM Patient
UNION ALL
SELECT CONCAT('Bác sĩ: ', COUNT(*)) FROM Doctor
UNION ALL
SELECT CONCAT('Thuốc: ', COUNT(*)) FROM Medicine
UNION ALL
SELECT CONCAT('Bệnh án: ', COUNT(*)) FROM MedicalRecord
UNION ALL
SELECT CONCAT('Lịch hẹn hôm nay: ', COUNT(*)) FROM Appointment WHERE appointment_date = CURDATE();
