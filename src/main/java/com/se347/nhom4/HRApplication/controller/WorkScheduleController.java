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
                System.out.println(">>>WORK-SCHEDULE MODULE: Attemping to Fetch all work schedules");
                List<ResWorkSchedule> response = workScheduleService.findAll().stream()
                                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                                .toList();
                System.out.println(">>>WORK-SCHEDULE MODULE: Successfully fetched all work schedules");
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        @ApiMessage("Lấy thông tin lịch làm việc theo ID")
        public ResponseEntity<ResWorkSchedule> getWorkScheduleById(@PathVariable("id") Long id) {
                System.out.println(">>>WORK-SCHEDULE MODULE: Attemping to Fetch work schedule by ID: " + id);
                ResponseEntity<ResWorkSchedule> res = workScheduleService.findById(id)
                                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
                System.out.println(">>>WORK-SCHEDULE MODULE: Successfully fetched work schedule by ID: " + id);
                return res;
        }

        @GetMapping("/employee/{employeeId}")
        @ApiMessage("Lấy danh sách lịch làm việc theo nhân viên")
        public ResponseEntity<List<ResWorkSchedule>> getWorkSchedulesByEmployeeId(
                        @PathVariable("employeeId") Long employeeId) {
                System.out
                                .println(">>>WORK-SCHEDULE MODULE: Attemping to Fetch work schedules by Employee ID: "
                                                + employeeId);
                List<ResWorkSchedule> response = workScheduleService.findByEmployeeId(employeeId).stream()
                                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                                .toList();
                System.out
                                .println(">>>WORK-SCHEDULE MODULE: Successfully fetched work schedules by Employee ID: "
                                                + employeeId);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/shift/{shiftId}")
        @ApiMessage("Lấy danh sách lịch làm việc theo ca làm việc")
        public ResponseEntity<List<ResWorkSchedule>> getWorkSchedulesByShiftId(@PathVariable("shiftId") Long shiftId) {
                System.out.println(
                                ">>>WORK-SCHEDULE MODULE: Attemping to Fetch work schedules by Shift ID: " + shiftId);
                List<ResWorkSchedule> response = workScheduleService.findByShiftId(shiftId).stream()
                                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                                .toList();
                System.out.println(
                                ">>>WORK-SCHEDULE MODULE: Successfully fetched work schedules by Shift ID: " + shiftId);
                return ResponseEntity.ok(response);
        }

        /**
         * Lấy danh sách lịch làm việc theo ca làm việc và khoảng thời gian cụ thể.
         * Endpoint này cho phép truy vấn tất cả các lịch làm việc được gán cho một ca
         * cụ thể
         * trong khoảng thời gian từ ngày bắt đầu đến ngày kết thúc.
         *
         * @param shiftId   ID của ca làm việc cần tìm lịch làm việc
         * @param startDate Ngày bắt đầu của khoảng thời gian (định dạng: yyyy-MM-dd)
         * @param endDate   Ngày kết thúc của khoảng thời gian (định dạng: yyyy-MM-dd)
         * @return ResponseEntity chứa ResShiftListWorkSchedule với danh sách lịch làm
         *         việc
         *         của ca đã chỉ định trong khoảng thời gian
         *
         * @throws IllegalArgumentException nếu shiftId không tồn tại hoặc khoảng thời
         *                                  gian không hợp lệ
         *
         *                                  <h3>Ví dụ sử dụng API:</h3>
         * 
         *                                  GET
         *                                  /api/v1/work-schedules/shift/1/date-range?startDate=2025-12-01&endDate=2025-12-31
         */
        @GetMapping("/shift/{shiftId}/date-range")
        @ApiMessage("Lấy lịch làm việc theo ca và khoảng thời gian")
        public ResponseEntity<ResShiftListWorkSchedule> getWorkSchedulesByShiftIdAndDateRange(
                        @PathVariable("shiftId") Long shiftId,
                        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
                System.out.println(
                                ">>>WORK-SCHEDULE MODULE: Attemping to Fetch work schedules by Shift ID and Date Range: "
                                                + shiftId + ", " + startDate + " to " + endDate);
                List<ResWorkSchedule> response = workScheduleService
                                .findByShiftIdAndWorkDateBetween(shiftId, startDate, endDate).stream()
                                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                                .toList();
                System.out.println(
                                ">>>WORK-SCHEDULE MODULE: Successfully fetched work schedules by Shift ID and Date Range: "
                                                + shiftId + ", " + startDate + " to " + endDate);
                ResShiftListWorkSchedule shiftListWorkSchedule = new ResShiftListWorkSchedule(response);
                return ResponseEntity.ok(shiftListWorkSchedule);
        }

        @GetMapping("/date/{workDate}")
        @ApiMessage("Lấy danh sách lịch làm việc theo ngày")
        public ResponseEntity<List<ResWorkSchedule>> getWorkSchedulesByWorkDate(
                        @PathVariable("workDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
                System.out.println(
                                ">>>WORK-SCHEDULE MODULE: Attemping to Fetch work schedules by Work Date: " + workDate);
                List<ResWorkSchedule> response = workScheduleService.findByWorkDate(workDate).stream()
                                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                                .toList();
                System.out.println(">>>WORK-SCHEDULE MODULE: Successfully fetched work schedules by Work Date: "
                                + workDate);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/employee/{employeeId}/date/{workDate}")
        @ApiMessage("Lấy lịch làm việc của nhân viên theo ngày")
        public ResponseEntity<List<ResWorkSchedule>> getWorkSchedulesByEmployeeIdAndWorkDate(
                        @PathVariable("employeeId") Long employeeId,
                        @PathVariable("workDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
                System.out.println(
                                ">>>WORK-SCHEDULE MODULE: Attemping to Fetch work schedules by Employee ID and Work Date: "
                                                + employeeId + ", " + workDate);
                List<ResWorkSchedule> response = workScheduleService.findByEmployeeIdAndWorkDate(employeeId, workDate)
                                .stream()
                                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                                .toList();
                System.out.println(
                                ">>>WORK-SCHEDULE MODULE: Successfully fetched work schedules by Employee ID and Work Date: "
                                                + employeeId + ", " + workDate);
                return ResponseEntity.ok(response);
        }

        /**
         * Lấy danh sách lịch làm việc của một nhân viên cụ thể trong khoảng thời gian.
         * Endpoint này trả về thông tin chi tiết về tất cả các ca làm việc được gán
         * cho nhân viên trong khoảng ngày từ startDate đến endDate.
         *
         * @param employeeId ID của nhân viên cần lấy lịch làm việc
         * @param startDate  Ngày bắt đầu của khoảng thời gian (định dạng: yyyy-MM-dd)
         * @param endDate    Ngày kết thúc của khoảng thời gian (định dạng: yyyy-MM-dd)
         * @return ResponseEntity chứa ResEmpListWorkSchedule với thông tin nhân viên
         *         và danh sách lịch làm việc trong khoảng thời gian
         *
         * @throws IllegalArgumentException nếu employeeId không tồn tại hoặc khoảng
         *                                  thời gian không hợp lệ
         *
         *                                  <h3>Ví dụ sử dụng API:</h3>
         * 
         *                                  <pre>
         * GET /api/v1/work-schedules/employee/5/date-range?startDate=2025-12-01&endDate=2025-12-31
         *                                  </pre>
         */
        @GetMapping("/employee/{employeeId}/date-range")
        @ApiMessage("Lấy lịch làm việc của nhân viên theo khoảng thời gian")
        public ResponseEntity<ResEmpListWorkSchedule> getWorkSchedulesByEmployeeIdAndDateRange(
                        @PathVariable("employeeId") Long employeeId,
                        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
                System.out.println(
                                ">>>WORK-SCHEDULE MODULE: Attemping to Fetch work schedules by Employee ID and Date Range: "
                                                + employeeId + ", " + startDate + " to " + endDate);
                List<ResWorkSchedule> workSchedules = workScheduleService
                                .findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate).stream()
                                .map(ws -> new ResWorkSchedule(ws, dayTypeService))
                                .toList();
                System.out.println(
                                ">>>WORK-SCHEDULE MODULE: Successfully fetched work schedules by Employee ID and Date Range: "
                                                + employeeId + ", " + startDate + " to " + endDate);
                ResEmpListWorkSchedule response = new ResEmpListWorkSchedule(workSchedules.get(0).getEmployee(),
                                startDate,
                                endDate, workSchedules);
                return ResponseEntity.ok(response);
        }

        @PostMapping
        @ApiMessage("Tạo mới lịch làm việc")
        public ResponseEntity<ResWorkSchedule> createWorkSchedule(@RequestBody WorkSchedule workSchedule) {
                System.out.println(">>>WORK-SCHEDULE MODULE: Attemping to Create new work schedule: " + workSchedule);
                WorkSchedule createdWorkSchedule = workScheduleService.createWorkSchedule(workSchedule);
                System.out.println(">>>WORK-SCHEDULE MODULE: Successfully created new work schedule: "
                                + createdWorkSchedule);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ResWorkSchedule(createdWorkSchedule, dayTypeService));
        }

        @PutMapping("/{id}")
        @ApiMessage("Cập nhật lịch làm việc")
        public ResponseEntity<ResWorkSchedule> updateWorkSchedule(@PathVariable("id") Long id,
                        @RequestBody WorkSchedule workSchedule) {
                System.out.println(">>>WORK-SCHEDULE MODULE: Attemping to Update work schedule ID: " + id);
                WorkSchedule updatedWorkSchedule = workScheduleService.updateWorkSchedule(id, workSchedule);
                System.out.println(">>>WORK-SCHEDULE MODULE: Successfully updated work schedule ID: " + id);
                return ResponseEntity.ok(new ResWorkSchedule(updatedWorkSchedule, dayTypeService));
        }

        @DeleteMapping("/{id}")
        @ApiMessage("Xóa lịch làm việc")
        public ResponseEntity<Void> deleteWorkSchedule(@PathVariable("id") Long id) {
                workScheduleService.deleteById(id);
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/exists")
        @ApiMessage("Kiểm tra lịch làm việc có tồn tại")
        public ResponseEntity<Boolean> checkWorkScheduleExists(
                        @RequestParam("employeeId") Long employeeId,
                        @RequestParam("shiftId") Long shiftId,
                        @RequestParam("workDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
                System.out.println(
                                ">>>WORK-SCHEDULE MODULE: Attemping to Check existence of work schedule for Employee ID: "
                                                + employeeId + ", Shift ID: " + shiftId + ", Work Date: " + workDate);
                boolean exists = workScheduleService.existsByEmployeeIdAndShiftIdAndWorkDate(employeeId, shiftId,
                                workDate);
                return ResponseEntity.ok(exists);
        }
}