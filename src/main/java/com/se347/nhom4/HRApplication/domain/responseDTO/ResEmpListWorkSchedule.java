package com.se347.nhom4.HRApplication.domain.responseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ResEmpListWorkSchedule {
    ResWorkSchedule.Emp employee;
    LocalDate dayFrom;
    LocalDate dayTo;
    Long totalExpectedWage;
    List<DailyWorkSchedule> dailySchedules;

    /**
     * DTO đại diện cho lịch làm việc của một ngày cụ thể.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class DailyWorkSchedule {
        LocalDate date;
        List<ResWorkSchedule> workSchedules;
        Long dailyExpectedWage;

        public DailyWorkSchedule(LocalDate date, List<ResWorkSchedule> workSchedules) {
            this.date = date;
            this.workSchedules = workSchedules;
            // Tính tổng expectedWage của ngày
            this.dailyExpectedWage = workSchedules.stream()
                    .map(ResWorkSchedule::getExpectedWage)
                    .reduce(0L, Long::sum);
        }
    }

    public ResEmpListWorkSchedule(ResWorkSchedule.Emp employee, LocalDate dayFrom, LocalDate dayTo,
            List<ResWorkSchedule> workSchedules) {
        this.employee = employee;
        this.dayFrom = dayFrom;
        this.dayTo = dayTo;

        // Nhóm workSchedules theo ngày
        this.dailySchedules = workSchedules.stream()
                .collect(Collectors.groupingBy(ResWorkSchedule::getWorkDate))
                .entrySet().stream()
                .map(entry -> new DailyWorkSchedule(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> a.getDate().compareTo(b.getDate())) // Sắp xếp theo ngày tăng dần
                .toList();

        // Tính tổng expectedWage từ tất cả các ngày
        this.totalExpectedWage = dailySchedules.stream()
                .map(DailyWorkSchedule::getDailyExpectedWage)
                .reduce(0L, Long::sum);
    }
}
