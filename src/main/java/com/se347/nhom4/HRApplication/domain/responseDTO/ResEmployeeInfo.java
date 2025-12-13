package com.se347.nhom4.HRApplication.domain.responseDTO;

import java.time.Instant;

import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.util.enums.StatusEnum;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ResEmployeeInfo {
    private Long id;

    String fullname;
    String email;
    String phone;

    Instant hiredDate;
    StatusEnum status;

    public ResEmployeeInfo(Employee employee) {
        this.id = employee.getId();
        this.fullname = employee.getFullname();
        this.email = employee.getEmail();
        this.phone = employee.getPhone();
        this.hiredDate = employee.getHiredDate();
        this.status = employee.getStatus();
    }
}
