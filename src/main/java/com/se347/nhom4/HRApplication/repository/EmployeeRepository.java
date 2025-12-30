package com.se347.nhom4.HRApplication.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.util.enums.StatusEnum;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    Employee findByEmailAndRefreshToken(String email, String refreshToken);

    List<Employee> findByStatus(StatusEnum status);
}
