SET NAMES utf8mb4;
SET CHARACTER
SET utf8mb4;

USE clinic_management;


INSERT INTO Role
    (role_name, description)
VALUES
    ('ADMIN', 'Quản trị hệ thống'),
    ('DOCTOR', 'Bác sĩ'),
    ('NURSE', 'Y tá'),
    ('RECEPTIONIST', 'Lễ tân'),
    ('ACCOUNTANT', 'Kế toán'),
    ('PATIENT', 'Bệnh nhân (xem lịch sử khám)'),
    ('PHARMACIST', 'Dược sĩ / Kho dược');


INSERT INTO `User`
    (username, password_hash, full_name, email, phone, role_id)
VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Quản trị viên', 'admin@clinic.local', NULL, 1),
    ('doctor', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BS. Nguyễn Văn A', 'doctor@clinic.local', NULL, 2),
    ('letan', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Thị B', 'letan@clinic.local', NULL, 4),
    ('ketoan', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trần Thị C', 'ketoan@clinic.local', NULL, 5),
    ('duocsi', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Phạm Văn D', 'duocsi@clinic.local', NULL, 7),
    ('doctor2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BS. Trần Thị Mai', 'doctor2@clinic.local', '0901234568', 2),
    ('nurse1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'YT. Phạm Thị Hồng', 'nurse1@clinic.local', '0901234569', 3);


INSERT INTO Doctor
    (user_id, specialty, license_no)
VALUES
    (2, 'Nội tổng quát', 'BS-2024-001'),
    (6, 'Nhi khoa', 'BS-2024-002');

INSERT INTO Service
    (service_name, price, description)
VALUES
    ('Khám tổng quát', 150000, 'Khám bệnh tổng quát'),
    ('Xét nghiệm máu', 200000, 'Xét nghiệm công thức máu'),
    ('Xét nghiệm nước tiểu', 100000, 'Xét nghiệm nước tiểu thường quy'),
    ('Siêu âm bụng', 300000, 'Siêu âm bụng tổng quát'),
    ('Đo điện tim (ECG)', 250000, 'Đo điện tim');

INSERT INTO Medicine
    (medicine_code, medicine_name, generic_name, unit, dosage_form, cost_price, sell_price, stock_qty, min_threshold, manufacturer, expiry_date, description)
VALUES
    ('MED001', 'Paracetamol 500mg', 'Paracetamol', 'Viên', 'Viên nén', 1200, 2000, 500, 50, 'Công ty Dược Hậu Giang', '2027-12-31', 'Giảm đau, hạ sốt'),
    ('MED002', 'Amoxicillin 500mg', 'Amoxicillin', 'Viên', 'Viên nang', 1800, 3000, 300, 30, 'Công ty Dược Domesco', '2027-12-31', 'Kháng sinh phổ rộng'),
    ('MED003', 'Omeprazole 20mg', 'Omeprazole', 'Viên', 'Viên nang cứng', 3000, 5000, 200, 20, 'Công ty Dược Imexpharm', '2027-12-31', 'Ức chế bơm proton, điều trị dạ dày'),
    ('MED004', 'Loratadine 10mg', 'Loratadine', 'Viên', 'Viên nén', 2500, 4000, 150, 15, 'Công ty Dược OPV', '2027-12-31', 'Kháng histamin, chống dị ứng'),
    ('MED005', 'Vitamin C 1000mg', 'Ascorbic Acid', 'Viên', 'Viên sủi', 900, 1500, 1000, 100, 'Công ty Dược Traphaco', '2027-12-31', 'Bổ sung vitamin C'),
    ('MED006', 'Ibuprofen 400mg', 'Ibuprofen', 'Viên', 'Viên nén', 2000, 3500, 400, 40, 'Công ty Dược Hậu Giang', '2027-06-30', 'Giảm đau, kháng viêm không steroid'),
    ('MED007', 'Cetirizine 10mg', 'Cetirizine', 'Viên', 'Viên nén', 1500, 2500, 300, 30, 'Công ty Dược OPV', '2027-12-15', 'Kháng histamin thế hệ 2'),
    ('MED008', 'Metformin 500mg', 'Metformin', 'Viên', 'Viên nén', 2200, 3800, 250, 25, 'Công ty Dược Domesco', '2027-09-20', 'Điều trị đái tháo đường type 2'),
    ('MED009', 'Amlodipine 5mg', 'Amlodipine', 'Viên', 'Viên nén', 3500, 5500, 200, 20, 'Công ty Dược Imexpharm', '2027-08-10', 'Hạ huyết áp, thuốc chẹn kênh calci'),
    ('MED010', 'Azithromycin 250mg', 'Azithromycin', 'Viên', 'Viên nén', 5000, 8000, 150, 15, 'Công ty Dược Pymepharco', '2027-03-25', 'Kháng sinh nhóm macrolid'),
    ('MED011', 'Dexamethasone 0.5mg', 'Dexamethasone', 'Viên', 'Viên nén', 1000, 1800, 500, 50, 'Công ty Dược Traphaco', '2027-11-30', 'Corticosteroid kháng viêm'),
    ('MED012', 'Salbutamol 2mg', 'Salbutamol', 'Viên', 'Viên nén', 1200, 2000, 350, 35, 'Công ty Dược Mekophar', '2027-07-15', 'Giãn phế quản, điều trị hen'),
    ('MED013', 'Pantoprazole 40mg', 'Pantoprazole', 'Viên', 'Viên nén', 4000, 6500, 180, 18, 'Công ty Dược Imexpharm', '2027-10-20', 'Ức chế bơm proton'),
    ('MED014', 'Diclofenac 50mg', 'Diclofenac', 'Viên', 'Viên nén', 1800, 3000, 280, 28, 'Công ty Dược Hậu Giang', '2027-05-10', 'Giảm đau kháng viêm NSAID'),
    ('MED015', 'Ciprofloxacin 500mg', 'Ciprofloxacin', 'Viên', 'Viên nén', 3200, 5200, 220, 22, 'Công ty Dược Domesco', '2027-04-30', 'Kháng sinh nhóm fluoroquinolone'),
    ('MED016', 'Metoclopramid 10mg', 'Metoclopramide', 'Viên', 'Viên nén', 1500, 2500, 300, 30, 'Công ty Dược Hậu Giang', '2027-12-31', 'Chống nôn, kích thích nhu động dạ dày'),
    ('MED017', 'Bisoprolol 5mg', 'Bisoprolol', 'Viên', 'Viên nén bao phim', 3000, 5000, 200, 20, 'Công ty Dược Domesco', '2027-10-15', 'Chẹn beta, điều trị tăng huyết áp'),
    ('MED018', 'Atorvastatin 20mg', 'Atorvastatin', 'Viên', 'Viên nén bao phim', 4500, 7500, 180, 18, 'Công ty Dược Imexpharm', '2027-09-20', 'Hạ mỡ máu nhóm statin'),
    ('MED019', 'Loperamide 2mg', 'Loperamide', 'Viên', 'Viên nang', 1200, 2000, 400, 40, 'Công ty Dược OPV', '2027-11-30', 'Chống tiêu chảy'),
    ('MED020', 'Doxycycline 100mg', 'Doxycycline', 'Viên', 'Viên nang', 2000, 3500, 250, 25, 'Công ty Dược Pymepharco', '2027-08-10', 'Kháng sinh nhóm tetracycline');


INSERT INTO MedicineIngredient
    (medicine_id, ingredient_name)
VALUES
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


INSERT INTO ClinicConfig
    (config_key, config_value, description)
VALUES
    ('clinic_name', 'Phòng Khám Đa Khoa ABC', 'Tên phòng khám'),
    ('clinic_address', '123 Đường Nguyễn Văn Linh, Q.7, TP.HCM', 'Địa chỉ'),
    ('clinic_phone', '028-1234-5678', 'Số điện thoại'),
    ('clinic_email', 'info@phongkhamabc.vn', 'Email liên hệ'),
    ('default_exam_fee', '150000', 'Phí khám mặc định (VNĐ)'),
    ('working_hours', '07:30 - 17:00', 'Giờ làm việc'),
    ('invoice_prefix', 'HD', 'Tiền tố mã hóa đơn'),
    ('max_queue_per_day', '50', 'Số lượng BN tối đa mỗi ngày'),
    ('follow_up_reminder_days', '1', 'Số ngày trước tái khám gửi nhắc nhở'),
    ('prescription_validity_days', '7', 'Số ngày hiệu lực đơn thuốc'),
    ('allow_partial_dispensing', 'true', 'Cho phép phát thuốc một phần');


INSERT INTO Patient
    (full_name, gender, date_of_birth, phone, id_card, address, allergy_note, patient_type)
VALUES
    ('Nguyễn Văn Hùng', 'MALE', '1985-03-15', '0912345001', '079185001001', '123 Lê Lợi, Q.1, TP.HCM', 'NULL', 'REVISIT'),
    ('Trần Thị Lan', 'FEMALE', '1990-07-22', '0912345002', '079190002002', '45 Nguyễn Huệ, Q.1, TP.HCM', 'NULL', 'REVISIT'),
    ('Lê Minh Tuấn', 'MALE', '1978-11-08', '0912345003', '079178003003', '78 Trần Hưng Đạo, Q.5, TP.HCM', 'NULL', 'REVISIT'),
    ('Phạm Thị Hoa', 'FEMALE', '1995-01-30', '0912345004', '079195004004', '12 Hai Bà Trưng, Q.3, TP.HCM', 'NULL', 'FIRST_VISIT'),
    ('Hoàng Văn Đức', 'MALE', '1960-06-12', '0912345005', '079160005005', '90 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', 'NULL', 'REVISIT'),
    ('Vũ Thị Mai Anh', 'FEMALE', '2000-09-05', '0912345006', '079200006006', '34 Lý Tự Trọng, Q.1, TP.HCM', 'NULL', 'REVISIT'),
    ('Đặng Quốc Bảo', 'MALE', '1988-04-17', '0912345007', '079188007007', '56 Pasteur, Q.3, TP.HCM', 'NULL', 'FIRST_VISIT'),
    ('Bùi Thị Ngọc Trinh', 'FEMALE', '1972-12-25', '0912345008', '0791720₀8₀₀₈', '67 Cách Mạng Tháng 8, Q.1₀, TP.HCM', 'NULL', 'FIRST_VISIT'),
    ('Ngô Thanh Tùng', 'MALE', '1992-08-03', '0912345009', '079192009009', '23 Võ Văn Tần, Q.3, TP.HCM', 'NULL', 'FIRST_VISIT'),
    ('Lý Thị Kim Ngân', 'FEMALE', '1983-05-20', '0912345010', '079183010010', '89 Nam Kỳ Khởi Nghĩa, Q.1, TP.HCM', 'NULL', 'FIRST_VISIT');


INSERT INTO Appointment
    (patient_id, doctor_id, appointment_date, start_time, end_time, status, reason)
VALUES
    (1, 1, '2026-03-09', '09:00:00', '10:00:00', 'COMPLETED', 'Khám tổng quát'),
    (2, 1, '2026-03-09', '10:30:00', '11:30:00', 'COMPLETED', 'Tái khám định kỳ'),
    (3, 2, '2026-03-10', '08:00:00', '09:00:00', 'CHECKED_IN', 'Khám nhi'),
    (4, 1, '2026-03-10', '09:00:00', '10:00:00', 'SCHEDULED', 'Khám nội');

