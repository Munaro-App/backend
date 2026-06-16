package com.carrot.munaro.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "잘못된 요청입니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 오류입니다."
    ),

    // Auth
    EMAIL_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "EMAIL_ALREADY_EXISTS",
            "이미 존재하는 이메일입니다."
    ),

    INVALID_LOGIN(
            HttpStatus.BAD_REQUEST,
            "INVALID_LOGIN",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),

    KAKAO_LOGIN_FAILED(
            HttpStatus.BAD_REQUEST,
            "KAKAO_LOGIN_FAILED",
            "카카오 로그인에 실패했습니다."
    ),

    KAKAO_SERVER_TIMEOUT(
            HttpStatus.REQUEST_TIMEOUT,
            "KAKAO_SERVER_TIMEOUT",
            "카카오 서버 응답 시간이 초과되었습니다."
    ),

    KAKAO_USER_INFO_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "KAKAO_USER_INFO_NOT_FOUND",
            "카카오 사용자 정보를 불러올 수 없습니다."
    ),

    GOOGLE_LOGIN_FAILED(
            HttpStatus.BAD_REQUEST,
            "GOOGLE_LOGIN_FAILED",
            "구글 로그인에 실패했습니다."
    ),

    GOOGLE_USER_INFO_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "GOOGLE_USER_INFO_NOT_FOUND",
            "구글 사용자 정보를 불러올 수 없습니다."
    ),

    // User
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_NOT_FOUND",
            "사용자를 찾을 수 없습니다."
    ),

    PROFILE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PROFILE_NOT_FOUND",
            "프로필을 찾을 수 없습니다."
    ),

    NICKNAME_ALREADY_EXISTS(
            HttpStatus.BAD_REQUEST,
            "NICKNAME_ALREADY_EXISTS",
            "이미 사용 중인 닉네임입니다."
    ),

    // TouristSpot
    TOURIST_SPOT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "TOURIST_SPOT_NOT_FOUND",
            "관광지를 찾을 수 없습니다."
    ),

    // Season
    SEASON_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "SEASON_NOT_FOUND",
            "시즌을 찾을 수 없습니다."
    ),

    // Quiz
    QUIZ_GENERATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "QUIZ_GENERATION_FAILED",
            "퀴즈 생성에 실패했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
