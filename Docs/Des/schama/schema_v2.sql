-- =============================================
-- 1. QUẢN TRỊ & DANH MỤC (ADMIN & MASTER DATA)
-- =============================================

-- Bảng người dùng (Tài khoản đăng nhập)
CREATE TABLE NguoiDung (
    MaNguoiDung bigserial PRIMARY KEY,
    TenDangNhap varchar(50) NOT NULL UNIQUE,
    MatKhauHash varchar(255) NOT NULL, -- Mật khẩu đã mã hóa (BCrypt)
    HoTen varchar(100) NOT NULL,
    VaiTro varchar(20) NOT NULL, -- 'ADMIN', 'BAC_SI', 'LE_TAN', 'DUOC_SI', 'THU_NGAN'
    TrangThai boolean DEFAULT true -- true: Hoạt động, false: Khóa
);

-- Danh mục phòng (Để phân luồng và báo cáo)
CREATE TABLE PhongKham (
    MaPhong serial PRIMARY KEY,
    TenPhong varchar(100) NOT NULL, -- VD: "Phòng Nội 1", "Phòng Siêu âm"
    LoaiPhong varchar(50) NOT NULL -- 'KHAM_BENH', 'CAN_LAM_SANG'
);

-- Thông tin nhân viên (Mở rộng từ User)
CREATE TABLE NhanVien (
    MaNhanVien bigserial PRIMARY KEY,
    MaNguoiDung bigint REFERENCES NguoiDung(MaNguoiDung),
    MaPhong int REFERENCES PhongKham(MaPhong), -- Bác sĩ thuộc phòng nào
    ChuyenKhoa varchar(100), -- VD: Nội, Nhi, Tai Mũi Họng
    SoDienThoai varchar(20)
);

-- Danh mục Dịch vụ (Bảng giá niêm yết)
CREATE TABLE DichVu (
    MaDichVu serial PRIMARY KEY,
    TenDichVu varchar(200) NOT NULL,
    GiaDichVu decimal(15,2) NOT NULL, -- Giá bán thực tế
    NhomDichVu varchar(50), -- 'KHAM_BENH', 'XET_NGHIEM', 'X_QUANG', 'SIEU_AM'
    TrangThai boolean DEFAULT true
);

-- Danh mục Thuốc (Kho dược đơn giản)
CREATE TABLE Thuoc (
    MaThuoc serial PRIMARY KEY,
    TenThuoc varchar(200) NOT NULL,
    HoatChat varchar(200),
    DonViTinh varchar(20), -- Viên, Vỉ, Chai
    GiaBan decimal(15,2) NOT NULL,
    SoLuongTon int DEFAULT 0, -- Tồn kho khả dụng
    HanSuDung date -- Cảnh báo nếu thuốc hết hạn
);

-- =============================================
-- 2. TIẾP ĐÓN & LỊCH HẸN (FRONT OFFICE)
-- =============================================

-- Hồ sơ bệnh nhân (Master Data khách hàng)
CREATE TABLE BenhNhan (
    MaBenhNhan bigserial PRIMARY KEY,
    HoTen varchar(100) NOT NULL,
    SoDienThoai varchar(20) NOT NULL, -- Key để tìm kiếm
    NgaySinh date,
    GioiTinh varchar(10), -- 'NAM', 'NU'
    DiaChi text,
    -- Lưu ý: Những thông tin này là cố định, ít thay đổi
    TienSuDiUng text, -- Cảnh báo ĐỎ (VD: Dị ứng Penicillin)
    BenhManTinh text, -- VD: Tiểu đường, Cao huyết áp
    NgayTao timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Lịch hẹn (Booking)
CREATE TABLE LichHen (
    MaLichHen bigserial PRIMARY KEY,
    TenBenhNhan varchar(100), -- Tên khách nhập trên web/app
    SoDienThoai varchar(20) NOT NULL,
    NgayHen date NOT NULL,
    GioHen time NOT NULL,
    LyDoKham text,
    TrangThai varchar(20) DEFAULT 'MOI_TAO', -- 'MOI_TAO', 'DA_DEN', 'HUY'
    MaBenhNhan bigint REFERENCES BenhNhan(MaBenhNhan) -- Nullable (Map khi khách đến)
);

-- =============================================
-- 3. QUY TRÌNH KHÁM (WORKFLOW)
-- =============================================

-- Lượt khám (Visit) - Chỉ quản lý QUY TRÌNH, không chứa bệnh án
CREATE TABLE LuotKham (
    MaLuotKham bigserial PRIMARY KEY,
    MaBenhNhan bigint NOT NULL REFERENCES BenhNhan(MaBenhNhan),
    MaBacSi bigint REFERENCES NhanVien(MaNhanVien), 
    MaPhong int REFERENCES PhongKham(MaPhong),
    
    -- Queueing (Xếp hàng)
    NgayKham date DEFAULT CURRENT_DATE,
    SoThuTu int NOT NULL, -- STT hiển thị trên màn hình (reset mỗi ngày)
    UuTien boolean DEFAULT false, -- True = Đẩy lên đầu hàng
    
    -- State Machine (Trạng thái quy trình)
    TrangThai varchar(20) DEFAULT 'CHO_KHAM', 
    -- 'CHO_KHAM' -> 'DANG_KHAM' -> 'CHO_CLS' -> 'CO_KQ_CLS' -> 'HOAN_THANH' -> 'HUY'
    
    -- Tài chính (Flag để Thu ngân biết đã chốt chưa)
    TrangThaiThanhToan varchar(20) DEFAULT 'CHUA_THANH_TOAN', -- 'CHUA_TT', 'DA_TT'
    ThoiGianTao timestamp DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 4. CHUYÊN MÔN (CLINICAL DATA - BÁC SĨ MỚI THẤY)
-- =============================================

-- Bệnh án điện tử (Tách riêng khỏi LuotKham để bảo mật và chi tiết)
CREATE TABLE BenhAn (
    MaBenhAn bigserial PRIMARY KEY,
    MaLuotKham bigint NOT NULL UNIQUE REFERENCES LuotKham(MaLuotKham), -- 1-1 Relationship
    
    -- 1. Chỉ số sinh tồn (Vital Signs)
    Mach int,          
    NhietDo decimal(4,1),
    HuyetAp varchar(20),  
    CanNang decimal(5,2),
    BMI decimal(4,2),
    
    -- 2. Khám lâm sàng
    LyDoKham text,        -- Triệu chứng cơ năng
    KhamThucThe text,     -- Bác sĩ quan sát/nghe phổi...
    
    -- 3. Chẩn đoán
    ChanDoanChinh text,   -- Kết luận bệnh
    MaICD10 varchar(20),  -- Mã bệnh quốc tế (nếu làm kỹ)
    LoiDanBacSi text,     -- Dặn dò/Hẹn tái khám
    
    ThoiGianCapNhat timestamp DEFAULT CURRENT_TIMESTAMP
);

-- Chỉ định Cận lâm sàng (CLS)
CREATE TABLE ChiDinhDichVu (
    MaChiDinh bigserial PRIMARY KEY,
    MaLuotKham bigint NOT NULL REFERENCES LuotKham(MaLuotKham),
    MaDichVu int NOT NULL REFERENCES DichVu(MaDichVu),
    
    SoLuong int DEFAULT 1,
    DonGia decimal(15,2) NOT NULL, -- Snapshot giá
    GhiChu text, -- Yêu cầu của bác sĩ cho KTV
    
    -- Kết quả (KTV nhập)
    KetQua text, 
    FileKetQua text, -- Link ảnh
    
    TrangThai varchar(20) DEFAULT 'CHUA_THUC_HIEN', -- 'CHUA_THUC_HIEN', 'DA_CO_KET_QUA'
    DaThanhToan boolean DEFAULT false -- Logic: Chưa đóng tiền chưa làm
);

-- Đơn thuốc & Chi tiết
CREATE TABLE DonThuoc (
    MaDon bigserial PRIMARY KEY,
    MaLuotKham bigint NOT NULL REFERENCES LuotKham(MaLuotKham),
    LoiDanChung text,
    NgayKe timestamp DEFAULT CURRENT_TIMESTAMP,
    TrangThai varchar(20) DEFAULT 'MOI_KE' -- 'MOI_KE', 'DA_PHAT_THUOC'
);

CREATE TABLE ChiTietDonThuoc (
    MaChiTiet bigserial PRIMARY KEY,
    MaDon bigint NOT NULL REFERENCES DonThuoc(MaDon),
    MaThuoc int NOT NULL REFERENCES Thuoc(MaThuoc),
    
    SoLuong int NOT NULL,
    CachDung text, -- "Sáng 1, Tối 1"
    DonGia decimal(15,2) NOT NULL, -- Snapshot giá
    ThanhTien decimal(15,2) GENERATED ALWAYS AS (SoLuong * DonGia) STORED
);

-- =============================================
-- 5. TÀI CHÍNH (FINANCE)
-- =============================================

-- Hóa đơn (Phiếu thu tiền)
CREATE TABLE HoaDon (
    MaHoaDon bigserial PRIMARY KEY,
    MaLuotKham bigint NOT NULL REFERENCES LuotKham(MaLuotKham),
    MaThuNgan bigint REFERENCES NhanVien(MaNhanVien),
    
    TongTien decimal(15,2) NOT NULL, -- Tổng phải thu
    GiamGia decimal(15,2) DEFAULT 0,
    ThucThu decimal(15,2) NOT NULL,  -- Khách thực trả
    
    PhuongThucTT varchar(20) DEFAULT 'TIEN_MAT', -- 'TIEN_MAT', 'CHUYEN_KHOAN'
    NgayThanhToan timestamp DEFAULT CURRENT_TIMESTAMP,
    NoiDungThu text
);

-- Chi tiết hóa đơn (Flattened - Phẳng hóa để in ấn và báo cáo nhanh)
CREATE TABLE ChiTietHoaDon (
    MaChiTietHD bigserial PRIMARY KEY,
    MaHoaDon bigint NOT NULL REFERENCES HoaDon(MaHoaDon),
    
    -- Thông tin Snapshot (Lưu cứng)
    TenMuc varchar(255) NOT NULL, -- Tên Thuốc hoặc Dịch vụ tại thời điểm bán
    DonViTinh varchar(20),
    SoLuong int NOT NULL,
    DonGia decimal(15,2) NOT NULL,
    ThanhTien decimal(15,2) NOT NULL,
    
    -- Phân loại (Quan trọng để báo cáo doanh thu: Tiền thuốc riêng, Tiền khám riêng)
    LoaiMuc varchar(20) NOT NULL, -- 'DICH_VU', 'THUOC'
    
    -- Tham chiếu ngược (Optional - Nullable)
    -- Dùng để truy vết: Dòng tiền này đến từ phiếu chỉ định nào?
    Ref_MaChiDinh bigint,    
    Ref_MaChiTietDon bigint 
);