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

    // ==========================
    // GPS - vị trí lúc check-out
    // ==========================

    // Vĩ độ tại thời điểm check-in
    @Column(name = "check_in_lat")
    private Double checkInLat;

    // Kinh độ tại thời điểm check-in
    @Column(name = "check_in_lng")
    private Double checkInLng;

    // Độ chính xác GPS lúc check-in (mét) - càng nhỏ càng chính xác
    @Column(name = "check_in_accuracy_meters")
    private Integer checkInAccuracyMeters;

    // Khoảng cách từ vị trí check-in đến điểm làm việc (mét) - server tính để audit
    @Column(name = "check_in_distance_meters")
    private Integer checkInDistanceMeters;

    // ==========================
    // GPS - vị trí lúc check-out
    // ==========================

    // Vĩ độ tại thời điểm check-out
    @Column(name = "check_out_lat")
    private Double checkOutLat;

    // Kinh độ tại thời điểm check-out
    @Column(name = "check_out_lng")
    private Double checkOutLng;

    // Độ chính xác GPS lúc check-out (mét)
    @Column(name = "check_out_accuracy_meters")
    private Integer checkOutAccuracyMeters;

    // Khoảng cách từ vị trí check-out đến điểm làm việc (mét)
    @Column(name = "check_out_distance_meters")
    private Integer checkOutDistanceMeters;

}
