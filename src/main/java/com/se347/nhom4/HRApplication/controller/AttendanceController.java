package com.se347.nhom4.HRApplication.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCheckIn;
import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCheckOut;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResAttendance;
import com.se347.nhom4.HRApplication.service.AttendanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Check-in theo workScheduleId + GPS
     * Ví dụ: POST /api/attendances/check-in?employeeId=1
     */
    @PostMapping("/check-in")
    public ResAttendance checkIn(
            @RequestParam Long employeeId,
            @RequestBody ReqCheckIn req
    ) {
        return attendanceService.checkIn(employeeId, req);
    }

    /**
     * Check-out theo workScheduleId + GPS
     * Ví dụ: POST /api/attendances/check-out?employeeId=1
     */
    @PostMapping("/check-out")
    public ResAttendance checkOut(
            @RequestParam Long employeeId,
            @RequestBody ReqCheckOut req
    ) {
        return attendanceService.checkOut(employeeId, req);
    }

    /**
     * Lấy chi tiết attendance theo id
     * Ví dụ: GET /api/attendances/10
     */
    @GetMapping("/{id}")
    public ResAttendance getById(@PathVariable Long id) {
        return attendanceService.getById(id);
    }

    /**
     * Lấy danh sách chấm công của bản thân theo khoảng ngày
     * Ví dụ: GET /api/attendances/my?employeeId=1&from=2025-12-01&to=2025-12-31
     */
    @GetMapping("/my")
    public List<ResAttendance> myAttendances(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return attendanceService.getMyAttendances(employeeId, from, to);
    }
}
