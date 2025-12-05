/**
 * 결제 API - API-PAY-001
 */
import apiClient from './client'
import type { ApprovePaymentRequest, ApprovePaymentResponse } from '@/types'

/**
 * 결제 승인
 * API ID: API-PAY-001
 * Method: POST
 * Path: /api/payments/approve
 *
 * 비즈니스 규칙:
 * - Rule-03: 금액 계산 규칙 (amount === Order.totalAmount)
 * - Rule-04: 상태 전이 규칙 (Order: PENDING_PAYMENT → PAID, Payment: REQUESTED → APPROVED)
 * - Rule-05: 히스토리 기록 규칙 (PAYMENT_APPROVED / PAYMENT_FAILED)
 */
export async function approvePayment(
  request: ApprovePaymentRequest,
): Promise<ApprovePaymentResponse> {
  const response = await apiClient.post<ApprovePaymentResponse>(
    '/payments/approve',
    request,
  )
  return response.data
}

