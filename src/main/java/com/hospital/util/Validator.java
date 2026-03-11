package com.hospital.util;

import com.hospital.exception.ValidationException;

public final class Validator {

    private static final String PHONE_REGEX = "^0\\d{9}$";
    private static final String ID_CARD_REGEX = "^\\d{12}$";

    private Validator() {}

    public static void validatePhone(String phone) {
        if (phone == null || !phone.matches(PHONE_REGEX)) {
            throw new ValidationException("phone",
                    "Số điện thoại không hợp lệ. Phải gồm 10 chữ số, bắt đầu bằng 0.");
        }
    }

    public static void validateIdCard(String cccd) {
        if (cccd == null || !cccd.matches(ID_CARD_REGEX)) {
            throw new ValidationException("id_card",
                    "Số CCCD không hợp lệ. Phải gồm đúng 12 chữ số.");
        }
    }

    public static void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName,
                    fieldName + " không được để trống.");
        }
    }

    public static void validateRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(fieldName,
                    String.format("%s phải nằm trong khoảng %.0f đến %.0f (giá trị hiện tại: %.2f).",
                            fieldName, min, max, value));
        }
    }

    public static void validatePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName,
                    fieldName + " phải lớn hơn 0.");
        }
    }

    public static void validateNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName,
                    fieldName + " không được âm.");
        }
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches(PHONE_REGEX);
    }


    public static boolean isValidIdCard(String cccd) {
        return cccd != null && cccd.matches(ID_CARD_REGEX);
    }
}
