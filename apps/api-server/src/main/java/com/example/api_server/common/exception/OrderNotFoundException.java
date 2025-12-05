package com.example.api_server.common.exception;

/**
 * 주문을 찾을 수 없을 때 발생하는 예외
 * HTTP Status: 404 NOT FOUND
 */
public class OrderNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "ORDER_NOT_FOUND";
    private static final String DEFAULT_MESSAGE = "주문을 찾을 수 없습니다.";

    public OrderNotFoundException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public OrderNotFoundException(String orderId) {
        super(ERROR_CODE, DEFAULT_MESSAGE, "Order ID: " + orderId);
    }
}
