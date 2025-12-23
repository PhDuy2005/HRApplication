package com.se347.nhom4.HRApplication.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.se347.nhom4.HRApplication.domain.table.WorkSchedule;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

    List<WorkSchedule> findByEmployeeId(Long employeeId);

    List<WorkSchedule> findByShiftId(Long shiftId);

    List<WorkSchedule> findByWorkDate(LocalDate workDate);

    List<WorkSchedule> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    List<WorkSchedule> findByEmployeeIdAndWorkDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    boolean existsByEmployeeIdAndShiftIdAndWorkDate(Long employeeId, Long shiftId, LocalDate workDate);
}