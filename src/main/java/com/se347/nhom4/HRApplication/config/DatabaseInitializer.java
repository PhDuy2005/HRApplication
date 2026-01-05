package com.se347.nhom4.HRApplication.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.domain.table.Permission;
import com.se347.nhom4.HRApplication.domain.table.Role;
import com.se347.nhom4.HRApplication.repository.EmployeeRepository;
import com.se347.nhom4.HRApplication.service.PermissionService;
import com.se347.nhom4.HRApplication.service.RoleService;
import com.se347.nhom4.HRApplication.util.enums.StatusEnum;

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
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        boolean permissionsCreated = false;
        boolean rolesCreated = false;

        // Kiểm tra và khởi tạo permissions
        if (!permissionService.hasAnyPermissions()) {
            System.out.println(">>>DATABASE INITIALIZER: Bảng permissions trống, đang khởi tạo dữ liệu...");
            initPermissions();
            permissionsCreated = true;
            System.out.println(">>>DATABASE INITIALIZER: Khởi tạo permissions thành công!");
        } else {
            System.out.println(">>>DATABASE INITIALIZER: Bảng permissions đã có dữ liệu ("
                    + permissionService.count() + " permissions)");
        }

        // Kiểm tra và khởi tạo roles
        if (!roleService.hasAnyRoles()) {
            System.out.println(">>>DATABASE INITIALIZER: Bảng roles trống, đang khởi tạo dữ liệu...");
            initRoles();
            rolesCreated = true;
            System.out.println(">>>DATABASE INITIALIZER: Khởi tạo roles thành công!");
        } else {
            System.out.println(">>>DATABASE INITIALIZER: Bảng roles đã có dữ liệu (" + roleService.count() + " roles)");
        }

        // Nếu có permissions mới được tạo hoặc roles mới được tạo, cập nhật lại quan hệ
        if (permissionsCreated || rolesCreated) {
            System.out.println(">>>DATABASE INITIALIZER: Đang cập nhật lại quan hệ role-permission...");
            updateRolePermissions();
            System.out.println(">>>DATABASE INITIALIZER: Cập nhật quan hệ role-permission thành công!");
        }

        // Kiểm tra và tạo tài khoản admin
        initAdminAccount();
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
        createPermission("Xem chấm công của bản thân", "/api/v1/attendances/my", "GET", "ATTENDANCE");
        createPermission("Xem chấm công theo lịch làm việc", "/api/v1/attendances/my/{workScheduleId}", "GET",
                "ATTENDANCE");
        createPermission("Tạo chấm công thủ công", "/api/v1/attendances", "POST", "ATTENDANCE");
        createPermission("Cập nhật chấm công", "/api/v1/attendances/{id}", "PUT", "ATTENDANCE");
        createPermission("Xóa chấm công", "/api/v1/attendances/{id}", "DELETE", "ATTENDANCE");
        createPermission("Xem tổng hợp chấm công theo tuần", "/api/v2/attendances/weekly-summary", "GET",
                "ATTENDANCE");

        // ==================== WORK SCHEDULE MODULE ====================
        createPermission("Xem lịch làm việc", "/api/v1/work-schedules", "GET", "WORK_SCHEDULE");
        createPermission("Xem chi tiết lịch làm việc", "/api/v1/work-schedules/{id}", "GET", "WORK_SCHEDULE");
        createPermission("Xem lịch làm việc theo nhân viên", "/api/v1/work-schedules/employee/{employeeId}", "GET",
                "WORK_SCHEDULE");
        createPermission("Xem lịch làm việc theo ca", "/api/v1/work-schedules/shift/{shiftId}", "GET",
                "WORK_SCHEDULE");
        createPermission("Xem lịch làm việc theo ca và khoảng thời gian",
                "/api/v1/work-schedules/shift/{shiftId}/date-range", "GET", "WORK_SCHEDULE");
        createPermission("Xem lịch làm việc theo ngày", "/api/v1/work-schedules/date/{workDate}", "GET",
                "WORK_SCHEDULE");
        createPermission("Xem lịch làm việc nhân viên theo ngày",
                "/api/v1/work-schedules/employee/{employeeId}/date/{workDate}", "GET", "WORK_SCHEDULE");
        createPermission("Xem lịch làm việc nhân viên theo khoảng thời gian",
                "/api/v1/work-schedules/employee/{employeeId}/date-range", "GET", "WORK_SCHEDULE");
        createPermission("Kiểm tra lịch làm việc tồn tại", "/api/v1/work-schedules/exists", "GET", "WORK_SCHEDULE");
        createPermission("Tạo lịch làm việc", "/api/v1/work-schedules", "POST", "WORK_SCHEDULE");
        createPermission("Cập nhật lịch làm việc", "/api/v1/work-schedules/{id}", "PUT", "WORK_SCHEDULE");
        createPermission("Xóa lịch làm việc", "/api/v1/work-schedules/{id}", "DELETE", "WORK_SCHEDULE");

        // ==================== SHIFT MODULE ====================
        createPermission("Xem danh sách ca làm việc", "/api/v1/shifts", "GET", "SHIFT");
        createPermission("Xem danh sách ca làm việc đang hoạt động", "/api/v1/shifts/active", "GET", "SHIFT");
        createPermission("Xem chi tiết ca làm việc", "/api/v1/shifts/{id}", "GET", "SHIFT");
        createPermission("Tìm kiếm ca làm việc theo tên", "/api/v1/shifts/search", "GET", "SHIFT");
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
        addPermissionToList(employeePermissions, "Xem chấm công của bản thân");
        addPermissionToList(employeePermissions, "Xem chấm công theo lịch làm việc");
        addPermissionToList(employeePermissions, "Xem tổng hợp chấm công theo tuần");

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

    /**
     * Cập nhật role cho tài khoản admin@gmail.com
     */
    private void initAdminAccount() {
        String adminEmail = "admin@gmail.com";

        // Lấy role ADMIN
        Role adminRole = roleService.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Role ADMIN không tồn tại"));

        // Lấy role EMPLOYEE để cập nhật cho các tài khoản khác
        Role employeeRole = roleService.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Role EMPLOYEE không tồn tại"));

        // Kiểm tra và cập nhật role cho admin@gmail.com
        employeeRepository.findByEmail(adminEmail).ifPresentOrElse(
                admin -> {
                    if (admin.getRole() == null || !admin.getRole().getName().equals("ADMIN")) {
                        admin.setRole(adminRole);
                        employeeRepository.save(admin);
                        System.out.println(">>>DATABASE INITIALIZER: Đã cập nhật role ADMIN cho: " + adminEmail);
                    } else {
                        System.out.println(">>>DATABASE INITIALIZER: Tài khoản " + adminEmail + " đã có role ADMIN");
                    }
                },
                () -> {
                    // Tài khoản chưa tồn tại, tạo mới
                    System.out.println(">>>DATABASE INITIALIZER: Đang tạo tài khoản admin...");
                    Employee admin = new Employee();
                    admin.setFullname("Administrator");
                    admin.setEmail(adminEmail);
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setPhone("0000000000");
                    admin.setHiredDate(LocalDate.now());
                    admin.setStatus(StatusEnum.ACTIVE);
                    admin.setRole(adminRole);
                    admin.setEmployeeSalaryTypes(new ArrayList<>());
                    admin.setMonthlySalaries(new ArrayList<>());
                    admin.setShiftRates(new ArrayList<>());
                    admin.setShiftOtRates(new ArrayList<>());
                    admin.setEmployeePenalties(new ArrayList<>());
                    admin.setAttendancePenalties(new ArrayList<>());

                    employeeRepository.save(admin);
                    System.out.println("  ✓ Đã tạo tài khoản admin:");
                    System.out.println("    - Email: " + adminEmail);
                    System.out.println("    - Password: admin123");
                    System.out.println("    - Role: ADMIN");
                });

        // Cập nhật role EMPLOYEE cho các tài khoản khác (nếu chưa có role)
        List<Employee> employeesWithoutRole = employeeRepository.findAll().stream()
                .filter(emp -> emp.getRole() == null && !emp.getEmail().equals(adminEmail))
                .toList();

        if (!employeesWithoutRole.isEmpty()) {
            System.out
                    .println(">>>DATABASE INITIALIZER: Đang cập nhật role EMPLOYEE cho các tài khoản chưa có role...");
            for (Employee emp : employeesWithoutRole) {
                emp.setRole(employeeRole);
                employeeRepository.save(emp);
                System.out.println("  ✓ Đã cập nhật role EMPLOYEE cho: " + emp.getEmail());
            }
        }
    }

    private void addPermissionToList(List<Permission> permissionList, String permissionName) {
        permissionService.findByName(permissionName).ifPresent(permissionList::add);
    }

    /**
     * Cập nhật lại quan hệ giữa role và permission
     * Method này đảm bảo rằng:
     * - ADMIN role luôn có tất cả permissions
     * - EMPLOYEE role có đúng các permissions cần thiết
     */
    private void updateRolePermissions() {
        // Lấy tất cả permissions từ database
        List<Permission> allPermissions = permissionService.findAll();

        // Cập nhật ADMIN role với tất cả permissions
        roleService.findByName("ADMIN").ifPresent(adminRole -> {
            adminRole.setPermissions(new ArrayList<>(allPermissions));
            roleService.save(adminRole);
            System.out.println("  ✓ Đã cập nhật ADMIN role với " + allPermissions.size() + " permissions");
        });

        // Cập nhật EMPLOYEE role với permissions cần thiết
        roleService.findByName("EMPLOYEE").ifPresent(employeeRole -> {
            List<Permission> employeePermissions = new ArrayList<>();

            // Thêm permissions cho Employee
            addPermissionToList(employeePermissions, "Đăng nhập");
            addPermissionToList(employeePermissions, "Lấy thông tin tài khoản");
            addPermissionToList(employeePermissions, "Refresh token");
            addPermissionToList(employeePermissions, "Đăng xuất");

            // Xem thông tin nhân viên
            addPermissionToList(employeePermissions, "Xem danh sách nhân viên active");
            addPermissionToList(employeePermissions, "Xem chi tiết nhân viên");

            // Chấm công
            addPermissionToList(employeePermissions, "Check-in");
            addPermissionToList(employeePermissions, "Check-out");
            addPermissionToList(employeePermissions, "Xem danh sách chấm công");
            addPermissionToList(employeePermissions, "Xem chi tiết chấm công");
            addPermissionToList(employeePermissions, "Xem chấm công của bản thân");
            addPermissionToList(employeePermissions, "Xem chấm công theo lịch làm việc");
            addPermissionToList(employeePermissions, "Xem tổng hợp chấm công theo tuần");

            // Xem lịch làm việc
            addPermissionToList(employeePermissions, "Xem lịch làm việc");
            addPermissionToList(employeePermissions, "Xem chi tiết lịch làm việc");
            addPermissionToList(employeePermissions, "Xem lịch làm việc theo nhân viên");
            addPermissionToList(employeePermissions, "Xem lịch làm việc theo ca");
            addPermissionToList(employeePermissions, "Xem lịch làm việc theo ca và khoảng thời gian");
            addPermissionToList(employeePermissions, "Xem lịch làm việc theo ngày");
            addPermissionToList(employeePermissions, "Xem lịch làm việc nhân viên theo ngày");
            addPermissionToList(employeePermissions, "Xem lịch làm việc nhân viên theo khoảng thời gian");
            addPermissionToList(employeePermissions, "Kiểm tra lịch làm việc tồn tại");

            // Xem ca làm việc
            addPermissionToList(employeePermissions, "Xem danh sách ca làm việc");
            addPermissionToList(employeePermissions, "Xem danh sách ca làm việc đang hoạt động");
            addPermissionToList(employeePermissions, "Xem chi tiết ca làm việc");
            addPermissionToList(employeePermissions, "Tìm kiếm ca làm việc theo tên");

            // Xem địa điểm làm việc
            addPermissionToList(employeePermissions, "Xem danh sách địa điểm làm việc");
            addPermissionToList(employeePermissions, "Xem chi tiết địa điểm làm việc");

            // Xem lương
            addPermissionToList(employeePermissions, "Xem bảng lương");
            addPermissionToList(employeePermissions, "Xem chi tiết lương");

            employeeRole.setPermissions(employeePermissions);
            roleService.save(employeeRole);
            System.out.println("  ✓ Đã cập nhật EMPLOYEE role với " + employeePermissions.size() + " permissions");
        });
    }
}
