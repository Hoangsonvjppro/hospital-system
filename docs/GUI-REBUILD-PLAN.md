# KẾ HOẠCH XÂY DỰNG LẠI GUI - HỆ THỐNG QUẢN LÝ PHÒNG MẠCH TƯ

## 1. TỔNG QUAN

### Hiện trạng
| Layer | Trạng thái |
|-------|-----------|
| **Model** | ✅ 33 entity classes — Hoàn thiện |
| **DAO** | ✅ 32 DAO classes — Hoàn thiện |
| **BUS** | ✅ 27 BUS classes + 9 Events — Hoàn thiện |
| **GUI** | ⚠️ Chỉ có `LoginFrame` + các stub rỗng. Cần xây dựng lại |

### Nguyên tắc thiết kế
- **Sidebar navigation** (kế thừa từ `BaseFrame`): menu trái 240px + content panel giữa
- **FlatLaf Look & Feel** — giao diện hiện đại, phẳng
- **UIConstants** — tất cả color/font/spacing tập trung 1 file
- **Reusable components** — `RoundedPanel`, `RoundedButton`, `StatCard`, `StatusBadge`
- **Role-based frame** — mỗi vai trò có frame riêng với menu phù hợp
- **Panel tái tạo mỗi lần chuyển** (không cache) — đảm bảo data luôn mới

---

## 2. CẤU TRÚC THƯ MỤC MỚI

```
src/main/java/com/hospital/gui/
│
├── BaseFrame.java              ← Abstract base: sidebar + content layout
├── LoginFrame.java             ← Đăng nhập (đã có)
├── MainFrame.java              ← Router: phân vai trò → mở frame tương ứng
│
├── common/                     ← Components dùng chung
│   ├── RoundedBorder.java
│   ├── RoundedButton.java
│   ├── RoundedPanel.java
│   ├── StatCard.java
│   ├── StatusBadge.java
│   ├── UIConstants.java
│   └── PatientSearchWidget.java     ← Widget tìm kiếm BN dùng chung
│
├── receptionist/               ← LỄ TÂN
│   ├── ReceptionistFrame.java       ← Frame chính lễ tân
│   ├── ReceptionPanel.java          ← ① Tiếp nhận BN (đăng ký/tái khám)
│   ├── QueueDisplayPanel.java       ← ② Bảng hàng đợi (màn hình chờ)
│   ├── PatientPanel.java            ← Quản lý hồ sơ BN
│   ├── AppointmentPanel.java        ← Đặt lịch hẹn
│   └── PaymentPanel.java            ← ⑦ Thanh toán & Thu ngân
│
├── doctor/                     ← BÁC SĨ
│   ├── DoctorFrame.java             ← Frame chính bác sĩ
│   ├── DoctorDashboardPanel.java    ← Dashboard: hàng đợi + thống kê hôm nay
│   ├── ExaminationPanel.java        ← ③ Thăm khám (tích hợp: sinh hiệu + triệu chứng + chẩn đoán)
│   ├── PatientHistoryPanel.java     ← Xem tiền sử bệnh án cũ
│   ├── LabOrderPanel.java           ← ④ Yêu cầu xét nghiệm
│   ├── LabResultViewPanel.java      ← ④ Xem kết quả XN → chẩn đoán xác định
│   ├── PrescriptionPanel.java       ← ⑤ Kê đơn thuốc (kiểm tra tương tác + dị ứng)
│   ├── CompletionPanel.java         ← ⑧ Kết thúc: lưu bệnh án + hẹn tái khám
│   └── DoctorSchedulePanel.java     ← Quản lý lịch làm việc
│
├── pharmacist/                 ← DƯỢC SĨ / KHO DƯỢC
│   ├── PharmacistFrame.java         ← Frame chính dược
│   ├── DispensingPanel.java         ← ⑥ Phát thuốc theo đơn
│   ├── MedicinePanel.java           ← Quản lý danh mục thuốc
│   ├── StockImportPanel.java        ← Nhập hàng theo lô (GoodsReceipt + MedicineBatch)
│   ├── StockHistoryPanel.java       ← Lịch sử nhập/xuất kho (StockTransaction)
│   └── SupplierPanel.java           ← Quản lý nhà cung cấp
│
├── accountant/                 ← KẾ TOÁN
│   ├── AccountantFrame.java         ← Frame chính kế toán
│   ├── InvoiceListPanel.java        ← Danh sách hóa đơn
│   ├── RevenueReportPanel.java      ← Báo cáo doanh thu
│   └── FinanceDashboardPanel.java   ← Dashboard tài chính
│
├── admin/                      ← QUẢN TRỊ
│   ├── AdminFrame.java              ← Frame chính admin
│   ├── AdminDashboardPanel.java     ← Tổng quan hệ thống
│   ├── AccountManagementPanel.java  ← Quản lý tài khoản
│   ├── ClinicConfigPanel.java       ← Cấu hình phòng khám
│   ├── ServiceManagementPanel.java  ← Quản lý dịch vụ & giá
│   └── SystemReportPanel.java       ← Báo cáo toàn hệ thống
│
└── lab/                        ← XÉT NGHIỆM (nếu có nhân viên XN riêng)
    ├── LabFrame.java                ← Frame nhân viên XN
    └── LabProcessingPanel.java      ← Xử lý + nhập kết quả XN
```

---

## 3. QUY TRÌNH CHÍNH & MAPPING VÀO PANEL

### FLOW CHÍNH: Hành trình bệnh nhân

```
┌─────────────────────────────────────────────────────────────────────┐
│                    QUY TRÌNH PHÒNG MẠCH TƯ                         │
│                                                                     │
│  RECEPTIONIST                  DOCTOR                  PHARMACIST   │
│  ┌──────────┐                 ┌──────────┐            ┌──────────┐  │
│  │① Tiếp    │──→Queue──→     │③ Thăm    │            │⑥ Phát    │  │
│  │  nhận    │                 │  khám    │            │  thuốc   │  │
│  │Reception │                 │Examinati │            │Dispensing│  │
│  │Panel     │                 │onPanel   │            │Panel     │  │
│  └──────────┘                 └────┬─────┘            └────▲─────┘  │
│       │                            │                       │         │
│       │                    ┌───────┼───────┐               │         │
│       │                    ▼       ▼       ▼               │         │
│       │              ④LabOrder  ⑤Prescrip  ⑧Complete       │         │
│       │              Panel      tionPanel  ionPanel        │         │
│       │                    │               │               │         │
│       │                    ▼               └───────────────┘         │
│       │              ④LabResult                                      │
│       │              ViewPanel                                       │
│       │                                                              │
│  ┌──────────┐                                                        │
│  │⑦ Thanh   │◄── Tổng hợp: Phí khám + XN + Thuốc                  │
│  │  toán    │                                                        │
│  │Payment   │                                                        │
│  │Panel     │                                                        │
│  └──────────┘                                                        │
└─────────────────────────────────────────────────────────────────────┘
```

### CHI TIẾT TỪNG BƯỚC

#### ① TIẾP NHẬN — `receptionist/ReceptionPanel.java`
**BUS sử dụng:** `PatientBUS`, `QueueBUS`, `PatientAllergyBUS`, `PatientChronicDiseaseBUS`, `ClinicConfigBUS`

| Chức năng | Mô tả | BUS method |
|-----------|--------|------------|
| Tìm BN cũ | Tìm theo SĐT / CCCD / Tên | `PatientBUS.findByPhone()`, `findByCccd()` |
| Đăng ký BN mới | Form: Họ tên, SĐT, CCCD, ngày sinh, giới tính, địa chỉ | `PatientBUS.insert()` |
| Ghi dị ứng | Thêm tiền sử dị ứng | `PatientAllergyBUS.insert()` |
| Phân loại | Khám lần đầu / Tái khám / Cấp cứu | `PatientBUS.update(patientType)` |
| Thêm vào hàng đợi | Gán số thứ tự + ưu tiên | `QueueBUS.addToQueue()` |
| Thu phí khám | Tạo invoice phí khám (hoặc ghi nợ) | `InvoiceBUS.createInvoiceFromMedicalRecord()` |

**Giao diện:**
```
┌─────────────────────────────────────────────────────┐
│  🔍 Tìm kiếm BN: [____SĐT/CCCD/Tên____] [Tìm]    │
├──────────────────────┬──────────────────────────────┤
│  THÔNG TIN BỆNH NHÂN│  HÀNG ĐỢI HÔM NAY          │
│  ┌────────────────┐  │  ┌────────────────────────┐  │
│  │ Họ tên: [____] │  │  │ STT│Tên    │ƯT │Trạng │  │
│  │ SĐT:   [____] │  │  │ 001│Nguyễn │BT │Chờ   │  │
│  │ CCCD:  [____]  │  │  │ 002│Trần   │CC │Khám  │  │
│  │ Ngày sinh:[__] │  │  │ 003│Lê     │BT │Chờ   │  │
│  │ Giới tính:[__] │  │  └────────────────────────┘  │
│  │ Địa chỉ: [___]│  │                              │
│  │ Dị ứng: [____]│  │  Tổng chờ: 5 | Đang khám: 1 │
│  │ Loại: [▼ KLĐ] │  │                              │
│  └────────────────┘  │                              │
│  [Đăng ký & Xếp hàng]│                              │
└──────────────────────┴──────────────────────────────┘
```

---

#### ② HÀNG ĐỢI — `receptionist/QueueDisplayPanel.java`
**BUS sử dụng:** `QueueBUS`

| Chức năng | Mô tả | BUS method |
|-----------|--------|------------|
| Hiển thị hàng đợi | Danh sách BN đang chờ, sắp xếp ưu tiên | `QueueBUS.findAll()` |
| Gọi BN tiếp theo | Chuyển WAITING → IN_PROGRESS | `QueueBUS.callNext()` |
| Hủy lượt | Hủy BN không đến | `QueueBUS.markAsCancelled()` |
| Thống kê | Số chờ / đang khám / hoàn tất | `QueueBUS.findAll()` + count |

**Giao diện:**
```
┌─────────────────────────────────────────────┐
│          📺 BẢNG SỐ THỨ TỰ                  │
│  ┌───────────────────────────────────────┐   │
│  │  ĐANG KHÁM:  #003 - NGUYỄN VĂN A     │   │
│  │              Bác sĩ: Trần B           │   │
│  └───────────────────────────────────────┘   │
│                                              │
│  STT  │ Tên bệnh nhân  │ Ưu tiên │ Trạng   │
│  ─────┼────────────────┼─────────┼──────── │
│  001  │ Lê Văn C       │ 🔴 CC   │ Chờ     │
│  002  │ Phạm Thị D     │ 🟡 NCA  │ Chờ     │
│  004  │ Hoàng E        │ 🟢 BT   │ Chờ     │
│                                              │
│  Chờ: 3  │  Đang khám: 1  │  Hoàn tất: 12  │
└─────────────────────────────────────────────┘
```

---

#### ③ THĂM KHÁM — `doctor/ExaminationPanel.java`
**BUS sử dụng:** `MedicalRecordBUS`, `QueueBUS`, `PatientBUS`, `PatientAllergyBUS`, `PatientChronicDiseaseBUS`, `Icd10CodeBUS`

Panel tích hợp (thay vì tách VitalSignsPanel + SymptomsPanel riêng):

| Chức năng | Mô tả | BUS method |
|-----------|--------|------------|
| Nhận BN từ queue | Xem BN tiếp theo, bắt đầu khám | `QueueBUS.callNext()` |
| Xem tiền sử | Bệnh án cũ, dị ứng, bệnh mãn tính | `MedicalRecordBUS.findByPatient()` |
| Ghi sinh hiệu | Mạch, HA, nhiệt độ, cân nặng, chiều cao, SpO2 | `MedicalRecordBUS.update()` |
| Ghi triệu chứng | Mô tả triệu chứng | `MedicalRecordBUS.update()` |
| Chẩn đoán | ICD-10 code + mô tả | `MedicalRecordBUS.update()`, `Icd10CodeBUS.findAll()` |
| Quyết định | Kê đơn / Yêu cầu XN / Chuyển viện | Navigate to Panel tương ứng |

**Giao diện (3 cột):**
```
┌─────────────────────────────────────────────────────────────────┐
│  BN: #003 Nguyễn Văn A │ 45 tuổi │ Nam │ Tái khám             │
├───────────────┬─────────────────────┬───────────────────────────┤
│ TIỀN SỬ      │ SINH HIỆU & T.CHỨNG│ CHẨN ĐOÁN & QUYẾT ĐỊNH  │
│ ┌───────────┐│ Mạch:    [72] bpm   │ ICD-10: [🔍 J06.9    ]  │
│ │Lần khám cũ││ HA:  [120/80] mmHg  │ Chẩn đoán:              │
│ │2025-12-01 ││ Nhiệt độ:[37.2] °C  │ [Nhiễm trùng đường hô  ]│
│ │J06.9-NKHH ││ Cân nặng:[68] kg    │ [hấp trên cấp          ]│
│ │           ││ C.cao:  [170] cm    │                          │
│ └───────────┘│ SpO2:   [98] %      │ ┌────────────────────┐   │
│ Dị ứng:     │                      │ │[Kê đơn thuốc    ]  │   │
│ ⚠ Penicillin│ Triệu chứng:        │ │[Yêu cầu XN      ]  │   │
│   (SEVERE)  │ [Sốt, ho, đau họng  ]│ │[Kết thúc khám   ]  │   │
│ Bệnh mãn    │ [3 ngày, kèm chảy   ]│ │[Chuyển viện      ]  │   │
│ tính:       │ [mũi               ]│ └────────────────────┘   │
│ • ĐTĐ type 2│                      │                          │
└───────────────┴─────────────────────┴───────────────────────────┘
```

---

#### ④ XÉT NGHIỆM — `doctor/LabOrderPanel.java` + `doctor/LabResultViewPanel.java` + `lab/LabProcessingPanel.java`

**Yêu cầu XN (Bác sĩ) — `LabOrderPanel.java`:**
| BUS | Method |
|-----|--------|
| `ServiceOrderBUS` | `insert()` — tạo yêu cầu dịch vụ XN |
| `LabResultBUS` | `findByRecord()` — xem kết quả |

**Xử lý XN (NV XN) — `LabProcessingPanel.java`:**
| BUS | Method |
|-----|--------|
| `ServiceOrderBUS` | `findByStatus("ORDERED")` → danh sách chờ XN |
| `LabResultBUS` | `insert()` — nhập kết quả |
| `ServiceOrderBUS` | `update(status=COMPLETED)` — đánh dấu hoàn tất |

**Xem kết quả (Bác sĩ) — `LabResultViewPanel.java`:**
| BUS | Method |
|-----|--------|
| `LabResultBUS` | `findByRecord()` — kết quả XN cho BN đang khám |

---

#### ⑤ KÊ ĐƠN THUỐC — `doctor/PrescriptionPanel.java`
**BUS sử dụng:** `PrescriptionBUS`, `MedicineBUS`, `DrugInteractionBUS`, `PatientAllergyBUS`

| Chức năng | Mô tả | BUS method |
|-----------|--------|------------|
| Chọn thuốc | Tra cứu từ danh mục | `MedicineBUS.findAll()` |
| Thêm vào đơn | Liều, SL, cách dùng, thời gian | Build `PrescriptionDetail` |
| Kiểm tra dị ứng | So sánh thành phần thuốc vs dị ứng BN | `PrescriptionBUS.checkAllergies()` |
| Kiểm tra tương tác | Kiểm tra drug interaction | `DrugInteractionBUS` |
| Kiểm tra tồn kho | Thuốc có đủ SL không | `MedicineBUS.findById().stockQty` |
| Lưu đơn | Tạo prescription + details trong transaction | `PrescriptionBUS.createPrescription()` |

**Giao diện:**
```
┌──────────────────────────────────────────────────────────────┐
│  KÊ ĐƠN THUỐC - BN: Nguyễn Văn A │ Chẩn đoán: J06.9      │
├──────────────────────────────────────────────────────────────┤
│  🔍 Tìm thuốc: [_____________] [Thêm]                       │
│                                                              │
│  ⚠️ CẢNH BÁO: BN dị ứng Penicillin (SEVERE)                │
│                                                              │
│  STT│Tên thuốc      │Liều    │SL │Cách dùng     │Thành tiền│
│  ───┼───────────────┼────────┼───┼──────────────┼─────────│
│  1  │Paracetamol 500│500mg   │20 │2v x 3 lần/ng│  30,000 │
│  2  │Amoxicillin 500│500mg   │21 │1v x 3 lần/ng│ 105,000 │
│     │               │        │   │  ⚠ Tương tác│          │
│  3  │Dextromethorphan│15mg   │10 │1v x 2 lần/ng│  25,000 │
│  ───┼───────────────┼────────┼───┼──────────────┼─────────│
│                                         Tổng:    160,000 đ  │
│                                                              │
│  [💾 Lưu đơn (DRAFT)]  [✅ Xác nhận đơn]  [🖨 In đơn]      │
└──────────────────────────────────────────────────────────────┘
```

---

#### ⑥ PHÁT THUỐC — `pharmacist/DispensingPanel.java`
**BUS sử dụng:** `DispensingBUS`, `PrescriptionBUS`, `MedicineBUS`, `MedicineBatchBUS`

| Chức năng | Mô tả | BUS method |
|-----------|--------|------------|
| Danh sách đơn chờ | Đơn CONFIRMED chờ phát | `PrescriptionBUS.getPendingPrescriptions()` |
| Xem chi tiết đơn | Thuốc + SL yêu cầu | `PrescriptionBUS.getPrescriptionDetails()` |
| Kiểm tra tồn kho | Đủ thuốc không | `MedicineBUS.findById()` |
| Chọn lô thuốc | FEFO (hết hạn trước xuất trước) | `MedicineBatchBUS.findAvailableFEFO()` |
| Phát thuốc | Tạo dispensing record | `DispensingBUS.insert()` |
| Tính tiền thuốc | Tổng tiền thuốc | Tự tính từ PrescriptionDetail |

**Giao diện:**
```
┌────────────────────────────────────────────────────────────────┐
│  💊 PHÁT THUỐC                                                │
├─────────────────────┬──────────────────────────────────────────┤
│  ĐƠN THUỐC CHỜ     │  CHI TIẾT ĐƠN: #RX00045                │
│  ┌─────────────────┐│  BN: Nguyễn Văn A │ BS: Trần B          │
│  │ #RX00045 ← chọn ││                                         │
│  │ Nguyễn Văn A    ││  Thuốc       │ YC │Phát│Lô     │Tồn   │
│  │ 14:30 - 3 thuốc ││  ────────────┼────┼────┼───────┼────── │
│  │                  ││  Paracetamol │ 20 │[20]│LO2401 │ 500  │
│  │ #RX00046        ││  Amoxicillin │ 21 │[21]│LO2402 │ 200  │
│  │ Trần Thị C      ││  Dextrometh. │ 10 │[10]│LO2403 │ 150  │
│  │ 14:45 - 2 thuốc ││                                         │
│  └─────────────────┘│  Tổng tiền thuốc: 160,000 đ             │
│                      │  [✅ Xác nhận phát thuốc]               │
└─────────────────────┴──────────────────────────────────────────┘
```

---

#### ⑦ THANH TOÁN — `receptionist/PaymentPanel.java`
**BUS sử dụng:** `InvoiceBUS`, `MedicalRecordBUS`

| Chức năng | Mô tả | BUS method |
|-----------|--------|------------|
| DS chờ thanh toán | Invoice PENDING | `InvoiceBUS.getPendingInvoices()` |
| Xem chi tiết | Phí khám + XN + Thuốc | `InvoiceBUS.getInvoiceDetails()` |
| Thanh toán | Tiền mặt / Chuyển khoản | `InvoiceBUS.markAsPaid()` |
| In hóa đơn | Print invoice | Export logic |

**Giao diện:**
```
┌────────────────────────────────────────────────────────────────┐
│  💰 THANH TOÁN                                                │
├─────────────────────┬──────────────────────────────────────────┤
│  HÓA ĐƠN CHỜ TT    │  CHI TIẾT: #HD00123                     │
│  ┌─────────────────┐│  BN: Nguyễn Văn A                       │
│  │ HD00123  ← chọn  ││  ─────────────────────────────────      │
│  │ Nguyễn Văn A    ││  Phí khám:            150,000 đ         │
│  │ 310,000 đ       ││  ─────────────────────────────────      │
│  │                  ││  Thuốc:                                 │
│  │ HD00124         ││    Paracetamol x20     30,000 đ         │
│  │ Trần Thị C      ││    Amoxicillin x21    105,000 đ         │
│  │ 200,000 đ       ││    Dextrometh.  x10    25,000 đ         │
│  └─────────────────┘│  ─────────────────────────────────      │
│                      │  TỔNG:                310,000 đ         │
│                      │                                         │
│                      │  PT thanh toán: [▼ Tiền mặt       ]    │
│                      │  Tiền nhận:     [___________]           │
│                      │  Tiền thừa:     0 đ                     │
│                      │  [💰 Thanh toán]  [🖨 In hóa đơn]      │
└─────────────────────┴──────────────────────────────────────────┘
```

---

#### ⑧ KẾT THÚC — `doctor/CompletionPanel.java`
**BUS sử dụng:** `MedicalRecordBUS`, `FollowUpBUS`

| Chức năng | Mô tả | BUS method |
|-----------|--------|------------|
| Xem tóm tắt | Bệnh án đầy đủ | `MedicalRecordBUS.findById()` |
| Lưu bệnh án | Đánh dấu hoàn tất | `MedicalRecordBUS.update(status=COMPLETED)` |
| Hẹn tái khám | Chọn ngày + lý do | `FollowUpBUS.createFollowUp()` |
| Chuyển thanh toán | Push sang queue thanh toán | `InvoiceBUS.createInvoiceFromMedicalRecord()` |

---

### FLOW PHỤ: Nhập hàng theo lô

#### NHẬP HÀNG — `pharmacist/StockImportPanel.java`
**BUS sử dụng:** `GoodsReceiptBUS`, `MedicineBatchBUS`, `StockTransactionBUS`, `SupplierBUS`, `MedicineBUS`

| Chức năng | Mô tả | BUS method |
|-----------|--------|------------|
| Chọn NCC | Danh sách nhà cung cấp | `SupplierBUS.findActive()` |
| Tạo phiếu nhập | Tạo GoodsReceipt header | `GoodsReceiptBUS.insert()` |
| Thêm thuốc vào phiếu | Chọn thuốc + SL + giá nhập + số lô + HSD | Build line items |
| Tạo batch | Tạo MedicineBatch cho mỗi dòng | `MedicineBatchBUS.insert()` |
| Cập nhật tồn kho | Tăng stock_qty | `StockTransactionBUS.insert(IMPORT)` |
| Xác nhận phiếu | Hoàn tất | `GoodsReceiptBUS.update()` |

**Giao diện:**
```
┌──────────────────────────────────────────────────────────────────┐
│  📦 NHẬP HÀNG THEO LÔ                                           │
├──────────────────────────────────────────────────────────────────┤
│  NCC: [▼ Công ty Dược phẩm ABC    ]  Ngày nhập: [2026-03-10]   │
│  Mã phiếu: PN00056                                              │
├──────────────────────────────────────────────────────────────────┤
│  🔍 Thêm thuốc: [___________] [+ Thêm]                          │
│                                                                  │
│  STT│Mã thuốc│Tên thuốc      │Số lô  │HSD       │SL  │Giá nhập │
│  ───┼────────┼───────────────┼───────┼──────────┼────┼──────── │
│  1  │MED001  │Paracetamol 500│LO2603 │2027-03-10│500 │  1,200  │
│  2  │MED002  │Amoxicillin 500│LO2604 │2027-06-15│200 │  4,500  │
│  3  │MED005  │Dextromethorphan│LO2605│2027-12-01│150 │  2,200  │
│  ───┼────────┼───────────────┼───────┼──────────┼────┼──────── │
│                                          Tổng tiền: 2,310,000 đ │
│                                                                  │
│  [💾 Lưu nháp]  [✅ Xác nhận nhập kho]  [❌ Hủy]                │
└──────────────────────────────────────────────────────────────────┘
```

#### QUẢN LÝ NHÀ CUNG CẤP — `pharmacist/SupplierPanel.java`
**BUS:** `SupplierBUS` — CRUD nhà cung cấp

#### LỊCH SỬ KHO — `pharmacist/StockHistoryPanel.java`
**BUS:** `StockTransactionBUS` — Xem lịch sử nhập/xuất/điều chỉnh kho

---

## 4. PHÂN QUYỀN & MENU THEO VAI TRÒ

### 🏥 Lễ tân (RECEPTIONIST) — `ReceptionistFrame`
| # | Icon | Menu | Panel | Bước QT |
|---|------|------|-------|---------|
| 1 | 📋 | Tiếp nhận BN | `ReceptionPanel` | ① |
| 2 | 🕐 | Hàng đợi | `QueueDisplayPanel` | ② |
| 3 | 👤 | Hồ sơ bệnh nhân | `PatientPanel` | — |
| 4 | 📅 | Lịch hẹn | `AppointmentPanel` | — |
| — | | ── separator ── | | |
| 5 | 💰 | Thanh toán | `PaymentPanel` | ⑦ |

### 🩺 Bác sĩ (DOCTOR) — `DoctorFrame`
| # | Icon | Menu | Panel | Bước QT |
|---|------|------|-------|---------|
| 1 | 📊 | Dashboard | `DoctorDashboardPanel` | — |
| 2 | 🩺 | Khám bệnh | `ExaminationPanel` | ③ |
| 3 | 📜 | Tiền sử bệnh nhân | `PatientHistoryPanel` | ③ |
| — | | ── Xét nghiệm ── | | |
| 4 | 🧪 | Yêu cầu XN | `LabOrderPanel` | ④ |
| 5 | 📊 | Kết quả XN | `LabResultViewPanel` | ④ |
| — | | ── Điều trị ── | | |
| 6 | 💊 | Kê đơn thuốc | `PrescriptionPanel` | ⑤ |
| 7 | ✅ | Kết thúc khám | `CompletionPanel` | ⑧ |
| 8 | 📅 | Lịch làm việc | `DoctorSchedulePanel` | — |

### 💊 Dược sĩ (PHARMACIST) — `PharmacistFrame`
| # | Icon | Menu | Panel | Bước QT |
|---|------|------|-------|---------|
| 1 | 💉 | Phát thuốc | `DispensingPanel` | ⑥ |
| 2 | 💊 | Kho thuốc | `MedicinePanel` | — |
| — | | ── Nhập kho ── | | |
| 3 | 📦 | Nhập hàng theo lô | `StockImportPanel` | Nhập hàng |
| 4 | 📋 | Lịch sử kho | `StockHistoryPanel` | — |
| 5 | 🏭 | Nhà cung cấp | `SupplierPanel` | — |

### 💰 Kế toán (ACCOUNTANT) — `AccountantFrame`
| # | Icon | Menu | Panel |
|---|------|------|-------|
| 1 | 💰 | Thanh toán | `PaymentPanel` |
| 2 | 📄 | Hóa đơn | `InvoiceListPanel` |
| 3 | 📊 | Dashboard tài chính | `FinanceDashboardPanel` |
| 4 | 📈 | Báo cáo doanh thu | `RevenueReportPanel` |

### 🛡️ Admin (ADMIN) — `AdminFrame`
| # | Icon | Menu | Panel |
|---|------|------|-------|
| 1 | 📊 | Tổng quan | `AdminDashboardPanel` |
| — | | ── Quy trình khám ── | |
| 2 | 📋 | Tiếp nhận | `ReceptionPanel` |
| 3 | 🩺 | Khám bệnh | `ExaminationPanel` |
| 4 | 💊 | Phát thuốc | `DispensingPanel` |
| 5 | 💰 | Thanh toán | `PaymentPanel` |
| — | | ── Quản lý ── | |
| 6 | 👤 | Tài khoản | `AccountManagementPanel` |
| 7 | 🏥 | Dịch vụ & Giá | `ServiceManagementPanel` |
| 8 | ⚙️ | Cấu hình | `ClinicConfigPanel` |
| 9 | 📈 | Báo cáo | `SystemReportPanel` |

### 🔬 Nhân viên XN (nếu có) — `LabFrame`
| # | Icon | Menu | Panel |
|---|------|------|-------|
| 1 | 🔬 | Xử lý xét nghiệm | `LabProcessingPanel` |

---

## 5. BUS ↔ GUI MAPPING TỔNG HỢP

Đảm bảo mọi BUS class đều có GUI tương ứng:

| BUS Class | GUI Panel(s) | Ghi chú |
|-----------|-------------|---------|
| `AccountBUS` | `admin/AccountManagementPanel` | |
| `AppointmentBUS` | `receptionist/AppointmentPanel` | |
| `ClinicConfigBUS` | `admin/ClinicConfigPanel` | |
| `DispensingBUS` | `pharmacist/DispensingPanel` | |
| `DoctorBUS` | `admin/AccountManagementPanel` | Quản lý trong tài khoản |
| `DrugInteractionBUS` | `doctor/PrescriptionPanel` | Kiểm tra khi kê đơn |
| `FollowUpBUS` | `doctor/CompletionPanel` | Hẹn tái khám |
| `GoodsReceiptBUS` | `pharmacist/StockImportPanel` | **MỚI** |
| `Icd10CodeBUS` | `doctor/ExaminationPanel` | Autocomplete ICD-10 |
| `InvoiceBUS` | `receptionist/PaymentPanel`, `accountant/InvoiceListPanel` | |
| `LabResultBUS` | `doctor/LabResultViewPanel`, `lab/LabProcessingPanel` | |
| `MedicalAttachmentBUS` | `doctor/ExaminationPanel` | Đính kèm file trong khám |
| `MedicalRecordBUS` | `doctor/ExaminationPanel`, `doctor/CompletionPanel` | |
| `MedicineBatchBUS` | `pharmacist/StockImportPanel`, `pharmacist/DispensingPanel` | **MỚI** |
| `MedicineBUS` | `pharmacist/MedicinePanel` | |
| `MedicineIngredientBUS` | `pharmacist/MedicinePanel` | Tab thành phần thuốc |
| `PatientAllergyBUS` | `receptionist/ReceptionPanel`, `doctor/ExaminationPanel` | |
| `PatientBUS` | `receptionist/ReceptionPanel`, `receptionist/PatientPanel` | |
| `PatientChronicDiseaseBUS` | `receptionist/ReceptionPanel`, `doctor/ExaminationPanel` | |
| `PrescriptionBUS` | `doctor/PrescriptionPanel`, `pharmacist/DispensingPanel` | |
| `QueueBUS` | `receptionist/QueueDisplayPanel`, `doctor/DoctorDashboardPanel` | |
| `ScheduleBUS` | `doctor/DoctorSchedulePanel` | |
| `ServiceBUS` | `admin/ServiceManagementPanel` | **MỚI** |
| `ServiceOrderBUS` | `doctor/LabOrderPanel`, `lab/LabProcessingPanel` | |
| `SpecialtyBUS` | `admin/AccountManagementPanel` | Chuyên khoa bác sĩ |
| `StockTransactionBUS` | `pharmacist/StockHistoryPanel` | **MỚI** |
| `SupplierBUS` | `pharmacist/SupplierPanel` | **MỚI** |

---

## 6. THỨ TỰ TRIỂN KHAI (PHASE)

### Phase 0: Nền tảng (Foundation)
| # | Task | File(s) |
|---|------|---------|
| 0.1 | Hoàn thiện `BaseFrame` | `gui/BaseFrame.java` |
| 0.2 | Tạo `UIConstants` + components | `gui/common/*` |
| 0.3 | Tạo `MainFrame` (router) | `gui/MainFrame.java` |
| 0.4 | Cập nhật `LoginFrame` (nếu cần) | `gui/LoginFrame.java` |

### Phase 1: Lõi quy trình khám (Core Flow) — ① → ② → ③ → ⑤
| # | Task | File(s) | BUS |
|---|------|---------|-----|
| 1.1 | `ReceptionistFrame` + menu | `receptionist/ReceptionistFrame.java` | — |
| 1.2 | `ReceptionPanel` — Tiếp nhận | `receptionist/ReceptionPanel.java` | PatientBUS, QueueBUS, PatientAllergyBUS |
| 1.3 | `QueueDisplayPanel` — Hàng đợi | `receptionist/QueueDisplayPanel.java` | QueueBUS |
| 1.4 | `DoctorFrame` + menu | `doctor/DoctorFrame.java` | — |
| 1.5 | `DoctorDashboardPanel` | `doctor/DoctorDashboardPanel.java` | QueueBUS |
| 1.6 | `ExaminationPanel` — Khám | `doctor/ExaminationPanel.java` | MedicalRecordBUS, PatientBUS, Icd10CodeBUS |
| 1.7 | `PrescriptionPanel` — Kê đơn | `doctor/PrescriptionPanel.java` | PrescriptionBUS, MedicineBUS, DrugInteractionBUS |

### Phase 2: Dược & Thanh toán — ⑥ → ⑦ → ⑧
| # | Task | File(s) | BUS |
|---|------|---------|-----|
| 2.1 | `PharmacistFrame` + menu | `pharmacist/PharmacistFrame.java` | — |
| 2.2 | `DispensingPanel` — Phát thuốc | `pharmacist/DispensingPanel.java` | DispensingBUS, PrescriptionBUS |
| 2.3 | `PaymentPanel` — Thanh toán | `receptionist/PaymentPanel.java` | InvoiceBUS |
| 2.4 | `CompletionPanel` — Kết thúc | `doctor/CompletionPanel.java` | MedicalRecordBUS, FollowUpBUS |

### Phase 3: Xét nghiệm — ④
| # | Task | File(s) | BUS |
|---|------|---------|-----|
| 3.1 | `LabOrderPanel` — Yêu cầu XN | `doctor/LabOrderPanel.java` | ServiceOrderBUS |
| 3.2 | `LabProcessingPanel` — Xử lý | `lab/LabProcessingPanel.java` | ServiceOrderBUS, LabResultBUS |
| 3.3 | `LabResultViewPanel` — Kết quả | `doctor/LabResultViewPanel.java` | LabResultBUS |
| 3.4 | `LabFrame` (nếu cần) | `lab/LabFrame.java` | — |

### Phase 4: Nhập kho & Quản lý dược
| # | Task | File(s) | BUS |
|---|------|---------|-----|
| 4.1 | `MedicinePanel` — Kho thuốc | `pharmacist/MedicinePanel.java` | MedicineBUS, MedicineIngredientBUS |
| 4.2 | `StockImportPanel` — Nhập lô | `pharmacist/StockImportPanel.java` | GoodsReceiptBUS, MedicineBatchBUS, StockTransactionBUS |
| 4.3 | `StockHistoryPanel` — Lịch sử | `pharmacist/StockHistoryPanel.java` | StockTransactionBUS |
| 4.4 | `SupplierPanel` — NCC | `pharmacist/SupplierPanel.java` | SupplierBUS |

### Phase 5: Kế toán & Báo cáo
| # | Task | File(s) | BUS |
|---|------|---------|-----|
| 5.1 | `AccountantFrame` + menu | `accountant/AccountantFrame.java` | — |
| 5.2 | `InvoiceListPanel` | `accountant/InvoiceListPanel.java` | InvoiceBUS |
| 5.3 | `FinanceDashboardPanel` | `accountant/FinanceDashboardPanel.java` | DashboardDAO, ReportDAO |
| 5.4 | `RevenueReportPanel` | `accountant/RevenueReportPanel.java` | ReportDAO |

### Phase 6: Admin & Hệ thống
| # | Task | File(s) | BUS |
|---|------|---------|-----|
| 6.1 | `AdminFrame` + menu | `admin/AdminFrame.java` | — |
| 6.2 | `AdminDashboardPanel` | `admin/AdminDashboardPanel.java` | DashboardDAO |
| 6.3 | `AccountManagementPanel` | `admin/AccountManagementPanel.java` | AccountBUS, DoctorBUS, SpecialtyBUS |
| 6.4 | `ClinicConfigPanel` | `admin/ClinicConfigPanel.java` | ClinicConfigBUS |
| 6.5 | `ServiceManagementPanel` | `admin/ServiceManagementPanel.java` | ServiceBUS |
| 6.6 | `SystemReportPanel` | `admin/SystemReportPanel.java` | ReportDAO |

### Phase 7: Bổ sung
| # | Task | File(s) |
|---|------|---------|
| 7.1 | `PatientPanel` — QL hồ sơ BN | `receptionist/PatientPanel.java` |
| 7.2 | `PatientHistoryPanel` — Tiền sử | `doctor/PatientHistoryPanel.java` |
| 7.3 | `AppointmentPanel` — Lịch hẹn | `receptionist/AppointmentPanel.java` |
| 7.4 | `DoctorSchedulePanel` — Lịch BS | `doctor/DoctorSchedulePanel.java` |
| 7.5 | `PatientSearchWidget` — Widget chung | `common/PatientSearchWidget.java` |

---

## 7. TỔNG KẾT SỐ FILE CẦN TẠO

| Thư mục | Số file | Loại |
|---------|---------|------|
| `gui/` (root) | 3 | BaseFrame, LoginFrame (sẵn), MainFrame |
| `gui/common/` | 7 | UIConstants + 5 components + PatientSearchWidget |
| `gui/receptionist/` | 6 | Frame + 5 panels |
| `gui/doctor/` | 9 | Frame + 8 panels |
| `gui/pharmacist/` | 6 | Frame + 5 panels |
| `gui/accountant/` | 4 | Frame + 3 panels |
| `gui/admin/` | 6 | Frame + 5 panels |
| `gui/lab/` | 2 | Frame + 1 panel |
| **TỔNG** | **~43** | (~38 mới + ~5 sẵn có) |

---

## 8. GHI CHÚ KỸ THUẬT

### Event-Driven Updates
Sử dụng `EventBus` để cập nhật real-time giữa các panel:
- `PatientRegisteredEvent` → Cập nhật QueueDisplayPanel
- `QueueUpdatedEvent` → Cập nhật DoctorDashboardPanel
- `ExaminationStartedEvent` → Cập nhật QueueDisplayPanel (đang khám)
- `PrescriptionCreatedEvent` → Cập nhật DispensingPanel (đơn mới)
- `DispensingCompletedEvent` → Cập nhật PaymentPanel (chờ thanh toán)
- `PaymentCompletedEvent` → Cập nhật CompletionPanel

### Model thiếu cần bổ sung
- `GoodsReceiptDetail` — Chi tiết phiếu nhập (thuốc, SL, giá nhập/dòng). Cần tạo Model + DAO + BUS trước khi làm `StockImportPanel`.

### Validation UI
- Sử dụng `BusinessException` từ BUS → hiển thị lỗi inline trên form
- Real-time validation (phone 10 digits, CCCD 12 digits, etc.)
- Confirm dialog trước các action quan trọng (xóa, hủy, thanh toán)
