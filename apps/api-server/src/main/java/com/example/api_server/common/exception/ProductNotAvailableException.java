package com.example.api_server.common.exception;

/**
 * 상품이 판매 불가 상태일 때 발생하는 예외
 * HTTP Status: 400 BAD REQUEST
 */
public class ProductNotAvailableException extends BusinessException {

    private static final String ERROR_CODE = "PRODUCT_NOT_AVAILABLE";
    private static final String DEFAULT_MESSAGE = "판매 불가능한 상품입니다.";

    public ProductNotAvailableException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public ProductNotAvailableException(Long productId) {
        super(ERROR_CODE, DEFAULT_MESSAGE, "Product ID: " + productId);
    }
}
