package com.se347.nhom4.HRApplication.domain.requestDTO;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho admin tạo/cập nhật check-in thủ công
 * Không yêu cầu GPS, không ràng buộc thời gian
 */
@Data
public class ReqAdminCheckIn {

    @NotNull(message = "Thời gian check-in không được để trống")
    private Instant checkInTime;
}
