package com.example.api_server.payment.repository;

import com.example.api_server.payment.domain.Payment;
import com.example.api_server.payment.domain.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("PaymentRepository 테스트")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("결제를 저장하고 조회할 수 있다")
    void save_조회_성공() {
        // given
        Payment payment = Payment.builder()
                .orderId("ORD-20251204-0001")
                .paymentKey("pay_abc123")
                .amount(258000L)
                .status(PaymentStatus.REQUESTED)
                .build();

        // when
        Payment saved = paymentRepository.save(payment);
        paymentRepository.flush();

        // then
        Optional<Payment> found = paymentRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getOrderId()).isEqualTo("ORD-20251204-0001");
        assertThat(found.get().getPaymentKey()).isEqualTo("pay_abc123");
        assertThat(found.get().getAmount()).isEqualTo(258000L);
        assertThat(found.get().getStatus()).isEqualTo(PaymentStatus.REQUESTED);
    }

    @Test
    @DisplayName("orderId로 결제를 조회할 수 있다")
    void findByOrderId_성공() {
        // given
        Payment payment = Payment.builder()
                .orderId("ORD-20251204-0001")
                .paymentKey("pay_abc123")
                .amount(258000L)
                .status(PaymentStatus.REQUESTED)
                .build();
        paymentRepository.save(payment);
        paymentRepository.flush();

        // when
        Optional<Payment> found = paymentRepository.findByOrderId("ORD-20251204-0001");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getPaymentKey()).isEqualTo("pay_abc123");
    }

    @Test
    @DisplayName("존재하지 않는 orderId로 조회 시 Optional.empty를 반환한다")
    void findByOrderId_존재하지않으면_empty() {
        // when
        Optional<Payment> found = paymentRepository.findByOrderId("ORD-NONEXISTENT");

        // then
        assertThat(found).isEmpty();
    }
}