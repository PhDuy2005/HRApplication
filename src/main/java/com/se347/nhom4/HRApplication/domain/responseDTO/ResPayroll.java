package com.se347.nhom4.HRApplication.domain.responseDTO;

import com.se347.nhom4.HRApplication.domain.table.Payroll;
import com.se347.nhom4.HRApplication.util.enums.PayrollStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResPayroll {
    private Long payrollId;
    private Emp employee;
    private Integer month;
    private Integer year;
    private Long totalHour;
    private Long totalOtHour;
    private Long baseSalary;
    private Long shiftSalary;
    private Long otSalary;
    private Long penaltyTotal;
    private Long finalSalary;
    private PayrollStatusEnum status;

    public ResPayroll(Payroll entity) {
        this.payrollId = entity.getPayrollId();
        this.month = entity.getMonth();
        this.year = entity.getYear();
        this.totalHour = entity.getTotalHour();
        this.totalOtHour = entity.getTotalOtHour();
        this.baseSalary = entity.getBaseSalary();
        this.shiftSalary = entity.getShiftSalary();
        this.otSalary = entity.getOtSalary();
        this.penaltyTotal = entity.getPenaltyTotal();
        this.finalSalary = entity.getFinalSalary();
        this.status = entity.getStatus();

        Emp emp = new Emp();
        emp.id = entity.getEmployee().getId();
        emp.fullname = entity.getEmployee().getFullname();
        this.employee = emp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Emp {
        Long id;
        String fullname;
    }
}
