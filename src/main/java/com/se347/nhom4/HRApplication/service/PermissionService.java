package com.se347.nhom4.HRApplication.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.nhom4.HRApplication.domain.table.Permission;
import com.se347.nhom4.HRApplication.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    /**
     * Tạo permission mới nếu chưa tồn tại
     */
    @Transactional
    public Permission createPermission(Permission permission) {
        // Kiểm tra xem permission đã tồn tại chưa (theo apiPath và method)
        if (permissionRepository.existsByApiPathAndMethod(permission.getApiPath(), permission.getMethod())) {
            throw new RuntimeException("Permission đã tồn tại với apiPath: " + permission.getApiPath()
                    + " và method: " + permission.getMethod());
        }
        return permissionRepository.save(permission);
    }

    /**
     * Tạo permission nếu chưa tồn tại, không throw exception
     */
    @Transactional
    public Permission createPermissionIfNotExists(Permission permission) {
        Optional<Permission> existing = permissionRepository.findByApiPathAndMethod(
                permission.getApiPath(),
                permission.getMethod());

        if (existing.isPresent()) {
            return existing.get();
        }

        return permissionRepository.save(permission);
    }

    /**
     * Lấy tất cả permissions
     */
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    /**
     * Tìm permission theo ID
     */
    public Optional<Permission> findById(Long id) {
        return permissionRepository.findById(id);
    }

    /**
     * Tìm permission theo name
     */
    public Optional<Permission> findByName(String name) {
        return permissionRepository.findByName(name);
    }

    /**
     * Kiểm tra permission có tồn tại không
     */
    public boolean existsById(Long id) {
        return permissionRepository.existsById(id);
    }

    /**
     * Kiểm tra có permission nào trong database không
     */
    public boolean hasAnyPermissions() {
        return permissionRepository.count() > 0;
    }

    /**
     * Đếm số lượng permissions
     */
    public long count() {
        return permissionRepository.count();
    }

    /**
     * Xóa permission theo ID
     */
    @Transactional
    public void deleteById(Long id) {
        permissionRepository.deleteById(id);
    }

    /**
     * Cập nhật permission
     */
    @Transactional
    public Permission updatePermission(Long id, Permission permission) {
        Permission existingPermission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission không tồn tại với ID: " + id));

        existingPermission.setName(permission.getName());
        existingPermission.setApiPath(permission.getApiPath());
        existingPermission.setMethod(permission.getMethod());
        existingPermission.setModule(permission.getModule());

        return permissionRepository.save(existingPermission);
    }
}
