package com.example.api_server.payment.domain;

import com.example.api_server.payment.domain.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Payment 엔티티 테스트")
class PaymentTest {

    @Test
    @DisplayName("REQUESTED 상태의 결제는 APPROVED로 전이할 수 있다")
    void approve_정상_상태전이() {
        // given
        Payment payment = Payment.builder()
                .id(1L)
                .orderId("ORD-20251204-0001")
                .paymentKey("pay_abc123")
                .amount(258000L)
                .status(PaymentStatus.REQUESTED)
                .build();

        // when
        payment.approve("SUCCESS", "결제 승인 성공");

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getPgResultCode()).isEqualTo("SUCCESS");
        assertThat(payment.getPgResultMessage()).isEqualTo("결제 승인 성공");
        assertThat(payment.isApproved()).isTrue();
    }

    @Test
    @DisplayName("APPROVED 상태의 결제를 다시 approve하려 하면 예외가 발생한다")
    void approve_이미_APPROVED_상태면_예외() {
        // given
        Payment payment = Payment.builder()
                .id(1L)
                .orderId("ORD-20251204-0001")
                .paymentKey("pay_abc123")
                .amount(258000L)
                .status(PaymentStatus.APPROVED)
                .pgResultCode("SUCCESS")
                .pgResultMessage("결제 승인 성공")
                .build();

        // when & then
        assertThatThrownBy(() -> payment.approve("SUCCESS", "결제 승인 성공"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제가 요청 상태가 아닙니다");
    }

    @Test
    @DisplayName("REQUESTED 상태의 결제는 DECLINED로 전이할 수 있다")
    void decline_정상_상태전이() {
        // given
        Payment payment = Payment.builder()
                .id(1L)
                .orderId("ORD-20251204-0001")
                .paymentKey("pay_abc123")
                .amount(258000L)
                .status(PaymentStatus.REQUESTED)
                .build();

        // when
        payment.decline("FAIL_001", "결제 승인 실패");

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DECLINED);
        assertThat(payment.getPgResultCode()).isEqualTo("FAIL_001");
        assertThat(payment.getPgResultMessage()).isEqualTo("결제 승인 실패");
        assertThat(payment.isApproved()).isFalse();
    }

    @Test
    @DisplayName("DECLINED 상태의 결제를 다시 decline하려 하면 예외가 발생한다")
    void decline_이미_DECLINED_상태면_예외() {
        // given
        Payment payment = Payment.builder()
                .id(1L)
                .orderId("ORD-20251204-0001")
                .paymentKey("pay_abc123")
                .amount(258000L)
                .status(PaymentStatus.DECLINED)
                .pgResultCode("FAIL_001")
                .pgResultMessage("결제 승인 실패")
                .build();

        // when & then
        assertThatThrownBy(() -> payment.decline("FAIL_001", "결제 승인 실패"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제가 요청 상태가 아닙니다");
    }

    @Test
    @DisplayName("isApproved는 APPROVED 상태일 때 true를 반환한다")
    void isApproved_APPROVED_상태면_true() {
        // given
        Payment payment = Payment.builder()
                .id(1L)
                .orderId("ORD-20251204-0001")
                .paymentKey("pay_abc123")
                .amount(258000L)
                .status(PaymentStatus.APPROVED)
                .build();

        // when
        boolean result = payment.isApproved();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isApproved는 REQUESTED 상태일 때 false를 반환한다")
    void isApproved_REQUESTED_상태면_false() {
        // given
        Payment payment = Payment.builder()
                .id(1L)
                .orderId("ORD-20251204-0001")
                .paymentKey("pay_abc123")
                .amount(258000L)
                .status(PaymentStatus.REQUESTED)
                .build();

        // when
        boolean result = payment.isApproved();

        // then
        assertThat(result).isFalse();
    }
}