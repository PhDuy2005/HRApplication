package com.se347.nhom4.HRApplication.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.se347.nhom4.HRApplication.domain.responseDTO.ResWeeklySummary;
import com.se347.nhom4.HRApplication.service.AttendanceService;
import com.se347.nhom4.HRApplication.util.annotation.ApiMessage;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/attendances")
@RequiredArgsConstructor
public class AttendanceControllerV2 {

    private final AttendanceService attendanceService;

    /**
     * Lấy weekly summary cho tất cả nhân viên active
     * Thay thế 3 API calls (employees/active + work-schedules + attendances) thành
     * 1 call
     * 
     * GET
     * /api/v2/attendances/weekly-summary?startDate=2025-12-23&endDate=2025-12-29
     */
    @GetMapping("/weekly-summary")
    @ApiMessage("Lấy tổng hợp chấm công theo tuần cho tất cả nhân viên")
    public ResponseEntity<ResWeeklySummary> getWeeklySummary(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        System.out.println(">>>ATTENDANCE V2: Fetching weekly summary from " + startDate + " to " + endDate);
        ResWeeklySummary summary = attendanceService.getWeeklySummary(startDate, endDate);
        System.out.println(">>>ATTENDANCE V2: Successfully fetched weekly summary for " +
                summary.getEmployees().size() + " employees");

        return ResponseEntity.ok(summary);
    }
}
