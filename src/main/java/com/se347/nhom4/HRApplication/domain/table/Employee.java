package com.se347.nhom4.HRApplication.domain.table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCreateEmpDTO;
import com.se347.nhom4.HRApplication.util.SecurityUtil;
import com.se347.nhom4.HRApplication.util.enums.SalaryTypeEnum;
import com.se347.nhom4.HRApplication.util.enums.StatusEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String fullname;
    String email;
    String password; // phải hash trước khi lưu
    String phone;

    LocalDate hiredDate = LocalDate.now();
    StatusEnum status = StatusEnum.ACTIVE;

    @Column(name = "allowance")
    private Long allowance; // Phụ cấp cố định hàng tháng (VNĐ)

    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    @ManyToOne
    @JoinColumn(name = "role_id")
    @JsonIgnoreProperties(value = { "employees", "permissions" })
    private Role role;

    /**
     * Lịch sử loại hình lương của nhân viên theo thời gian.
     * Quan hệ 1-n: 1 Employee có nhiều EmployeeSalaryType.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<EmployeeSalaryType> employeeSalaryTypes = new ArrayList<>();

    /**
     * Lịch sử mức lương tháng của nhân viên theo thời gian.
     * Quan hệ 1-n: 1 Employee có nhiều MonthlySalary.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<MonthlySalary> monthlySalaries = new ArrayList<>();

    /**
     * Danh sách shift rates của nhân viên (bao gồm base và special rates).
     * ShiftRate là abstract class nên JPA sẽ tự động map cả ShiftBaseRate và
     * ShiftSpecialRate.
     * Quan hệ 1-n: 1 Employee có nhiều ShiftRate.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<ShiftRate> shiftRates = new ArrayList<>();

    /**
     * Danh sách OT rates của nhân viên.
     * Quan hệ 1-n: 1 Employee có nhiều ShiftOtRate.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShiftOtRate> shiftOtRates = new ArrayList<>();

    /**
     * Danh sách penalties của nhân viên.
     * Quan hệ 1-n: 1 Employee có nhiều EmployeePenalty.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeePenalty> employeePenalties = new ArrayList<>();

    /**
     * Danh sách attendance penalties của nhân viên.
     * Quan hệ 1-n: 1 Employee có nhiều AttendancePenalty.
     */
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttendancePenalty> attendancePenalties = new ArrayList<>();

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

    /**
     * CÁC HÀM KHÁC
     **/

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
     * Lấy loại hình lương hiện tại đang áp dụng.
     * 
     * @return EmployeeSalaryType đang active, hoặc null nếu không có.
     */
    public EmployeeSalaryType getCurrentSalaryType() {
        return employeeSalaryTypes.stream()
                .filter(EmployeeSalaryType::isActive)
                .findFirst()
                .orElse(null);
    }

    /**
     * Lấy mức lương tháng hiện tại đang áp dụng.
     * 
     * @return MonthlySalary đang active, hoặc null nếu không có hoặc không phải
     *         MONTHLY salary.
     */
    public MonthlySalary getCurrentMonthlySalary() {
        return monthlySalaries.stream()
                .filter(MonthlySalary::isCurrentlyActive)
                .findFirst()
                .orElse(null);
    }

    /**
     * Lấy tất cả shift rates đang active.
     * 
     * @return List các ShiftRate đang active.
     */
    public List<ShiftRate> getActiveShiftRates() {
        return shiftRates.stream()
                .filter(ShiftRate::getIsActive)
                .toList();
    }

}