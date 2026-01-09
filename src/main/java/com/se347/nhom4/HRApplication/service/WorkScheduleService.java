package com.se347.nhom4.HRApplication.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService.Work;
import org.springframework.stereotype.Service;

import com.se347.nhom4.HRApplication.domain.responseDTO.ResWeeklyByShift;
import com.se347.nhom4.HRApplication.domain.table.Attendance;
import com.se347.nhom4.HRApplication.domain.table.Shift;
import com.se347.nhom4.HRApplication.domain.table.WorkSchedule;
import com.se347.nhom4.HRApplication.domain.table.WorkSite;
import com.se347.nhom4.HRApplication.repository.AttendanceRepository;
import com.se347.nhom4.HRApplication.repository.EmployeeRepository;
import com.se347.nhom4.HRApplication.repository.PayrollRepository;
import com.se347.nhom4.HRApplication.repository.ShiftRepository;
import com.se347.nhom4.HRApplication.repository.WorkScheduleRepository;
import com.se347.nhom4.HRApplication.repository.WorkSiteRepository;
import com.se347.nhom4.HRApplication.util.enums.PayrollStatusEnum;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final WorkSiteRepository workSiteRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;

    // gán WorkSite cho WorkSchedule (phục vụ GPS chấm công)
    @Transactional
    public WorkSchedule assignWorkSite(Long workScheduleId, Long workSiteId) {
        WorkSchedule ws = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new IllegalArgumentException("WorkSchedule not found with id: " + workScheduleId));

        WorkSite site = workSiteRepository.findById(workSiteId)
                .orElseThrow(() -> new IllegalArgumentException("WorkSite not found with id: " + workSiteId));

        if (site.getActive() != null && !site.getActive()) {
            throw new IllegalArgumentException("WorkSite đang inactive, không thể gán");
        }

        ws.setWorkSite(site);
        return workScheduleRepository.save(ws);
    }

    // bỏ WorkSite khỏi WorkSchedule
    @Transactional
    public WorkSchedule removeWorkSite(Long workScheduleId) {
        WorkSchedule ws = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new IllegalArgumentException("WorkSchedule not found with id: " + workScheduleId));

        ws.setWorkSite(null);
        return workScheduleRepository.save(ws);
    }

    /**
     * Get all work schedules.
     */
    public List<WorkSchedule> findAll() {
        System.out.println(">>>WORK-SCHEDULE MODULE: Attemping to Fetch all work schedules in WS Service");
        return workScheduleRepository.findAll();
    }

    /**
     * Find work schedule by ID.
     */
    public Optional<WorkSchedule> findById(Long id) {
        System.out.println(">>>WORK-SCHEDULE MODULE: Attemping to Fetch work schedule by ID in WS Service: " + id);
        return workScheduleRepository.findById(id);
    }

    /**
     * Get work schedules by employee ID.
     */
    public List<WorkSchedule> findByEmployeeId(Long employeeId) {
        return workScheduleRepository.findByEmployeeId(employeeId);
    }

    /**
     * Get work schedules by shift ID.
     */
    public List<WorkSchedule> findByShiftId(Long shiftId) {
        return workScheduleRepository.findByShiftId(shiftId);
    }

    /**
     * Get work schedules by work date.
     */
    public List<WorkSchedule> findByWorkDate(LocalDate workDate) {
        return workScheduleRepository.findByWorkDate(workDate);
    }

    /**
     * Get work schedules by employee ID and work date.
     */
    public List<WorkSchedule> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate) {
        return workScheduleRepository.findByEmployeeIdAndWorkDate(employeeId, workDate);
    }

    /**
     * Get active work schedules by employee ID and date range.
     */
    public List<WorkSchedule> findByEmployeeIdAndWorkDateBetween(Long employeeId, LocalDate startDate,
            LocalDate endDate) {
        List<WorkSchedule> workSchedules = workScheduleRepository.findByEmployeeIdAndWorkDateBetween(employeeId,
                startDate, endDate);
        List<WorkSchedule> activeWorkSchedules = new ArrayList<>();
        for (WorkSchedule ws : workSchedules) {
            if (ws.getShift().getIsActive() == true) {
                activeWorkSchedules.add(ws);
            }
        }
        return activeWorkSchedules;
    }

    /**
     * Get active work schedules by shift ID and date range.
     */
    public List<WorkSchedule> findByShiftIdAndWorkDateBetween(Long shiftId, LocalDate startDate, LocalDate endDate) {
        List<WorkSchedule> workSchedules = workScheduleRepository.findByShiftIdAndWorkDateBetween(shiftId, startDate,
                endDate);
        List<WorkSchedule> activeWorkSchedules = new ArrayList<>();
        for (WorkSchedule ws : workSchedules) {
            if (ws.getShift().getIsActive() == true) {
                activeWorkSchedules.add(ws);
            }
        }
        return activeWorkSchedules;
    }

    /**
     * Create new work schedule.
     */
    public WorkSchedule createWorkSchedule(WorkSchedule workSchedule) {
        assertPayrollOpen(
                workSchedule.getEmployee() != null ? workSchedule.getEmployee().getId() : null,
                workSchedule.getWorkDate());

        if (workSchedule.getEmployee() == null || workSchedule.getEmployee().getId() == null) {
            throw new IllegalArgumentException("employee.id is required");
        }
        if (workSchedule.getShift() == null || workSchedule.getShift().getId() == null) {
            throw new IllegalArgumentException("shift.id is required");
        }

        var emp = employeeRepository.findById(workSchedule.getEmployee().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Employee not found: " + workSchedule.getEmployee().getId()));
        var shift = shiftRepository.findById(workSchedule.getShift().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Shift not found: " + workSchedule.getShift().getId()));

        workSchedule.setEmployee(emp);
        workSchedule.setShift(shift);

        assertNoOverlappingShift(emp.getId(), workSchedule.getWorkDate(), shift, null);

        // kiểm tra worksite id
        if (workSchedule.getWorkSite() == null || workSchedule.getWorkSite().getId() == null)
            throw new IllegalArgumentException("workSite.id is required");
        var site = workSiteRepository.findById(workSchedule.getWorkSite().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "WorkSite not found: " + workSchedule.getWorkSite().getId()));
        if (site.getActive() != null && !site.getActive())
            throw new IllegalArgumentException("WorkSite inactive");
        workSchedule.setWorkSite(site);

        return workScheduleRepository.save(workSchedule);
    }

    /**
     * Update work schedule.
     */
    @Transactional
    public WorkSchedule updateWorkSchedule(Long id, WorkSchedule req) {
        WorkSchedule existing = workScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkSchedule not found: " + id));

        // Khóa chỉnh sửa nếu payroll của tháng đã chốt (APPROVED/PAID)
        Long oldEmployeeId = existing.getEmployee() != null ? existing.getEmployee().getId() : null;
        LocalDate oldWorkDate = existing.getWorkDate();
        assertPayrollOpen(oldEmployeeId, oldWorkDate);
        assertNoCheckedInAttendance(existing.getId());

        // 1) WorkDate
        if (req.getWorkDate() != null) {
            existing.setWorkDate(req.getWorkDate());
        }

        // hoặc:
        // if (req.getDescription() != null)
        // existing.setDescription(req.getDescription());

        // 3) Employee (nếu cho phép đổi nhân viên)
        if (req.getEmployee() != null && req.getEmployee().getId() != null) {
            var emp = employeeRepository.findById(req.getEmployee().getId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Employee not found: " + req.getEmployee().getId()));
            // nếu có active:
            // if (Boolean.FALSE.equals(emp.getActive())) throw new
            // IllegalArgumentException("Employee inactive");
            existing.setEmployee(emp);
        }

        // 4) Shift (nếu cho phép đổi ca)
        if (req.getShift() != null && req.getShift().getId() != null) {
            var shift = shiftRepository.findById(req.getShift().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + req.getShift().getId()));
            // nếu shift có active:
            // if (Boolean.FALSE.equals(shift.getIsActive())) throw new
            // IllegalArgumentException("Shift inactive");
            existing.setShift(shift);
        }

        // 5) WorkSite (đoạn bạn đã làm, giữ lại)
        if (req.getWorkSite() != null && req.getWorkSite().getId() != null) {
            var site = workSiteRepository.findById(req.getWorkSite().getId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("WorkSite not found: " + req.getWorkSite().getId()));
            if (Boolean.FALSE.equals(site.getActive()))
                throw new IllegalArgumentException("WorkSite inactive");
            existing.setWorkSite(site);
        }

        // Nếu sau khi update đổi nhân viên hoặc ngày thì cũng khóa theo payroll mới
        Long newEmployeeId = existing.getEmployee() != null ? existing.getEmployee().getId() : null;
        LocalDate newWorkDate = existing.getWorkDate();
        if ((oldEmployeeId != null && !oldEmployeeId.equals(newEmployeeId))
                || (oldWorkDate != null && !oldWorkDate.equals(newWorkDate))) {
            assertPayrollOpen(newEmployeeId, newWorkDate);
        }

        assertNoOverlappingShift(
                existing.getEmployee() != null ? existing.getEmployee().getId() : null,
                existing.getWorkDate(),
                existing.getShift(),
                existing.getId());

        return workScheduleRepository.save(existing);
    }

    /**
     * Delete work schedule by ID.
     */
    @Transactional
    public void deleteById(Long id) {
        WorkSchedule existing = workScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkSchedule not found: " + id));

        assertPayrollOpen(
                existing.getEmployee() != null ? existing.getEmployee().getId() : null,
                existing.getWorkDate());
        assertNoCheckedInAttendance(existing.getId());

        workScheduleRepository.delete(existing);
    }

    /**
     * Check if work schedule exists by employee, shift and work date.
     */
    public boolean existsByEmployeeIdAndShiftIdAndWorkDate(Long employeeId, Long shiftId, LocalDate workDate) {
        return workScheduleRepository.existsByEmployeeIdAndShiftIdAndWorkDate(employeeId, shiftId, workDate);
    }

    private void assertPayrollOpen(Long employeeId, LocalDate workDate) {
        if (employeeId == null || workDate == null) {
            return;
        }

        int month = workDate.getMonthValue();
        int year = workDate.getYear();
        payrollRepository.findByEmployee_IdAndMonthAndYear(employeeId, month, year)
                .ifPresent(payroll -> {
                    PayrollStatusEnum status = payroll.getStatus();
                    if (status == PayrollStatusEnum.APPROVED || status == PayrollStatusEnum.PAID) {
                        throw new IllegalStateException(
                                "Bảng lương tháng " + month + "/" + year + " của nhân viên này đã chốt, không thể thay đổi lịch làm");
                    }
                });
    }

    private void assertNoCheckedInAttendance(Long workScheduleId) {
        if (workScheduleId == null) {
            return;
        }
        attendanceRepository.findByWorkSchedule_Id(workScheduleId).ifPresent(attendance -> {
            if (attendance.getCheckIn() != null) {
                throw new IllegalStateException(
                        "Ca đã được check-in, không thể thay đổi lịch làm");
            }
        });
    }

    private void assertNoOverlappingShift(Long employeeId, LocalDate workDate, Shift shift, Long excludeId) {
        if (employeeId == null || workDate == null || shift == null) {
            return;
        }

        LocalDateTime start = workDate.atTime(shift.getStartTime());
        LocalDateTime end = workDate.atTime(shift.getEndTime());
        if (!shift.getEndTime().isAfter(shift.getStartTime())) {
            end = workDate.plusDays(1).atTime(shift.getEndTime());
        }

        List<WorkSchedule> existingSchedules = workScheduleRepository
                .findByEmployeeIdAndWorkDate(employeeId, workDate);

        for (WorkSchedule ws : existingSchedules) {
            if (excludeId != null && excludeId.equals(ws.getId())) {
                continue;
            }

            Shift otherShift = ws.getShift();
            if (otherShift == null) {
                continue;
            }

            LocalDateTime otherStart = workDate.atTime(otherShift.getStartTime());
            LocalDateTime otherEnd = workDate.atTime(otherShift.getEndTime());
            if (!otherShift.getEndTime().isAfter(otherShift.getStartTime())) {
                otherEnd = workDate.plusDays(1).atTime(otherShift.getEndTime());
            }

            if (start.isBefore(otherEnd) && otherStart.isBefore(end)) {
                throw new IllegalArgumentException("Nhân viên đã có ca trùng giờ trong ngày");
            }
        }
    }

    /**
     * Get weekly work schedules grouped by shift with attendance data.
     * Optimized to reduce 206 API calls to 1 call.
     * 
     * @param startDate Start date of the week
     * @param endDate   End date of the week
     * @return Weekly summary by shift
     */
    public ResWeeklyByShift getWeeklyByShift(LocalDate startDate, LocalDate endDate) {
        // Step 1: Fetch all active shifts
        var shifts = shiftRepository.findByIsActiveTrue();

        // Step 2: Fetch all work schedules in date range
        var workSchedules = workScheduleRepository.findByWorkDateBetween(startDate, endDate);

        // Step 3: Fetch all attendances in date range
        var attendances = attendanceRepository.findByWorkDateBetween(startDate, endDate);

        // Build attendance map by work schedule ID for O(1) lookup
        Map<Long, Attendance> attendanceMap = attendances.stream()
                .collect(Collectors.toMap(
                        att -> att.getWorkSchedule().getId(),
                        att -> att,
                        (a1, a2) -> a1 // In case of duplicates, keep first
                ));

        // Group work schedules by shift ID and date
        Map<Long, Map<LocalDate, List<WorkSchedule>>> schedulesByShiftAndDate = workSchedules.stream()
                .collect(Collectors.groupingBy(
                        ws -> ws.getShift().getId(),
                        Collectors.groupingBy(WorkSchedule::getWorkDate)));

        // Build response
        List<ResWeeklyByShift.ShiftScheduleSummary> shiftSummaries = shifts.stream()
                .map(shift -> {
                    // Build shift info
                    ResWeeklyByShift.Shift shiftInfo = new ResWeeklyByShift.Shift(
                            shift.getId(),
                            shift.getName(),
                            shift.getStartTime(),
                            shift.getEndTime(),
                            shift.getStandardHours(),
                            shift.getColorCode());

                    // Build daily schedules for this shift
                    List<ResWeeklyByShift.DailySchedule> dailySchedules = new ArrayList<>();
                    LocalDate currentDate = startDate;

                    while (!currentDate.isAfter(endDate)) {
                        LocalDate date = currentDate;

                        // Get schedules for this shift and date
                        List<WorkSchedule> dateSchedules = schedulesByShiftAndDate
                                .getOrDefault(shift.getId(), Map.of())
                                .getOrDefault(date, List.of());

                        // Build schedule with attendance
                        List<ResWeeklyByShift.ScheduleWithAttendance> schedulesWithAttendance = dateSchedules.stream()
                                .map(ws -> {
                                    ResWeeklyByShift.Employee employee = new ResWeeklyByShift.Employee(
                                            ws.getEmployee().getId(),
                                            ws.getEmployee().getFullname(),
                                            ws.getEmployee().getEmail());

                                    ResWeeklyByShift.ScheduleWithAttendance schedule = new ResWeeklyByShift.ScheduleWithAttendance(
                                            ws.getId(),
                                            ws.getWorkDate(),
                                            employee);

                                    // Find attendance for this work schedule
                                    Attendance att = attendanceMap.get(ws.getId());
                                    if (att != null) {
                                        schedule.setAttendance(new ResWeeklyByShift.Attendance(
                                                att.getId(),
                                                att.getCheckIn(),
                                                att.getCheckOut(),
                                                att.getLateTime(),
                                                att.getEarlyLeave(),
                                                att.getOvertime()
                                        // att.getStatus() != null ? att.getStatus().name() : null
                                        ));
                                    }

                                    return schedule;
                                })
                                .collect(Collectors.toList());

                        dailySchedules.add(new ResWeeklyByShift.DailySchedule(date, schedulesWithAttendance));
                        currentDate = currentDate.plusDays(1);
                    }

                    return new ResWeeklyByShift.ShiftScheduleSummary(shiftInfo, dailySchedules);
                })
                .collect(Collectors.toList());

        return new ResWeeklyByShift(startDate, endDate, shiftSummaries);
    }
}
