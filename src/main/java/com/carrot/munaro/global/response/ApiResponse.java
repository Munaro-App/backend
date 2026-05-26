package com.carrot.munaro.global.response;

import com.carrot.munaro.global.exception.ErrorCode;
import com.carrot.munaro.global.response.ErrorResponse;

public record ApiResponse<T>(

        boolean success,
        T data,
        Object error

) {

    // 성공
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    // 실패
    public static <T> ApiResponse<T> fail(
            ErrorCode errorCode,
            String message
    ) {
        return new ApiResponse<>(
                false,
                null,
                new ErrorResponse(
                        errorCode.getCode(),
                        message
                )
        );
    }
}