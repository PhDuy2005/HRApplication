package com.se347.nhom4.HRApplication.domain.table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    Instant hiredDate = Instant.now();
    StatusEnum status = StatusEnum.ACTIVE;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    @ManyToOne
    @JoinColumn(name = "role_id")
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
     * Constructor để tạo Employee từ ReqCreateEmpDTO.
     * 
     * @return Employee entity
     */
    public Employee(ReqCreateEmpDTO dto) {
        this.fullname = dto.getFullname();
        this.email = dto.getEmail();
        this.password = dto.getPassword();
        this.phone = dto.getPhone();
        this.hiredDate = dto.getHiredDate() != null ? dto.getHiredDate() : Instant.now();
        this.status = dto.getStatus() != null ? dto.getStatus() : StatusEnum.ACTIVE;

        this.employeeSalaryTypes = new ArrayList<>();
        EmployeeSalaryType empSalaryType = new EmployeeSalaryType(dto.getEmpSalaryType());
        System.out.println(">>>CREATE EMPLOYEE MODULE: Type of salary: " + empSalaryType.getSalaryType());
        empSalaryType.setEmployee(this);
        this.employeeSalaryTypes.add(empSalaryType);

        if (empSalaryType.getSalaryType() == SalaryTypeEnum.SHIFT) {
            this.shiftRates = new ArrayList<>();
            for (ReqCreateEmpDTO.CreateEmpShiftRate rateDTO : dto.getEmpShiftRates()) {
                ShiftRate shiftRate;
                if (rateDTO.getShiftId() == null) {
                    // Tạo ShiftBaseRate
                    shiftRate = new ShiftBaseRate();
                } else {
                    // Tạo ShiftSpecialRate
                    ShiftSpecialRate specialRate = new ShiftSpecialRate();
                    // specialRate.setShift(rateDTO.getShiftId()); // sẽ set sau khi fetch Shift
                    // entity
                    specialRate.setPriority(rateDTO.getPriority());
                    specialRate.setNote(rateDTO.getNote());
                    shiftRate = specialRate;
                }
                shiftRate.setEmployee(this);
                shiftRate.setDayType(rateDTO.getDayType());
                shiftRate.setBaseRate(rateDTO.getBaseRate());
                shiftRate.setRateMultiplier(rateDTO.getRateMultiplier());
                shiftRate.setEffectiveFrom(
                        rateDTO.getEffectiveFrom() != null ? rateDTO.getEffectiveFrom() : Instant.now());
                shiftRate.setEffectiveTo(rateDTO.getEffectiveTo());
                shiftRate.setIsActive(rateDTO.getIsActive() != null ? rateDTO.getIsActive() : true);

                this.shiftRates.add(shiftRate);
            }
        } else if (empSalaryType.getSalaryType() == SalaryTypeEnum.MONTHLY) {
            this.monthlySalaries = new ArrayList<>();
            MonthlySalary monthlySalary = new MonthlySalary(dto.getEmpMonthlySalary());
            monthlySalary.setEmployee(this);
            this.monthlySalaries.add(monthlySalary);
        }
    }

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

/*
 * public class ReqCreateEmpDTO {
 * String fullname;
 * String email;
 * String password; // raw pwd
 * String phone;
 * 
 * Instant hiredDate;
 * StatusEnum status;
 * 
 * CreateEmpSalaryType empSalaryType;
 * 
 * List<CreateEmpShiftRate> empShiftRates;
 * 
 * CreateEmpMonthlySalary empMonthlySalary;
 * 
 * @Data
 * 
 * @AllArgsConstructor
 * 
 * @NoArgsConstructor
 * 
 * @Builder
 * public static class CreateEmpSalaryType {
 * private SalaryTypeEnum salaryType;
 * private Instant effectiveFrom;
 * private Instant effectiveTo;
 * }
 * 
 * @Data
 * 
 * @AllArgsConstructor
 * 
 * @NoArgsConstructor
 * 
 * @Builder
 * public static class CreateEmpShiftRate {
 * private DayTypeEnum dayType;
 * private Long baseRate; // Lương cơ bản theo ca. Đơn vị: VNĐ/h
 * private BigDecimal rateMultiplier; // Hệ số nhân lương theo ca
 * private Instant effectiveFrom;
 * private Instant effectiveTo;
 * private Boolean isActive;
 * 
 * // Fields cho ShiftSpecialRate (nếu shiftId != null)
 * private Long shiftId; // null = base rate, not null = special rate
 * private Integer priority;
 * private String note;
 * }
 * 
 * @Data
 * 
 * @AllArgsConstructor
 * 
 * @NoArgsConstructor
 * 
 * @Builder
 * public static class CreateEmpMonthlySalary {
 * private BigDecimal baseSalary;
 * private BigDecimal allowance;
 * private BigDecimal performanceMultiplier;
 * private Instant effectiveFrom;
 * private Instant effectiveTo;
 * }
 * }
 */
