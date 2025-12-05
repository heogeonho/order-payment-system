package com.example.api_server.order.repository;

import com.example.api_server.order.domain.Order;
import com.example.api_server.order.domain.OrderStatus;
import com.example.api_server.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("OrderRepository 테스트")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문을 저장하고 조회할 수 있다")
    void save_조회_성공() {
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
        orderRepository.save(order);
        orderRepository.flush();

        // then
        Optional<Order> found = orderRepository.findById("ORD-20251204-0001");
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getProductId()).isEqualTo(101L);
        assertThat(found.get().getQuantity()).isEqualTo(2);
        assertThat(found.get().getTotalAmount()).isEqualTo(258000L);
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 시 Optional.empty를 반환한다")
    void findById_존재하지않으면_empty() {
        // when
        Optional<Order> found = orderRepository.findById("ORD-NONEXISTENT");

        // then
        assertThat(found).isEmpty();
    }
}