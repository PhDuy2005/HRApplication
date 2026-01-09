package com.se347.nhom4.HRApplication.domain.table;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Entity;
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
@Table(name = "work_schedules", uniqueConstraints = {
        // tránh trùng lịch: 1 nhân viên cùng 1 ca trong cùng 1 ngày
        @UniqueConstraint(name = "work_schedules_shift_date", columnNames = { "employee_id", "shift_id", "work_date" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: work_schedules.employee_id -> employees.id
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // FK: work_schedules.shift_id -> shifts.id
    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @jakarta.persistence.Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @ManyToOne
    private WorkSite workSite;
}
