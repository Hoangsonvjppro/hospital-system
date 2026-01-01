-- =============================================
-- 1. QUẢN TRỊ HỆ THỐNG (ADMIN & IAM)
-- =============================================

CREATE TABLE IF NOT EXISTS "VaiTro" ( -- Roles
    "MaVaiTro" serial,
    "TenVaiTro" varchar(50) NOT NULL UNIQUE,
    "MoTa" varchar(255),
    PRIMARY KEY("MaVaiTro")
);

CREATE TYPE "TrangThai_t" AS ENUM ('HOAT_DONG', 'KHOA', 'VO_HIEU'); -- ACTIVE, LOCKED, DISABLED

CREATE TABLE IF NOT EXISTS "NguoiDung" ( -- Users
    "MaNguoiDung" bigserial,
    "TenDangNhap" varchar(50) NOT NULL UNIQUE,
    "MatKhauHash" varchar(255) NOT NULL,
    "Email" varchar(100),
    "TrangThai" TrangThai_t DEFAULT 'HOAT_DONG',
    "NgayTao" timestamp DEFAULT CURRENT_TIMESTAMP,
    "LanDangNhapCuoi" timestamp,
    PRIMARY KEY("MaNguoiDung")
);

CREATE TABLE IF NOT EXISTS "NguoiDung_VaiTro" ( -- UserRoles
    "MaNguoiDung" bigint,
    "MaVaiTro" int,
    PRIMARY KEY("MaNguoiDung", "MaVaiTro")
);

-- =============================================
-- 2. NHÂN SỰ & DANH MỤC (HRM & MASTER DATA)
-- =============================================

CREATE TYPE "LoaiKhoa_t" AS ENUM ('LAM_SANG', 'CAN_LAM_SANG', 'DUOC', 'HANH_CHINH');

CREATE TABLE IF NOT EXISTS "KhoaPhong" ( -- Departments
    "MaKhoa" serial,
    "TenKhoa" varchar(100) NOT NULL,
    "LoaiKhoa" LoaiKhoa_t NOT NULL,
    PRIMARY KEY("MaKhoa")
);

CREATE TABLE IF NOT EXISTS "NhanVien" ( -- Employees
    "MaNhanVien" bigserial,
    "MaNguoiDung" bigint UNIQUE,
    "HoTen" varchar(100) NOT NULL,
    "SoDienThoai" varchar(20),
    "ChucDanh" varchar(50),
    "MaKhoa" int,
    PRIMARY KEY("MaNhanVien")
);

CREATE TABLE IF NOT EXISTS "DichVu" ( -- Services
    "MaDichVu" serial,
    "TenDichVu" varchar(200) NOT NULL,
    "DonViTinh" varchar(20),
    "GiaCoBan" decimal(15,2) NOT NULL,
    "GiaBHYT" decimal(15,2),
    "MaKhoa" int, -- Khoa thực hiện
    "KichHoat" boolean DEFAULT true,
    PRIMARY KEY("MaDichVu")
);

CREATE TABLE IF NOT EXISTS "ICD10" (
    "MaBenh" varchar(10), -- Code
    "TenBenh" varchar(255) NOT NULL,
    "Chuong" varchar(50), -- Chapter
    PRIMARY KEY("MaBenh")
);

-- =============================================
-- 3. BỆNH NHÂN & TIẾP ĐÓN (PATIENT & RECEPTION)
-- =============================================

CREATE TYPE "GioiTinh_t" AS ENUM ('NAM', 'NU', 'KHAC');

CREATE TABLE IF NOT EXISTS "BenhNhan" ( -- Patients
    "MaBenhNhan" bigserial,
    "HoTen" varchar(100) NOT NULL,
    "NgaySinh" date NOT NULL,
    "GioiTinh" GioiTinh_t NOT NULL,
    "SoDienThoai" varchar(20),
    "CCCD" varchar(20),
    "DiaChi" text,
    "NguoiLienHeKhanCap" varchar(255),
    "DaGopHoSo" boolean DEFAULT false, -- IsMerged
    "MaBenhNhanGoc" bigint NOT NULL, -- ParentPatientID
    "NgayTao" timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY("MaBenhNhan")
);
CREATE INDEX "idx_timkiem_benhnhan" ON "BenhNhan" ("SoDienThoai", "CCCD");

CREATE TABLE IF NOT EXISTS "TheBHYT" ( -- InsuranceCards
    "MaThe" bigserial,
    "MaBenhNhan" bigint NOT NULL,
    "SoTheBHYT" varchar(20) NOT NULL,
    "GiaTriTu" date,
    "GiaTriDen" date,
    "MaNoiKCB" varchar(10), -- RegistrationCode
    PRIMARY KEY("MaThe")
);

-- =============================================
-- 4. KHÁM CHỮA BỆNH (CLINICAL CORE)
-- =============================================

CREATE TYPE "TrangThaiKham_t" AS ENUM ('CHO_KHAM', 'DANG_KHAM', 'CHO_CLS', 'HOAN_THANH', 'HUY');
CREATE TYPE "LoaiKham_t" AS ENUM ('BHYT', 'DICH_VU', 'CAP_CUU');

CREATE TABLE IF NOT EXISTS "LuotKham" ( -- MedicalVisits
    "MaLuotKham" bigserial,
    "MaBenhNhan" bigint NOT NULL,
    "MaLeTan" bigint,
    "MaBacSi" bigint,
    "NgayKham" timestamp DEFAULT CURRENT_TIMESTAMP,
    "TrangThai" TrangThaiKham_t DEFAULT 'CHO_KHAM',
    "LoaiKham" LoaiKham_t NOT NULL,
    
    -- Snapshot BHYT
    "SoTheBHYT_SuDung" varchar(20),
    "MucHuong" decimal(5,2) DEFAULT 0, -- BenefitRate
    "DungTuyen" boolean, -- IsRightRoute
    
    "TrieuChungSoBo" text,
    PRIMARY KEY("MaLuotKham")
);
CREATE INDEX "idx_ngaykham" ON "LuotKham" ("NgayKham");
CREATE INDEX "idx_trangthaikham" ON "LuotKham" ("TrangThai");

CREATE TABLE IF NOT EXISTS "BenhAn" ( -- ClinicalRecords
    "MaBenhAn" bigserial,
    "MaLuotKham" bigint NOT NULL UNIQUE,
    -- Chỉ số sinh tồn
    "Mach" int,
    "NhietDo" decimal(4,1),
    "HuyetAp" varchar(20),
    "NhipTho" int,
    "CanNang" decimal(5,2),
    "ChieuCao" decimal(5,2),
    "BMI" decimal(4,2),
    
    "TienSuBenh" text,
    "KhamThucThe" text,
    "MaICD_Chinh" varchar(10),
    "MaICD_Phu" text,
    "KetLuan" text,
    "HuongDieuTri" text, -- TreatmentPlan
    PRIMARY KEY("MaBenhAn")
);

-- =============================================
-- 5. CẬN LÂM SÀNG (LAB/RIS ORDERS)
-- =============================================

CREATE TYPE "TrangThaiChiDinh_t" AS ENUM ('DA_CHI_DINH', 'DA_THANH_TOAN', 'DANG_THUC_HIEN', 'HOAN_THANH');

CREATE TABLE IF NOT EXISTS "PhieuChiDinh" ( -- ServiceOrders
    "MaPhieu" bigserial,
    "MaLuotKham" bigint NOT NULL,
    "MaBacSi" bigint NOT NULL,
    "NgayChiDinh" timestamp DEFAULT CURRENT_TIMESTAMP,
    "TrangThai" TrangThaiChiDinh_t DEFAULT 'DA_CHI_DINH',
    PRIMARY KEY("MaPhieu")
);

CREATE TABLE IF NOT EXISTS "ChiTietChiDinh" ( -- ServiceOrderDetails
    "MaChiTiet" bigserial,
    "MaPhieu" bigint NOT NULL,
    "MaDichVu" int NOT NULL,
    "SoLuong" int DEFAULT 1,
    "GiaTaiThoiDiem" decimal(15,2) NOT NULL, -- Quan trọng để chốt doanh thu
    "GhiChu" text,
    
    -- Kết quả
    "KetQuaText" text,
    "KetQuaHinhAnhURL" text,
    "MaKyThuatVien" bigint,
    "ThoiGianHoanThanh" timestamp,
    PRIMARY KEY("MaChiTiet")
);

-- =============================================
-- 6. DƯỢC & KHO (PHARMACY & INVENTORY)
-- =============================================

CREATE TABLE IF NOT EXISTS "Thuoc" ( -- Drugs
    "MaThuoc" serial,
    "TenThuoc" varchar(200) NOT NULL,
    "HoatChat" varchar(200),
    "DonViTinh" varchar(20),
    "TonToiThieu" int,
    "GiaBan" decimal(15,2),
    PRIMARY KEY("MaThuoc")
);
CREATE INDEX "idx_tenthuoc" ON "Thuoc" ("TenThuoc");

CREATE TABLE IF NOT EXISTS "LoThuoc" ( -- InventoryBatches
    "MaLo" bigserial,
    "MaThuoc" int NOT NULL,
    "SoLo" varchar(50) NOT NULL, -- BatchNumber
    "HanSuDung" date NOT NULL,
    "SoLuongNhap" int NOT NULL,
    "SoLuongHienTai" int NOT NULL,
    "NgayNhap" timestamp DEFAULT CURRENT_TIMESTAMP,
    "NhaCungCap" varchar(100),
    PRIMARY KEY("MaLo")
);
-- Index hỗ trợ FEFO (Hết hạn trước xuất trước)
CREATE INDEX "idx_kho_fefo" ON "LoThuoc" ("MaThuoc", "HanSuDung", "SoLuongHienTai");

CREATE TYPE "TrangThaiDon_t" AS ENUM ('NHAP', 'DA_CHOT', 'DA_PHAT');

CREATE TABLE IF NOT EXISTS "DonThuoc" ( -- Prescriptions
    "MaDon" bigserial,
    "MaLuotKham" bigint NOT NULL,
    "TrangThai" TrangThaiDon_t DEFAULT 'NHAP',
    PRIMARY KEY("MaDon")
);

CREATE TABLE IF NOT EXISTS "ChiTietDonThuoc" ( -- PrescriptionDetails
    "MaChiTiet" bigserial,
    "MaDon" bigint NOT NULL,
    "MaThuoc" int NOT NULL,
    "SoLuong" int NOT NULL,
    "LieuDung" text, -- Sáng/Chiều/Tối
    "CachDung" text, -- Uống sau ăn...
    PRIMARY KEY("MaChiTiet")
);

CREATE TYPE "LoaiGiaoDich_t" AS ENUM ('NHAP_KHO', 'XUAT_KHO', 'CAN_BANG', 'TRA_HANG');

CREATE TABLE IF NOT EXISTS "TheKho" ( -- StockLedger
    "MaGiaoDich" bigserial,
    "MaLo" bigint NOT NULL,
    "LoaiGiaoDich" LoaiGiaoDich_t NOT NULL,
    "SoLuongThayDoi" int NOT NULL, -- Âm hoặc Dương
    "MaThamChieu" bigint, -- ID DonThuoc hoặc ID PhieuNhap
    "NgayGiaoDich" timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY("MaGiaoDich")
);

-- =============================================
-- 7. TÀI CHÍNH (BILLING)
-- =============================================

CREATE TYPE "PhuongThucTT_t" AS ENUM ('TIEN_MAT', 'CHUYEN_KHOAN', 'THE');
CREATE TYPE "TrangThaiHD_t" AS ENUM ('CHUA_TT', 'DA_TT', 'HOAN_TIEN');

CREATE TABLE IF NOT EXISTS "HoaDon" ( -- Invoices
    "MaHoaDon" bigserial,
    "MaLuotKham" bigint NOT NULL,
    "MaThuNgan" bigint,
    "TongTien" decimal(15,2) NOT NULL,
    "BHYTChiTra" decimal(15,2) DEFAULT 0,
    "BenhNhanTra" decimal(15,2) DEFAULT 0,
    "PhuongThucTT" PhuongThucTT_t DEFAULT 'TIEN_MAT',
    "TrangThai" TrangThaiHD_t DEFAULT 'CHUA_TT',
    "NgayTao" timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY("MaHoaDon")
);
CREATE INDEX "idx_hoadon_luotkham" ON "HoaDon" ("MaLuotKham");

CREATE TYPE "LoaiThamChieu_t" AS ENUM ('DICH_VU', 'THUOC');

CREATE TABLE IF NOT EXISTS "ChiTietHoaDon" ( -- InvoiceDetails
    "MaChiTiet" bigserial,
    "MaHoaDon" bigint NOT NULL,
    "LoaiMuc" LoaiThamChieu_t NOT NULL,
    "MaMuc" bigint NOT NULL, -- ID ChiTietChiDinh hoặc ID ChiTietDonThuoc
    "DonGia" decimal(15,2) NOT NULL,
    "SoLuong" int NOT NULL,
    "ThanhTien" decimal(15,2) NOT NULL,
    PRIMARY KEY("MaChiTiet")
);

-- =============================================
-- KHÓA NGOẠI (FOREIGN KEYS)
-- =============================================

ALTER TABLE "NguoiDung_VaiTro" ADD FOREIGN KEY("MaNguoiDung") REFERENCES "NguoiDung"("MaNguoiDung");
ALTER TABLE "NguoiDung_VaiTro" ADD FOREIGN KEY("MaVaiTro") REFERENCES "VaiTro"("MaVaiTro");

ALTER TABLE "NhanVien" ADD FOREIGN KEY("MaNguoiDung") REFERENCES "NguoiDung"("MaNguoiDung");
ALTER TABLE "NhanVien" ADD FOREIGN KEY("MaKhoa") REFERENCES "KhoaPhong"("MaKhoa");

ALTER TABLE "DichVu" ADD FOREIGN KEY("MaKhoa") REFERENCES "KhoaPhong"("MaKhoa");

ALTER TABLE "TheBHYT" ADD FOREIGN KEY("MaBenhNhan") REFERENCES "BenhNhan"("MaBenhNhan");

ALTER TABLE "LuotKham" ADD FOREIGN KEY("MaBenhNhan") REFERENCES "BenhNhan"("MaBenhNhan");
ALTER TABLE "LuotKham" ADD FOREIGN KEY("MaBacSi") REFERENCES "NhanVien"("MaNhanVien");

ALTER TABLE "BenhAn" ADD FOREIGN KEY("MaLuotKham") REFERENCES "LuotKham"("MaLuotKham");
ALTER TABLE "BenhAn" ADD FOREIGN KEY("MaICD_Chinh") REFERENCES "ICD10"("MaBenh");

ALTER TABLE "PhieuChiDinh" ADD FOREIGN KEY("MaLuotKham") REFERENCES "LuotKham"("MaLuotKham");

ALTER TABLE "ChiTietChiDinh" ADD FOREIGN KEY("MaPhieu") REFERENCES "PhieuChiDinh"("MaPhieu");
ALTER TABLE "ChiTietChiDinh" ADD FOREIGN KEY("MaDichVu") REFERENCES "DichVu"("MaDichVu");

ALTER TABLE "LoThuoc" ADD FOREIGN KEY("MaThuoc") REFERENCES "Thuoc"("MaThuoc");

ALTER TABLE "DonThuoc" ADD FOREIGN KEY("MaLuotKham") REFERENCES "LuotKham"("MaLuotKham");

ALTER TABLE "ChiTietDonThuoc" ADD FOREIGN KEY("MaDon") REFERENCES "DonThuoc"("MaDon");
ALTER TABLE "ChiTietDonThuoc" ADD FOREIGN KEY("MaThuoc") REFERENCES "Thuoc"("MaThuoc");

ALTER TABLE "TheKho" ADD FOREIGN KEY("MaLo") REFERENCES "LoThuoc"("MaLo");

ALTER TABLE "HoaDon" ADD FOREIGN KEY("MaLuotKham") REFERENCES "LuotKham"("MaLuotKham");

ALTER TABLE "ChiTietHoaDon" ADD FOREIGN KEY("MaHoaDon") REFERENCES "HoaDon"("MaHoaDon");