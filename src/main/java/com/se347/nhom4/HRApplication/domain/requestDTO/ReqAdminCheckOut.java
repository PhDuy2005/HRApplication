package com.se347.nhom4.HRApplication.domain.requestDTO;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho admin tạo/cập nhật check-out thủ công
 * Không yêu cầu GPS, không ràng buộc thời gian
 */
@Data
public class ReqAdminCheckOut {

    @NotNull(message = "Thời gian check-out không được để trống")
    private Instant checkOutTime;
}
