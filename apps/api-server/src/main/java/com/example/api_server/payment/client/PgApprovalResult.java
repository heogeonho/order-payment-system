package com.example.api_server.payment.client;

import lombok.Builder;
import lombok.Getter;

/**
 * PG 승인 결과 DTO
 * PG사로부터 받은 승인 결과를 담는 객체
 */
@Getter
@Builder
public class PgApprovalResult {

    /**
     * PG사 결과 코드
     */
    private final String resultCode;

    /**
     * PG사 결과 메시지
     */
    private final String resultMessage;

    /**
     * 승인 성공 여부
     */
    private final boolean success;

    /**
     * 승인 성공 결과 생성
     */
    public static PgApprovalResult success() {
        return PgApprovalResult.builder()
                .resultCode("0000")
                .resultMessage("승인 성공")
                .success(true)
                .build();
    }

    /**
     * 승인 실패 결과 생성
     */
    public static PgApprovalResult failure(String resultCode, String resultMessage) {
        return PgApprovalResult.builder()
                .resultCode(resultCode)
                .resultMessage(resultMessage)
                .success(false)
                .build();
    }
}
