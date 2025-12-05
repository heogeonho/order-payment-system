/**
 * Payment Store - 결제 관련 상태 관리
 *
 * 관련 API: API-PAY-001 (POST /api/payments/approve)
 *
 * 비즈니스 규칙:
 * - Rule-03: 금액 계산 규칙 (amount === Order.totalAmount)
 * - Rule-04: 상태 전이 규칙 (Order: PENDING_PAYMENT → PAID, Payment: REQUESTED → APPROVED)
 * - Rule-05: 히스토리 기록 규칙 (PAYMENT_APPROVED / PAYMENT_FAILED)
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { approvePayment } from '@/api'
import type {
  ApprovePaymentRequest,
  ApprovePaymentResponse,
  ApiErrorResponse,
  ErrorCode,
} from '@/types'
import axios from 'axios'

export const usePaymentStore = defineStore('payment', () => {
  // ============================================
  // State
  // ============================================

  /** 결제 승인 결과 */
  const paymentResult = ref<ApprovePaymentResponse | null>(null)

  /** 로딩 상태 */
  const isLoading = ref(false)

  /** 에러 정보 */
  const error = ref<ApiErrorResponse | null>(null)

  // ============================================
  // Actions
  // ============================================

  /**
   * 결제 승인 요청
   * API: POST /api/payments/approve
   *
   * @param request 결제 승인 요청 (orderId, paymentKey, amount)
   * @returns 성공 여부
   *
   * 에러 케이스:
   * - ORDER_NOT_FOUND: 주문이 존재하지 않음
   * - ORDER_NOT_PAYABLE: 결제 가능 상태가 아님 (이미 PAID 또는 PAYMENT_FAILED)
   * - AMOUNT_MISMATCH: 결제 금액 불일치 (Rule-03 위반)
   * - PG_APPROVAL_FAILED: PG 승인 실패
   * - PAYMENT_ALREADY_APPROVED: 이미 승인된 결제
   */
  async function requestApproval(request: ApprovePaymentRequest): Promise<boolean> {
    isLoading.value = true
    error.value = null
    paymentResult.value = null

    try {
      const response = await approvePayment(request)
      paymentResult.value = response
      return true
    } catch (e) {
      // API 에러 응답 처리
      if (axios.isAxiosError(e) && e.response?.data) {
        const errorData = e.response.data as ApiErrorResponse
        error.value = {
          code: errorData.code || ('UNKNOWN_ERROR' as ErrorCode),
          message: errorData.message || '결제 승인 중 오류가 발생했습니다.',
          detail: errorData.detail,
        }
      } else if (e instanceof Error) {
        error.value = {
          code: 'UNKNOWN_ERROR' as ErrorCode,
          message: e.message,
        }
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 결제 결과 초기화
   */
  function clearPaymentResult(): void {
    paymentResult.value = null
  }

  /**
   * 에러 초기화
   */
  function clearError(): void {
    error.value = null
  }

  /**
   * 전체 상태 초기화
   */
  function reset(): void {
    paymentResult.value = null
    error.value = null
    isLoading.value = false
  }

  // ============================================
  // Return
  // ============================================
  return {
    // State
    paymentResult,
    isLoading,
    error,
    // Actions
    requestApproval,
    clearPaymentResult,
    clearError,
    reset,
  }
})
