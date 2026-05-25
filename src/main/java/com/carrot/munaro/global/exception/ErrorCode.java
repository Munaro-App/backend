package com.carrot.munaro.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "COMMON_400",
            "잘못된 요청입니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "COMMON_500",
            "서버 오류입니다."
    ),

    // Auth
    EMAIL_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "AUTH_001",
            "이미 존재하는 이메일입니다."
    ),

    INVALID_LOGIN(
            HttpStatus.BAD_REQUEST,
            "AUTH_002",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(
            HttpStatus status,
            String code,
            String message
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}