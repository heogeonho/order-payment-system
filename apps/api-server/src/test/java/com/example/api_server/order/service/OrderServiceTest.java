package com.example.api_server.order.service;

import com.example.api_server.common.exception.OrderNotFoundException;
import com.example.api_server.common.exception.OutOfStockException;
import com.example.api_server.common.exception.ProductNotAvailableException;
import com.example.api_server.common.exception.ProductNotFoundException;
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

@DisplayName("OrderService 테스트")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderHistoryRepository orderHistoryRepository;

    @Mock
    private ProductService productService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("BE_UT_01: 주문 생성 성공")
    void createOrder_성공() throws Exception {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(1L)
                .productId(1L)
                .quantity(2)
                .build();

        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();

        given(productService.getProductOrThrow(1L)).willReturn(product);
        willDoNothing().given(productService).validateProductAvailability(product);
        willDoNothing().given(productService).validateStockAvailability(product, 2);
        given(objectMapper.writeValueAsString(any())).willReturn("{\"userId\":1,\"productId\":1,\"quantity\":2}");

        Order savedOrder = Order.builder()
                .orderId("ORD-20251205-0001")
                .userId(1L)
                .productId(1L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        // when
        CreateOrderResponse response = orderService.createOrder(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).startsWith("ORD-");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getTotalAmount()).isEqualTo(258000L); // 129000 * 2
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);

        // verify
        verify(productService).getProductOrThrow(1L);
        verify(productService).validateProductAvailability(product);
        verify(productService).validateStockAvailability(product, 2);
        verify(orderRepository).save(any(Order.class));
        verify(orderHistoryRepository).save(any(OrderHistory.class));

        // OrderHistory 검증
        ArgumentCaptor<OrderHistory> historyCaptor = ArgumentCaptor.forClass(OrderHistory.class);
        verify(orderHistoryRepository).save(historyCaptor.capture());
        OrderHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getEventType()).isEqualTo(OrderEventType.ORDER_CREATED);
    }

    @Test
    @DisplayName("BE_UT_02: 주문 생성 실패 - 상품 없음")
    void createOrder_실패_상품없음() {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(1L)
                .productId(999L)
                .quantity(1)
                .build();

        given(productService.getProductOrThrow(999L))
                .willThrow(new ProductNotFoundException(999L));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");

        verify(orderRepository, never()).save(any());
        verify(orderHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("BE_UT_02: 주문 생성 실패 - 상품 판매 불가")
    void createOrder_실패_상품판매불가() {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(1L)
                .productId(1L)
                .quantity(1)
                .build();

        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(false)
                .build();

        given(productService.getProductOrThrow(1L)).willReturn(product);
        willThrow(new ProductNotAvailableException(1L))
                .given(productService).validateProductAvailability(product);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotAvailableException.class)
                .hasMessageContaining("판매 불가능한 상품입니다");

        verify(orderRepository, never()).save(any());
        verify(orderHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("BE_UT_02: 주문 생성 실패 - 재고 부족")
    void createOrder_실패_재고부족() {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(1L)
                .productId(1L)
                .quantity(15)
                .build();

        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();

        given(productService.getProductOrThrow(1L)).willReturn(product);
        willDoNothing().given(productService).validateProductAvailability(product);
        willThrow(new OutOfStockException(15, 10))
                .given(productService).validateStockAvailability(product, 15);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("재고가 부족합니다");

        verify(orderRepository, never()).save(any());
        verify(orderHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("BE_UT_02: 주문 생성 실패 - 수량 0")
    void createOrder_실패_수량0() {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(1L)
                .productId(1L)
                .quantity(0)
                .build();

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(QuantityInvalidException.class)
                .hasMessageContaining("유효하지 않은 수량입니다");

        verify(productService, never()).getProductOrThrow(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("BE_UT_02: 주문 생성 실패 - 수량 음수")
    void createOrder_실패_수량음수() {
        // given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(1L)
                .productId(1L)
                .quantity(-1)
                .build();

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(QuantityInvalidException.class)
                .hasMessageContaining("유효하지 않은 수량입니다");

        verify(productService, never()).getProductOrThrow(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("주문 조회 성공")
    void getOrderOrThrow_성공() {
        // given
        String orderId = "ORD-20251205-0001";
        Order order = Order.builder()
                .orderId(orderId)
                .userId(1L)
                .productId(1L)
                .quantity(2)
                .totalAmount(258000L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when
        Order result = orderService.getOrderOrThrow(orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("주문 조회 실패 - 주문 없음")
    void getOrderOrThrow_실패() {
        // given
        String orderId = "ORD-99999999-9999";
        given(orderRepository.findById(orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderOrThrow(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다");
    }
}
