package com.se347.nhom4.HRApplication.domain.responseDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.domain.table.EmployeeSalaryType;
import com.se347.nhom4.HRApplication.domain.table.MonthlySalary;
import com.se347.nhom4.HRApplication.domain.table.ShiftBaseRate;
import com.se347.nhom4.HRApplication.domain.table.ShiftRate;
import com.se347.nhom4.HRApplication.domain.table.ShiftSpecialRate;
import com.se347.nhom4.HRApplication.util.enums.DayTypeEnum;
import com.se347.nhom4.HRApplication.util.enums.SalaryTypeEnum;
import com.se347.nhom4.HRApplication.util.enums.StatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ResEmployeeInfo {
    private Long id;

    String fullname;
    String email;
    String phone;

    LocalDate hiredDate;
    StatusEnum status;

    // // Role info
    RoleInfo role;

    // Salary type hiện tại
    SalaryTypeInfo currentSalaryType;

    // // Shift rates hiện đang active
    // List<ShiftRateInfo> activeShiftRates;

    // // Monthly salary hiện đang active
    // MonthlySalaryInfo currentMonthlySalary;

    // Audit fields
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;

    public ResEmployeeInfo(Employee employee) {
        this.id = employee.getId();
        this.fullname = employee.getFullname();
        this.email = employee.getEmail();
        this.phone = employee.getPhone();
        this.hiredDate = employee.getHiredDate();
        this.status = employee.getStatus();

        // // Role
        // if (employee.getRole() != null) {
        // this.role = new RoleInfo(employee.getRole().getId(),
        // employee.getRole().getName());
        // }

        // Current salary type
        // EmployeeSalaryType currentSalaryType = employee.getCurrentSalaryType();
        // if (currentSalaryType != null) {
        // this.currentSalaryType = new SalaryTypeInfo(
        // currentSalaryType.getSalaryType(),
        // currentSalaryType.getEffectiveFrom(),
        // currentSalaryType.getEffectiveTo(),
        // currentSalaryType.getNote());
        // }

        // // Active shift rates
        // this.activeShiftRates = employee.getActiveShiftRates().stream()
        // .map(rate -> {
        // if (rate instanceof ShiftBaseRate) {
        // return new ShiftRateInfo(
        // rate.getId(),
        // "BASE",
        // rate.getDayType(),
        // rate.getBaseRate(),
        // rate.getRateMultiplier(),
        // null,
        // null,
        // null);
        // } else if (rate instanceof ShiftSpecialRate special) {
        // return new ShiftRateInfo(
        // rate.getId(),
        // "SPECIAL",
        // rate.getDayType(),
        // rate.getBaseRate(),
        // rate.getRateMultiplier(),
        // special.getShift() != null ? special.getShift().getId() : null,
        // special.getPriority(),
        // special.getNote());
        // }
        // return null;
        // })
        // .filter(info -> info != null)
        // .collect(Collectors.toList());

        // // Current monthly salary
        // MonthlySalary currentMonthlySalary = employee.getCurrentMonthlySalary();
        // if (currentMonthlySalary != null) {
        // this.currentMonthlySalary = new MonthlySalaryInfo(
        // currentMonthlySalary.getBaseSalary(),
        // currentMonthlySalary.getAllowance(),
        // currentMonthlySalary.getPerformanceMultiplier(),
        // currentMonthlySalary.getEffectiveFrom(),
        // currentMonthlySalary.getEffectiveTo());
        // }

        // Audit
        this.createdAt = employee.getCreatedAt();
        this.updatedAt = employee.getUpdatedAt();
        this.createdBy = employee.getCreatedBy();
        this.updatedBy = employee.getUpdatedBy();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleInfo {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SalaryTypeInfo {
        private SalaryTypeEnum salaryType;
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
        private String note;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShiftRateInfo {
        private Long id;
        private String type; // "BASE" or "SPECIAL"
        private DayTypeEnum dayType;
        private Long baseRate;
        private BigDecimal rateMultiplier;

        // For special rates only
        private Long shiftId;
        private Integer priority;
        private String note;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlySalaryInfo {
        private Long baseSalary;
        private Long allowance;
        private BigDecimal performanceMultiplier;
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
    }
}
