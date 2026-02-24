-- ============================================================
-- SEED 3 USER DEMO: admin, doctor, letan
-- Password cho tất cả: "password"
-- ============================================================

USE clinic_management;

-- Kiểm tra nếu user chưa tồn tại thì mới insert
INSERT IGNORE INTO `User` (username, password_hash, full_name, email, role_id) VALUES
    ('admin',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Quản trị viên',   'admin@clinic.local',    1),
    ('doctor',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'BS. Nguyễn Văn A', 'doctor@clinic.local',   2),
    ('letan',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Thị B',        'letan@clinic.local',    4);

-- Tạo record Doctor cho user bác sĩ
INSERT IGNORE INTO Doctor (user_id, specialty, license_no)
SELECT user_id, 'Nội tổng quát', 'BS-2024-001'
FROM `User` WHERE username = 'doctor';

-- Kiểm tra kết quả
SELECT user_id, username, full_name, role_id FROM `User`;
