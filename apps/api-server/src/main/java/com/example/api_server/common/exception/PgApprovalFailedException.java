package com.example.api_server.common.exception;

/**
 * PG 결제 승인이 실패했을 때 발생하는 예외
 * HTTP Status: 400 BAD REQUEST
 */
public class PgApprovalFailedException extends BusinessException {

    private static final String ERROR_CODE = "PG_APPROVAL_FAILED";
    private static final String DEFAULT_MESSAGE = "PG 결제 승인에 실패했습니다.";

    public PgApprovalFailedException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public PgApprovalFailedException(String pgResultCode, String pgResultMessage) {
        super(ERROR_CODE, DEFAULT_MESSAGE,
              String.format("PG Code: %s, Message: %s", pgResultCode, pgResultMessage));
    }
}
