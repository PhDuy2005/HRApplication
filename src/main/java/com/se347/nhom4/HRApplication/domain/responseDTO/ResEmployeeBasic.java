package com.se347.nhom4.HRApplication.domain.responseDTO;

import java.time.Instant;
import java.time.LocalDate;

import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.util.enums.StatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đơn giản để trả về thông tin cơ bản của nhân viên.
 * Dùng cho API list all employees.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ResEmployeeBasic {
    private Long id;
    private String fullname;
    private String email;
    private String phone;
    private StatusEnum status;
    private RoleInfo role;
    private LocalDate hiredDate;
    private Instant updatedAt;

    public ResEmployeeBasic(Employee employee) {
        this.id = employee.getId();
        this.fullname = employee.getFullname();
        this.email = employee.getEmail();
        this.phone = employee.getPhone();
        this.status = employee.getStatus();
        this.hiredDate = employee.getHiredDate();
        this.updatedAt = employee.getUpdatedAt();
        // Role
        if (employee.getRole() != null) {
            this.role = new RoleInfo(employee.getRole().getId(), employee.getRole().getName());
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleInfo {
        private Long id;
        private String name;
    }
}
