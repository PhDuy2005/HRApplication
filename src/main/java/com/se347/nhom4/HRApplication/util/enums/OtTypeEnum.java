package com.se347.nhom4.HRApplication.util.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OtTypeEnum {
    NORMAL_OT("OT ngày thường"),
    WEEKEND_OT("OT cuối tuần"),
    HOLIDAY_OT("OT ngày lễ");

    private final String description;
}
