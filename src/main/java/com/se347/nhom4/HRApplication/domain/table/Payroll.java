package com.se347.nhom4.HRApplication.domain.table;

import com.se347.nhom4.HRApplication.util.enums.PayrollStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payrolls",
    uniqueConstraints = {
        // 1 nhân viên chỉ có 1 bảng lương cho 1 tháng/năm
        @UniqueConstraint(name = "payroll_employee", columnNames = {"employee_id", "month", "year"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_id")
    private Long payrollId;

    // FK: payrolls.employee_id -> employees.id
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "total_hour")
    private Long totalHour;

    @Column(name = "total_ot_hour")
    private Long totalOtHour;

    @Column(name = "base_salary")
    private Long baseSalary;

    @Column(name = "shift_salary")
    private Long shiftSalary;

    @Column(name = "ot_salary")
    private Long otSalary;

    @Column(name = "penalty_total")
    private Long penaltyTotal;

    @Column(name = "final_salary")
    private Long finalSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PayrollStatusEnum status;
}
