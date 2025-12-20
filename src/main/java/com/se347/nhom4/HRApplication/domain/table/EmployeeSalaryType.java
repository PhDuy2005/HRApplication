package com.se347.nhom4.HRApplication.domain.table;

import java.time.Instant;
import java.time.LocalDate;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCreateEmpDTO;
import com.se347.nhom4.HRApplication.util.SecurityUtil;
import com.se347.nhom4.HRApplication.util.enums.SalaryTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity định nghĩa loại hình lương của nhân viên theo thời gian.
 * 
 * Cho phép nhân viên thay đổi loại lương theo thời gian:
 * - Thời gian đầu: SHIFT (lương theo ca)
 * - Sau khi lên chức: MONTHLY (lương cố định)
 * - Thời gian thử việc: DAY (lương theo ngày)
 * 
 * Quan hệ 1-n với Employee:
 * - 1 Employee có nhiều EmployeeSalaryType theo thời gian
 * - Mỗi thời điểm chỉ có 1 SalaryType active
 */
@Entity
@Table(name = "employee_salary_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSalaryType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nhân viên được áp dụng loại lương này.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Loại hình lương: SHIFT, MONTHLY
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "salary_type", nullable = false, length = 20)
    private SalaryTypeEnum salaryType;

    /**
     * Ngày bắt đầu áp dụng loại lương này.
     */
    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    /**
     * Ngày kết thúc áp dụng.
     * NULL = áp dụng vô thời hạn (current active)
     */
    @Column(name = "effective_to")
    private Instant effectiveTo;

    /**
     * Ghi chú về việc thay đổi loại lương.
     */
    @Column(name = "note", length = 500)
    private String note;

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

    public EmployeeSalaryType(ReqCreateEmpDTO.CreateEmpSalaryType dto) {
        this.salaryType = dto.getSalaryType();
        this.effectiveFrom = dto.getEffectiveFrom();
        this.effectiveTo = dto.getEffectiveTo();
        this.note = dto.getNote();
    }

    /**
     * CÁC HÀM KHÁC
     */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("system");
    }

    /**
     * Kiểm tra loại lương này có đang active không.
     * 
     * @return true nếu effectiveTo == null hoặc > now
     */
    public boolean isActive() {
        return effectiveTo == null || effectiveTo.isAfter(Instant.now());
    }
}