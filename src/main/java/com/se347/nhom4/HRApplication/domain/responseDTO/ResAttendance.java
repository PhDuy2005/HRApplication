package com.se347.nhom4.HRApplication.domain.responseDTO;

import java.time.Instant;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResAttendance {
    private Long id;
    private Long employeeId;
    private Long workScheduleId;

    private LocalDate workDate;
    private Instant checkIn;
    private Instant checkOut;

    private Integer totalWorkTime;
    private Integer overtime;
    private Integer lateTime;
    private Integer earlyLeave;
    private String status; // AttendanceStatusEnum as string

    // GPS check-in
    private Double checkInLat;
    private Double checkInLng;
    private Integer checkInAccuracyMeters;
    private Integer checkInDistanceMeters;

    // GPS check-out
    private Double checkOutLat;
    private Double checkOutLng;
    private Integer checkOutAccuracyMeters;
    private Integer checkOutDistanceMeters;
}
