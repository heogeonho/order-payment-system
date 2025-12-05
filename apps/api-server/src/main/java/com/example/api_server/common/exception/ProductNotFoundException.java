package com.example.api_server.common.exception;

/**
 * 상품을 찾을 수 없을 때 발생하는 예외
 * HTTP Status: 404 NOT FOUND
 */
public class ProductNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "PRODUCT_NOT_FOUND";
    private static final String DEFAULT_MESSAGE = "상품을 찾을 수 없습니다.";

    public ProductNotFoundException() {
        super(ERROR_CODE, DEFAULT_MESSAGE);
    }

    public ProductNotFoundException(Long productId) {
        super(ERROR_CODE, DEFAULT_MESSAGE, "Product ID: " + productId);
    }
}
