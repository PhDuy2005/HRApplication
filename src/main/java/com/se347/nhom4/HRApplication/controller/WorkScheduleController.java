package com.se347.nhom4.HRApplication.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.se347.nhom4.HRApplication.domain.responseDTO.ResEmpListWorkSchedule;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResShiftListWorkSchedule;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResWorkSchedule;
import com.se347.nhom4.HRApplication.domain.table.WorkSchedule;
import com.se347.nhom4.HRApplication.service.DayTypeService;
import com.se347.nhom4.HRApplication.service.WorkScheduleService;
import com.se347.nhom4.HRApplication.util.annotation.ApiMessage;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/work-schedules")
@RequiredArgsConstructor
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;
    private final DayTypeService dayTypeService;

    @GetMapping
    @ApiMessage("Lấy danh sách tất cả lịch làm việc")
    public ResponseEntity<List<ResWorkSchedule>> getAllWorkSchedules() {
        List<ResWorkSchedule> response = workScheduleService.findAll().stream()
                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy thông tin lịch làm việc theo ID")
    public ResponseEntity<ResWorkSchedule> getWorkScheduleById(@PathVariable Long id) {
        return workScheduleService.findById(id)
                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    @ApiMessage("Lấy danh sách lịch làm việc theo nhân viên")
    public ResponseEntity<List<ResWorkSchedule>> getWorkSchedulesByEmployeeId(@PathVariable Long employeeId) {
        List<ResWorkSchedule> response = workScheduleService.findByEmployeeId(employeeId).stream()
                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shift/{shiftId}")
    @ApiMessage("Lấy danh sách lịch làm việc theo ca làm việc")
    public ResponseEntity<List<ResWorkSchedule>> getWorkSchedulesByShiftId(@PathVariable Long shiftId) {
        List<ResWorkSchedule> response = workScheduleService.findByShiftId(shiftId).stream()
                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shift/{shiftId}/date-range")
    @ApiMessage("Lấy lịch làm việc theo ca và khoảng thời gian")
    public ResponseEntity<ResShiftListWorkSchedule> getWorkSchedulesByShiftIdAndDateRange(
            @PathVariable("shiftId") Long shiftId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ResWorkSchedule> response = workScheduleService
                .findByShiftIdAndWorkDateBetween(shiftId, startDate, endDate).stream()
                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                .toList();
        ResShiftListWorkSchedule shiftListWorkSchedule = new ResShiftListWorkSchedule(response);
        return ResponseEntity.ok(shiftListWorkSchedule);
    }

    @GetMapping("/date/{workDate}")
    @ApiMessage("Lấy danh sách lịch làm việc theo ngày")
    public ResponseEntity<List<ResWorkSchedule>> getWorkSchedulesByWorkDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
        List<ResWorkSchedule> response = workScheduleService.findByWorkDate(workDate).stream()
                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/{employeeId}/date/{workDate}")
    @ApiMessage("Lấy lịch làm việc của nhân viên theo ngày")
    public ResponseEntity<List<ResWorkSchedule>> getWorkSchedulesByEmployeeIdAndWorkDate(
            @PathVariable Long employeeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
        List<ResWorkSchedule> response = workScheduleService.findByEmployeeIdAndWorkDate(employeeId, workDate).stream()
                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/{employeeId}/date-range")
    @ApiMessage("Lấy lịch làm việc của nhân viên theo khoảng thời gian")
    public ResponseEntity<ResEmpListWorkSchedule> getWorkSchedulesByEmployeeIdAndDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ResWorkSchedule> workSchedules = workScheduleService
                .findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate).stream()
                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                .toList();
        ResEmpListWorkSchedule response = new ResEmpListWorkSchedule(workSchedules.get(0).getEmployee(), startDate,
                endDate, workSchedules);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @ApiMessage("Tạo mới lịch làm việc")
    public ResponseEntity<ResWorkSchedule> createWorkSchedule(@RequestBody WorkSchedule workSchedule) {
        WorkSchedule createdWorkSchedule = workScheduleService.createWorkSchedule(workSchedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResWorkSchedule(createdWorkSchedule, dayTypeService));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật lịch làm việc")
    public ResponseEntity<ResWorkSchedule> updateWorkSchedule(@PathVariable Long id,
            @RequestBody WorkSchedule workSchedule) {
        WorkSchedule updatedWorkSchedule = workScheduleService.updateWorkSchedule(id, workSchedule);
        return ResponseEntity.ok(new ResWorkSchedule(updatedWorkSchedule, dayTypeService));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa lịch làm việc")
    public ResponseEntity<Void> deleteWorkSchedule(@PathVariable Long id) {
        workScheduleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    @ApiMessage("Kiểm tra lịch làm việc có tồn tại")
    public ResponseEntity<Boolean> checkWorkScheduleExists(
            @RequestParam Long employeeId,
            @RequestParam Long shiftId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
        boolean exists = workScheduleService.existsByEmployeeIdAndShiftIdAndWorkDate(employeeId, shiftId, workDate);
        return ResponseEntity.ok(exists);
    }
}