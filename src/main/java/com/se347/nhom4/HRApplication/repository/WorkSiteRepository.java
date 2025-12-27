package com.se347.nhom4.HRApplication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.se347.nhom4.HRApplication.domain.table.WorkSite;

public interface WorkSiteRepository extends JpaRepository<WorkSite, Long> {
    List<WorkSite> findByActiveTrue();
}
