package com.example.api_server.common.exception;

/**
 * 주문이 결제 가능한 상태가 아닐 때 발생하는 예외
 * HTTP Status: 409 CONFLICT
 */
public class OrderNotPayableException extends BusinessException {

    private static final String ERROR_CODE = "ORDER_NOT_PAYABLE";
    private static final String DEFAULT_MESSAGE = "결제할 수 없는 주문 상태입니다.";

    public OrderNotPayableException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public OrderNotPayableException(String orderId, String currentStatus) {
        super(ERROR_CODE, DEFAULT_MESSAGE,
              String.format("Order ID: %s, Current Status: %s", orderId, currentStatus));
    }
}
