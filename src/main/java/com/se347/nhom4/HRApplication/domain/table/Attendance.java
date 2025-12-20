package com.se347.nhom4.HRApplication.domain.table;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attendances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: attendances.employee_id -> employees.id
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * FK nullable: attendances.work_schedule_id -> work_schedules.id
     * Theo ERD: WorkSchedule 1 - 0..1 Attendance
     */
    @OneToOne(optional = true)
    @JoinColumn(name = "work_schedule_id", nullable = true, unique = true)
    private WorkSchedule workSchedule;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in")
    private Instant checkIn;

    @Column(name = "check_out")
    private Instant checkOut;

    // phút
    @Column(name = "total_work_time")
    private Integer totalWorkTime;

    @Column(name = "overtime")
    private Integer overtime;

    @Column(name = "late_time")
    private Integer lateTime;
}
