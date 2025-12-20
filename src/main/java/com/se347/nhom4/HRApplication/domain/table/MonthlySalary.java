package com.se347.nhom4.HRApplication.domain.table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCreateEmpDTO;
import com.se347.nhom4.HRApplication.util.SecurityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity quản lý lương tháng cố định cho nhân viên.
 * 
 * Dành cho nhân viên có SalaryType = MONTHLY.
 * Lương không phụ thuộc vào số giờ làm việc, số ca, hay attendance.
 * 
 * Cấu trúc lương:
 * - Base Salary: Lương cơ bản hàng tháng
 * - Allowance: Phụ cấp (ăn trưa, xăng xe, điện thoại, etc.)
 * - Total = Base Salary + Allowance - Penalties
 * 
 * Quan hệ với Employee:
 * - 1 Employee có nhiều MonthlySalary theo thời gian (lịch sử tăng lương)
 * - Mỗi thời điểm chỉ có 1 MonthlySalary active
 */
@Entity
@Table(name = "monthly_salaries", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "employee_id", "effective_from" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySalary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nhân viên được áp dụng lương tháng này.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Lương cơ bản hàng tháng (VNĐ).
     * Không bao gồm phụ cấp.
     * 
     * Ví dụ: 15,000,000 VNĐ/tháng
     */
    @Column(name = "base_salary", nullable = false)
    private Long baseSalary;

    /**
     * Tổng phụ cấp hàng tháng (VNĐ).
     * Bao gồm: phụ cấp ăn trưa, xăng xe, điện thoại, chuyên cần, etc.
     * 
     * Ví dụ: 2,000,000 VNĐ (500k ăn trưa + 1000k xăng xe + 500k điện thoại)
     */
    @Column(name = "allowance", nullable = false)
    private Long allowance;

    /**
     * Ngày bắt đầu áp dụng mức lương này.
     * Thường là ngày tăng lương hoặc ngày ký hợp đồng.
     */
    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    /**
     * Ngày kết thúc áp dụng mức lương này.
     * NULL = đang áp dụng hiện tại
     */
    @Column(name = "effective_to")
    private Instant effectiveTo;

    /**
     * Hệ số điều chỉnh lương dựa trên performance.
     * Default: 1.0 (100% lương)
     * 
     * Ví dụ:
     * - Performance tốt: 1.1 (110% lương)
     * - Performance kém: 0.9 (90% lương)
     * - Performance trung bình: 1.0 (100% lương)
     */
    @Column(name = "performance_multiplier", precision = 5, scale = 2)
    private BigDecimal performanceMultiplier = BigDecimal.ONE;

    /**
     * Ghi chú về mức lương này.
     * Ví dụ: "Tăng lương sau review Q4", "Lương khởi điểm"
     */
    @Column(name = "note", length = 500)
    private String note;

    /**
     * Lương này có đang active không.
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

    /**
     * CÁC CONSTRUCTOR KHÔNG ĐƯỢC ĐỊNH NGHĨA BỞI ANNOTATION
     **/

    public MonthlySalary(ReqCreateEmpDTO.CreateEmpMonthlySalary dto) {
        this.baseSalary = dto.getBaseSalary();
        this.allowance = dto.getAllowance();
        this.effectiveFrom = dto.getEffectiveFrom();
        this.effectiveTo = dto.getEffectiveTo();
        this.performanceMultiplier = dto.getPerformanceMultiplier();
        this.note = dto.getNote();
    }

    /**
     * CÁC HÀM KHÁC
     **/

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
        if (isActive == null) {
            isActive = true;
        }
        if (performanceMultiplier == null) {
            performanceMultiplier = BigDecimal.ONE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    /**
     * Tính tổng lương gốc trước khi trừ penalty.
     * 
     * @return baseSalary + allowance
     */
    public Long calculateGrossMonthlyPay() {
        return baseSalary + allowance;
    }

    /**
     * Tính lương thực tế sau khi áp dụng performance multiplier.
     * 
     * @return (baseSalary + allowance) * performanceMultiplier
     */
    public Long calculateAdjustedMonthlyPay() {
        BigDecimal grossPay = BigDecimal.valueOf(calculateGrossMonthlyPay());
        return grossPay.multiply(performanceMultiplier).longValue();
    }

    /**
     * Kiểm tra lương này có đang active không.
     * 
     * @return true nếu effectiveTo == null hoặc > now
     */
    public boolean isCurrentlyActive() {
        return isActive && (effectiveTo == null || effectiveTo.isAfter(Instant.now()));
    }
}