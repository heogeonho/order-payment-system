/**
 * OrderCompleteView 컴포넌트 테스트 (SCR-HP-004)
 *
 * 테스트 케이스 (feature-spec.md 5.3):
 * - FE_UT_03: 결제 승인 성공 시 주문 완료 화면 렌더링
 * - FE_UT_04: 결제 실패 시 오류 메시지 및 재시도 버튼 노출
 *
 * DOM 접근: data-testid 기반만 사용 (4. DOM 접근 규칙)
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { createRouter, createMemoryHistory } from 'vue-router'
import OrderCompleteView from './OrderCompleteView.vue'
import { usePaymentStore } from '@/stores/payment'
import type { CreateOrderResponse, ApprovePaymentResponse } from '@/types'

// 테스트용 라우터 설정
const routes = [
  { path: '/products', name: 'product-list', component: { template: '<div>List</div>' } },
  { path: '/orders/:orderId/complete', name: 'order-complete', component: OrderCompleteView },
]

// 테스트용 주문 데이터
const mockOrder: CreateOrderResponse = {
  orderId: 'ORD-20251204-0001',
  userId: 1,
  productId: 101,
  quantity: 2,
  totalAmount: 258000,
  status: 'PENDING_PAYMENT',
}

// 테스트용 결제 승인 응답 데이터
const mockPaymentResult: ApprovePaymentResponse = {
  orderId: 'ORD-20251204-0001',
  paymentId: 10,
  paymentKey: 'pay_abc123',
  amount: 258000,
  paymentStatus: 'APPROVED',
  orderStatus: 'PAID',
  approvedAt: '2025-12-04T12:34:56',
}

describe('OrderCompleteView (SCR-HP-004)', () => {
  let router: ReturnType<typeof createRouter>

  beforeEach(async () => {
    // Given: 라우터 초기화
    router = createRouter({
      history: createMemoryHistory(),
      routes,
    })
    router.push('/orders/ORD-20251204-0001/complete')
    await router.isReady()
  })

  describe('FE_UT_03 - 결제 승인 성공 시 주문 완료 화면 렌더링', () => {
    it('payment 승인 응답 후 order-complete-message가 표시된다', async () => {
      // Given: 결제 성공 상태
      const wrapper = mount(OrderCompleteView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                order: {
                  currentOrder: mockOrder,
                  isLoading: false,
                  error: null,
                },
                payment: {
                  paymentResult: mockPaymentResult,
                  isLoading: false,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      await flushPromises()

      // Then: order-complete-message 표시
      const completeMessage = wrapper.find('[data-testid="order-complete-message"]')
      expect(completeMessage.exists()).toBe(true)
      expect(completeMessage.text()).toContain('주문이 정상적으로 완료되었습니다')

      wrapper.unmount()
    })

    it('주문번호가 order-complete-order-id에 표시된다', async () => {
      // Given: 결제 성공 상태
      const wrapper = mount(OrderCompleteView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                order: {
                  currentOrder: mockOrder,
                  isLoading: false,
                  error: null,
                },
                payment: {
                  paymentResult: mockPaymentResult,
                  isLoading: false,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      await flushPromises()

      // Then: order-complete-order-id에 주문번호 표시
      const orderId = wrapper.find('[data-testid="order-complete-order-id"]')
      expect(orderId.exists()).toBe(true)
      expect(orderId.text()).toContain('ORD-20251204-0001')

      wrapper.unmount()
    })

    it('결제 금액이 표시된다', async () => {
      // Given: 결제 성공 상태
      const wrapper = mount(OrderCompleteView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                order: {
                  currentOrder: mockOrder,
                  isLoading: false,
                  error: null,
                },
                payment: {
                  paymentResult: mockPaymentResult,
                  isLoading: false,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      await flushPromises()

      // Then: 결제 금액 표시 (258,000원)
      expect(wrapper.text()).toContain('258,000원')

      wrapper.unmount()
    })
  })

  describe('FE_UT_04 - 결제 실패 시 오류 메시지 및 재시도 버튼 노출', () => {
    it('payment-error-message가 표시된다', async () => {
      // Given: 결제 실패 상태
      const wrapper = mount(OrderCompleteView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                order: {
                  currentOrder: mockOrder,
                  isLoading: false,
                  error: null,
                },
                payment: {
                  paymentResult: null,
                  isLoading: false,
                  error: {
                    code: 'PG_APPROVAL_FAILED',
                    message: 'PG사 결제 승인에 실패했습니다.',
                  },
                },
              },
            }),
            router,
          ],
        },
      })

      await flushPromises()

      // Then: payment-error-message 표시
      const errorMessage = wrapper.find('[data-testid="payment-error-message"]')
      expect(errorMessage.exists()).toBe(true)
      expect(errorMessage.text()).toBe('PG사 결제 승인에 실패했습니다.')

      wrapper.unmount()
    })

    it('payment-retry-button이 노출된다', async () => {
      // Given: 결제 실패 상태
      const wrapper = mount(OrderCompleteView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                order: {
                  currentOrder: mockOrder,
                  isLoading: false,
                  error: null,
                },
                payment: {
                  paymentResult: null,
                  isLoading: false,
                  error: {
                    code: 'AMOUNT_MISMATCH',
                    message: '결제 금액이 일치하지 않습니다.',
                  },
                },
              },
            }),
            router,
          ],
        },
      })

      await flushPromises()

      // Then: payment-retry-button 노출
      const retryButton = wrapper.find('[data-testid="payment-retry-button"]')
      expect(retryButton.exists()).toBe(true)
      expect(retryButton.text()).toBe('다시 결제하기')

      wrapper.unmount()
    })

    it('재시도 버튼 클릭 시 결제 승인이 재요청된다', async () => {
      // Given: 결제 실패 상태
      const wrapper = mount(OrderCompleteView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                order: {
                  currentOrder: mockOrder,
                  isLoading: false,
                  error: null,
                },
                payment: {
                  paymentResult: null,
                  isLoading: false,
                  error: {
                    code: 'PG_APPROVAL_FAILED',
                    message: 'PG사 결제 승인에 실패했습니다.',
                  },
                },
              },
            }),
            router,
          ],
        },
      })

      const paymentStore = usePaymentStore()

      await flushPromises()

      // When: 재시도 버튼 클릭
      const retryButton = wrapper.find('[data-testid="payment-retry-button"]')
      await retryButton.trigger('click')
      await flushPromises()

      // Then: clearError와 requestApproval이 호출됨
      expect(paymentStore.clearError).toHaveBeenCalled()
      expect(paymentStore.requestApproval).toHaveBeenCalled()

      wrapper.unmount()
    })
  })

  describe('로딩 상태', () => {
    it('결제 진행 중일 때 payment-loading-indicator가 표시된다', async () => {
      // Given: 결제 로딩 중 상태
      const wrapper = mount(OrderCompleteView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                order: {
                  currentOrder: mockOrder,
                  isLoading: false,
                  error: null,
                },
                payment: {
                  paymentResult: null,
                  isLoading: true,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      await flushPromises()

      // Then: loading indicator 표시
      const loadingIndicator = wrapper.find('[data-testid="payment-loading-indicator"]')
      expect(loadingIndicator.exists()).toBe(true)
      expect(loadingIndicator.text()).toContain('결제 처리 중입니다')

      wrapper.unmount()
    })
  })

  describe('AMOUNT_MISMATCH 에러 (FE-SC-03)', () => {
    it('금액 불일치 시 에러 메시지가 표시된다', async () => {
      // Given: 금액 불일치 에러 상태
      const wrapper = mount(OrderCompleteView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                order: {
                  currentOrder: mockOrder,
                  isLoading: false,
                  error: null,
                },
                payment: {
                  paymentResult: null,
                  isLoading: false,
                  error: {
                    code: 'AMOUNT_MISMATCH',
                    message: '결제 금액이 일치하지 않습니다.',
                  },
                },
              },
            }),
            router,
          ],
        },
      })

      await flushPromises()

      // Then: 에러 메시지 표시
      const errorMessage = wrapper.find('[data-testid="payment-error-message"]')
      expect(errorMessage.text()).toBe('결제 금액이 일치하지 않습니다.')

      // Then: 재시도 버튼 노출
      const retryButton = wrapper.find('[data-testid="payment-retry-button"]')
      expect(retryButton.exists()).toBe(true)

      wrapper.unmount()
    })
  })
})

