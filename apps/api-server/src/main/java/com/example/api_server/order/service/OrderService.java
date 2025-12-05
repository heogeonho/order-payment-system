package com.example.api_server.order.service;

import com.example.api_server.common.exception.OrderNotFoundException;
import com.example.api_server.common.exception.QuantityInvalidException;
import com.example.api_server.order.domain.Order;
import com.example.api_server.order.domain.OrderEventType;
import com.example.api_server.order.domain.OrderHistory;
import com.example.api_server.order.domain.OrderStatus;
import com.example.api_server.order.dto.CreateOrderRequest;
import com.example.api_server.order.dto.CreateOrderResponse;
import com.example.api_server.order.repository.OrderHistoryRepository;
import com.example.api_server.order.repository.OrderRepository;
import com.example.api_server.product.domain.Product;
import com.example.api_server.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 주문 서비스
 * 주문 생성 및 관리 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    /**
     * 주문 생성
     *
     * @param request 주문 생성 요청
     * @return 주문 생성 응답
     */
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order - userId: {}, productId: {}, quantity: {}",
                request.getUserId(), request.getProductId(), request.getQuantity());

        // 1. 수량 검증
        validateQuantity(request.getQuantity());

        // 2. 상품 조회 및 검증
        Product product = productService.getProductOrThrow(request.getProductId());
        productService.validateProductAvailability(product);
        productService.validateStockAvailability(product, request.getQuantity());

        // TODO: 재고 차감은 결제 승인 성공 시점(PaymentService)에 처리
        //       현재는 재고 검증만 수행하고 실제 차감은 하지 않음

        // 3. 총 금액 계산
        Long totalAmount = product.getDiscountPrice() * request.getQuantity();

        // 4. 주문 ID 생성
        String orderId = generateOrderId();

        // 5. 주문 생성
        Order order = Order.builder()
                .orderId(orderId)
                .userId(request.getUserId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        orderRepository.save(order);

        // 6. 주문 이력 기록
        recordOrderHistory(orderId, OrderEventType.ORDER_CREATED, request);

        log.info("Order created successfully - orderId: {}", orderId);

        return CreateOrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build();
    }

    /**
     * 주문 조회 (없으면 예외 발생)
     *
     * @param orderId 주문 ID
     * @return 주문 엔티티
     * @throws OrderNotFoundException 주문을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Order getOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * 수량 검증
     */
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new QuantityInvalidException(quantity);
        }
    }

    /**
     * 주문 ID 생성
     * 형식: ORD-YYYYMMDD-NNNN
     */
    private String generateOrderId() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return String.format("ORD-%s-%s", datePart, randomPart);
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
