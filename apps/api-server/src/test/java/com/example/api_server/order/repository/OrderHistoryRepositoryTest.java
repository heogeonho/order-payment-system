package com.example.api_server.order.repository;

import com.example.api_server.order.domain.OrderHistory;
import com.example.api_server.order.domain.OrderEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("OrderHistoryRepository 테스트")
class OrderHistoryRepositoryTest {

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Test
    @DisplayName("주문 히스토리를 저장할 수 있다")
    void save_성공() {
        // given
        OrderHistory history = OrderHistory.builder()
                .orderId("ORD-20251204-0001")
                .eventType(OrderEventType.ORDER_CREATED)
                .payloadJson("{\"productId\": 101, \"quantity\": 2, \"totalAmount\": 258000}")
                .build();

        // when
        OrderHistory saved = orderHistoryRepository.save(history);
        orderHistoryRepository.flush();

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderId()).isEqualTo("ORD-20251204-0001");
        assertThat(saved.getEventType()).isEqualTo(OrderEventType.ORDER_CREATED);
    }

    @Test
    @DisplayName("orderId로 히스토리 목록을 생성 시간 순으로 조회할 수 있다")
    void findByOrderIdOrderByCreatedAtAsc_성공() throws InterruptedException {
        // given
        OrderHistory history1 = OrderHistory.builder()
                .orderId("ORD-20251204-0001")
                .eventType(OrderEventType.ORDER_CREATED)
                .payloadJson("{\"productId\": 101}")
                .build();
        orderHistoryRepository.save(history1);
        orderHistoryRepository.flush();

        Thread.sleep(10); // 시간 차이를 두기 위해

        OrderHistory history2 = OrderHistory.builder()
                .orderId("ORD-20251204-0001")
                .eventType(OrderEventType.PAYMENT_APPROVED)
                .payloadJson("{\"paymentKey\": \"pay_abc123\"}")
                .build();
        orderHistoryRepository.save(history2);
        orderHistoryRepository.flush();

        // when
        List<OrderHistory> histories = orderHistoryRepository.findByOrderIdOrderByCreatedAtAsc("ORD-20251204-0001");

        // then
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).getEventType()).isEqualTo(OrderEventType.ORDER_CREATED);
        assertThat(histories.get(1).getEventType()).isEqualTo(OrderEventType.PAYMENT_APPROVED);
    }

    @Test
    @DisplayName("존재하지 않는 orderId로 조회 시 빈 리스트를 반환한다")
    void findByOrderIdOrderByCreatedAtAsc_존재하지않으면_빈리스트() {
        // when
        List<OrderHistory> histories = orderHistoryRepository.findByOrderIdOrderByCreatedAtAsc("ORD-NONEXISTENT");

        // then
        assertThat(histories).isEmpty();
    }
}