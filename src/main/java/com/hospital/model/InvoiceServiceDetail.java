package com.hospital.model;


public class InvoiceServiceDetail extends BaseModel {

    private long invoiceId;
    private long serviceOrderId;
    private String serviceName;
    private int quantity;
    private double unitPrice;
    private double lineTotal;     

   

    public InvoiceServiceDetail() {
        this.quantity = 1;
    }

    public InvoiceServiceDetail(long invoiceId, long serviceOrderId,
                                String serviceName, int quantity, double unitPrice) {
        this.invoiceId = invoiceId;
        this.serviceOrderId = serviceOrderId;
        this.serviceName = serviceName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = quantity * unitPrice;
    }


    public long getInvoiceId()                   { return invoiceId; }
    public void setInvoiceId(long v)             { this.invoiceId = v; }

    public long getServiceOrderId()              { return serviceOrderId; }
    public void setServiceOrderId(long v)        { this.serviceOrderId = v; }

    public String getServiceName()               { return serviceName; }
    public void setServiceName(String v)         { this.serviceName = v; }

    public int getQuantity()                     { return quantity; }
    public void setQuantity(int v)               { this.quantity = v; }

    public double getUnitPrice()                 { return unitPrice; }
    public void setUnitPrice(double v)           { this.unitPrice = v; }

    public double getLineTotal() {
        return (lineTotal > 0) ? lineTotal : (double) quantity * unitPrice;
    }

    public void setLineTotal(double v) { this.lineTotal = v; }


    @Override
    public String toString() {
        return "InvoiceServiceDetail{" +
                "id=" + id +
                ", invoiceId=" + invoiceId +
                ", serviceName='" + serviceName + '\'' +
                ", qty=" + quantity +
                ", unitPrice=" + unitPrice +
                ", lineTotal=" + getLineTotal() +
                '}';
    }
}
