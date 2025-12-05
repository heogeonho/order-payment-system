package com.example.api_server.common.exception;

/**
 * 주문 수량이 유효하지 않을 때 발생하는 예외
 * HTTP Status: 400 BAD REQUEST
 */
public class QuantityInvalidException extends BusinessException {

    private static final String ERROR_CODE = "QUANTITY_INVALID";
    private static final String DEFAULT_MESSAGE = "유효하지 않은 수량입니다.";

    public QuantityInvalidException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public QuantityInvalidException(int quantity) {
        super(ERROR_CODE, DEFAULT_MESSAGE, "Quantity: " + quantity);
    }
}
