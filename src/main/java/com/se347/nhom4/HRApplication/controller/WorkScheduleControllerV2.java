package com.se347.nhom4.HRApplication.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.se347.nhom4.HRApplication.domain.responseDTO.ResWeeklyByShift;
import com.se347.nhom4.HRApplication.service.WorkScheduleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/work-schedules")
@RequiredArgsConstructor
@Tag(name = "Work Schedule V2", description = "Optimized Work Schedule APIs")
public class WorkScheduleControllerV2 {

    private final WorkScheduleService workScheduleService;

    /**
     * Get weekly work schedules grouped by shift with attendance data.
     * Optimized API: Reduces 206 API calls to 1 call.
     * 
     * @param startDate Start date of the week (YYYY-MM-DD)
     * @param endDate   End date of the week (YYYY-MM-DD)
     * @return Weekly summary by shift with all attendance data
     */
    @GetMapping("/weekly-by-shift")
    @Operation(summary = "Xem lịch làm việc theo ca theo tuần (Tối ưu)", description = "API tối ưu để xem lịch làm việc theo ca trong khoảng thời gian, kèm dữ liệu chấm công. "
            +
            "Giảm từ ~206 API calls xuống còn 1 call. Chỉ trả về các ca đang active.")
    public ResponseEntity<ResWeeklyByShift> getWeeklyByShift(
            @Parameter(description = "Ngày bắt đầu tuần (YYYY-MM-DD)", example = "2025-12-22", required = true) @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Ngày kết thúc tuần (YYYY-MM-DD)", example = "2025-12-28", required = true) @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ResWeeklyByShift result = workScheduleService.getWeeklyByShift(startDate, endDate);
        return ResponseEntity.ok(result);
    }
}
