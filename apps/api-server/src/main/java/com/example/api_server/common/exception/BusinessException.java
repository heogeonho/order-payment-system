package com.example.api_server.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외 기본 클래스
 * 모든 도메인 예외는 이 클래스를 상속받아 구현
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final String detail;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.detail = null;
    }

    public BusinessException(String errorCode, String message, String detail) {
        super(message);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
