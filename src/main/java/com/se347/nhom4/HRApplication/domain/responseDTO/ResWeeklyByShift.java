package com.se347.nhom4.HRApplication.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResWeeklyByShift {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ShiftScheduleSummary> shifts;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShiftScheduleSummary {
        private Shift shift;
        private List<DailySchedule> dailySchedules;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Shift {
        private Long id;
        private String name;
        private LocalTime startTime;
        private LocalTime endTime;
        private Double standardHours;
        private String colorCode;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailySchedule {
        private LocalDate date;
        private List<ScheduleWithAttendance> schedules;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleWithAttendance {
        private Long id;
        private LocalDate workDate;
        private Employee employee;
        private Attendance attendance; // Can be null

        public ScheduleWithAttendance(Long id, LocalDate workDate, Employee employee) {
            this.id = id;
            this.workDate = workDate;
            this.employee = employee;
            this.attendance = null;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Employee {
        private Long id;
        private String fullname;
        private String email;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attendance {
        private Long id;
        private Instant checkIn;
        private Instant checkOut;
        private Integer lateTime;
        private Integer earlyLeaveTime;
        private Integer overtime;
        // private String status;
    }
}
