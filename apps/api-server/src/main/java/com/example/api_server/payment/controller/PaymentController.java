package com.example.api_server.payment.controller;

import com.example.api_server.payment.dto.ApprovePaymentRequest;
import com.example.api_server.payment.dto.ApprovePaymentResponse;
import com.example.api_server.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 결제 컨트롤러
 * 결제 승인 API를 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 승인
     * POST /api/payments/approve
     *
     * @param request 결제 승인 요청
     * @return 결제 승인 응답 (200 OK)
     */
    @PostMapping("/approve")
    public ResponseEntity<ApprovePaymentResponse> approvePayment(@Valid @RequestBody ApprovePaymentRequest request) {
        log.info("POST /api/payments/approve - orderId: {}, paymentKey: {}, amount: {}",
                request.getOrderId(), request.getPaymentKey(), request.getAmount());

        ApprovePaymentResponse response = paymentService.approvePayment(request);

        log.info("Payment approved successfully - orderId: {}, paymentId: {}, paymentStatus: {}",
                response.getOrderId(), response.getPaymentId(), response.getPaymentStatus());
        return ResponseEntity.ok(response);
    }
}
