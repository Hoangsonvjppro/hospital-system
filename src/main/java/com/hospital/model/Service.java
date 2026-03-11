package com.hospital.model;

import java.math.BigDecimal;

public class Service extends BaseModel {
    private String  serviceName;
    private BigDecimal price;
    private String  description;
    private boolean isActive;

    public Service() {
        this.isActive = true;
        this.price = BigDecimal.ZERO;
    }

    public String getServiceName()                   { return serviceName; }
    public void setServiceName(String v)             { this.serviceName = v; }

    public BigDecimal getPrice()                     { return price; }
    public void setPrice(BigDecimal v)               { this.price = v; }

    public String getDescription()                   { return description; }
    public void setDescription(String v)             { this.description = v; }

    public boolean isActive()                        { return isActive; }
    public void setActive(boolean v)                 { this.isActive = v; }

    @Override
    public String toString() {
        return "Service{id=" + id
                + ", serviceName='" + serviceName + "'"
                + ", price=" + price
                + ", isActive=" + isActive + "}";
    }
}
