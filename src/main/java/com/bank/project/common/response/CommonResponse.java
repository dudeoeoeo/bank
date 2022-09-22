package com.bank.project.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {

    private boolean result;
    private T body;

    public static <T> CommonResponse success(T body) {
        return new CommonResponse<>(true, body);
    }
    public static <T> CommonResponse failed(T body) {
        return new CommonResponse<>(false, body);
    }
}
