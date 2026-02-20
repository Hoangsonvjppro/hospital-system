package com.hospital.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Lớp cơ sở cho tất cả các entity/model.
 * Base class for all entity/model classes.
 */
public abstract class BaseModel implements Serializable {
    protected int id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public BaseModel() {
    }

    public BaseModel(int id) {
        this.id = id;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public abstract String toString();
}
