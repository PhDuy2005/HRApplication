package com.se347.nhom4.HRApplication.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.nhom4.HRApplication.domain.table.Role;
import com.se347.nhom4.HRApplication.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    /**
     * Tạo role mới
     */
    @Transactional
    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new RuntimeException("Role đã tồn tại với tên: " + role.getName());
        }
        return roleRepository.save(role);
    }

    /**
     * Tạo role nếu chưa tồn tại, không throw exception
     */
    @Transactional
    public Role createRoleIfNotExists(Role role) {
        Optional<Role> existing = roleRepository.findByName(role.getName());

        if (existing.isPresent()) {
            return existing.get();
        }

        return roleRepository.save(role);
    }

    /**
     * Lấy tất cả roles
     */
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    /**
     * Tìm role theo ID
     */
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    /**
     * Tìm role theo name
     */
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    /**
     * Kiểm tra role có tồn tại không
     */
    public boolean existsById(Long id) {
        return roleRepository.existsById(id);
    }

    /**
     * Kiểm tra có role nào trong database không
     */
    public boolean hasAnyRoles() {
        return roleRepository.count() > 0;
    }

    /**
     * Đếm số lượng roles
     */
    public long count() {
        return roleRepository.count();
    }

    /**
     * Xóa role theo ID
     */
    @Transactional
    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }

    /**
     * Cập nhật role
     */
    @Transactional
    public Role updateRole(Long id, Role role) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại với ID: " + id));

        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());
        existingRole.setActive(role.isActive());
        existingRole.setPermissions(role.getPermissions());

        return roleRepository.save(existingRole);
    }
}
