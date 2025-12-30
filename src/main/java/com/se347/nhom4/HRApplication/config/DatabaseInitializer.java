package com.se347.nhom4.HRApplication.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.se347.nhom4.HRApplication.domain.table.Permission;
import com.se347.nhom4.HRApplication.domain.table.Role;
import com.se347.nhom4.HRApplication.service.PermissionService;
import com.se347.nhom4.HRApplication.service.RoleService;

import lombok.RequiredArgsConstructor;

/**
 * Component tự động khởi tạo permissions và roles khi start application
 * nếu các bảng chưa có dữ liệu
 */
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final PermissionService permissionService;
    private final RoleService roleService;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra và khởi tạo permissions
        if (!permissionService.hasAnyPermissions()) {
            System.out.println(">>>DATABASE INITIALIZER: Bảng permissions trống, đang khởi tạo dữ liệu...");
            initPermissions();
            System.out.println(">>>DATABASE INITIALIZER: Khởi tạo permissions thành công!");
        } else {
            System.out.println(">>>DATABASE INITIALIZER: Bảng permissions đã có dữ liệu ("
                    + permissionService.count() + " permissions)");
        }

        // Kiểm tra và khởi tạo roles
        if (!roleService.hasAnyRoles()) {
            System.out.println(">>>DATABASE INITIALIZER: Bảng roles trống, đang khởi tạo dữ liệu...");
            initRoles();
            System.out.println(">>>DATABASE INITIALIZER: Khởi tạo roles thành công!");
        } else {
            System.out.println(">>>DATABASE INITIALIZER: Bảng roles đã có dữ liệu ("
                    + roleService.count() + " roles)");
        }
    }

    /**
     * Khởi tạo các permissions cơ bản cho hệ thống
     */
    private void initPermissions() {
        // ==================== AUTH MODULE ====================
        createPermission("Đăng nhập", "/api/v1/auth/login", "POST", "AUTH");
        createPermission("Lấy thông tin tài khoản", "/api/v1/auth/account", "GET", "AUTH");
        createPermission("Refresh token", "/api/v1/auth/refresh", "GET", "AUTH");
        createPermission("Đăng xuất", "/api/v1/auth/logout", "POST", "AUTH");

        // ==================== EMPLOYEE MODULE ====================
        createPermission("Xem danh sách nhân viên", "/api/v1/employees", "GET", "EMPLOYEE");
        createPermission("Xem danh sách nhân viên active", "/api/v1/employees/active", "GET", "EMPLOYEE");
        createPermission("Xem chi tiết nhân viên", "/api/v1/employees/{id}", "GET", "EMPLOYEE");
        createPermission("Tạo nhân viên mới", "/api/v1/employees", "POST", "EMPLOYEE");
        createPermission("Cập nhật thông tin nhân viên", "/api/v1/employees/{id}/basic-info", "PUT", "EMPLOYEE");
        createPermission("Xóa nhân viên", "/api/v1/employees/{id}", "DELETE", "EMPLOYEE");

        // ==================== ATTENDANCE MODULE ====================
        createPermission("Xem danh sách chấm công", "/api/v1/attendances", "GET", "ATTENDANCE");
        createPermission("Xem chi tiết chấm công", "/api/v1/attendances/{id}", "GET", "ATTENDANCE");
        createPermission("Check-in", "/api/v1/attendances/check-in", "POST", "ATTENDANCE");
        createPermission("Check-out", "/api/v1/attendances/check-out", "POST", "ATTENDANCE");
        createPermission("Tạo chấm công thủ công", "/api/v1/attendances", "POST", "ATTENDANCE");
        createPermission("Cập nhật chấm công", "/api/v1/attendances/{id}", "PUT", "ATTENDANCE");
        createPermission("Xóa chấm công", "/api/v1/attendances/{id}", "DELETE", "ATTENDANCE");

        // ==================== WORK SCHEDULE MODULE ====================
        createPermission("Xem lịch làm việc", "/api/v1/work-schedules", "GET", "WORK_SCHEDULE");
        createPermission("Xem chi tiết lịch làm việc", "/api/v1/work-schedules/{id}", "GET", "WORK_SCHEDULE");
        createPermission("Tạo lịch làm việc", "/api/v1/work-schedules", "POST", "WORK_SCHEDULE");
        createPermission("Cập nhật lịch làm việc", "/api/v1/work-schedules/{id}", "PUT", "WORK_SCHEDULE");
        createPermission("Xóa lịch làm việc", "/api/v1/work-schedules/{id}", "DELETE", "WORK_SCHEDULE");

        // ==================== SHIFT MODULE ====================
        createPermission("Xem danh sách ca làm việc", "/api/v1/shifts", "GET", "SHIFT");
        createPermission("Xem chi tiết ca làm việc", "/api/v1/shifts/{id}", "GET", "SHIFT");
        createPermission("Tạo ca làm việc mới", "/api/v1/shifts", "POST", "SHIFT");
        createPermission("Cập nhật ca làm việc", "/api/v1/shifts/{id}", "PUT", "SHIFT");
        createPermission("Xóa ca làm việc", "/api/v1/shifts/{id}", "DELETE", "SHIFT");

        // ==================== WORK SITE MODULE ====================
        createPermission("Xem danh sách địa điểm làm việc", "/api/v1/work-sites", "GET", "WORK_SITE");
        createPermission("Xem chi tiết địa điểm làm việc", "/api/v1/work-sites/{id}", "GET", "WORK_SITE");
        createPermission("Tạo địa điểm làm việc", "/api/v1/work-sites", "POST", "WORK_SITE");
        createPermission("Cập nhật địa điểm làm việc", "/api/v1/work-sites/{id}", "PUT", "WORK_SITE");
        createPermission("Xóa địa điểm làm việc", "/api/v1/work-sites/{id}", "DELETE", "WORK_SITE");

        // ==================== ROLE MODULE ====================
        createPermission("Xem danh sách vai trò", "/api/v1/roles", "GET", "ROLE");
        createPermission("Xem chi tiết vai trò", "/api/v1/roles/{id}", "GET", "ROLE");
        createPermission("Tạo vai trò mới", "/api/v1/roles", "POST", "ROLE");
        createPermission("Cập nhật vai trò", "/api/v1/roles/{id}", "PUT", "ROLE");
        createPermission("Xóa vai trò", "/api/v1/roles/{id}", "DELETE", "ROLE");

        // ==================== PERMISSION MODULE ====================
        createPermission("Xem danh sách quyền", "/api/v1/permissions", "GET", "PERMISSION");
        createPermission("Xem chi tiết quyền", "/api/v1/permissions/{id}", "GET", "PERMISSION");
        createPermission("Tạo quyền mới", "/api/v1/permissions", "POST", "PERMISSION");
        createPermission("Cập nhật quyền", "/api/v1/permissions/{id}", "PUT", "PERMISSION");
        createPermission("Xóa quyền", "/api/v1/permissions/{id}", "DELETE", "PERMISSION");

        // ==================== SALARY MODULE ====================
        createPermission("Xem bảng lương", "/api/v1/salaries", "GET", "SALARY");
        createPermission("Xem chi tiết lương", "/api/v1/salaries/{id}", "GET", "SALARY");
        createPermission("Tính lương", "/api/v1/salaries/calculate", "POST", "SALARY");
        createPermission("Cập nhật lương", "/api/v1/salaries/{id}", "PUT", "SALARY");

        // ==================== REPORT MODULE ====================
        createPermission("Xem báo cáo tổng quan", "/api/v1/reports/overview", "GET", "REPORT");
        createPermission("Xem báo cáo chấm công", "/api/v1/reports/attendance", "GET", "REPORT");
        createPermission("Xem báo cáo lương", "/api/v1/reports/salary", "GET", "REPORT");
        createPermission("Xuất báo cáo Excel", "/api/v1/reports/export", "GET", "REPORT");
    }

    /**
     * Helper method để tạo permission
     */
    private void createPermission(String name, String apiPath, String method, String module) {
        Permission permission = new Permission(name, apiPath, method, module);
        permissionService.createPermissionIfNotExists(permission);
        System.out.println("  ✓ Đã tạo permission: " + name);
    }

    /**
     * Khởi tạo các roles cơ bản cho hệ thống
     */
    private void initRoles() {
        // Lấy tất cả permissions từ database
        List<Permission> allPermissions = permissionService.findAll();

        // ==================== ROLE: ADMIN ====================
        // Admin có tất cả quyền
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Quản trị viên - Có toàn quyền truy cập hệ thống");
        adminRole.setActive(true);
        adminRole.setPermissions(new ArrayList<>(allPermissions));

        roleService.createRoleIfNotExists(adminRole);
        System.out.println("  ✓ Đã tạo role: ADMIN với " + allPermissions.size() + " permissions");

        // ==================== ROLE: EMPLOYEE ====================
        // Employee có quyền hạn chế
        List<Permission> employeePermissions = new ArrayList<>();

        // Thêm permissions cho Employee
        addPermissionToList(employeePermissions, "Đăng nhập");
        addPermissionToList(employeePermissions, "Lấy thông tin tài khoản");
        addPermissionToList(employeePermissions, "Refresh token");
        addPermissionToList(employeePermissions, "Đăng xuất");

        // Xem thông tin nhân viên (chỉ xem)
        addPermissionToList(employeePermissions, "Xem danh sách nhân viên active");
        addPermissionToList(employeePermissions, "Xem chi tiết nhân viên");

        // Chấm công (nhân viên có thể check-in/check-out)
        addPermissionToList(employeePermissions, "Check-in");
        addPermissionToList(employeePermissions, "Check-out");
        addPermissionToList(employeePermissions, "Xem danh sách chấm công");
        addPermissionToList(employeePermissions, "Xem chi tiết chấm công");

        // Xem lịch làm việc của mình
        addPermissionToList(employeePermissions, "Xem lịch làm việc");
        addPermissionToList(employeePermissions, "Xem chi tiết lịch làm việc");

        // Xem ca làm việc
        addPermissionToList(employeePermissions, "Xem danh sách ca làm việc");
        addPermissionToList(employeePermissions, "Xem chi tiết ca làm việc");

        // Xem địa điểm làm việc
        addPermissionToList(employeePermissions, "Xem danh sách địa điểm làm việc");
        addPermissionToList(employeePermissions, "Xem chi tiết địa điểm làm việc");

        // Xem lương của mình
        addPermissionToList(employeePermissions, "Xem bảng lương");
        addPermissionToList(employeePermissions, "Xem chi tiết lương");

        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");
        employeeRole.setDescription("Nhân viên - Quyền truy cập cơ bản");
        employeeRole.setActive(true);
        employeeRole.setPermissions(employeePermissions);

        roleService.createRoleIfNotExists(employeeRole);
        System.out.println("  ✓ Đã tạo role: EMPLOYEE với " + employeePermissions.size() + " permissions");
    }

    /**
     * Helper method để thêm permission vào list theo tên
     */
    private void addPermissionToList(List<Permission> permissionList, String permissionName) {
        permissionService.findByName(permissionName).ifPresent(permissionList::add);
    }
}
