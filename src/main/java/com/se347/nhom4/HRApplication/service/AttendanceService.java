package com.se347.nhom4.HRApplication.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqAdminCheckIn;
import com.se347.nhom4.HRApplication.domain.requestDTO.ReqAdminCheckOut;
import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCheckIn;
import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCheckOut;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResAttendance;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResWeeklySummary;
import com.se347.nhom4.HRApplication.domain.table.Attendance;
import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.domain.table.WorkSchedule;
import com.se347.nhom4.HRApplication.domain.table.WorkSite;
import com.se347.nhom4.HRApplication.repository.AttendanceRepository;
import com.se347.nhom4.HRApplication.repository.EmployeeRepository;
import com.se347.nhom4.HRApplication.repository.WorkScheduleRepository;
import com.se347.nhom4.HRApplication.util.enums.AttendanceStatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final EmployeeRepository employeeRepository;

    private static final ZoneId VN_TZ = ZoneId.of("Asia/Ho_Chi_Minh");

    // Cửa sổ check-in: cho phép check-in từ trước giờ vào ca 30 phút đến trước giờ
    // tan ca
    private static final int CHECK_IN_WINDOW_MINUTES_BEFORE = 30;
    private static final int CHECK_OUT_MAX_HOURS_AFTER = 6;

    // =========================
    // API
    // =========================

    @Transactional
    public ResAttendance checkIn(Long employeeId, ReqCheckIn req) {
        System.out.println(">>>ATTENDANCE MODULE: Check-in attempt for employeeId: " + employeeId +
                ", workScheduleId: " + req.getWorkScheduleId() + " in AttendanceService");
        System.out.println(">>>ATTENDANCE MODULE: finding WorkSchedule with id: " + req.getWorkScheduleId());
        WorkSchedule schedule = workScheduleRepository.findById(req.getWorkScheduleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "WorkSchedule not found with id: " + req.getWorkScheduleId()));

        validateOwner(schedule, employeeId);

        int distanceMeters = validateWorkSiteAndComputeDistance(
                schedule, req.getLat(), req.getLng(), req.getAccuracyMeters());

        System.out.println(">>>ATTENDANCE MODULE: finding Attendance for WorkSchedule id: " + schedule.getId());
        Attendance attendance = attendanceRepository.findByWorkSchedule_Id(schedule.getId())
                .orElseGet(() -> buildNewAttendance(schedule));

        if (attendance.getCheckIn() != null) {
            System.out.println(">>>ATTENDANCE MODULE: Check-in already done for WorkSchedule id: " + schedule.getId());
            throw new IllegalArgumentException("Ca này đã check-in rồi");
        }

        // Validation: Check-in window (từ trước giờ vào ca X phút đến trước giờ tan ca)
        validateCheckInWindow(schedule);

        // Validation: Không có 2 ca đang mở (nhưng cho phép nếu ca trước đã
        // AUTO_CLOSED)
        validateNoActiveShift(employeeId, schedule.getWorkDate(), schedule.getId());

        Instant now = Instant.now();
        attendance.setCheckIn(now);
        attendance.setStatus(AttendanceStatusEnum.ACTIVE);

        attendance.setCheckInLat(req.getLat());
        attendance.setCheckInLng(req.getLng());
        attendance.setCheckInAccuracyMeters(req.getAccuracyMeters());
        attendance.setCheckInDistanceMeters(distanceMeters);

        System.out.println(
                ">>>ATTENDANCE MODULE: Check-in recorded at " + now + " for WorkSchedule id: " + schedule.getId());

        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }

    @Transactional
    public ResAttendance checkOut(Long employeeId, ReqCheckOut req) {
        System.out.println(">>>ATTENDANCE MODULE: Check-out attempt for employeeId: " + employeeId +
                ", workScheduleId: " + req.getWorkScheduleId() + " in AttendanceService");
        System.out.println(">>>ATTENDANCE MODULE: finding WorkSchedule with id: " + req.getWorkScheduleId());
        WorkSchedule schedule = workScheduleRepository.findById(req.getWorkScheduleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "WorkSchedule not found with id: " + req.getWorkScheduleId()));

        validateOwner(schedule, employeeId);

        int distanceMeters = validateWorkSiteAndComputeDistance(
                schedule, req.getLat(), req.getLng(), req.getAccuracyMeters());

        System.out.println(">>>ATTENDANCE MODULE: finding Attendance for WorkSchedule id: " + schedule.getId());
        Attendance attendance = attendanceRepository.findByWorkSchedule_Id(schedule.getId())
                .orElseThrow(() -> new IllegalArgumentException("Chưa check-in nên không thể check-out"));

        if (attendance.getCheckIn() == null) {
            System.out.println(">>>ATTENDANCE MODULE: Check-out attempted without check-in for WorkSchedule id: "
                    + schedule.getId());
            throw new IllegalArgumentException("Chưa check-in nên không thể check-out");
        }
        if (attendance.getCheckOut() != null) {
            System.out.println(">>>ATTENDANCE MODULE: Check-out already done for WorkSchedule id: " + schedule.getId());
            throw new IllegalArgumentException("Ca này đã check-out rồi");
        }

        // Validation: Nếu đã AUTO_CLOSED thì không cho checkout
        if (attendance.getStatus() == AttendanceStatusEnum.AUTO_CLOSED) {
            throw new IllegalArgumentException("Ca này đã tự đóng sau 6 tiếng, không thể checkout");
        }

        Instant now = Instant.now();
        if (now.isBefore(attendance.getCheckIn())) {
            System.out.println(">>>ATTENDANCE MODULE: Invalid check-out time for WorkSchedule id: " + schedule.getId());
            throw new IllegalArgumentException("Thời gian check-out không hợp lệ");
        }

        // Validation: Checkout muộn tối đa 6 tiếng sau giờ tan ca
        validateCheckOutMaxDelay(schedule, now);

        attendance.setCheckOut(now);
        attendance.setStatus(AttendanceStatusEnum.COMPLETED);

        attendance.setCheckOutLat(req.getLat());
        attendance.setCheckOutLng(req.getLng());
        attendance.setCheckOutAccuracyMeters(req.getAccuracyMeters());
        attendance.setCheckOutDistanceMeters(distanceMeters);
        System.out.println(
                ">>>ATTENDANCE MODULE: Check-out recorded at " + now + " for WorkSchedule id: " + schedule.getId());

        // Tính totalWorkTime theo công thức payable minutes (clamp trong khung ca)
        calculatePayableWorkTime(attendance, schedule);
        System.out.println(">>>ATTENDANCE MODULE: Total work time calculated: " + attendance.getTotalWorkTime()
                + " minutes for WorkSchedule id: " + schedule.getId());

        applyLateAndOvertime(attendance, schedule);

        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ResAttendance getById(Long id) {
        Attendance a = attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attendance not found with id: " + id));
        return toResponse(a);
    }

    @Transactional(readOnly = true)
    public List<ResAttendance> getMyAttendances(Long employeeId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ");
        }

        return attendanceRepository.findByEmployee_IdAndWorkDateBetween(employeeId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResAttendance getAttendanceByWorkSchedule(Long workScheduleId, Long employeeId) {
        // Kiểm tra WorkSchedule có tồn tại không
        WorkSchedule schedule = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "WorkSchedule not found with id: " + workScheduleId));

        // Kiểm tra quyền sở hữu
        validateOwner(schedule, employeeId);

        // Lấy attendance theo workScheduleId
        Attendance attendance = attendanceRepository.findByWorkSchedule_Id(workScheduleId)
                .orElse(null);

        if (attendance == null) {
            // Nếu chưa có attendance thì tạo một attendance trống (chưa check-in)
            attendance = buildNewAttendance(schedule);
        }

        return toResponse(attendance);
    }

    // ======================
    // Validation + Helpers
    // ======================

    /**
     * Validation: Check-in window - chỉ cho check-in từ trước giờ vào ca X phút đến
     * trước giờ tan ca
     */
    private void validateCheckInWindow(WorkSchedule schedule) {
        if (schedule.getShift() == null) {
            return; // Không có shift thì không validate
        }

        LocalDate workDate = schedule.getWorkDate();
        LocalTime startTime = schedule.getShift().getStartTime();
        LocalTime endTime = schedule.getShift().getEndTime();

        if (workDate == null || startTime == null || endTime == null) {
            return;
        }

        ZonedDateTime scheduledStart = workDate.atTime(startTime).atZone(VN_TZ);
        ZonedDateTime windowStart = scheduledStart.minusMinutes(CHECK_IN_WINDOW_MINUTES_BEFORE);

        // Ca qua ngày: endTime < startTime (vd 22:00 -> 06:00)
        LocalDate endDate = endTime.isBefore(startTime) ? workDate.plusDays(1) : workDate;
        ZonedDateTime scheduledEnd = endDate.atTime(endTime).atZone(VN_TZ);

        ZonedDateTime now = Instant.now().atZone(VN_TZ);

        if (now.isBefore(windowStart)) {
            throw new IllegalArgumentException(
                    "Chưa đến cửa sổ check-in (cho phép từ " + CHECK_IN_WINDOW_MINUTES_BEFORE
                            + " phút trước giờ vào ca)");
        }

        if (now.isAfter(scheduledEnd) || now.isEqual(scheduledEnd)) {
            throw new IllegalArgumentException("Đã quá giờ tan ca, không thể check-in");
        }
    }

    /**
     * Validation: Không có 2 ca đang mở (nhưng cho phép nếu ca trước đã
     * AUTO_CLOSED)
     */
    private void validateNoActiveShift(Long employeeId, LocalDate workDate, Long currentScheduleId) {
        // Tìm các attendance của nhân viên cùng ngày, đã check-in nhưng chưa check-out
        // và chưa AUTO_CLOSED
        List<Attendance> activeAttendances = attendanceRepository
                .findByEmployee_IdAndWorkDateAndCheckInNotNullAndCheckOutNullAndStatusNotAutoClosed(
                        employeeId, workDate);

        // Loại bỏ attendance của ca hiện tại
        activeAttendances = activeAttendances.stream()
                .filter(a -> a.getWorkSchedule() == null
                        || !a.getWorkSchedule().getId().equals(currentScheduleId))
                .toList();

        if (!activeAttendances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Bạn đang có ca chưa checkout. Vui lòng checkout ca trước đó trước khi check-in ca mới");
        }
    }

    /**
     * Validation: Checkout muộn tối đa 6 tiếng sau giờ tan ca
     */
    private void validateCheckOutMaxDelay(WorkSchedule schedule, Instant now) {
        if (schedule.getShift() == null) {
            return;
        }

        LocalDate workDate = schedule.getWorkDate();
        LocalTime endTime = schedule.getShift().getEndTime();

        if (workDate == null || endTime == null) {
            return;
        }

        // Ca qua ngày: endTime < startTime (vd 22:00 -> 06:00)
        LocalTime startTime = schedule.getShift().getStartTime();
        LocalDate endDate = endTime.isBefore(startTime) ? workDate.plusDays(1) : workDate;
        ZonedDateTime scheduledEnd = endDate.atTime(endTime).atZone(VN_TZ);
        ZonedDateTime maxCheckOutTime = scheduledEnd.plusHours(CHECK_OUT_MAX_HOURS_AFTER);

        ZonedDateTime nowVn = now.atZone(VN_TZ);

        if (nowVn.isAfter(maxCheckOutTime)) {
            throw new IllegalArgumentException(
                    "Đã quá " + CHECK_OUT_MAX_HOURS_AFTER
                            + " tiếng sau giờ tan ca, không thể checkout. Ca này sẽ tự động đóng");
        }
    }

    /**
     * Tính totalWorkTime theo công thức payable minutes (clamp trong khung ca)
     * pay_start = max(checkin, shift_start)
     * pay_end = min(checkout, shift_end)
     * pay_minutes = max(0, pay_end - pay_start)
     * Và cap: pay_minutes <= shift_minutes
     */
    private void calculatePayableWorkTime(Attendance attendance, WorkSchedule schedule) {
        if (schedule.getShift() == null || attendance.getCheckIn() == null
                || attendance.getCheckOut() == null) {
            attendance.setTotalWorkTime(0);
            return;
        }

        LocalDate workDate = schedule.getWorkDate();
        LocalTime startTime = schedule.getShift().getStartTime();
        LocalTime endTime = schedule.getShift().getEndTime();

        if (workDate == null || startTime == null || endTime == null) {
            attendance.setTotalWorkTime(0);
            return;
        }

        ZonedDateTime scheduledStart = workDate.atTime(startTime).atZone(VN_TZ);
        LocalDate endDate = endTime.isBefore(startTime) ? workDate.plusDays(1) : workDate;
        ZonedDateTime scheduledEnd = endDate.atTime(endTime).atZone(VN_TZ);

        ZonedDateTime checkInVn = attendance.getCheckIn().atZone(VN_TZ);
        ZonedDateTime checkOutVn = attendance.getCheckOut().atZone(VN_TZ);

        // pay_start = max(checkin, shift_start)
        ZonedDateTime payStart = checkInVn.isAfter(scheduledStart) ? checkInVn : scheduledStart;

        // pay_end = min(checkout, shift_end)
        ZonedDateTime payEnd = checkOutVn.isBefore(scheduledEnd) ? checkOutVn : scheduledEnd;

        // pay_minutes = max(0, pay_end - pay_start)
        int payMinutes = (int) Duration.between(payStart, payEnd).toMinutes();
        payMinutes = Math.max(0, payMinutes);

        // Cap: pay_minutes <= shift_minutes
        int shiftMinutes = (int) Duration.between(scheduledStart, scheduledEnd).toMinutes();
        payMinutes = Math.min(payMinutes, shiftMinutes);

        attendance.setTotalWorkTime(payMinutes);
    }

    private void validateOwner(WorkSchedule schedule, Long employeeId) {
        System.out.println(">>>ATTENDANCE MODULE: Validating ownership for employeeId: " + employeeId +
                ", workScheduleId: " + schedule.getId());
        if (schedule.getEmployee() == null || schedule.getEmployee().getId() == null) {
            throw new IllegalArgumentException("WorkSchedule thiếu thông tin nhân viên");
        }
        if (!schedule.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("Bạn không có quyền chấm công ca này");
        }
        System.out.println(">>>ATTENDANCE MODULE: Ownership validated successfully");
    }

    private int validateWorkSiteAndComputeDistance(WorkSchedule schedule, double lat, double lng, int accuracyMeters) {
        System.out.println(">>>ATTENDANCE MODULE: Validating WorkSite for WorkSchedule id: " + schedule.getId());
        WorkSite site = schedule.getWorkSite();
        if (site == null) {
            throw new IllegalArgumentException("Ca làm việc chưa gán WorkSite nên không thể chấm công GPS");
        }

        // field của bạn là 'active'
        if (site.getActive() != null && !site.getActive()) {
            throw new IllegalArgumentException("Địa điểm làm việc đang bị tắt (inactive)");
        }

        Integer allowedAcc = site.getAllowedAccuracyMaxMeters();
        if (allowedAcc != null && accuracyMeters > allowedAcc) {
            throw new IllegalArgumentException("GPS không đủ chính xác (accuracy quá lớn), vui lòng thử lại");
        }

        if (site.getLatitude() == null || site.getLongitude() == null) {
            throw new IllegalArgumentException("WorkSite thiếu tọa độ (latitude/longitude)");
        }
        if (site.getRadiusMeters() == null) {
            throw new IllegalArgumentException("WorkSite thiếu bán kính (radiusMeters)");
        }

        int distance = GeoUtils.distanceMeters(lat, lng, site.getLatitude(), site.getLongitude());

        if (distance > site.getRadiusMeters()) {
            throw new IllegalArgumentException("Bạn đang ở ngoài phạm vi chấm công (" + distance + "m)");
        }

        System.out.println(">>>ATTENDANCE MODULE: WorkSite validated successfully, distance: " + distance + "m");
        return distance;
    }

    private Attendance buildNewAttendance(WorkSchedule schedule) {
        Employee emp = schedule.getEmployee();

        LocalDate workDate = schedule.getWorkDate();
        if (workDate == null) {
            workDate = LocalDate.now(VN_TZ);
        }

        return Attendance.builder()
                .employee(emp)
                .workSchedule(schedule)
                .workDate(workDate)
                .status(AttendanceStatusEnum.ACTIVE)
                .build();
    }

    /**
     * WorkDate (LocalDate) + Shift (LocalTime) vs checkIn/checkOut (Instant)
     * Có xử lý ca qua ngày.
     */
    private void applyLateAndOvertime(Attendance attendance, WorkSchedule schedule) {

        if (schedule.getShift() == null)
            return;

        LocalDate workDate = schedule.getWorkDate();
        LocalTime startTime = schedule.getShift().getStartTime();
        LocalTime endTime = schedule.getShift().getEndTime();

        if (workDate == null || startTime == null || endTime == null)
            return;

        ZonedDateTime scheduledStart = workDate.atTime(startTime).atZone(VN_TZ);

        // Ca qua ngày: endTime < startTime (vd 22:00 -> 06:00)
        LocalDate endDate = endTime.isBefore(startTime) ? workDate.plusDays(1) : workDate;
        ZonedDateTime scheduledEnd = endDate.atTime(endTime).atZone(VN_TZ);

        ZonedDateTime checkInVn = attendance.getCheckIn() != null ? attendance.getCheckIn().atZone(VN_TZ) : null;
        ZonedDateTime checkOutVn = attendance.getCheckOut() != null ? attendance.getCheckOut().atZone(VN_TZ) : null;

        int late = 0;
        if (checkInVn != null && checkInVn.isAfter(scheduledStart)) {
            late = (int) Duration.between(scheduledStart, checkInVn).toMinutes();
        }
        attendance.setLateTime(Math.max(late, 0));

        int ot = 0;
        if (checkOutVn != null && checkOutVn.isAfter(scheduledEnd)) {
            ot = (int) Duration.between(scheduledEnd, checkOutVn).toMinutes();
        }
        attendance.setOvertime(Math.max(ot, 0));

        int early = 0;
        if (checkOutVn != null && checkOutVn.isBefore(scheduledEnd)) {
            early = (int) Duration.between(checkOutVn, scheduledEnd).toMinutes();
        }
        attendance.setEarlyLeave(Math.max(early, 0));

        System.out.println(">>>ATTENDANCE MODULE: Late time calculated: " + attendance.getLateTime()
                + " minutes, Overtime calculated: " + attendance.getOvertime()
                + " minutes, Early leave calculated: " + attendance.getEarlyLeave()
                + " minutes for WorkSchedule id: " + schedule.getId());
    }

    private ResAttendance toResponse(Attendance a) {
        return ResAttendance.builder()
                .id(a.getId())
                .employeeId(a.getEmployee() != null ? a.getEmployee().getId() : null)
                .workScheduleId(a.getWorkSchedule() != null ? a.getWorkSchedule().getId() : null)

                .workDate(a.getWorkDate())
                .checkIn(a.getCheckIn())
                .checkOut(a.getCheckOut())

                .totalWorkTime(a.getTotalWorkTime())
                .overtime(a.getOvertime())
                .lateTime(a.getLateTime())
                .earlyLeave(a.getEarlyLeave())
                .status(a.getStatus() != null ? a.getStatus().name() : null)

                // GPS
                .checkInLat(a.getCheckInLat())
                .checkInLng(a.getCheckInLng())
                .checkInAccuracyMeters(a.getCheckInAccuracyMeters())
                .checkInDistanceMeters(a.getCheckInDistanceMeters())

                .checkOutLat(a.getCheckOutLat())
                .checkOutLng(a.getCheckOutLng())
                .checkOutAccuracyMeters(a.getCheckOutAccuracyMeters())
                .checkOutDistanceMeters(a.getCheckOutDistanceMeters())
                .build();
    }

    // ======================
    // Geo Utils (1 file)
    // ======================
    private static final class GeoUtils {
        private static final double EARTH_RADIUS_METERS = 6371000.0;

        private GeoUtils() {
        }

        static int distanceMeters(double lat1, double lng1, double lat2, double lng2) {
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                            * Math.sin(dLng / 2) * Math.sin(dLng / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return (int) Math.round(EARTH_RADIUS_METERS * c);
        }
    }

    // =========================
    // WEEKLY SUMMARY API
    // =========================

    /**
     * Lấy weekly summary cho tất cả nhân viên active trong khoảng thời gian
     */
    public ResWeeklySummary getWeeklySummary(LocalDate startDate, LocalDate endDate) {
        // 1. Lấy tất cả nhân viên active
        List<Employee> activeEmployees = employeeRepository.findByStatus(
                com.se347.nhom4.HRApplication.util.enums.StatusEnum.ACTIVE);

        // 2. Lấy tất cả work schedules trong khoảng thời gian
        List<WorkSchedule> allSchedules = workScheduleRepository.findByWorkDateBetween(startDate, endDate);

        // 3. Lấy tất cả attendances trong khoảng thời gian
        List<Attendance> allAttendances = attendanceRepository.findByWorkDateBetween(startDate, endDate);

        // 4. Group schedules và attendances theo employeeId
        Map<Long, List<WorkSchedule>> schedulesByEmployee = allSchedules.stream()
                .collect(Collectors.groupingBy(ws -> ws.getEmployee().getId()));

        Map<Long, List<Attendance>> attendancesByEmployee = allAttendances.stream()
                .collect(Collectors.groupingBy(att -> att.getEmployee().getId()));

        // 5. Tạo summary cho từng nhân viên
        List<ResWeeklySummary.EmployeeSummary> employeeSummaries = activeEmployees.stream()
                .map(emp -> buildEmployeeSummary(
                        emp,
                        schedulesByEmployee.getOrDefault(emp.getId(), List.of()),
                        attendancesByEmployee.getOrDefault(emp.getId(), List.of())))
                .collect(Collectors.toList());

        return ResWeeklySummary.builder()
                .startDate(startDate)
                .endDate(endDate)
                .employees(employeeSummaries)
                .build();
    }

    private ResWeeklySummary.EmployeeSummary buildEmployeeSummary(
            Employee employee,
            List<WorkSchedule> schedules,
            List<Attendance> attendances) {

        // Map attendances by workScheduleId
        Map<Long, Attendance> attendanceMap = attendances.stream()
                .filter(att -> att.getWorkSchedule() != null)
                .collect(Collectors.toMap(
                        att -> att.getWorkSchedule().getId(),
                        att -> att,
                        (a1, a2) -> a1));

        int totalScheduled = schedules.size();
        int workedCount = 0;
        int totalWorkedMinutes = 0;
        int absentCount = 0;
        int absentHours = 0;
        int lateCount = 0;
        int totalLateMinutes = 0;
        int earlyLeaveCount = 0;
        int totalEarlyLeaveMinutes = 0;
        int overtimeCount = 0;
        int totalOvertimeMinutes = 0;

        for (WorkSchedule schedule : schedules) {
            Attendance attendance = attendanceMap.get(schedule.getId());

            if (attendance != null && attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
                // Đã chấm công
                workedCount++;

                // Tính total work time
                if (attendance.getTotalWorkTime() != null) {
                    totalWorkedMinutes += attendance.getTotalWorkTime();
                }

                // Tính late
                if (attendance.getLateTime() != null && attendance.getLateTime() > 0) {
                    lateCount++;
                    totalLateMinutes += attendance.getLateTime();
                }

                // Tính early leave
                if (attendance.getEarlyLeave() != null && attendance.getEarlyLeave() > 0) {
                    earlyLeaveCount++;
                    totalEarlyLeaveMinutes += attendance.getEarlyLeave();
                }

                // Tính overtime
                if (attendance.getOvertime() != null && attendance.getOvertime() > 0) {
                    overtimeCount++;
                    totalOvertimeMinutes += attendance.getOvertime();
                }
            } else {
                // Vắng mặt
                absentCount++;
                if (schedule.getShift() != null && schedule.getShift().getStandardHours() != null) {
                    absentHours += schedule.getShift().getStandardHours();
                }
            }
        }

        // Build statistics
        ResWeeklySummary.Statistics statistics = ResWeeklySummary.Statistics.builder()
                .totalScheduled(totalScheduled)
                .worked(ResWeeklySummary.WorkedStats.builder()
                        .count(workedCount)
                        .totalHours(totalWorkedMinutes / 60)
                        .build())
                .absent(ResWeeklySummary.AbsentStats.builder()
                        .count(absentCount)
                        .totalHours(absentHours)
                        .build())
                .late(ResWeeklySummary.LateStats.builder()
                        .count(lateCount)
                        .totalMinutes(totalLateMinutes)
                        .build())
                .earlyLeave(ResWeeklySummary.EarlyLeaveStats.builder()
                        .count(earlyLeaveCount)
                        .totalMinutes(totalEarlyLeaveMinutes)
                        .build())
                .overtime(ResWeeklySummary.OvertimeStats.builder()
                        .count(overtimeCount)
                        .totalMinutes(totalOvertimeMinutes)
                        .build())
                .build();

        return ResWeeklySummary.EmployeeSummary.builder()
                .employee(ResWeeklySummary.Employee.builder()
                        .id(employee.getId())
                        .fullname(employee.getFullname())
                        .email(employee.getEmail())
                        .department(null) // TODO: Add department if exists
                        .build())
                .statistics(statistics)
                .build();
    }

    // ====================================
    // ADMIN APIs - Không validate GPS và thời gian
    // ====================================

    /**
     * Admin tạo check-in thủ công
     * Không yêu cầu GPS, không ràng buộc thời gian
     */
    @Transactional
    public ResAttendance adminCheckIn(Long workScheduleId, ReqAdminCheckIn req) {
        System.out.println(">>>ATTENDANCE MODULE: Admin check-in for workScheduleId: " + workScheduleId);

        WorkSchedule schedule = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "WorkSchedule not found with id: " + workScheduleId));

        Attendance attendance = attendanceRepository.findByWorkSchedule_Id(schedule.getId())
                .orElseGet(() -> buildNewAttendance(schedule));

        if (attendance.getCheckIn() != null) {
            throw new IllegalArgumentException("Ca này đã có check-in rồi");
        }

        attendance.setCheckIn(req.getCheckInTime());
        attendance.setStatus(AttendanceStatusEnum.ACTIVE);

        // Không set GPS fields (null)
        attendance.setCheckInLat(null);
        attendance.setCheckInLng(null);
        attendance.setCheckInAccuracyMeters(null);
        attendance.setCheckInDistanceMeters(null);

        System.out.println(">>>ATTENDANCE MODULE: Admin check-in recorded at " + req.getCheckInTime());

        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }

    /**
     * Admin tạo check-out thủ công
     * Không yêu cầu GPS, không ràng buộc thời gian
     */
    @Transactional
    public ResAttendance adminCheckOut(Long workScheduleId, ReqAdminCheckOut req) {
        System.out.println(">>>ATTENDANCE MODULE: Admin check-out for workScheduleId: " + workScheduleId);

        WorkSchedule schedule = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "WorkSchedule not found with id: " + workScheduleId));

        Attendance attendance = attendanceRepository.findByWorkSchedule_Id(schedule.getId())
                .orElseThrow(() -> new IllegalArgumentException("Chưa có check-in cho ca này"));

        if (attendance.getCheckIn() == null) {
            throw new IllegalArgumentException("Chưa có check-in nên không thể check-out");
        }

        if (attendance.getCheckOut() != null) {
            throw new IllegalArgumentException("Ca này đã có check-out rồi");
        }

        if (req.getCheckOutTime().isBefore(attendance.getCheckIn())) {
            throw new IllegalArgumentException("Thời gian check-out phải sau thời gian check-in");
        }

        attendance.setCheckOut(req.getCheckOutTime());
        attendance.setStatus(AttendanceStatusEnum.COMPLETED);

        // Không set GPS fields (null)
        attendance.setCheckOutLat(null);
        attendance.setCheckOutLng(null);
        attendance.setCheckOutAccuracyMeters(null);
        attendance.setCheckOutDistanceMeters(null);

        // Tính totalWorkTime và áp dụng late/overtime
        calculatePayableWorkTime(attendance, schedule);
        applyLateAndOvertime(attendance, schedule);

        System.out.println(">>>ATTENDANCE MODULE: Admin check-out recorded at " + req.getCheckOutTime());

        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }

    /**
     * Admin cập nhật check-in thủ công
     * Không yêu cầu GPS, không ràng buộc thời gian
     */
    @Transactional
    public ResAttendance adminUpdateCheckIn(Long workScheduleId, ReqAdminCheckIn req) {
        System.out.println(">>>ATTENDANCE MODULE: Admin update check-in for workScheduleId: " + workScheduleId);

        WorkSchedule schedule = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "WorkSchedule not found with id: " + workScheduleId));

        Attendance attendance = attendanceRepository.findByWorkSchedule_Id(schedule.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy attendance cho ca này"));

        // Validation: Nếu đã có checkout, checkIn mới phải trước checkOut
        if (attendance.getCheckOut() != null && req.getCheckInTime().isAfter(attendance.getCheckOut())) {
            throw new IllegalArgumentException("Thời gian check-in phải trước thời gian check-out");
        }

        attendance.setCheckIn(req.getCheckInTime());

        // Nếu đã có checkout, tính lại work time
        if (attendance.getCheckOut() != null) {
            calculatePayableWorkTime(attendance, schedule);
            applyLateAndOvertime(attendance, schedule);
        }

        System.out.println(">>>ATTENDANCE MODULE: Admin updated check-in to " + req.getCheckInTime());

        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }

    /**
     * Admin cập nhật check-out thủ công
     * Không yêu cầu GPS, không ràng buộc thời gian
     */
    @Transactional
    public ResAttendance adminUpdateCheckOut(Long workScheduleId, ReqAdminCheckOut req) {
        System.out.println(">>>ATTENDANCE MODULE: Admin update check-out for workScheduleId: " + workScheduleId);

        WorkSchedule schedule = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "WorkSchedule not found with id: " + workScheduleId));

        Attendance attendance = attendanceRepository.findByWorkSchedule_Id(schedule.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy attendance cho ca này"));

        if (attendance.getCheckIn() == null) {
            throw new IllegalArgumentException("Chưa có check-in nên không thể cập nhật check-out");
        }

        if (req.getCheckOutTime().isBefore(attendance.getCheckIn())) {
            throw new IllegalArgumentException("Thời gian check-out phải sau thời gian check-in");
        }

        attendance.setCheckOut(req.getCheckOutTime());
        attendance.setStatus(AttendanceStatusEnum.COMPLETED);

        // Tính lại work time
        calculatePayableWorkTime(attendance, schedule);
        applyLateAndOvertime(attendance, schedule);

        System.out.println(">>>ATTENDANCE MODULE: Admin updated check-out to " + req.getCheckOutTime());

        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }
}
