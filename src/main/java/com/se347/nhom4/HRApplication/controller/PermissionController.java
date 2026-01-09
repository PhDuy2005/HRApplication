package com.se347.nhom4.HRApplication.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqAddPermissionDTO;
import com.se347.nhom4.HRApplication.domain.table.Permission;
import com.se347.nhom4.HRApplication.domain.table.Role;
import com.se347.nhom4.HRApplication.service.PermissionService;
import com.se347.nhom4.HRApplication.service.RoleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;
    private final RoleService roleService;

    @PostMapping("/addIntoRole")
    public ResponseEntity<Role> addPermission(@RequestBody ReqAddPermissionDTO dto) {
        System.out.println(
                ">>>PERMISSION MODULE: Adding permission to role, roleId: " + dto.getRole().getId().toString());
        Permission curPermission = this.permissionService.createPermissionIfNotExists(dto.getPermission());
        System.out.println(">>>PERMISSION MODULE: Current Permission ID: " + curPermission.getId());
        // Role role = this.roleService.findById(dto.getRoleId()).orElse(null);
        Role role = dto.getRole();
        System.out.println(">>>PERMISSION MODULE: Current Role ID: " + (role != null ? role.getId() : "null"));
        if (role != null) {
            role = this.roleService.addPermissionIntoRole(role, curPermission).orElse(null);
            System.out.println(">>>PERMISSION MODULE: Updated Role ID: " + (role != null ? role.getId() : "null"));
            return ResponseEntity.ok(role);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
        Permission createdPermission = this.permissionService.createPermissionIfNotExists(permission);
        return ResponseEntity.ok(createdPermission);
    }

    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        List<Permission> permissions = this.permissionService.findAll();
        return ResponseEntity.ok(permissions);
    }
}
