package com.se347.nhom4.HRApplication.domain.requestDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.se347.nhom4.HRApplication.util.enums.DayTypeEnum;
// import com.se347.nhom4.HRApplication.util.enums.OtTypeEnum;
import com.se347.nhom4.HRApplication.util.enums.SalaryTypeEnum;
import com.se347.nhom4.HRApplication.util.enums.StatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReqCreateEmpDTO {
    String fullname;
    String email;
    String password; // raw pwd
    String phone;

    LocalDate hiredDate;
    StatusEnum status;

    CreateEmpSalaryType empSalaryType;

    /**
     * Cấu hình shift rates cho nhân viên (tính lương theo ca).
     * Danh sách này có thể bao gồm cả base rate và special rate.
     * - Nếu shiftId = null: Tạo ShiftBaseRate (áp dụng cho tất cả ca)
     * - Nếu shiftId != null: Tạo ShiftSpecialRate (áp dụng cho ca cụ thể)
     */
    List<CreateEmpShiftRate> empShiftRates;

    /**
     * Cấu hình lương tháng cho nhân viên (tính lương theo tháng).
     * Chỉ áp dụng khi empSalaryType.salaryType = MONTHLY.
     */
    CreateEmpMonthlySalary empMonthlySalary;

    /**
     * Cấu hình OT rates cho nhân viên (tính lương làm thêm giờ).
     * Danh sách các tỷ lệ OT theo loại ngày và loại OT.
     * TODO: Review - Tính năng mới cần kiểm tra
     */
    // List<CreateEmpOtRate> empOtRates;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateEmpSalaryType {
        private SalaryTypeEnum salaryType;
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
        private String note;
    }

    /**
     * DTO để tạo Shift Rate cho nhân viên mới.
     * Hỗ trợ tạo cả ShiftBaseRate và ShiftSpecialRate.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateEmpShiftRate {
        private DayTypeEnum dayType;
        private Long baseRate; // Lương cơ bản theo ca. Đơn vị: VNĐ/h
        private BigDecimal rateMultiplier; // Hệ số nhân lương theo ca
        private Instant effectiveFrom;
        private Instant effectiveTo;
        private Boolean isActive;

        // Fields cho ShiftSpecialRate (nếu shiftId != null)
        private Long shiftId; // null = base rate, not null = special rate
        private Integer priority;
        private String note;
    }

    /**
     * DTO để tạo Monthly Salary cho nhân viên mới.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateEmpMonthlySalary {
        private Long baseSalary;
        private Long allowance;
        private BigDecimal performanceMultiplier;
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
        private String note;
    }

    /**
     * DTO để tạo OT Rate cho nhân viên mới.
     * Cấu hình tỷ lệ lương làm thêm giờ theo loại ngày và loại OT.
     * TODO: Review - Tính năng mới cần kiểm tra
     */
    // @Data
    // @AllArgsConstructor
    // @NoArgsConstructor
    // @Builder
    // public static class CreateEmpOtRate {
    // private OtTypeEnum otType; // NORMAL_OT | WEEKEND_OT | HOLIDAY_OT
    // private DayTypeEnum dayType; // WEEKDAY | SATURDAY | SUNDAY
    // private BigDecimal rateMultiplier; // Hệ số nhân (VD: 1.5, 2.0, 3.0)
    // private Boolean isActive;
    // private Instant effectiveFrom;
    // private Instant effectiveTo;
    // }
}
