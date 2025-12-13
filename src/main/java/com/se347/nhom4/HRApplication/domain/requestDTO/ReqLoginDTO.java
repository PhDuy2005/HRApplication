package com.se347.nhom4.HRApplication.domain.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReqLoginDTO {
    @NotBlank(message = "Không được để trống tên đăng nhập")
    private String username;
    @NotBlank(message = "Không được để trống mật khẩu")
    private String password;
}
