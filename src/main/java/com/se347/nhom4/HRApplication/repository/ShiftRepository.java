package com.se347.nhom4.HRApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.se347.nhom4.HRApplication.domain.table.Shift;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    // Tìm ca theo tên duy nhất
    Optional<Shift> findByName(String name);

    List<Shift> findByIsActiveTrueAndNameContainingIgnoreCase(String name);

    // Lấy tất cả ca đang active
    List<Shift> findByIsActiveTrue();

    List<Shift> findAll();
}
