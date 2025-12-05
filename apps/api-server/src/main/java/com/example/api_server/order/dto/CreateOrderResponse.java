package com.example.api_server.order.dto;

import com.example.api_server.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * 주문 생성 응답 DTO
 */
@Getter
@Builder
public class CreateOrderResponse {

    /**
     * 주문 ID
     */
    private final String orderId;

    /**
     * 사용자 ID
     */
    private final Long userId;

    /**
     * 상품 ID
     */
    private final Long productId;

    /**
     * 주문 수량
     */
    private final Integer quantity;

    /**
     * 총 금액 (할인 가격 × 수량)
     */
    private final Long totalAmount;

    /**
     * 주문 상태
     */
    private final OrderStatus status;
}
