/**
 * Order Store - 주문 관련 상태 관리
 *
 * 관련 API: API-ORD-001 (POST /api/orders)
 *
 * 비즈니스 규칙:
 * - Rule-01: 단일 상품 주문 제한
 * - Rule-02: 수량 및 재고 규칙 (quantity >= 1, quantity <= availableStock)
 * - Rule-03: 금액 계산 규칙 (totalAmount = discountPrice × quantity)
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { createOrder } from '@/api'
import type {
  CreateOrderRequest,
  CreateOrderResponse,
  ApiErrorResponse,
  ErrorCode,
} from '@/types'
import axios from 'axios'

export const useOrderStore = defineStore('order', () => {
  // ============================================
  // State
  // ============================================

  /** 현재 생성된 주문 정보 */
  const currentOrder = ref<CreateOrderResponse | null>(null)

  /** 로딩 상태 */
  const isLoading = ref(false)

  /** 에러 정보 */
  const error = ref<ApiErrorResponse | null>(null)

  // ============================================
  // Actions
  // ============================================

  /**
   * 주문 생성
   * API: POST /api/orders
   *
   * @param request 주문 생성 요청 (userId, productId, quantity)
   * @returns 성공 여부
   *
   * 에러 케이스:
   * - PRODUCT_NOT_FOUND: 상품이 존재하지 않음
   * - PRODUCT_NOT_AVAILABLE: 상품이 판매 불가 상태
   * - OUT_OF_STOCK: 재고 부족
   * - QUANTITY_INVALID: 수량 오류 (0 이하)
   */
  async function submitOrder(request: CreateOrderRequest): Promise<boolean> {
    isLoading.value = true
    error.value = null
    currentOrder.value = null

    try {
      const response = await createOrder(request)
      currentOrder.value = response
      return true
    } catch (e) {
      // API 에러 응답 처리
      if (axios.isAxiosError(e) && e.response?.data) {
        const errorData = e.response.data as ApiErrorResponse
        error.value = {
          code: errorData.code || ('UNKNOWN_ERROR' as ErrorCode),
          message: errorData.message || '주문 생성 중 오류가 발생했습니다.',
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
   * 현재 주문 정보 초기화
   */
  function clearOrder(): void {
    currentOrder.value = null
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
    currentOrder.value = null
    error.value = null
    isLoading.value = false
  }

  // ============================================
  // Return
  // ============================================
  return {
    // State
    currentOrder,
    isLoading,
    error,
    // Actions
    submitOrder,
    clearOrder,
    clearError,
    reset,
  }
})

