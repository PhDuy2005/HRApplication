package com.se347.nhom4.HRApplication.domain.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqShiftDTO {

    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean isActive;

    @Size(max = 7)
    private String colorCode;
}
