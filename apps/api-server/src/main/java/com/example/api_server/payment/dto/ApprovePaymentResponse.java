package com.example.api_server.payment.dto;

import com.example.api_server.order.domain.OrderStatus;
import com.example.api_server.payment.domain.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 결제 승인 응답 DTO
 */
@Getter
@Builder
public class ApprovePaymentResponse {

    /**
     * 주문 ID
     */
    private final String orderId;

    /**
     * 결제 ID
     */
    private final Long paymentId;

    /**
     * PG사에서 발급한 결제 키
     */
    private final String paymentKey;

    /**
     * 결제 금액
     */
    private final Long amount;

    /**
     * 결제 상태
     */
    private final PaymentStatus paymentStatus;

    /**
     * 주문 상태
     */
    private final OrderStatus orderStatus;

    /**
     * 승인 시각 (결제 생성 시각)
     */
    private final LocalDateTime approvedAt;
}
