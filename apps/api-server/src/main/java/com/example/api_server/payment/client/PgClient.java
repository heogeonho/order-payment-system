package com.example.api_server.payment.client;

/**
 * PG(Payment Gateway) 연동 클라이언트 인터페이스
 * 외부 PG사와의 통신을 담당
 */
public interface PgClient {

    /**
     * PG사에 결제 승인을 요청
     *
     * @param paymentKey PG사에서 발급한 결제 키
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @return PG 승인 결과
     */
    PgApprovalResult approve(String paymentKey, String orderId, Long amount);
}