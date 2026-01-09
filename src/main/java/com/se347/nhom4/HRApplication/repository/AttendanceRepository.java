package com.se347.nhom4.HRApplication.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.se347.nhom4.HRApplication.domain.table.Attendance;
import com.se347.nhom4.HRApplication.util.enums.AttendanceStatusEnum;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByWorkSchedule_Id(Long workScheduleId);

    List<Attendance> findByEmployee_IdAndWorkDateBetween(Long employeeId, LocalDate from, LocalDate to);

    List<Attendance> findByWorkDateBetween(LocalDate from, LocalDate to);

    /**
     * Tìm các attendance đã check-in nhưng chưa check-out và chưa AUTO_CLOSED
     * (để validate không có 2 ca đang mở)
     */
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId " +
            "AND a.workDate = :workDate " +
            "AND a.checkIn IS NOT NULL " +
            "AND a.checkOut IS NULL " +
            "AND (a.status IS NULL OR a.status != com.se347.nhom4.HRApplication.util.enums.AttendanceStatusEnum.AUTO_CLOSED)")
    List<Attendance> findByEmployee_IdAndWorkDateAndCheckInNotNullAndCheckOutNullAndStatusNotAutoClosed(
            @Param("employeeId") Long employeeId,
            @Param("workDate") LocalDate workDate);

    /**
     * Tìm các attendance cần tự đóng (đã check-in, chưa check-out, chưa AUTO_CLOSED)
     */
    @Query("SELECT a FROM Attendance a WHERE a.checkIn IS NOT NULL " +
            "AND a.checkOut IS NULL " +
            "AND (a.status IS NULL OR a.status != com.se347.nhom4.HRApplication.util.enums.AttendanceStatusEnum.AUTO_CLOSED)")
    List<Attendance> findAttendancesToAutoClose();
}
