package com.se347.nhom4.HRApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.se347.nhom4.HRApplication.domain.table.Shift;
import com.se347.nhom4.HRApplication.domain.table.WorkSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

    // Lấy lịch của 1 nhân viên theo ngày (có thể trả nhiều ca trong ngày)
    List<WorkSchedule> findByEmployee_IdAndWorkDate(Long employeeId, LocalDate workDate);

    // Lấy lịch của 1 nhân viên trong khoảng ngày
    List<WorkSchedule> findByEmployee_IdAndWorkDateBetween(Long employeeId, LocalDate from, LocalDate to);

    // Lấy lịch theo ca + ngày + nhân viên (khớp unique constraint employee_id, shift_id, work_date)
    Optional<WorkSchedule> findByEmployee_IdAndShift_IdAndWorkDate(Long employeeId, Long shiftId, LocalDate workDate);

    // (Tuỳ chọn) Lấy toàn bộ lịch theo ngày (cho quản lý xem lịch toàn công ty)
    List<WorkSchedule> findByWorkDate(LocalDate workDate);

    // (Tuỳ chọn) Lấy lịch theo WorkSite trong khoảng ngày (nếu bạn cần)
    List<WorkSchedule> findByWorkSite_IdAndWorkDateBetween(Long workSiteId, LocalDate from, LocalDate to);

}
