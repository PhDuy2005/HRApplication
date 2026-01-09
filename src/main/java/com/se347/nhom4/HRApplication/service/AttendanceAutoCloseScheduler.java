package com.se347.nhom4.HRApplication.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.se347.nhom4.HRApplication.domain.table.Attendance;
import com.se347.nhom4.HRApplication.domain.table.WorkSchedule;
import com.se347.nhom4.HRApplication.repository.AttendanceRepository;
import com.se347.nhom4.HRApplication.util.enums.AttendanceStatusEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled task để tự động đóng các ca chấm công sau 6 tiếng
 * Chạy mỗi 30 phút một lần
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceAutoCloseScheduler {

    private final AttendanceRepository attendanceRepository;
    private static final ZoneId VN_TZ = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int CHECK_OUT_MAX_HOURS_AFTER = 6;

    @Scheduled(fixedRate = 1800000) // Chạy mỗi 30 phút (30 * 60 * 1000 ms)
    @Transactional
    public void autoCloseAttendances() {
        log.info(">>>ATTENDANCE AUTO-CLOSE: Bắt đầu tự động đóng ca chấm công...");

        List<Attendance> attendancesToClose = attendanceRepository
                .findAttendancesToAutoClose();

        int closedCount = 0;

        for (Attendance attendance : attendancesToClose) {
            try {
                WorkSchedule schedule = attendance.getWorkSchedule();
                if (schedule == null || schedule.getShift() == null) {
                    continue;
                }

                LocalDate workDate = schedule.getWorkDate();
                LocalTime endTime = schedule.getShift().getEndTime();
                LocalTime startTime = schedule.getShift().getStartTime();

                if (workDate == null || endTime == null || attendance.getCheckIn() == null) {
                    continue;
                }

                // Tính scheduledEnd
                LocalDate endDate = endTime.isBefore(startTime) ? workDate.plusDays(1) : workDate;
                ZonedDateTime scheduledEnd = endDate.atTime(endTime).atZone(VN_TZ);
                ZonedDateTime maxCheckOutTime = scheduledEnd.plusHours(CHECK_OUT_MAX_HOURS_AFTER);

                ZonedDateTime checkInVn = attendance.getCheckIn().atZone(VN_TZ);
                Instant now = Instant.now();
                ZonedDateTime nowVn = now.atZone(VN_TZ);

                // Nếu đã quá 6 tiếng sau giờ tan ca
                if (nowVn.isAfter(maxCheckOutTime)) {
                    // Tự đóng ca: set status = AUTO_CLOSED, giữ checkOut = null
                    attendance.setStatus(AttendanceStatusEnum.AUTO_CLOSED);
                    attendanceRepository.save(attendance);

                    closedCount++;
                    log.info(">>>ATTENDANCE AUTO-CLOSE: Đã tự đóng ca ID {} của nhân viên {}",
                            attendance.getId(),
                            attendance.getEmployee() != null ? attendance.getEmployee().getId() : "N/A");
                }
            } catch (Exception e) {
                log.error(">>>ATTENDANCE AUTO-CLOSE: Lỗi khi tự đóng ca ID {}: {}",
                        attendance.getId(), e.getMessage());
            }
        }

        log.info(">>>ATTENDANCE AUTO-CLOSE: Hoàn thành. Đã tự đóng {} ca", closedCount);
    }
}

