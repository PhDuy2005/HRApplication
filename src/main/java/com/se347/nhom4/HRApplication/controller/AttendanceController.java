package com.se347.nhom4.HRApplication.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqAdminCheckIn;
import com.se347.nhom4.HRApplication.domain.requestDTO.ReqAdminCheckOut;
import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCheckIn;
import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCheckOut;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResAttendance;
import com.se347.nhom4.HRApplication.service.AttendanceService;
import com.se347.nhom4.HRApplication.util.annotation.ApiMessage;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Check-in theo workScheduleId + GPS
     * Ví dụ: POST /api/attendances/check-in?employeeId=1
     */
    @PostMapping("/check-in")
    @ApiMessage("Chấm công vào ca")
    public ResAttendance checkIn(
            @RequestParam("employeeId") Long employeeId,
            @RequestBody ReqCheckIn req) {
        System.out.println(">>>ATTENDANCE MODULE: Check-in attempt for employeeId: " + employeeId);
        return attendanceService.checkIn(employeeId, req);
    }

    /**
     * Check-out theo workScheduleId + GPS
     * Ví dụ: POST /api/attendances/check-out?employeeId=1
     */
    @PostMapping("/check-out")
    @ApiMessage("Chấm công ra ca")
    public ResAttendance checkOut(
            @RequestParam("employeeId") Long employeeId,
            @RequestBody ReqCheckOut req) {
        System.out.println(">>>ATTENDANCE MODULE: Check-out attempt for employeeId: " + employeeId);
        return attendanceService.checkOut(employeeId, req);
    }

    /**
     * Lấy chi tiết attendance theo id
     * Ví dụ: GET /api/attendances/10
     */
    @GetMapping("/{id}")
    @ApiMessage("Lấy chi tiết chấm công theo ID")
    public ResAttendance getById(@PathVariable("id") Long id) {
        System.out.println(">>>ATTENDANCE MODULE: Fetching attendance details for ID: " + id);
        return attendanceService.getById(id);
    }

    /**
     * Lấy danh sách chấm công của bản thân theo khoảng ngày
     * Ví dụ: GET /api/attendances/my?employeeId=1&from=2025-12-01&to=2025-12-31
     */
    @GetMapping("/my")
    @ApiMessage("Lấy danh sách chấm công của bản thân theo khoảng ngày")
    public List<ResAttendance> myAttendances(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        System.out.println(">>>ATTENDANCE MODULE: Fetching attendances for employeeId: " + employeeId +
                " from: " + from + " to: " + to);
        return attendanceService.getMyAttendances(employeeId, from, to);
    }

    /**
     * Lấy thông tin attendance theo workScheduleId của bản thân
     * Ví dụ: GET /api/attendances/my/123?employeeId=1
     */
    @GetMapping("/my/{workScheduleId}")
    @ApiMessage("Lấy thông tin chấm công theo workScheduleId của bản thân")
    public ResAttendance getAttendanceByWorkSchedule(
            @PathVariable("workScheduleId") Long workScheduleId,
            @RequestParam("employeeId") Long employeeId) {
        System.out.println(">>>ATTENDANCE MODULE: Fetching attendance for employeeId: " + employeeId +
                " and workScheduleId: " + workScheduleId);
        return attendanceService.getAttendanceByWorkSchedule(workScheduleId, employeeId);
    }

    // ====================================
    // ADMIN APIs - Quản lý chấm công thủ công
    // ====================================

    /**
     * Admin tạo check-in thủ công
     * Không yêu cầu GPS, không ràng buộc thời gian
     * Ví dụ: POST /api/v1/attendances/admin/checkin/123
     */
    @PostMapping("/admin/checkin/{workScheduleId}")
    @ApiMessage("Admin tạo check-in thủ công")
    public ResAttendance adminCheckIn(
            @PathVariable("workScheduleId") Long workScheduleId,
            @Valid @RequestBody ReqAdminCheckIn req) {
        System.out.println(">>>ATTENDANCE MODULE: Admin creating check-in for workScheduleId: " + workScheduleId);
        return attendanceService.adminCheckIn(workScheduleId, req);
    }

    /**
     * Admin tạo check-out thủ công
     * Không yêu cầu GPS, không ràng buộc thời gian
     * Ví dụ: POST /api/v1/attendances/admin/checkout/123
     */
    @PostMapping("/admin/checkout/{workScheduleId}")
    @ApiMessage("Admin tạo check-out thủ công")
    public ResAttendance adminCheckOut(
            @PathVariable("workScheduleId") Long workScheduleId,
            @Valid @RequestBody ReqAdminCheckOut req) {
        System.out.println(">>>ATTENDANCE MODULE: Admin creating check-out for workScheduleId: " + workScheduleId);
        return attendanceService.adminCheckOut(workScheduleId, req);
    }

    /**
     * Admin cập nhật check-in thủ công
     * Không yêu cầu GPS, không ràng buộc thời gian
     * Ví dụ: PUT /api/v1/attendances/admin/checkin/123
     */
    @PutMapping("/admin/checkin/{workScheduleId}")
    @ApiMessage("Admin cập nhật check-in thủ công")
    public ResAttendance adminUpdateCheckIn(
            @PathVariable("workScheduleId") Long workScheduleId,
            @Valid @RequestBody ReqAdminCheckIn req) {
        System.out.println(">>>ATTENDANCE MODULE: Admin updating check-in for workScheduleId: " + workScheduleId);
        return attendanceService.adminUpdateCheckIn(workScheduleId, req);
    }

    /**
     * Admin cập nhật check-out thủ công
     * Không yêu cầu GPS, không ràng buộc thời gian
     * Ví dụ: PUT /api/v1/attendances/admin/checkout/123
     */
    @PutMapping("/admin/checkout/{workScheduleId}")
    @ApiMessage("Admin cập nhật check-out thủ công")
    public ResAttendance adminUpdateCheckOut(
            @PathVariable("workScheduleId") Long workScheduleId,
            @Valid @RequestBody ReqAdminCheckOut req) {
        System.out.println(">>>ATTENDANCE MODULE: Admin updating check-out for workScheduleId: " + workScheduleId);
        return attendanceService.adminUpdateCheckOut(workScheduleId, req);
    }
}
