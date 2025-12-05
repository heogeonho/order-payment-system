package com.example.api_server.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock PG 클라이언트 구현체
 * 실제 PG사 연동 대신 시뮬레이션을 제공
 */
@Slf4j
@Component
public class MockPgClient implements PgClient {

    /**
     * Mock PG 승인 처리
     * - paymentKey가 "FAIL"로 시작하면 실패 반환
     * - amount가 음수이면 실패 반환
     * - 그 외에는 성공 반환
     */
    @Override
    public PgApprovalResult approve(String paymentKey, String orderId, Long amount) {
        log.info("MockPgClient.approve called - paymentKey: {}, orderId: {}, amount: {}",
                paymentKey, orderId, amount);

        // 실패 시나리오 1: paymentKey가 "FAIL"로 시작
        if (paymentKey != null && paymentKey.startsWith("FAIL")) {
            log.warn("PG approval failed - Invalid payment key: {}", paymentKey);
            return PgApprovalResult.failure("PG_INVALID_KEY", "유효하지 않은 결제 키입니다.");
        }

        // 실패 시나리오 2: amount가 음수
        if (amount != null && amount < 0) {
            log.warn("PG approval failed - Invalid amount: {}", amount);
            return PgApprovalResult.failure("PG_INVALID_AMOUNT", "유효하지 않은 결제 금액입니다.");
        }

        // 성공 시나리오
        log.info("PG approval succeeded for orderId: {}", orderId);
        return PgApprovalResult.success();
    }
}
