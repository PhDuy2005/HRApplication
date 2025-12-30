package com.se347.nhom4.HRApplication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.se347.nhom4.HRApplication.domain.table.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByName(String name);

    Optional<Permission> findByName(String name);

    boolean existsByApiPathAndMethod(String apiPath, String method);

    Optional<Permission> findByApiPathAndMethod(String apiPath, String method);
}
