package com.se347.nhom4.HRApplication.domain.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqCheckOut {

    @NotNull
    private Long workScheduleId;

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    @NotNull
    private Integer accuracyMeters;
}
