package com.se347.nhom4.HRApplication.domain.table;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity đại diện cho lương theo ca đặc biệt (special shift rate).
 * 
 * Áp dụng cho một ca làm việc cụ thể (Shift).
 * ShiftSpecialRate sẽ OVERRIDE ShiftBaseRate khi nhân viên làm ca này.
 * 
 * Ví dụ:
 * - Nhân viên A có:
 * + ShiftBaseRate WEEKDAY: 50,000 VNĐ/h x 1.0
 * + ShiftSpecialRate cho "Ca đêm" WEEKDAY: 60,000 VNĐ/h x 1.2
 * → Khi làm ca đêm vào WEEKDAY: dùng 60,000 x 1.2 = 72,000 VNĐ/h
 * → Khi làm ca khác vào WEEKDAY: dùng 50,000 x 1.0 = 50,000 VNĐ/h
 * 
 * Use case:
 * - Ca đêm (night shift): lương cao hơn
 * - Ca nguy hiểm: lương cao hơn
 * - Ca VIP/Special events: lương cao hơn
 * 
 * Quan hệ:
 * - 1 Employee có nhiều ShiftSpecialRate
 * - 1 Shift có nhiều ShiftSpecialRate (cho nhiều employees)
 * - Many-to-Many giữa Employee và Shift qua ShiftSpecialRate
 */
@Entity
@Table(name = "shift_special_rates")
@DiscriminatorValue("SPECIAL")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ShiftSpecialRate extends ShiftRate {

    /**
     * Ca làm việc cụ thể mà rate này áp dụng.
     * Quan hệ Many-to-One: Nhiều special rates có thể cho cùng 1 shift.
     * 
     * NOT NULL vì ShiftSpecialRate BẮT BUỘC phải gắn với 1 Shift cụ thể.
     * Đây là điểm khác biệt chính so với ShiftBaseRate.
     * 
     * Note: Entity Shift sẽ được tạo sau, hiện tại chỉ giữ reference.
     */
    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    /**
     * Ghi chú thêm về special rate này.
     * Ví dụ: "Ca đêm phụ thu 20%", "Ca lễ tết phụ thu 50%"
     */
    @Column(name = "note", length = 500)
    private String note;

    /**
     * Priority khi có nhiều ShiftSpecialRate trùng điều kiện.
     * Số càng cao, priority càng cao.
     * Default: 0
     * 
     * Ví dụ:
     * - ShiftSpecialRate A cho "Ca đêm" priority = 1
     * - ShiftSpecialRate B cho "Ca đêm ngày lễ" priority = 2
     * → Chọn B vì priority cao hơn
     */
    @Column(name = "priority")
    private Integer priority = 0;
}
