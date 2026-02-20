# ERD — Hệ thống Quản lý Phòng mạch tư (v4.0 — 20 bảng + 1 VIEW)

## Sơ đồ tổng quan

```mermaid
erDiagram
    Role ||--o{ User : "has many"
    User ||--o| Doctor : "is a"
    User |o--o| Patient : "optionally linked"
    
    Patient ||--o{ PatientAllergy : "has allergies"
    
    Doctor ||--o{ Schedule : "works"
    Doctor ||--o{ Appointment : "attends"
    Doctor ||--o{ MedicalRecord : "creates"
    
    Patient ||--o{ Appointment : "books"
    Patient ||--o{ MedicalRecord : "has"
    Patient ||--o{ Invoice : "pays"
    
    Appointment |o--|| MedicalRecord : "1-to-1"
    
    MedicalRecord ||--o{ ServiceOrder : "orders"
    MedicalRecord ||--o{ Prescription : "N prescriptions"
    MedicalRecord ||--o{ LabResult : "has results"
    MedicalRecord |o--o{ Invoice : "billed in"
    
    ServiceOrder }o--|| Service : "references"
    ServiceOrder |o--o| LabResult : "produces"
    
    Prescription ||--o{ PrescriptionDetail : "includes"
    PrescriptionDetail }o--|| Medicine : "references"
    PrescriptionDetail |o--o{ InvoiceMedicineDetail : "traced by"
    
    Medicine ||--o{ MedicineIngredient : "has ingredients"
    Medicine ||--o{ StockTransaction : "tracked by"
    
    Invoice ||--o{ InvoiceServiceDetail : "service lines"
    Invoice ||--o{ InvoiceMedicineDetail : "medicine lines"
    InvoiceServiceDetail }o--|| ServiceOrder : "FK hard"
    InvoiceMedicineDetail }o--|| Medicine : "FK hard"
```

## Luồng nghiệp vụ

```mermaid
flowchart LR
    A["Đặt lịch"] --> B["Đo sinh hiệu"]
    B --> C["Khám bệnh"]
    C --> D["Chỉ định CLS"]
    D --> E["Kết quả XN"]
    C --> F["Kê đơn thuốc"]
    F --> G["Cấp phát + StockTransaction"]
    C --> H["Tạo hóa đơn"]
    D --> H
    F -.->|prescription_detail_id| H
    H --> I["InvoiceSummary VIEW"]
```

## Cảnh báo dị ứng khi kê đơn

```sql
-- Query: kiểm tra BN có dị ứng với thuốc sắp kê không
SELECT pa.allergen_name, pa.severity, pa.reaction
FROM PatientAllergy pa
JOIN MedicineIngredient mi ON LOWER(pa.allergen_name) = LOWER(mi.ingredient_name)
WHERE pa.patient_id = :patient_id AND mi.medicine_id = :medicine_id;
```

## Báo cáo doanh thu (dùng InvoiceSummary VIEW)

```sql
SELECT MONTH(invoice_date) AS thang,
       SUM(service_total)  AS doanh_thu_dv,
       SUM(medicine_total) AS doanh_thu_thuoc,
       SUM(medicine_profit) AS loi_nhuan_thuoc,
       SUM(total_amount)   AS tong_doanh_thu
FROM InvoiceSummary
WHERE status = 'PAID'
GROUP BY MONTH(invoice_date);
```
