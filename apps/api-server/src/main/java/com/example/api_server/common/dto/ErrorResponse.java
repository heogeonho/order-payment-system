package com.example.api_server.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * API 에러 응답 DTO
 * 클라이언트에게 일관된 에러 응답 형식을 제공
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * 에러 코드 (예: PRODUCT_NOT_FOUND, OUT_OF_STOCK)
     */
    private final String code;

    /**
     * 사용자에게 표시할 에러 메시지
     */
    private final String message;

    /**
     * 추가 상세 정보 (선택적)
     */
    private final String detail;

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

    public static ErrorResponse of(String code, String message, String detail) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .detail(detail)
                .build();
    }
}
