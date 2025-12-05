package com.example.api_server.payment.service;

import com.example.api_server.common.exception.AmountMismatchException;
import com.example.api_server.common.exception.OrderNotPayableException;
import com.example.api_server.common.exception.PaymentAlreadyApprovedException;
import com.example.api_server.order.domain.Order;
import com.example.api_server.order.domain.OrderEventType;
import com.example.api_server.order.domain.OrderHistory;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 서비스
 * 결제 승인 및 관리 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderService orderService;
    private final PgClient pgClient;
    private final ObjectMapper objectMapper;

    /**
     * 결제 승인
     *
     * @param request 결제 승인 요청
     * @return 결제 승인 응답
     */
    public ApprovePaymentResponse approvePayment(ApprovePaymentRequest request) {
        log.info("Approving payment - orderId: {}, paymentKey: {}, amount: {}",
                request.getOrderId(), request.getPaymentKey(), request.getAmount());

        // 1. 주문 조회
        Order order = orderService.getOrderOrThrow(request.getOrderId());

        // 2. 주문 상태 검증 (PENDING_PAYMENT만 결제 가능)
        validateOrderPayable(order);

        // 3. 중복 결제 검증
        validatePaymentNotExists(request.getOrderId());

        // 4. 금액 검증
        validateAmount(request.getAmount(), order.getTotalAmount());

        // 5. PG사 승인 요청
        PgApprovalResult pgResult = pgClient.approve(
                request.getPaymentKey(),
                request.getOrderId(),
                request.getAmount()
        );

        Payment payment;
        if (pgResult.isSuccess()) {
            // PG 승인 성공
            payment = handlePgSuccess(order, request, pgResult);
        } else {
            // PG 승인 실패
            payment = handlePgFailure(order, request, pgResult);
        }

        log.info("Payment approval completed - orderId: {}, paymentStatus: {}, orderStatus: {}",
                request.getOrderId(), payment.getStatus(), order.getStatus());

        return ApprovePaymentResponse.builder()
                .orderId(order.getOrderId())
                .paymentId(payment.getId())
                .paymentKey(payment.getPaymentKey())
                .amount(payment.getAmount())
                .paymentStatus(payment.getStatus())
                .orderStatus(order.getStatus())
                .approvedAt(payment.getCreatedAt())
                .build();
    }

    /**
     * 주문이 결제 가능한 상태인지 검증
     */
    private void validateOrderPayable(Order order) {
        if (!order.isPendingPayment()) {
            throw new OrderNotPayableException(order.getOrderId(), order.getStatus().name());
        }
    }

    /**
     * 이미 결제가 존재하는지 검증
     */
    private void validatePaymentNotExists(String orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            if (payment.isApproved()) {
                throw new PaymentAlreadyApprovedException(orderId);
            }
        });
    }

    /**
     * 결제 금액과 주문 금액이 일치하는지 검증
     */
    private void validateAmount(Long requestAmount, Long orderAmount) {
        if (!requestAmount.equals(orderAmount)) {
            throw new AmountMismatchException(requestAmount, orderAmount);
        }
    }

    /**
     * PG 승인 성공 처리
     */
    private Payment handlePgSuccess(Order order, ApprovePaymentRequest request, PgApprovalResult pgResult) {
        log.info("PG approval succeeded - orderId: {}", order.getOrderId());

        // TODO: [재고 차감 로직 추가 필요]
        //       1. ProductRepository에서 상품 조회
        //       2. product.decreaseStock(order.getQuantity()) 호출
        //       3. productRepository.save(product)로 재고 차감 저장
        //       4. 재고 부족 시 OutOfStockException 발생 처리
        //       5. 유닛 테스트 추가:
        //          - 결제 성공 시 재고 차감 확인
        //          - 결제 실패 시 재고 변동 없음 확인
        //          - 동시성 테스트 (낙관적 락 또는 비관적 락 고려)

        // 1. Payment 엔티티 생성 (APPROVED)
        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .paymentKey(request.getPaymentKey())
                .amount(request.getAmount())
                .status(PaymentStatus.APPROVED)
                .pgResultCode(pgResult.getResultCode())
                .pgResultMessage(pgResult.getResultMessage())
                .build();
        payment = paymentRepository.save(payment);

        // 2. Order 상태 변경 (PAID)
        order.markAsPaid();
        orderRepository.save(order);

        // 3. OrderHistory 기록 (PAYMENT_APPROVED)
        recordOrderHistory(order.getOrderId(), OrderEventType.PAYMENT_APPROVED, request);

        return payment;
    }

    /**
     * PG 승인 실패 처리
     */
    private Payment handlePgFailure(Order order, ApprovePaymentRequest request, PgApprovalResult pgResult) {
        log.warn("PG approval failed - orderId: {}, pgCode: {}, pgMessage: {}",
                order.getOrderId(), pgResult.getResultCode(), pgResult.getResultMessage());

        // 1. Payment 엔티티 생성 (DECLINED)
        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .paymentKey(request.getPaymentKey())
                .amount(request.getAmount())
                .status(PaymentStatus.DECLINED)
                .pgResultCode(pgResult.getResultCode())
                .pgResultMessage(pgResult.getResultMessage())
                .build();
        payment = paymentRepository.save(payment);

        // 2. Order 상태 변경 (PAYMENT_FAILED)
        order.markAsPaymentFailed();
        orderRepository.save(order);

        // 3. OrderHistory 기록 (PAYMENT_FAILED)
        recordOrderHistory(order.getOrderId(), OrderEventType.PAYMENT_FAILED, request);

        return payment;
    }

    /**
     * 주문 이력 기록
     */
    private void recordOrderHistory(String orderId, OrderEventType eventType, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            OrderHistory history = OrderHistory.builder()
                    .orderId(orderId)
                    .eventType(eventType)
                    .payloadJson(payloadJson)
                    .build();
            orderHistoryRepository.save(history);
            log.debug("Order history recorded - orderId: {}, eventType: {}", orderId, eventType);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order history payload", e);
            throw new RuntimeException("주문 이력 기록 중 오류가 발생했습니다.", e);
        }
    }
}
