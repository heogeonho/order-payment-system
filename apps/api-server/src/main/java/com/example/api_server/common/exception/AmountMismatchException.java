package com.example.api_server.common.exception;

/**
 * 결제 금액이 주문 금액과 일치하지 않을 때 발생하는 예외
 * HTTP Status: 400 BAD REQUEST
 */
public class AmountMismatchException extends BusinessException {

    private static final String ERROR_CODE = "AMOUNT_MISMATCH";
    private static final String DEFAULT_MESSAGE = "결제 금액이 주문 금액과 일치하지 않습니다.";

    public AmountMismatchException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public AmountMismatchException(Long requestedAmount, Long orderAmount) {
        super(ERROR_CODE, DEFAULT_MESSAGE,
              String.format("Requested: %d, Order Amount: %d", requestedAmount, orderAmount));
    }
}
