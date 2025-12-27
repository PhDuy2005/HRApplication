package com.se347.nhom4.HRApplication.domain.requestDTO;

import lombok.Data;

@Data
public class ReqWorkSite {
    private String code;
    private String name;
    private String address;

    private Double latitude;
    private Double longitude;

    private Integer radiusMeters;              // bán kính hợp lệ
    private Integer allowedAccuracyMaxMeters;  // ví dụ 50m
}
