package com.hospital.model;

public class InvoiceMedicineDetail extends BaseModel {

    private long invoiceId;
    private long medicineId;
    private Long prescriptionDetailId;  
    private String medicineName;        
    private int quantity;
    private double unitPrice;          
    private double costPrice;       
    private double lineTotal;        
    private double profitTotal;     


    private String unit;             


    public InvoiceMedicineDetail() {
        this.quantity = 1;
    }

    public InvoiceMedicineDetail(long invoiceId, long medicineId,
                                 String medicineName, int quantity,
                                 double unitPrice, double costPrice) {
        this.invoiceId = invoiceId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.costPrice = costPrice;
        this.lineTotal = quantity * unitPrice;
        this.profitTotal = quantity * (unitPrice - costPrice);
    }


    public long getInvoiceId()                        { return invoiceId; }
    public void setInvoiceId(long v)                  { this.invoiceId = v; }

    public long getMedicineId()                       { return medicineId; }
    public void setMedicineId(long v)                 { this.medicineId = v; }

    public Long getPrescriptionDetailId()             { return prescriptionDetailId; }
    public void setPrescriptionDetailId(Long v)       { this.prescriptionDetailId = v; }

    public String getMedicineName()                   { return medicineName; }
    public void setMedicineName(String v)             { this.medicineName = v; }

    public int getQuantity()                          { return quantity; }
    public void setQuantity(int v)                    { this.quantity = v; }

    public double getUnitPrice()                      { return unitPrice; }
    public void setUnitPrice(double v)                { this.unitPrice = v; }

    public double getCostPrice()                      { return costPrice; }
    public void setCostPrice(double v)                { this.costPrice = v; }

    public double getLineTotal() {
        return (lineTotal > 0) ? lineTotal : (double) quantity * unitPrice;
    }
    public void setLineTotal(double v) { this.lineTotal = v; }


    public double getProfitTotal() {
        return (profitTotal > 0) ? profitTotal : (double) quantity * (unitPrice - costPrice);
    }
    public void setProfitTotal(double v) { this.profitTotal = v; }

    public String getUnit()                           { return unit; }
    public void setUnit(String v)                     { this.unit = v; }


    @Override
    public String toString() {
        return "InvoiceMedicineDetail{" +
                "id=" + id +
                ", invoiceId=" + invoiceId +
                ", medicineName='" + medicineName + '\'' +
                ", qty=" + quantity +
                ", unitPrice=" + unitPrice +
                ", costPrice=" + costPrice +
                ", lineTotal=" + getLineTotal() +
                '}';
    }
}
