package com.se347.nhom4.HRApplication.domain.responseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.se347.nhom4.HRApplication.domain.table.Shift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ResShiftListWorkSchedule {
    Shift shift;
    LocalDate dayFrom;
    LocalDate dayTo;
    List<DailyWorkSchedule> dailySchedules;
    Long totalExpectedWage;

    public ResShiftListWorkSchedule(List<ResWorkSchedule> workSchedules) {
        if (workSchedules.isEmpty()) {
            return;
        }

        this.shift = workSchedules.get(0).getShift();
        this.dayFrom = workSchedules.stream()
                .map(ResWorkSchedule::getWorkDate)
                .min(LocalDate::compareTo)
                .orElse(null);
        this.dayTo = workSchedules.stream()
                .map(ResWorkSchedule::getWorkDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        // Group by workDate
        Map<LocalDate, List<ResWorkSchedule>> groupedByDate = workSchedules.stream()
                .collect(Collectors.groupingBy(ResWorkSchedule::getWorkDate));

        this.dailySchedules = groupedByDate.entrySet().stream()
                .map(entry -> new DailyWorkSchedule(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());

        this.totalExpectedWage = workSchedules.stream()
                .map(ResWorkSchedule::getExpectedWage)
                .reduce(0L, Long::sum);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class DailyWorkSchedule {
        private LocalDate date;
        private List<ResWorkSchedule> schedules;
        private Long dailyExpectedWage;

        public DailyWorkSchedule(LocalDate date, List<ResWorkSchedule> schedules) {
            this.date = date;
            this.schedules = schedules;
            this.dailyExpectedWage = schedules.stream()
                    .map(ResWorkSchedule::getExpectedWage)
                    .reduce(0L, Long::sum);
        }
    }
}
