package com.example.yenanow.common.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역 예외 처리기.
 * springdoc-openapi 의 스캔 대상에서 제외하기 위해 @Hidden 을 사용.
 */
@RestControllerAdvice
@Hidden   // <‑‑ Swagger/OpenAPI 문서화 대상에서 제외
public class GlobalExceptionHandler {

    /* 1) 비즈니스 예외 */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode));
    }

    /* 2) @Valid / @Validated 바인딩 오류 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getStatus())
            .body(ErrorResponse.of(ErrorCode.BAD_REQUEST));
    }

    /* 3) 파라미터 제약 조건(@Size 등) 위반 */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getStatus())
            .body(ErrorResponse.of(ErrorCode.BAD_REQUEST));
    }

    /* 4) 그밖의 모든 예외 */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        // 로그를 남겨 두면 디버깅에 좋습니다.
        // log.error("Unhandled exception", e);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
