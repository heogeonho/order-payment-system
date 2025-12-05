package com.example.api_server.order.domain;

import com.example.api_server.order.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order 엔티티 테스트")
class OrderTest {

    @Test
    @DisplayName("PENDING_PAYMENT 상태의 주문은 PAID로 전이할 수 있다")
    void markAsPaid_정상_상태전이() {
        // given
        Order order = Order.builder()
                .orderId("ORD-20251204-0001")
                .userId(1L)
                .productId(101L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        // when
        order.markAsPaid();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.isPaid()).isTrue();
    }

    @Test
    @DisplayName("PAID 상태의 주문을 다시 PAID로 전이하려 하면 예외가 발생한다")
    void markAsPaid_이미_PAID_상태면_예외() {
        // given
        Order order = Order.builder()
                .orderId("ORD-20251204-0001")
                .userId(1L)
                .productId(101L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PAID)
                .build();

        // when & then
        assertThatThrownBy(() -> order.markAsPaid())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문이 결제 대기 상태가 아닙니다");
    }

    @Test
    @DisplayName("PENDING_PAYMENT 상태의 주문은 PAYMENT_FAILED로 전이할 수 있다")
    void markAsPaymentFailed_정상_상태전이() {
        // given
        Order order = Order.builder()
                .orderId("ORD-20251204-0001")
                .userId(1L)
                .productId(101L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        // when
        order.markAsPaymentFailed();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
    }

    @Test
    @DisplayName("PAYMENT_FAILED 상태의 주문을 다시 PAYMENT_FAILED로 전이하려 하면 예외가 발생한다")
    void markAsPaymentFailed_이미_PAYMENT_FAILED_상태면_예외() {
        // given
        Order order = Order.builder()
                .orderId("ORD-20251204-0001")
                .userId(1L)
                .productId(101L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PAYMENT_FAILED)
                .build();

        // when & then
        assertThatThrownBy(() -> order.markAsPaymentFailed())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문이 결제 대기 상태가 아닙니다");
    }

    @Test
    @DisplayName("isPendingPayment는 PENDING_PAYMENT 상태일 때 true를 반환한다")
    void isPendingPayment_PENDING_PAYMENT_상태면_true() {
        // given
        Order order = Order.builder()
                .orderId("ORD-20251204-0001")
                .userId(1L)
                .productId(101L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        // when
        boolean result = order.isPendingPayment();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isPaid는 PAID 상태일 때 true를 반환한다")
    void isPaid_PAID_상태면_true() {
        // given
        Order order = Order.builder()
                .orderId("ORD-20251204-0001")
                .userId(1L)
                .productId(101L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PAID)
                .build();

        // when
        boolean result = order.isPaid();

        // then
        assertThat(result).isTrue();
    }
}