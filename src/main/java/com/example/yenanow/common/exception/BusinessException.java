package com.example.yenanow.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직에서 사용자 정의 예외를 던질 때 사용.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
