package com.se347.nhom4.HRApplication.domain.responseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.domain.table.Shift;
import com.se347.nhom4.HRApplication.domain.table.ShiftSpecialRate;
import com.se347.nhom4.HRApplication.domain.table.WorkSchedule;
import com.se347.nhom4.HRApplication.service.DayTypeService;
import com.se347.nhom4.HRApplication.util.enums.DayTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResWorkSchedule {
    Long id;
    Emp employee;
    Shift shift;
    LocalDate workDate;
    Long expectedWage;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Emp {
        Long id;
        String fullname;
    }

    public ResWorkSchedule(WorkSchedule workSchedule, DayTypeService dayTypeService) {
        this.id = workSchedule.getId();
        this.employee = new Emp(
                workSchedule.getEmployee().getId(),
                workSchedule.getEmployee().getFullname());
        this.shift = workSchedule.getShift();
        this.workDate = workSchedule.getWorkDate();

        // Xác định loại ngày (WEEKDAY, SATURDAY, SUNDAY, HOLIDAY)
        Employee employeeEntity = workSchedule.getEmployee();
        DayTypeEnum dayType = dayTypeService.getDayType(workSchedule.getWorkDate());

        // Tìm ShiftRate phù hợp của nhân viên cho ca và loại ngày này
        // Ưu tiên: ShiftSpecialRate (có shiftId) > ShiftBaseRate
        var applicableShiftRate = employeeEntity.getActiveShiftRates().stream()
                .filter(rate -> rate.getDayType() == dayType)
                .filter(rate -> {
                    // Nếu là ShiftSpecialRate, kiểm tra shiftId có khớp không
                    if (rate instanceof com.se347.nhom4.HRApplication.domain.table.ShiftSpecialRate specialRate) {
                        return specialRate.getShift() != null
                                && specialRate.getShift().getId().equals(workSchedule.getShift().getId());
                    }
                    // Nếu là ShiftBaseRate, chấp nhận cho tất cả ca
                    return true;
                })
                .findFirst()
                .orElse(null);

        // Tính lương dự kiến cho ca làm việc (standard_hours)
        if (applicableShiftRate != null && workSchedule.getShift().getStandardHours() != null) {
            this.expectedWage = applicableShiftRate.calculateSalary(workSchedule.getShift().getStandardHours());
        } else {
            // Nếu không tìm thấy ShiftRate, để null hoặc 0
            this.expectedWage = 0L;
        }
    }
}
