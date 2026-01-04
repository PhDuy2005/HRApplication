package com.se347.nhom4.HRApplication.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.se347.nhom4.HRApplication.domain.table.Payroll;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByEmployee_IdAndMonthAndYear(Long employeeId, Integer month, Integer year);

    List<Payroll> findByEmployee_Id(Long employeeId);

    List<Payroll> findByMonthAndYear(Integer month, Integer year);
}
