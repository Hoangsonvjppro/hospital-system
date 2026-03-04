package com.hospital.util;

import com.hospital.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho Validator utility.
 */
class ValidatorTest {

    // ══════════════════════════════════════════════════════════
    //  validatePhone
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validatePhone")
    class ValidatePhoneTests {

        @Test
        @DisplayName("SĐT hợp lệ — 10 số bắt đầu bằng 0")
        void validPhone() {
            assertDoesNotThrow(() -> Validator.validatePhone("0912345678"));
        }

        @Test
        @DisplayName("SĐT hợp lệ — số cố định")
        void validPhoneFixedLine() {
            assertDoesNotThrow(() -> Validator.validatePhone("0281234567"));
        }

        @Test
        @DisplayName("SĐT null — ném ValidationException")
        void nullPhone() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> Validator.validatePhone(null));
            assertEquals("phone", ex.getFieldName());
        }

        @Test
        @DisplayName("SĐT 9 số — thiếu")
        void shortPhone() {
            assertThrows(ValidationException.class,
                    () -> Validator.validatePhone("091234567"));
        }

        @Test
        @DisplayName("SĐT 11 số — thừa")
        void longPhone() {
            assertThrows(ValidationException.class,
                    () -> Validator.validatePhone("09123456789"));
        }

        @Test
        @DisplayName("SĐT có chữ cái")
        void phoneWithLetters() {
            assertThrows(ValidationException.class,
                    () -> Validator.validatePhone("091234abcd"));
        }

        @Test
        @DisplayName("SĐT không bắt đầu bằng 0")
        void phoneNotStartingWithZero() {
            assertThrows(ValidationException.class,
                    () -> Validator.validatePhone("1912345678"));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  validateIdCard
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validateIdCard")
    class ValidateIdCardTests {

        @Test
        @DisplayName("CCCD hợp lệ — 12 số")
        void validIdCard() {
            assertDoesNotThrow(() -> Validator.validateIdCard("079185001001"));
        }

        @Test
        @DisplayName("CCCD null")
        void nullIdCard() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> Validator.validateIdCard(null));
            assertEquals("id_card", ex.getFieldName());
        }

        @Test
        @DisplayName("CCCD 11 số — thiếu")
        void shortIdCard() {
            assertThrows(ValidationException.class,
                    () -> Validator.validateIdCard("07918500100"));
        }

        @Test
        @DisplayName("CCCD 13 số — thừa")
        void longIdCard() {
            assertThrows(ValidationException.class,
                    () -> Validator.validateIdCard("0791850010011"));
        }

        @Test
        @DisplayName("CCCD có ký tự")
        void idCardWithLetters() {
            assertThrows(ValidationException.class,
                    () -> Validator.validateIdCard("07918500100a"));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  validateRequired
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validateRequired")
    class ValidateRequiredTests {

        @Test
        @DisplayName("Giá trị hợp lệ")
        void validRequired() {
            assertDoesNotThrow(() -> Validator.validateRequired("Nguyễn Văn A", "Họ tên"));
        }

        @Test
        @DisplayName("Giá trị null")
        void nullRequired() {
            assertThrows(ValidationException.class,
                    () -> Validator.validateRequired(null, "Họ tên"));
        }

        @Test
        @DisplayName("Giá trị rỗng")
        void emptyRequired() {
            assertThrows(ValidationException.class,
                    () -> Validator.validateRequired("", "Họ tên"));
        }

        @Test
        @DisplayName("Giá trị toàn khoảng trắng")
        void blankRequired() {
            assertThrows(ValidationException.class,
                    () -> Validator.validateRequired("   ", "Họ tên"));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  validateRange
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validateRange")
    class ValidateRangeTests {

        @Test
        @DisplayName("Trong khoảng")
        void inRange() {
            assertDoesNotThrow(() -> Validator.validateRange(36.5, 35.0, 42.0, "Nhiệt độ"));
        }

        @Test
        @DisplayName("Tại biên dưới")
        void atMinBoundary() {
            assertDoesNotThrow(() -> Validator.validateRange(35.0, 35.0, 42.0, "Nhiệt độ"));
        }

        @Test
        @DisplayName("Tại biên trên")
        void atMaxBoundary() {
            assertDoesNotThrow(() -> Validator.validateRange(42.0, 35.0, 42.0, "Nhiệt độ"));
        }

        @Test
        @DisplayName("Dưới biên dưới")
        void belowMin() {
            assertThrows(ValidationException.class,
                    () -> Validator.validateRange(34.9, 35.0, 42.0, "Nhiệt độ"));
        }

        @Test
        @DisplayName("Trên biên trên")
        void aboveMax() {
            assertThrows(ValidationException.class,
                    () -> Validator.validateRange(42.1, 35.0, 42.0, "Nhiệt độ"));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  validatePositive / validateNonNegative
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validatePositive & validateNonNegative")
    class ValidateNumericTests {

        @Test
        @DisplayName("Giá trị dương hợp lệ")
        void positiveOk() {
            assertDoesNotThrow(() -> Validator.validatePositive(100, "Giá"));
        }

        @Test
        @DisplayName("Giá trị 0 — không dương")
        void zeroNotPositive() {
            assertThrows(ValidationException.class,
                    () -> Validator.validatePositive(0, "Giá"));
        }

        @Test
        @DisplayName("Giá trị âm — không dương")
        void negativeNotPositive() {
            assertThrows(ValidationException.class,
                    () -> Validator.validatePositive(-5, "Giá"));
        }

        @Test
        @DisplayName("Giá trị 0 — hợp lệ cho nonNegative")
        void zeroNonNegativeOk() {
            assertDoesNotThrow(() -> Validator.validateNonNegative(0, "Giảm giá"));
        }

        @Test
        @DisplayName("Giá trị âm — không hợp lệ cho nonNegative")
        void negativeNotNonNegative() {
            assertThrows(ValidationException.class,
                    () -> Validator.validateNonNegative(-1, "Giảm giá"));
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Boolean helpers
    // ══════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isValid helpers")
    class BooleanHelperTests {

        @Test
        void isValidPhoneTrue() {
            assertTrue(Validator.isValidPhone("0912345678"));
        }

        @Test
        void isValidPhoneFalse() {
            assertFalse(Validator.isValidPhone("abc"));
            assertFalse(Validator.isValidPhone(null));
        }

        @Test
        void isValidIdCardTrue() {
            assertTrue(Validator.isValidIdCard("079185001001"));
        }

        @Test
        void isValidIdCardFalse() {
            assertFalse(Validator.isValidIdCard("123"));
            assertFalse(Validator.isValidIdCard(null));
        }
    }
}
