package com.carrot.munaro.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
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
    ),

    KAKAO_LOGIN_FAILED(
            HttpStatus.BAD_REQUEST,
            "AUTH_003",
            "카카오 로그인에 실패했습니다."
    ),

    KAKAO_SERVER_TIMEOUT(
            HttpStatus.REQUEST_TIMEOUT,
            "AUTH_004",
            "카카오 서버 응답 시간이 초과되었습니다."
    ),

    KAKAO_USER_INFO_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "AUTH_005",
            "카카오 사용자 정보를 불러올 수 없습니다."
    ),

    GOOGLE_LOGIN_FAILED(
            HttpStatus.BAD_REQUEST,
        "AUTH_006",
                "구글 로그인에 실패했습니다."
    ),

    GOOGLE_USER_INFO_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
        "AUTH_007",
                "구글 사용자 정보를 불러올 수 없습니다."
    ),

    // Tourist Spot
    TOURIST_SPOT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "TOURIST_SPOT_001",
            "관광지를 찾을 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
