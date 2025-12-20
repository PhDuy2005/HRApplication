package com.se347.nhom4.HRApplication.domain.table;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity đại diện cho lương theo ca cơ bản (default shift rate).
 * 
 * Đây là rate mặc định áp dụng cho nhân viên khi:
 * - Làm việc bất kỳ ca nào (không phân biệt ca cụ thể)
 * - Chưa có ShiftSpecialRate cho ca đó
 * 
 * Ví dụ:
 * - Nhân viên A có ShiftBaseRate:
 * + WEEKDAY: 50,000 VNĐ/h x 1.0 = 50,000 VNĐ/h
 * + SATURDAY: 50,000 VNĐ/h x 1.5 = 75,000 VNĐ/h
 * + SUNDAY: 50,000 VNĐ/h x 2.0 = 100,000 VNĐ/h
 * + HOLIDAY: 50,000 VNĐ/h x 3.0 = 150,000 VNĐ/h
 * 
 * Quan hệ với Employee:
 * - 1 Employee có nhiều ShiftBaseRate (mỗi DayType 1 record)
 * - Hoặc có thể có nhiều ShiftBaseRate theo thời gian (effectiveFrom/To)
 */
@Entity
@Table(name = "shift_base_rates")
@DiscriminatorValue("BASE")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ShiftBaseRate extends ShiftRate {

    /**
     * ShiftBaseRate không có fields riêng.
     * Chỉ kế thừa tất cả fields từ ShiftRate.
     * 
     * Lý do tách ra class riêng:
     * 1. Semantic clarity - phân biệt rõ base rate và special rate
     * 2. Polymorphism - có thể xử lý khác nhau nếu cần
     * 3. Future extension - dễ thêm logic/fields riêng sau này
     */

    // Có thể override method nếu cần logic riêng
    // Ví dụ: toString(), validation, calculation, etc.
}
