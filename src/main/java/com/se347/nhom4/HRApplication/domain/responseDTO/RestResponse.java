package com.se347.nhom4.HRApplication.domain.responseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestResponse<T> {
    private int statusCode;
    private String error;

    private Object message;
    private T data;
}
