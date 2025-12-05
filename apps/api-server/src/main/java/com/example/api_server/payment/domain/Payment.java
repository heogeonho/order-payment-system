package com.example.api_server.payment.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 50)
    private String orderId;

    @NotNull
    @Column(nullable = false, length = 100)
    private String paymentKey;

    @NotNull
    @Column(nullable = false)
    private Long amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 50)
    private String pgResultCode;

    @Column(length = 500)
    private String pgResultMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void approve(String pgResultCode, String pgResultMessage) {
        if (this.status != PaymentStatus.REQUESTED) {
            throw new IllegalStateException("결제가 요청 상태가 아닙니다.");
        }
        this.status = PaymentStatus.APPROVED;
        this.pgResultCode = pgResultCode;
        this.pgResultMessage = pgResultMessage;
    }

    public void decline(String pgResultCode, String pgResultMessage) {
        if (this.status != PaymentStatus.REQUESTED) {
            throw new IllegalStateException("결제가 요청 상태가 아닙니다.");
        }
        this.status = PaymentStatus.DECLINED;
        this.pgResultCode = pgResultCode;
        this.pgResultMessage = pgResultMessage;
    }

    public boolean isApproved() {
        return this.status == PaymentStatus.APPROVED;
    }
}
