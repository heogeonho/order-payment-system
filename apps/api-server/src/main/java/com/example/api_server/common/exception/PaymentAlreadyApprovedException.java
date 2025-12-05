package com.example.api_server.common.exception;

/**
 * 이미 승인된 결제를 다시 승인하려고 할 때 발생하는 예외
 * HTTP Status: 409 CONFLICT
 */
public class PaymentAlreadyApprovedException extends BusinessException {

    private static final String ERROR_CODE = "PAYMENT_ALREADY_APPROVED";
    private static final String DEFAULT_MESSAGE = "이미 승인된 결제입니다.";

    public PaymentAlreadyApprovedException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public PaymentAlreadyApprovedException(String orderId) {
        super(ERROR_CODE, DEFAULT_MESSAGE, "Order ID: " + orderId);
    }
}
