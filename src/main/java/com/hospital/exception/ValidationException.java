package com.hospital.exception;

/**
 * Exception cho lỗi validation dữ liệu đầu vào.
 * <p>
 * Ném bởi {@link com.hospital.util.Validator} hoặc các BUS class
 * khi dữ liệu không hợp lệ.
 */
public class ValidationException extends BusinessException {

    private final String fieldName;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    /**
     * Tên trường bị lỗi (nullable).
     */
    public String getFieldName() {
        return fieldName;
    }
}
