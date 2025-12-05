package com.example.api_server.order.controller;

import com.example.api_server.order.dto.CreateOrderRequest;
import com.example.api_server.order.dto.CreateOrderResponse;
import com.example.api_server.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 주문 컨트롤러
 * 주문 생성 API를 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성
     * POST /api/orders
     *
     * @param request 주문 생성 요청
     * @return 주문 생성 응답 (201 Created)
     */
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /api/orders - userId: {}, productId: {}, quantity: {}",
                request.getUserId(), request.getProductId(), request.getQuantity());

        CreateOrderResponse response = orderService.createOrder(request);

        log.info("Order created successfully - orderId: {}", response.getOrderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
