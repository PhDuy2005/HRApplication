package com.se347.nhom4.HRApplication.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCheckIn;
import com.se347.nhom4.HRApplication.domain.requestDTO.ReqCheckOut;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResAttendance;
import com.se347.nhom4.HRApplication.domain.table.Attendance;
import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.domain.table.WorkSchedule;
import com.se347.nhom4.HRApplication.domain.table.WorkSite;
import com.se347.nhom4.HRApplication.repository.AttendanceRepository;
import com.se347.nhom4.HRApplication.repository.WorkScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final WorkScheduleRepository workScheduleRepository;

    private static final ZoneId VN_TZ = ZoneId.of("Asia/Ho_Chi_Minh");

    // =========================
    // API
    // =========================

    @Transactional
    public ResAttendance checkIn(Long employeeId, ReqCheckIn req) {

        WorkSchedule schedule = workScheduleRepository.findById(req.getWorkScheduleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "WorkSchedule not found with id: " + req.getWorkScheduleId()));

        validateOwner(schedule, employeeId);

        int distanceMeters = validateWorkSiteAndComputeDistance(
                schedule, req.getLat(), req.getLng(), req.getAccuracyMeters()
        );

        Attendance attendance = attendanceRepository.findByWorkSchedule_Id(schedule.getId())
                .orElseGet(() -> buildNewAttendance(schedule));

        if (attendance.getCheckIn() != null) {
            throw new IllegalArgumentException("Ca này đã check-in rồi");
        }

        Instant now = Instant.now();
        attendance.setCheckIn(now);

        attendance.setCheckInLat(req.getLat());
        attendance.setCheckInLng(req.getLng());
        attendance.setCheckInAccuracyMeters(req.getAccuracyMeters());
        attendance.setCheckInDistanceMeters(distanceMeters);

        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }

    @Transactional
    public ResAttendance checkOut(Long employeeId, ReqCheckOut req) {

        WorkSchedule schedule = workScheduleRepository.findById(req.getWorkScheduleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "WorkSchedule not found with id: " + req.getWorkScheduleId()));

        validateOwner(schedule, employeeId);

        int distanceMeters = validateWorkSiteAndComputeDistance(
                schedule, req.getLat(), req.getLng(), req.getAccuracyMeters()
        );

        Attendance attendance = attendanceRepository.findByWorkSchedule_Id(schedule.getId())
                .orElseThrow(() -> new IllegalArgumentException("Chưa check-in nên không thể check-out"));

        if (attendance.getCheckIn() == null) {
            throw new IllegalArgumentException("Chưa check-in nên không thể check-out");
        }
        if (attendance.getCheckOut() != null) {
            throw new IllegalArgumentException("Ca này đã check-out rồi");
        }

        Instant now = Instant.now();
        if (now.isBefore(attendance.getCheckIn())) {
            throw new IllegalArgumentException("Thời gian check-out không hợp lệ");
        }

        attendance.setCheckOut(now);

        attendance.setCheckOutLat(req.getLat());
        attendance.setCheckOutLng(req.getLng());
        attendance.setCheckOutAccuracyMeters(req.getAccuracyMeters());
        attendance.setCheckOutDistanceMeters(distanceMeters);

        int totalMinutes = (int) Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
        attendance.setTotalWorkTime(Math.max(totalMinutes, 0));

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

    // ======================
    // Validation + Helpers
    // ======================

    private void validateOwner(WorkSchedule schedule, Long employeeId) {
        if (schedule.getEmployee() == null || schedule.getEmployee().getId() == null) {
            throw new IllegalArgumentException("WorkSchedule thiếu thông tin nhân viên");
        }
        if (!schedule.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("Bạn không có quyền chấm công ca này");
        }
    }

    private int validateWorkSiteAndComputeDistance(WorkSchedule schedule, double lat, double lng, int accuracyMeters) {
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
                .build();
    }

    /**
     * WorkDate (LocalDate) + Shift (LocalTime) vs checkIn/checkOut (Instant)
     * Có xử lý ca qua ngày.
     */
    private void applyLateAndOvertime(Attendance attendance, WorkSchedule schedule) {

        if (schedule.getShift() == null) return;

        LocalDate workDate = schedule.getWorkDate();
        LocalTime startTime = schedule.getShift().getStartTime();
        LocalTime endTime = schedule.getShift().getEndTime();

        if (workDate == null || startTime == null || endTime == null) return;

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

        private GeoUtils() {}

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
}
