/**
 * Order Store 테스트
 *
 * 테스트 범위: 2.2 스토어 테스트 (Pinia)
 * - state 초기화
 * - actions 동작 (submitOrder)
 * - 비동기 API 호출 로직 (axios mock)
 * - 실패 케이스/에러 상태 처리
 *
 * 관련 규칙:
 * - Rule-01: 단일 상품 주문 제한
 * - Rule-02: 수량 및 재고 규칙
 * - Rule-03: 금액 계산 규칙
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useOrderStore } from './order'
import * as ordersApi from '@/api/orders'
import type { CreateOrderResponse } from '@/types'
import { AxiosError } from 'axios'

// API Mock 설정
vi.mock('@/api/orders')

describe('useOrderStore', () => {
  // 테스트용 주문 응답 데이터 (feature-spec.md 3.1 기반)
  const mockOrderResponse: CreateOrderResponse = {
    orderId: 'ORD-20251204-0001',
    userId: 1,
    productId: 101,
    quantity: 2,
    totalAmount: 258000,
    status: 'PENDING_PAYMENT',
  }

  beforeEach(() => {
    // Given: 각 테스트 전에 Pinia 초기화
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('state 초기화', () => {
    it('초기 상태는 null과 false 값을 가진다', () => {
      // Given: Store 인스턴스 생성
      const store = useOrderStore()

      // Then: 초기 상태 검증
      expect(store.currentOrder).toBeNull()
      expect(store.isLoading).toBe(false)
      expect(store.error).toBeNull()
    })
  })

  describe('submitOrder - 주문 생성', () => {
    it('API 호출 성공 시 currentOrder가 설정되고 true를 반환한다', async () => {
      // Given: API가 주문 응답을 반환하도록 Mock 설정
      vi.mocked(ordersApi.createOrder).mockResolvedValue(mockOrderResponse)
      const store = useOrderStore()

      // When: 주문 생성 액션 호출
      const result = await store.submitOrder({
        userId: 1,
        productId: 101,
        quantity: 2,
      })

      // Then: 주문이 성공적으로 생성됨
      expect(result).toBe(true)
      expect(store.currentOrder).toEqual(mockOrderResponse)
      expect(store.currentOrder?.orderId).toBe('ORD-20251204-0001')
      expect(store.currentOrder?.totalAmount).toBe(258000)
      expect(store.currentOrder?.status).toBe('PENDING_PAYMENT')
      expect(store.isLoading).toBe(false)
      expect(store.error).toBeNull()
    })

    it('API 호출 중 isLoading이 true가 된다', async () => {
      // Given: API가 지연되도록 설정
      let resolvePromise: (value: CreateOrderResponse) => void
      vi.mocked(ordersApi.createOrder).mockImplementation(
        () =>
          new Promise((resolve) => {
            resolvePromise = resolve
          }),
      )
      const store = useOrderStore()

      // When: 주문 생성 시작 (await 없이)
      const submitPromise = store.submitOrder({
        userId: 1,
        productId: 101,
        quantity: 2,
      })

      // Then: 로딩 중 상태
      expect(store.isLoading).toBe(true)

      // Cleanup: Promise 해결
      resolvePromise!(mockOrderResponse)
      await submitPromise
    })

    it('OUT_OF_STOCK 에러 시 error 상태가 설정되고 false를 반환한다', async () => {
      // Given: API가 재고 부족 에러를 발생시키도록 Mock 설정
      const axiosError = new AxiosError('Request failed')
      axiosError.response = {
        data: {
          code: 'OUT_OF_STOCK',
          message: '재고가 부족합니다.',
        },
        status: 400,
        statusText: 'Bad Request',
        headers: {},
        config: {} as never,
      }
      vi.mocked(ordersApi.createOrder).mockRejectedValue(axiosError)
      const store = useOrderStore()

      // When: 주문 생성 액션 호출
      const result = await store.submitOrder({
        userId: 1,
        productId: 101,
        quantity: 100, // 재고보다 많은 수량
      })

      // Then: 에러 상태 검증 (Rule-02 위반)
      expect(result).toBe(false)
      expect(store.error).not.toBeNull()
      expect(store.error?.code).toBe('OUT_OF_STOCK')
      expect(store.error?.message).toBe('재고가 부족합니다.')
      expect(store.currentOrder).toBeNull()
      expect(store.isLoading).toBe(false)
    })

    it('PRODUCT_NOT_FOUND 에러 시 error 상태가 설정된다', async () => {
      // Given: API가 상품 없음 에러를 발생시키도록 Mock 설정
      const axiosError = new AxiosError('Request failed')
      axiosError.response = {
        data: {
          code: 'PRODUCT_NOT_FOUND',
          message: '상품을 찾을 수 없습니다.',
        },
        status: 400,
        statusText: 'Bad Request',
        headers: {},
        config: {} as never,
      }
      vi.mocked(ordersApi.createOrder).mockRejectedValue(axiosError)
      const store = useOrderStore()

      // When: 존재하지 않는 상품으로 주문 생성
      const result = await store.submitOrder({
        userId: 1,
        productId: 9999,
        quantity: 1,
      })

      // Then: 에러 상태 검증
      expect(result).toBe(false)
      expect(store.error?.code).toBe('PRODUCT_NOT_FOUND')
    })

    it('QUANTITY_INVALID 에러 시 error 상태가 설정된다', async () => {
      // Given: API가 수량 오류 에러를 발생시키도록 Mock 설정
      const axiosError = new AxiosError('Request failed')
      axiosError.response = {
        data: {
          code: 'QUANTITY_INVALID',
          message: '수량은 1 이상이어야 합니다.',
        },
        status: 400,
        statusText: 'Bad Request',
        headers: {},
        config: {} as never,
      }
      vi.mocked(ordersApi.createOrder).mockRejectedValue(axiosError)
      const store = useOrderStore()

      // When: 잘못된 수량으로 주문 생성 (Rule-02 위반)
      const result = await store.submitOrder({
        userId: 1,
        productId: 101,
        quantity: 0,
      })

      // Then: 에러 상태 검증
      expect(result).toBe(false)
      expect(store.error?.code).toBe('QUANTITY_INVALID')
    })

    it('네트워크 에러 시 일반 에러 메시지가 설정된다', async () => {
      // Given: 네트워크 에러 발생
      vi.mocked(ordersApi.createOrder).mockRejectedValue(
        new Error('Network Error'),
      )
      const store = useOrderStore()

      // When: 주문 생성 액션 호출
      const result = await store.submitOrder({
        userId: 1,
        productId: 101,
        quantity: 1,
      })

      // Then: 에러 상태 검증
      expect(result).toBe(false)
      expect(store.error).not.toBeNull()
      expect(store.error?.message).toBe('Network Error')
    })
  })

  describe('clearOrder - 주문 초기화', () => {
    it('currentOrder를 null로 초기화한다', async () => {
      // Given: 주문이 생성된 상태
      vi.mocked(ordersApi.createOrder).mockResolvedValue(mockOrderResponse)
      const store = useOrderStore()
      await store.submitOrder({ userId: 1, productId: 101, quantity: 2 })
      expect(store.currentOrder).not.toBeNull()

      // When: 주문 초기화
      store.clearOrder()

      // Then: currentOrder가 null
      expect(store.currentOrder).toBeNull()
    })
  })

  describe('clearError - 에러 초기화', () => {
    it('error를 null로 초기화한다', async () => {
      // Given: 에러가 발생한 상태
      vi.mocked(ordersApi.createOrder).mockRejectedValue(new Error('Error'))
      const store = useOrderStore()
      await store.submitOrder({ userId: 1, productId: 101, quantity: 1 })
      expect(store.error).not.toBeNull()

      // When: 에러 초기화
      store.clearError()

      // Then: error가 null
      expect(store.error).toBeNull()
    })
  })

  describe('reset - 전체 상태 초기화', () => {
    it('모든 상태를 초기값으로 리셋한다', async () => {
      // Given: 주문이 생성된 상태
      vi.mocked(ordersApi.createOrder).mockResolvedValue(mockOrderResponse)
      const store = useOrderStore()
      await store.submitOrder({ userId: 1, productId: 101, quantity: 2 })

      // When: 전체 리셋
      store.reset()

      // Then: 모든 상태가 초기값
      expect(store.currentOrder).toBeNull()
      expect(store.error).toBeNull()
      expect(store.isLoading).toBe(false)
    })
  })
})

