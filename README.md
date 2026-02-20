# Hospital Management System — Java Swing (3-Layer Architecture)

## Cấu trúc thư mục / Folder Structure

```
jv-hs/
├── src/main/java/com/hospital/
│   ├── Main.java                  # Điểm khởi đầu ứng dụng
│   ├── config/
│   │   └── DatabaseConfig.java    # Kết nối CSDL (Singleton)
│   ├── model/                     # Lớp 1: Data Layer (Entity/Model)
│   │   ├── BaseModel.java         #   └─ Entity cơ sở
│   │   └── SampleEntity.java      #   └─ Entity mẫu
│   ├── dao/                       # Lớp 2: Data Access Layer (DAO)
│   │   ├── BaseDAO.java           #   └─ Interface CRUD chung
│   │   └── SampleDAO.java         #   └─ DAO mẫu (JDBC)
│   ├── bus/                       # Lớp 3a: Business Logic Layer (BUS)
│   │   ├── BaseBUS.java           #   └─ BUS cơ sở + validate
│   │   └── SampleBUS.java         #   └─ BUS mẫu
│   ├── gui/                       # Lớp 3b: Presentation Layer (GUI)
│   │   ├── MainFrame.java         #   └─ JFrame chính
│   │   ├── panels/                #   └─ Các JPanel nội dung
│   │   │   └── SamplePanel.java
│   │   ├── dialogs/               #   └─ Các JDialog
│   │   └── components/            #   └─ Component tái sử dụng
│   └── util/                      # Tiện ích chung
│       └── AppUtils.java
├── src/main/resources/
│   ├── images/                    # Hình ảnh, icon
│   └── sql/                       # Script SQL
├── lib/                           # Thư viện JAR bên ngoài
├── docs/                          # Tài liệu dự án
└── README.md
```

## Kiến trúc 3 lớp / 3-Layer Architecture

```
┌─────────────────────────────────┐
│   GUI (Presentation Layer)      │  ← Giao diện người dùng (Swing)
│   gui/ panels/ dialogs/         │
└──────────────┬──────────────────┘
               │ gọi (calls)
┌──────────────▼──────────────────┐
│   BUS (Business Logic Layer)    │  ← Xử lý nghiệp vụ + validate
│   bus/                          │
└──────────────┬──────────────────┘
               │ gọi (calls)
┌──────────────▼──────────────────┐
│   DAO (Data Access Layer)       │  ← Truy vấn CSDL (JDBC)
│   dao/                          │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│   Database (MySQL)              │
└─────────────────────────────────┘
```

## Cách sử dụng

1. Cấu hình kết nối DB trong `config/DatabaseConfig.java`
2. Tạo entity mới trong `model/`
3. Tạo DAO tương ứng trong `dao/`
4. Tạo BUS tương ứng trong `bus/`
5. Tạo Panel/Dialog trong `gui/panels/` hoặc `gui/dialogs/`
6. Đăng ký menu trong `MainFrame.java`
