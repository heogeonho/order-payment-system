package com.example.api_server.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 결제 승인 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovePaymentRequest {

    /**
     * 주문 ID
     */
    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;

    /**
     * PG사에서 발급한 결제 키
     */
    @NotBlank(message = "결제 키는 필수입니다.")
    private String paymentKey;

    /**
     * 결제 금액
     */
    @NotNull(message = "결제 금액은 필수입니다.")
    @Positive(message = "결제 금액은 양수여야 합니다.")
    private Long amount;
}
