-- ============================================================
-- SEED DATA — Phòng Mạch Tư (schema v4)
-- Chạy sau schema.sql
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE clinic_v4;

-- ============================================================
-- 1. VAI TRÒ (Roles)
-- ============================================================

INSERT IGNORE INTO Role (role_name, description) VALUES
    ('ADMIN',        'Quản trị hệ thống'),
    ('DOCTOR',       'Bác sĩ'),
    ('NURSE',        'Y tá'),
    ('RECEPTIONIST', 'Lễ tân'),
    ('CASHIER',      'Kế toán / Thu ngân'),
    ('PHARMACIST',   'Dược sĩ / Kho dược');

-- ============================================================
-- 2. TÀI KHOẢN (Users)
-- Password cho tất cả: "password"
-- DataSeeder sẽ cập nhật lại password sau khi seed
-- ============================================================

INSERT IGNORE INTO `User` (username, password, full_name, email, phone, role_id) VALUES
    ('admin',    'password', 'Quản trị viên',     'admin@clinic.local',    '0900000001', 1),
    ('doctor',   'password', 'BS. Nguyễn Văn A',  'doctor@clinic.local',   '0900000002', 2),
    ('doctor2',  'password', 'BS. Trần Thị Mai',  'doctor2@clinic.local',  '0900000003', 2),
    ('nurse1',   'password', 'YT. Phạm Thị Hồng', 'nurse1@clinic.local',  '0900000004', 3),
    ('letan',    'password', 'Lê Thị B',          'letan@clinic.local',    '0900000005', 4),
    ('ketoan',   'password', 'Trần Thị C',        'ketoan@clinic.local',   '0900000006', 5),
    ('duocsi',   'password', 'Phạm Văn D',        'duocsi@clinic.local',   '0900000007', 6);

-- ============================================================
-- 3. CHUYÊN KHOA & BÁC SĨ
-- ============================================================

INSERT IGNORE INTO Specialty (specialty_name) VALUES
    ('Nội tổng quát'),
    ('Nhi khoa');

INSERT IGNORE INTO Doctor (user_id, specialty_id, license_no) VALUES
    (2, 1, 'BS-2024-001'),
    (3, 2, 'BS-2024-002');

-- ============================================================
-- 4. THUỐC (10 loại)
-- ============================================================

INSERT IGNORE INTO Medicine (medicine_code, medicine_name, generic_name, unit, dosage_form, manufacturer) VALUES
    ('MED001', 'Paracetamol 500mg',   'Paracetamol',       'Viên', 'Viên nén',  'Hậu Giang Pharma'),
    ('MED002', 'Amoxicillin 500mg',   'Amoxicillin',       'Viên', 'Viên nang', 'Domesco'),
    ('MED003', 'Omeprazol 20mg',      'Omeprazole',        'Viên', 'Viên nang', 'Stada'),
    ('MED004', 'Loratadin 10mg',      'Loratadine',        'Viên', 'Viên nén',  'Imexpharm'),
    ('MED005', 'Metformin 500mg',     'Metformin',         'Viên', 'Viên nén',  'Hậu Giang Pharma'),
    ('MED006', 'Amlodipine 5mg',      'Amlodipine',        'Viên', 'Viên nén',  'Stada'),
    ('MED007', 'Cefuroxim 500mg',     'Cefuroxime',        'Viên', 'Viên nén',  'Domesco'),
    ('MED008', 'Ibuprofen 400mg',     'Ibuprofen',         'Viên', 'Viên nén',  'Imexpharm'),
    ('MED009', 'Dextromethorphan 15mg','Dextromethorphan',  'Viên', 'Viên nén',  'Hậu Giang Pharma'),
    ('MED010', 'Loperamide 2mg',      'Loperamide',        'Viên', 'Viên nang', 'Stada');
