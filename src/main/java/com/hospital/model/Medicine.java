package com.hospital.model;

/**
 * Model thuốc / dược phẩm.
 */
public class Medicine extends BaseModel {
    private String medicineCode;
    private String name;
    private String unit;          // Đơn vị: viên, chai, hộp...
    private double price;         // Đơn giá
    private int    quantity;      // Số lượng tồn kho
    private int    minQuantity;   // Ngưỡng cảnh báo sắp hết
    private String category;      // Nhóm thuốc
    private String manufacturer;  // Nhà sản xuất
    private String expiryDate;    // Hạn sử dụng (dạng string dd/MM/yyyy)

    public Medicine() {}

    public Medicine(int id, String medicineCode, String name, String unit,
                    double price, int quantity, int minQuantity,
                    String category, String manufacturer, String expiryDate) {
        super(id);
        this.medicineCode = medicineCode;
        this.name         = name;
        this.unit         = unit;
        this.price        = price;
        this.quantity     = quantity;
        this.minQuantity  = minQuantity;
        this.category     = category;
        this.manufacturer = manufacturer;
        this.expiryDate   = expiryDate;
    }

    public boolean isLowStock() { return quantity <= minQuantity; }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String getMedicineCode()  { return medicineCode; }
    public void setMedicineCode(String v) { this.medicineCode = v; }

    public String getName()          { return name; }
    public void setName(String v)    { this.name = v; }

    public String getUnit()          { return unit; }
    public void setUnit(String v)    { this.unit = v; }

    public double getPrice()         { return price; }
    public void setPrice(double v)   { this.price = v; }

    public int getQuantity()         { return quantity; }
    public void setQuantity(int v)   { this.quantity = v; }

    public int getMinQuantity()      { return minQuantity; }
    public void setMinQuantity(int v){ this.minQuantity = v; }

    public String getCategory()      { return category; }
    public void setCategory(String v){ this.category = v; }

    public String getManufacturer()  { return manufacturer; }
    public void setManufacturer(String v) { this.manufacturer = v; }

    public String getExpiryDate()    { return expiryDate; }
    public void setExpiryDate(String v) { this.expiryDate = v; }

    @Override
    public String toString() { return medicineCode + " - " + name; }
}
