package com.example.yenanow.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 클라이언트로 내려보낼 에러 응답 DTO.
 */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int status;   // HTTP Status Code         예: 404
    private final String error; // HTTP 상태 명칭              예: "NOT_FOUND"
    private final String message; //                             예 : 대상 데이터를 찾을 수 없음

    /* Factory Method */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
            .status(errorCode.getStatus().value())
            .error(errorCode.getStatus().name())
            .message(errorCode.getMessage())
            .build();
    }
}
