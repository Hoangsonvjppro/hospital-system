package com.hospital.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho custom exception hierarchy.
 */
class ExceptionHierarchyTest {

    @Test
    @DisplayName("ValidationException extends BusinessException")
    void validationExceptionHierarchy() {
        ValidationException ex = new ValidationException("phone", "Số điện thoại không hợp lệ");
        assertInstanceOf(BusinessException.class, ex);
        assertInstanceOf(RuntimeException.class, ex);
        assertEquals("phone", ex.getFieldName());
        assertEquals("Số điện thoại không hợp lệ", ex.getMessage());
    }

    @Test
    @DisplayName("ValidationException without fieldName")
    void validationExceptionNoField() {
        ValidationException ex = new ValidationException("Dữ liệu không hợp lệ");
        assertNull(ex.getFieldName());
        assertEquals("Dữ liệu không hợp lệ", ex.getMessage());
    }

    @Test
    @DisplayName("InsufficientStockException extends BusinessException")
    void insufficientStockHierarchy() {
        InsufficientStockException ex = new InsufficientStockException(5L, 30, 10);
        assertInstanceOf(BusinessException.class, ex);
        assertEquals(5L, ex.getMedicineId());
        assertEquals(30, ex.getRequested());
        assertEquals(10, ex.getAvailable());
        assertTrue(ex.getMessage().contains("30"));
        assertTrue(ex.getMessage().contains("10"));
    }

    @Test
    @DisplayName("InsufficientStockException with medicine name")
    void insufficientStockWithName() {
        InsufficientStockException ex = new InsufficientStockException("Paracetamol 500mg", 50, 20);
        assertTrue(ex.getMessage().contains("Paracetamol 500mg"));
        assertEquals(50, ex.getRequested());
        assertEquals(20, ex.getAvailable());
    }

    @Test
    @DisplayName("DataAccessException wraps cause")
    void dataAccessExceptionCause() {
        Exception cause = new Exception("DB connection failed");
        DataAccessException ex = new DataAccessException("Lỗi truy vấn", cause);
        assertEquals("Lỗi truy vấn", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
