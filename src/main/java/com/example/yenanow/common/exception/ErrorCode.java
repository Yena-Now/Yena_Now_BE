package com.example.yenanow.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ==== AUTH (인증/토큰) ====
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "토큰을 찾을 수 없거나 형식이 잘못됨"),
    AUTH_INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 액세스 토큰"),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 리프레시 토큰"),
    AUTH_DUPLICATE_SIGNIN_DETECTED(HttpStatus.UNAUTHORIZED, "AUTH_004", "토큰이 Redis에 저장된 값과 다름"),
    AUTH_INVALID_SIGNIN(HttpStatus.BAD_REQUEST, "AUTH_005", "존재하지 않는 사용자 또는 비밀번호 불일치"),
    AUTH_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_006", "비밀번호 불일치"),

    // ==== USER (사용자) ====
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_001", "이미 존재하는 사용자"),

    // ==== COMMON (공통) ====
    COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_001", "대상 데이터를 찾을 수 없음"),
    COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_002", "올바르지 않은 요청"),
    COMMON_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "COMMON_003", "권한 부족"),
    COMMON_UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_004", "지원하지 않는 파일/타입"),
    COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_005",
        "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
