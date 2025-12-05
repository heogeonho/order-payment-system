package com.example.api_server.payment.service;

import com.example.api_server.common.exception.AmountMismatchException;
import com.example.api_server.common.exception.OrderNotPayableException;
import com.example.api_server.common.exception.PaymentAlreadyApprovedException;
import com.example.api_server.order.domain.Order;
import com.example.api_server.order.domain.OrderEventType;
import com.example.api_server.order.domain.OrderHistory;
import com.example.api_server.order.domain.OrderStatus;
import com.example.api_server.order.repository.OrderHistoryRepository;
import com.example.api_server.order.repository.OrderRepository;
import com.example.api_server.order.service.OrderService;
import com.example.api_server.payment.client.PgApprovalResult;
import com.example.api_server.payment.client.PgClient;
import com.example.api_server.payment.domain.Payment;
import com.example.api_server.payment.domain.PaymentStatus;
import com.example.api_server.payment.dto.ApprovePaymentRequest;
import com.example.api_server.payment.dto.ApprovePaymentResponse;
import com.example.api_server.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("PaymentService 테스트")
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderHistoryRepository orderHistoryRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private PgClient pgClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("BE_UT_03: 결제 승인 성공")
    void approvePayment_성공() throws Exception {
        // given
        ApprovePaymentRequest request = ApprovePaymentRequest.builder()
                .orderId("ORD-20251205-0001")
                .paymentKey("PAY-KEY-12345")
                .amount(258000L)
                .build();

        Order order = Order.builder()
                .orderId("ORD-20251205-0001")
                .userId(1L)
                .productId(1L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .orderId("ORD-20251205-0001")
                .paymentKey("PAY-KEY-12345")
                .amount(258000L)
                .status(PaymentStatus.APPROVED)
                .pgResultCode("0000")
                .pgResultMessage("승인 성공")
                .build();

        PgApprovalResult pgResult = PgApprovalResult.success();

        given(orderService.getOrderOrThrow("ORD-20251205-0001")).willReturn(order);
        given(paymentRepository.findByOrderId("ORD-20251205-0001")).willReturn(Optional.empty());
        given(pgClient.approve("PAY-KEY-12345", "ORD-20251205-0001", 258000L)).willReturn(pgResult);
        given(paymentRepository.save(any(Payment.class))).willReturn(payment);
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(objectMapper.writeValueAsString(any())).willReturn("{\"orderId\":\"ORD-20251205-0001\"}");

        // when
        ApprovePaymentResponse response = paymentService.approvePayment(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo("ORD-20251205-0001");
        assertThat(response.getPaymentId()).isEqualTo(1L);
        assertThat(response.getPaymentKey()).isEqualTo("PAY-KEY-12345");
        assertThat(response.getAmount()).isEqualTo(258000L);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PAID);

        // verify
        verify(pgClient).approve("PAY-KEY-12345", "ORD-20251205-0001", 258000L);
        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository).save(any(Order.class));

        // Order 상태 변경 검증
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        // OrderHistory 검증
        ArgumentCaptor<OrderHistory> historyCaptor = ArgumentCaptor.forClass(OrderHistory.class);
        verify(orderHistoryRepository).save(historyCaptor.capture());
        OrderHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getEventType()).isEqualTo(OrderEventType.PAYMENT_APPROVED);
    }

    @Test
    @DisplayName("BE_UT_04: 결제 승인 - PG 실패로 거절")
    void approvePayment_PG실패() throws Exception {
        // given
        ApprovePaymentRequest request = ApprovePaymentRequest.builder()
                .orderId("ORD-20251205-0001")
                .paymentKey("FAIL-KEY-12345")
                .amount(258000L)
                .build();

        Order order = Order.builder()
                .orderId("ORD-20251205-0001")
                .userId(1L)
                .productId(1L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .orderId("ORD-20251205-0001")
                .paymentKey("FAIL-KEY-12345")
                .amount(258000L)
                .status(PaymentStatus.DECLINED)
                .pgResultCode("PG_INVALID_KEY")
                .pgResultMessage("유효하지 않은 결제 키입니다.")
                .build();

        PgApprovalResult pgResult = PgApprovalResult.failure("PG_INVALID_KEY", "유효하지 않은 결제 키입니다.");

        given(orderService.getOrderOrThrow("ORD-20251205-0001")).willReturn(order);
        given(paymentRepository.findByOrderId("ORD-20251205-0001")).willReturn(Optional.empty());
        given(pgClient.approve("FAIL-KEY-12345", "ORD-20251205-0001", 258000L)).willReturn(pgResult);
        given(paymentRepository.save(any(Payment.class))).willReturn(payment);
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(objectMapper.writeValueAsString(any())).willReturn("{\"orderId\":\"ORD-20251205-0001\"}");

        // when
        ApprovePaymentResponse response = paymentService.approvePayment(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.DECLINED);
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);

        // verify
        verify(pgClient).approve("FAIL-KEY-12345", "ORD-20251205-0001", 258000L);

        // Order 상태 변경 검증
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);

        // OrderHistory 검증
        ArgumentCaptor<OrderHistory> historyCaptor = ArgumentCaptor.forClass(OrderHistory.class);
        verify(orderHistoryRepository).save(historyCaptor.capture());
        OrderHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getEventType()).isEqualTo(OrderEventType.PAYMENT_FAILED);
    }

    @Test
    @DisplayName("BE_UT_05: 결제 승인 실패 - 주문이 결제 가능 상태가 아님")
    void approvePayment_실패_주문상태불가() {
        // given
        ApprovePaymentRequest request = ApprovePaymentRequest.builder()
                .orderId("ORD-20251205-0001")
                .paymentKey("PAY-KEY-12345")
                .amount(258000L)
                .build();

        Order order = Order.builder()
                .orderId("ORD-20251205-0001")
                .userId(1L)
                .productId(1L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PAID)  // 이미 결제 완료된 상태
                .build();

        given(orderService.getOrderOrThrow("ORD-20251205-0001")).willReturn(order);

        // when & then
        assertThatThrownBy(() -> paymentService.approvePayment(request))
                .isInstanceOf(OrderNotPayableException.class)
                .hasMessageContaining("결제할 수 없는 주문 상태입니다");

        verify(pgClient, never()).approve(any(), any(), any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("BE_UT_05: 결제 승인 실패 - 금액 불일치")
    void approvePayment_실패_금액불일치() {
        // given
        ApprovePaymentRequest request = ApprovePaymentRequest.builder()
                .orderId("ORD-20251205-0001")
                .paymentKey("PAY-KEY-12345")
                .amount(100000L)  // 주문 금액과 다름
                .build();

        Order order = Order.builder()
                .orderId("ORD-20251205-0001")
                .userId(1L)
                .productId(1L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        given(orderService.getOrderOrThrow("ORD-20251205-0001")).willReturn(order);
        given(paymentRepository.findByOrderId("ORD-20251205-0001")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.approvePayment(request))
                .isInstanceOf(AmountMismatchException.class)
                .hasMessageContaining("결제 금액이 주문 금액과 일치하지 않습니다");

        verify(pgClient, never()).approve(any(), any(), any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("BE_UT_05: 결제 승인 실패 - 이미 승인된 결제")
    void approvePayment_실패_중복결제() {
        // given
        ApprovePaymentRequest request = ApprovePaymentRequest.builder()
                .orderId("ORD-20251205-0001")
                .paymentKey("PAY-KEY-12345")
                .amount(258000L)
                .build();

        Order order = Order.builder()
                .orderId("ORD-20251205-0001")
                .userId(1L)
                .productId(1L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        Payment existingPayment = Payment.builder()
                .id(1L)
                .orderId("ORD-20251205-0001")
                .paymentKey("PAY-KEY-EXISTING")
                .amount(258000L)
                .status(PaymentStatus.APPROVED)
                .build();

        given(orderService.getOrderOrThrow("ORD-20251205-0001")).willReturn(order);
        given(paymentRepository.findByOrderId("ORD-20251205-0001"))
                .willReturn(Optional.of(existingPayment));

        // when & then
        assertThatThrownBy(() -> paymentService.approvePayment(request))
                .isInstanceOf(PaymentAlreadyApprovedException.class)
                .hasMessageContaining("이미 승인된 결제입니다");

        verify(pgClient, never()).approve(any(), any(), any());
    }
}
