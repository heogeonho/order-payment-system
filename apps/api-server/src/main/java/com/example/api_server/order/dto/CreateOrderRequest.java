package com.example.api_server.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 주문 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    /**
     * 사용자 ID
     */
    @NotNull(message = "사용자 ID는 필수입니다.")
    @Positive(message = "사용자 ID는 양수여야 합니다.")
    private Long userId;

    /**
     * 상품 ID
     */
    @NotNull(message = "상품 ID는 필수입니다.")
    @Positive(message = "상품 ID는 양수여야 합니다.")
    private Long productId;

    /**
     * 주문 수량
     */
    @NotNull(message = "수량은 필수입니다.")
    @Positive(message = "수량은 양수여야 합니다.")
    private Integer quantity;
}
