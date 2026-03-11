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

    /**
     * Kiểm tra trường bắt buộc (không null, không rỗng).
     *
     * @param value     giá trị cần kiểm tra
     * @param fieldName tên trường (hiển thị trong thông báo lỗi)
     * @throws ValidationException nếu null hoặc rỗng
     */
    public static void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName,
                    fieldName + " không được để trống.");
        }
    }

    /**
     * Kiểm tra giá trị nằm trong khoảng [min, max].
     *
     * @param value     giá trị cần kiểm tra
     * @param min       giá trị tối thiểu
     * @param max       giá trị tối đa
     * @param fieldName tên trường
     * @throws ValidationException nếu ngoài khoảng
     */
    public static void validateRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(fieldName,
                    String.format("%s phải nằm trong khoảng %.0f đến %.0f (giá trị hiện tại: %.2f).",
                            fieldName, min, max, value));
        }
    }

    /**
     * Kiểm tra giá trị dương (> 0).
     *
     * @param value     giá trị cần kiểm tra
     * @param fieldName tên trường
     * @throws ValidationException nếu <= 0
     */
    public static void validatePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName,
                    fieldName + " phải lớn hơn 0.");
        }
    }

    /**
     * Kiểm tra giá trị không âm (>= 0).
     *
     * @param value     giá trị cần kiểm tra
     * @param fieldName tên trường
     * @throws ValidationException nếu < 0
     */
    public static void validateNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName,
                    fieldName + " không được âm.");
        }
    }

    // ══════════════════════════════════════════════════════════
    //  OPTIONAL VALIDATORS (trả về boolean, không ném exception)
    // ══════════════════════════════════════════════════════════

    /**
     * Kiểm tra phone hợp lệ (10 số, bắt đầu bằng 0).
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches(PHONE_REGEX);
    }

    /**
     * Kiểm tra CCCD hợp lệ (12 chữ số).
     */
    public static boolean isValidIdCard(String cccd) {
        return cccd != null && cccd.matches(ID_CARD_REGEX);
    }
}
