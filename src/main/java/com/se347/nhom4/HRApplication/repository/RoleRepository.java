package com.se347.nhom4.HRApplication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.se347.nhom4.HRApplication.domain.table.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByName(String name);

    Optional<Role> findByName(String name);

    Optional<Role> findByNameAndActiveTrue(String name);
}
