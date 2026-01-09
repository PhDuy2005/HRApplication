package com.se347.nhom4.HRApplication.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO cho request reset mật khẩu (admin reset cho nhân viên)
 * Không yêu cầu mật khẩu cũ
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqResetPasswordDTO {

    @NotNull(message = "ID nhân viên không được để trống")
    private Long employeeId;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword;
}
