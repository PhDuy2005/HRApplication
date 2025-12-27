package com.se347.nhom4.HRApplication.domain.responseDTO;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResWorkSite {
    private Long id;

    private String code;
    private String name;
    private String address;

    private Double latitude;
    private Double longitude;

    private Integer radiusMeters;
    private Integer allowedAccuracyMaxMeters;

    private Boolean active;

    private Instant createdAt;
    private Instant updatedAt;
}
