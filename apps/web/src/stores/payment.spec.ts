/**
 * Payment Store 테스트
 *
 * 테스트 범위: 2.2 스토어 테스트 (Pinia)
 * - state 초기화
 * - actions 동작 (requestApproval)
 * - 비동기 API 호출 로직 (axios mock)
 * - 실패 케이스/에러 상태 처리
 *
 * 관련 규칙:
 * - Rule-03: 금액 계산 규칙 (amount === Order.totalAmount)
 * - Rule-04: 상태 전이 규칙
 * - Rule-05: 히스토리 기록 규칙
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePaymentStore } from './payment'
import * as paymentsApi from '@/api/payments'
import type { ApprovePaymentResponse } from '@/types'
import { AxiosError } from 'axios'

// API Mock 설정
vi.mock('@/api/payments')

describe('usePaymentStore', () => {
  // 테스트용 결제 승인 응답 데이터 (feature-spec.md 3.2 기반)
  const mockPaymentResponse: ApprovePaymentResponse = {
    orderId: 'ORD-20251204-0001',
    paymentId: 10,
    paymentKey: 'pay_abc123',
    amount: 258000,
    paymentStatus: 'APPROVED',
    orderStatus: 'PAID',
    approvedAt: '2025-12-04T12:34:56',
  }

  beforeEach(() => {
    // Given: 각 테스트 전에 Pinia 초기화
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('state 초기화', () => {
    it('초기 상태는 null과 false 값을 가진다', () => {
      // Given: Store 인스턴스 생성
      const store = usePaymentStore()

      // Then: 초기 상태 검증
      expect(store.paymentResult).toBeNull()
      expect(store.isLoading).toBe(false)
      expect(store.error).toBeNull()
    })
  })

  describe('requestApproval - 결제 승인', () => {
    it('API 호출 성공 시 paymentResult가 설정되고 true를 반환한다', async () => {
      // Given: API가 결제 승인 응답을 반환하도록 Mock 설정
      vi.mocked(paymentsApi.approvePayment).mockResolvedValue(mockPaymentResponse)
      const store = usePaymentStore()

      // When: 결제 승인 액션 호출
      const result = await store.requestApproval({
        orderId: 'ORD-20251204-0001',
        paymentKey: 'pay_abc123',
        amount: 258000,
      })

      // Then: 결제가 성공적으로 승인됨
      expect(result).toBe(true)
      expect(store.paymentResult).toEqual(mockPaymentResponse)
      expect(store.paymentResult?.paymentStatus).toBe('APPROVED')
      expect(store.paymentResult?.orderStatus).toBe('PAID')
      expect(store.isLoading).toBe(false)
      expect(store.error).toBeNull()
    })

    it('API 호출 중 isLoading이 true가 된다', async () => {
      // Given: API가 지연되도록 설정
      let resolvePromise: (value: ApprovePaymentResponse) => void
      vi.mocked(paymentsApi.approvePayment).mockImplementation(
        () =>
          new Promise((resolve) => {
            resolvePromise = resolve
          }),
      )
      const store = usePaymentStore()

      // When: 결제 승인 시작 (await 없이)
      const approvalPromise = store.requestApproval({
        orderId: 'ORD-20251204-0001',
        paymentKey: 'pay_abc123',
        amount: 258000,
      })

      // Then: 로딩 중 상태
      expect(store.isLoading).toBe(true)

      // Cleanup: Promise 해결
      resolvePromise!(mockPaymentResponse)
      await approvalPromise
    })

    it('AMOUNT_MISMATCH 에러 시 error 상태가 설정되고 false를 반환한다 (Rule-03)', async () => {
      // Given: API가 금액 불일치 에러를 발생시키도록 Mock 설정
      const axiosError = new AxiosError('Request failed')
      axiosError.response = {
        data: {
          code: 'AMOUNT_MISMATCH',
          message: '결제 금액이 일치하지 않습니다.',
        },
        status: 400,
        statusText: 'Bad Request',
        headers: {},
        config: {} as never,
      }
      vi.mocked(paymentsApi.approvePayment).mockRejectedValue(axiosError)
      const store = usePaymentStore()

      // When: 잘못된 금액으로 결제 승인 요청 (Rule-03 위반)
      const result = await store.requestApproval({
        orderId: 'ORD-20251204-0001',
        paymentKey: 'pay_abc123',
        amount: 100000, // 실제 주문 금액과 다름
      })

      // Then: 에러 상태 검증
      expect(result).toBe(false)
      expect(store.error).not.toBeNull()
      expect(store.error?.code).toBe('AMOUNT_MISMATCH')
      expect(store.error?.message).toBe('결제 금액이 일치하지 않습니다.')
      expect(store.paymentResult).toBeNull()
      expect(store.isLoading).toBe(false)
    })

    it('ORDER_NOT_PAYABLE 에러 시 error 상태가 설정된다 (Rule-04)', async () => {
      // Given: API가 결제 불가 에러를 발생시키도록 Mock 설정
      const axiosError = new AxiosError('Request failed')
      axiosError.response = {
        data: {
          code: 'ORDER_NOT_PAYABLE',
          message: '이미 결제가 완료되었거나 결제 불가능한 주문입니다.',
        },
        status: 409,
        statusText: 'Conflict',
        headers: {},
        config: {} as never,
      }
      vi.mocked(paymentsApi.approvePayment).mockRejectedValue(axiosError)
      const store = usePaymentStore()

      // When: 이미 결제된 주문에 재결제 시도
      const result = await store.requestApproval({
        orderId: 'ORD-20251204-0001',
        paymentKey: 'pay_abc123',
        amount: 258000,
      })

      // Then: 에러 상태 검증
      expect(result).toBe(false)
      expect(store.error?.code).toBe('ORDER_NOT_PAYABLE')
    })

    it('PG_APPROVAL_FAILED 에러 시 error 상태가 설정된다', async () => {
      // Given: API가 PG 승인 실패 에러를 발생시키도록 Mock 설정
      const axiosError = new AxiosError('Request failed')
      axiosError.response = {
        data: {
          code: 'PG_APPROVAL_FAILED',
          message: 'PG사 결제 승인에 실패했습니다.',
          detail: 'PG_ERROR_CODE: CARD_LIMIT_EXCEEDED',
        },
        status: 400,
        statusText: 'Bad Request',
        headers: {},
        config: {} as never,
      }
      vi.mocked(paymentsApi.approvePayment).mockRejectedValue(axiosError)
      const store = usePaymentStore()

      // When: PG 승인 실패
      const result = await store.requestApproval({
        orderId: 'ORD-20251204-0001',
        paymentKey: 'pay_abc123',
        amount: 258000,
      })

      // Then: 에러 상태 검증
      expect(result).toBe(false)
      expect(store.error?.code).toBe('PG_APPROVAL_FAILED')
      expect(store.error?.detail).toBe('PG_ERROR_CODE: CARD_LIMIT_EXCEEDED')
    })

    it('ORDER_NOT_FOUND 에러 시 error 상태가 설정된다', async () => {
      // Given: API가 주문 없음 에러를 발생시키도록 Mock 설정
      const axiosError = new AxiosError('Request failed')
      axiosError.response = {
        data: {
          code: 'ORDER_NOT_FOUND',
          message: '주문을 찾을 수 없습니다.',
        },
        status: 404,
        statusText: 'Not Found',
        headers: {},
        config: {} as never,
      }
      vi.mocked(paymentsApi.approvePayment).mockRejectedValue(axiosError)
      const store = usePaymentStore()

      // When: 존재하지 않는 주문에 결제 시도
      const result = await store.requestApproval({
        orderId: 'ORD-INVALID',
        paymentKey: 'pay_abc123',
        amount: 258000,
      })

      // Then: 에러 상태 검증
      expect(result).toBe(false)
      expect(store.error?.code).toBe('ORDER_NOT_FOUND')
    })

    it('네트워크 에러 시 일반 에러 메시지가 설정된다', async () => {
      // Given: 네트워크 에러 발생
      vi.mocked(paymentsApi.approvePayment).mockRejectedValue(
        new Error('Network Error'),
      )
      const store = usePaymentStore()

      // When: 결제 승인 액션 호출
      const result = await store.requestApproval({
        orderId: 'ORD-20251204-0001',
        paymentKey: 'pay_abc123',
        amount: 258000,
      })

      // Then: 에러 상태 검증
      expect(result).toBe(false)
      expect(store.error).not.toBeNull()
      expect(store.error?.message).toBe('Network Error')
    })
  })

  describe('clearPaymentResult - 결제 결과 초기화', () => {
    it('paymentResult를 null로 초기화한다', async () => {
      // Given: 결제가 승인된 상태
      vi.mocked(paymentsApi.approvePayment).mockResolvedValue(mockPaymentResponse)
      const store = usePaymentStore()
      await store.requestApproval({
        orderId: 'ORD-20251204-0001',
        paymentKey: 'pay_abc123',
        amount: 258000,
      })
      expect(store.paymentResult).not.toBeNull()

      // When: 결제 결과 초기화
      store.clearPaymentResult()

      // Then: paymentResult가 null
      expect(store.paymentResult).toBeNull()
    })
  })

  describe('clearError - 에러 초기화', () => {
    it('error를 null로 초기화한다', async () => {
      // Given: 에러가 발생한 상태
      vi.mocked(paymentsApi.approvePayment).mockRejectedValue(new Error('Error'))
      const store = usePaymentStore()
      await store.requestApproval({
        orderId: 'ORD-20251204-0001',
        paymentKey: 'pay_abc123',
        amount: 258000,
      })
      expect(store.error).not.toBeNull()

      // When: 에러 초기화
      store.clearError()

      // Then: error가 null
      expect(store.error).toBeNull()
    })
  })

  describe('reset - 전체 상태 초기화', () => {
    it('모든 상태를 초기값으로 리셋한다', async () => {
      // Given: 결제가 승인된 상태
      vi.mocked(paymentsApi.approvePayment).mockResolvedValue(mockPaymentResponse)
      const store = usePaymentStore()
      await store.requestApproval({
        orderId: 'ORD-20251204-0001',
        paymentKey: 'pay_abc123',
        amount: 258000,
      })

      // When: 전체 리셋
      store.reset()

      // Then: 모든 상태가 초기값
      expect(store.paymentResult).toBeNull()
      expect(store.error).toBeNull()
      expect(store.isLoading).toBe(false)
    })
  })
})
