package com.hospital.exception;

/**
 * Exception khi số lượng thuốc tồn kho không đủ để phát.
 * <p>
 * Chứa thông tin medicine, số lượng yêu cầu, và số lượng khả dụng
 * để hiển thị thông báo chi tiết cho user.
 */
public class InsufficientStockException extends BusinessException {

    private final long medicineId;
    private final int requested;
    private final int available;

    public InsufficientStockException(long medicineId, int requested, int available) {
        super(String.format(
                "Không đủ thuốc (ID=%d): yêu cầu %d, tồn kho %d",
                medicineId, requested, available));
        this.medicineId = medicineId;
        this.requested = requested;
        this.available = available;
    }

    public InsufficientStockException(String medicineName, int requested, int available) {
        super(String.format(
                "Không đủ thuốc '%s': yêu cầu %d, tồn kho %d",
                medicineName, requested, available));
        this.medicineId = 0;
        this.requested = requested;
        this.available = available;
    }

    public long getMedicineId() {
        return medicineId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
