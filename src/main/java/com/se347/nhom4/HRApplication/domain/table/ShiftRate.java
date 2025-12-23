package com.se347.nhom4.HRApplication.domain.table;

import java.math.BigDecimal;
import java.time.Instant;

import com.se347.nhom4.HRApplication.util.SecurityUtil;
import com.se347.nhom4.HRApplication.util.enums.DayTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Abstract base class cho tất cả các loại shift rate.
 * Sử dụng JOINED inheritance strategy để đảm bảo:
 * - Data integrity (NOT NULL constraints)
 * - Storage efficiency (không có NULL columns)
 * - Dễ dàng mở rộng với các loại rate mới
 */
@Entity
@Table(name = "shift_rates")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "rate_type", discriminatorType = DiscriminatorType.STRING, length = 20)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ShiftRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nhân viên được áp dụng shift rate này.
     * Quan hệ Many-to-One: Nhiều shift rates thuộc về 1 employee.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Loại ngày áp dụng (WEEKDAY, SATURDAY, SUNDAY, HOLIDAY).
     * Mỗi loại ngày có thể có hệ số lương khác nhau.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20)
    private DayTypeEnum dayType;

    /**
     * Mức lương cơ bản (base rate) tính theo giờ.
     * Đơn vị: VNĐ/giờ
     * Ví dụ: 50000 = 50,000 VNĐ/giờ
     */
    @Column(name = "base_rate", nullable = false)
    private Long baseRate;

    /**
     * Hệ số nhân lương cho loại ngày này.
     * Ví dụ:
     * - WEEKDAY: 1.0 (lương cơ bản)
     * - SATURDAY: 1.5 (150% lương)
     * - SUNDAY: 2.0 (200% lương)
     * - HOLIDAY: 3.0 (300% lương)
     */
    @Column(name = "rate_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal rateMultiplier;

    /**
     * Ngày bắt đầu áp dụng rate này.
     * Cho phép thay đổi rate theo thời gian.
     */
    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    /**
     * Ngày kết thúc áp dụng rate này.
     * NULL = áp dụng vô thời hạn.
     */
    @Column(name = "effective_to")
    private Instant effectiveTo;

    /**
     * Rate này có đang active không.
     * Cho phép disable rate mà không cần xóa.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    /**
     * Tính lương thực tế dựa trên số giờ làm việc.
     * 
     * @param hours Số giờ làm việc.
     * @return Tổng lương = baseRate * rateMultiplier * hours
     */
    public Long calculateSalary(double hours) {
        BigDecimal hoursBD = BigDecimal.valueOf(hours);
        BigDecimal baseRateBD = BigDecimal.valueOf(baseRate);
        BigDecimal total = baseRateBD.multiply(rateMultiplier).multiply(hoursBD);
        return total.longValue();

    }
}
