package com.se347.nhom4.HRApplication.domain.requestDTO;

import com.se347.nhom4.HRApplication.domain.table.Permission;
import com.se347.nhom4.HRApplication.domain.table.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqAddPermissionDTO {
    private Role role;
    private Permission permission;
}
