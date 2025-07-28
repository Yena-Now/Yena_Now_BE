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

    private final int status;   // HTTP Status Code
    private final String code;  // 비즈니스 에러 코드
    private final String message;

    /* Factory Method */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
            .status(errorCode.getStatus().value())
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .build();
    }
}
