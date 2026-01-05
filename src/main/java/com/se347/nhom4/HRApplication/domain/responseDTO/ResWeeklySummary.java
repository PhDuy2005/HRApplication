package com.se347.nhom4.HRApplication.domain.responseDTO;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResWeeklySummary {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<EmployeeSummary> employees;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeSummary {
        private Employee employee;
        private Statistics statistics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Employee {
        private Long id;
        private String fullname;
        private String email;
        private String department;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Statistics {
        private Integer totalScheduled;
        private WorkedStats worked;
        private AbsentStats absent;
        private LateStats late;
        private EarlyLeaveStats earlyLeave;
        private OvertimeStats overtime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkedStats {
        private Integer count;
        private Integer totalHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AbsentStats {
        private Integer count;
        private Integer totalHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LateStats {
        private Integer count;
        private Integer totalMinutes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EarlyLeaveStats {
        private Integer count;
        private Integer totalMinutes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OvertimeStats {
        private Integer count;
        private Integer totalMinutes;
    }
}
