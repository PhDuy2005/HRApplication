package com.se347.nhom4.HRApplication.util.enums;

/**
 * Enum định nghĩa các loại hình lương của nhân viên.
 */
public enum SalaryTypeEnum {
    /**
     * Lương theo ca (shift-based)
     * Tính theo số ca làm việc và loại ca
     */
    SHIFT("Lương theo ca"),

    /**
     * Lương cố định hàng tháng (monthly salary)
     * Lương cố định không phụ thuộc vào số giờ/ca làm
     */
    MONTHLY("Lương tháng");

    private final String description;

    SalaryTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}