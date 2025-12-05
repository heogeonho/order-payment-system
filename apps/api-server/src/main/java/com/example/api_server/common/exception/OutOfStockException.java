package com.example.api_server.common.exception;

/**
 * 재고가 부족할 때 발생하는 예외
 * HTTP Status: 400 BAD REQUEST
 */
public class OutOfStockException extends BusinessException {

    private static final String ERROR_CODE = "OUT_OF_STOCK";
    private static final String DEFAULT_MESSAGE = "재고가 부족합니다.";

    public OutOfStockException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public OutOfStockException(int requestedQuantity, int availableStock) {
        super(ERROR_CODE, DEFAULT_MESSAGE,
              String.format("Requested: %d, Available: %d", requestedQuantity, availableStock));
    }
}
