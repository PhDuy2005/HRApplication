package com.se347.nhom4.HRApplication.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.se347.nhom4.HRApplication.domain.table.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByWorkSchedule_Id(Long workScheduleId);

    List<Attendance> findByEmployee_IdAndWorkDateBetween(Long employeeId, LocalDate from, LocalDate to);

    List<Attendance> findByWorkDateBetween(LocalDate from, LocalDate to);
}
