package com.example.yenanow.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // COMMON
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "토큰을 찾을 수 없거나 형식이 잘못됨"),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰"),
    DUPLICATE_SIGNIN_DETECTED(HttpStatus.UNAUTHORIZED, "토큰이 Redis에 저장된 값과 다름"),
    INVALID_SIGNIN(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자 또는 비밀번호 불일치"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호 불일치"),
    ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 데이터"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "대상 데이터를 찾을 수 없음"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "올바르지 않은 요청"),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "권한 부족"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 파일/타입"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
        "서버 내부 오류가 발생했습니다."),

    // USERS DOMAIN
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    NOT_FOUND_USER_PROFILE(HttpStatus.NOT_FOUND, "프로필 URL을 수정할 사용자를 찾을 수 없습니다."),

    // NCUT DOMAIN
    NOT_FOUND_NCUT(HttpStatus.NOT_FOUND, "해당 NCUT을 찾을 수 없습니다."),

    // COMMENT DOMAIN
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    FORBIDDEN_COMMENT(HttpStatus.FORBIDDEN, "댓글 수정 또는 삭제 권한이 없습니다."),

    // FOLLOW DOMAIN
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),
    FOLLOW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 팔로우 중입니다."),

    // FILM DOMAIN
    NOT_FOUND_CODE(HttpStatus.NOT_FOUND, "해당 방 코드를 찾을 수 없습니다."),
    NOT_FOUND_FRAME(HttpStatus.NOT_FOUND, "해당 프레임을 찾을 수 없습니다."),
    NOT_FOUND_BACKGROUND(HttpStatus.NOT_FOUND, "해당 배경을 찾을 수 없습니다."),
    NOT_FOUND_STICKER(HttpStatus.NOT_FOUND, "해당 스티커를 찾을 수 없습니다."),

    // RELAY DOMAIN
    NOT_FOUND_RELAY_CUT(HttpStatus.NOT_FOUND, "해당 릴레이 컷을 찾을 수 없습니다."),

    // RANKING DOMAIN
    RANKING_NOT_READY(HttpStatus.SERVICE_UNAVAILABLE, "랭킹 갱신 중입니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;
}