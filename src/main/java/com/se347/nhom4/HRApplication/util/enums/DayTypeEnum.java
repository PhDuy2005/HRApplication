package com.se347.nhom4.HRApplication.util.enums;

/**
 * Enum định nghĩa các loại ngày để tính hệ số lương.
 */
public enum DayTypeEnum {
    /**
     * Ngày thường (Thứ 2 - Thứ 6)
     */
    WEEKDAY("Ngày thường"),

    /**
     * Thứ 7
     */
    SATURDAY("Thứ bảy"),

    /**
     * Chủ nhật
     */
    SUNDAY("Chủ nhật"),

    /**
     * Ngày lễ
     */
    HOLIDAY("Ngày lễ");

    private final String description;

    DayTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
