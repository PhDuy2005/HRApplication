package com.se347.nhom4.HRApplication.util.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FrequencyTypeEnum {
    PER_TIME("Theo lần"),
    PER_MINUTE("Theo phút");

    private final String description;
}
