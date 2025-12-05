/**
 * ProductOrderView 컴포넌트 테스트 (SCR-HP-002)
 *
 * 테스트 케이스 (feature-spec.md 5.3):
 * - FE_UT_01: 수량 입력 후 주문하기 클릭 시 API 호출
 * - FE_UT_02: 주문 생성 성공 시 PG 결제 플로우로 이동
 * - FE_UT_05: 재고 부족/수량 오류 시 에러 메시지 표시
 *
 * DOM 접근: data-testid 기반만 사용 (4. DOM 접근 규칙)
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { createRouter, createMemoryHistory } from 'vue-router'
import ProductOrderView from './ProductOrderView.vue'
import { useProductStore } from '@/stores/product'
import { useOrderStore } from '@/stores/order'
import type { Product, CreateOrderResponse } from '@/types'

// 테스트용 라우터 설정
const routes = [
  { path: '/products', name: 'product-list', component: { template: '<div>List</div>' } },
  { path: '/products/:id', name: 'product-order', component: ProductOrderView },
  { path: '/orders/:orderId/complete', name: 'order-complete', component: { template: '<div>Complete</div>' } },
]

// 테스트용 상품 데이터 (feature-spec.md 기반)
const mockProduct: Product = {
  productId: 101,
  name: '무선 청소기 프리미엄',
  basePrice: 150000,
  discountPrice: 129000,
  availableStock: 50,
  available: true,
  createdAt: '2025-12-04T10:00:00',
}

// 테스트용 주문 응답 데이터
const mockOrderResponse: CreateOrderResponse = {
  orderId: 'ORD-20251204-0001',
  userId: 1,
  productId: 101,
  quantity: 2,
  totalAmount: 258000,
  status: 'PENDING_PAYMENT',
}

describe('ProductOrderView (SCR-HP-002)', () => {
  let router: ReturnType<typeof createRouter>

  beforeEach(async () => {
    // Given: 라우터 초기화
    router = createRouter({
      history: createMemoryHistory(),
      routes,
    })
    router.push('/products/101')
    await router.isReady()
  })

  describe('초기 렌더링', () => {
    it('상품 정보가 로드되면 상품명과 가격이 표시된다', async () => {
      // Given: 상품 정보가 있는 상태
      const wrapper = mount(ProductOrderView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  selectedProduct: mockProduct,
                  isLoading: false,
                  error: null,
                },
                order: {
                  currentOrder: null,
                  isLoading: false,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // Then: 상품 정보가 표시됨
      expect(wrapper.text()).toContain('무선 청소기 프리미엄')
      expect(wrapper.text()).toContain('129,000원')

      wrapper.unmount()
    })

    it('order-quantity-input의 기본값은 1이다', async () => {
      // Given: 상품 정보가 있는 상태
      const wrapper = mount(ProductOrderView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  selectedProduct: mockProduct,
                  isLoading: false,
                  error: null,
                },
                order: {
                  currentOrder: null,
                  isLoading: false,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // Then: 수량 입력 필드 기본값 검증
      const quantityInput = wrapper.find('[data-testid="order-quantity-input"]')
      expect((quantityInput.element as HTMLInputElement).value).toBe('1')

      wrapper.unmount()
    })
  })

  describe('FE_UT_01 - 수량 입력 후 주문하기 클릭 시 API 호출', () => {
    it('order-quantity-input 값 반영 후 주문 API가 호출된다', async () => {
      // Given: Pinia 설정 (stubActions: true로 기본 동작 차단)
      const pinia = createTestingPinia({
        createSpy: vi.fn,
        initialState: {
          product: {
            selectedProduct: mockProduct,
            isLoading: false,
            error: null,
          },
          order: {
            currentOrder: null,
            isLoading: false,
            error: null,
          },
        },
      })

      // Given: 상품 정보가 있는 상태
      const wrapper = mount(ProductOrderView, {
        global: {
          plugins: [pinia, router],
        },
      })

      const orderStore = useOrderStore()

      await flushPromises()

      // When: 수량을 2로 변경
      const quantityInput = wrapper.find('[data-testid="order-quantity-input"]')
      expect(quantityInput.exists()).toBe(true)
      await quantityInput.setValue(2)
      await flushPromises()

      // When: 주문하기 버튼 클릭
      const submitButton = wrapper.find('[data-testid="order-submit-button"]')
      expect(submitButton.exists()).toBe(true)
      await submitButton.trigger('click')
      await flushPromises()

      // Then: submitOrder가 올바른 파라미터로 호출됨
      expect(orderStore.submitOrder).toHaveBeenCalledWith({
        userId: 1,
        productId: 101,
        quantity: 2,
      })

      wrapper.unmount()
    })
  })

  describe('FE_UT_02 - 주문 생성 성공 시 PG 결제 플로우로 이동', () => {
    it('주문 응답 수신 후 주문 완료 화면으로 라우팅된다', async () => {
      // Given: 상품 정보가 있는 상태
      const wrapper = mount(ProductOrderView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              stubActions: false,
              initialState: {
                product: {
                  selectedProduct: mockProduct,
                  isLoading: false,
                  error: null,
                },
                order: {
                  currentOrder: null,
                  isLoading: false,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      const orderStore = useOrderStore()
      // Mock submitOrder to return success and set currentOrder
      vi.spyOn(orderStore, 'submitOrder').mockImplementation(async () => {
        orderStore.currentOrder = mockOrderResponse
        return true
      })

      // When: 주문하기 버튼 클릭
      const submitButton = wrapper.find('[data-testid="order-submit-button"]')
      await submitButton.trigger('click')
      await flushPromises()

      // Then: 주문 완료 화면으로 라우팅됨
      expect(router.currentRoute.value.name).toBe('order-complete')
      expect(router.currentRoute.value.params.orderId).toBe('ORD-20251204-0001')

      wrapper.unmount()
    })
  })

  describe('FE_UT_05 - 재고 부족/수량 오류 시 에러 메시지 표시', () => {
    it('재고보다 많은 수량 입력 시 주문 버튼이 비활성화된다', async () => {
      // Given: 상품 정보가 있는 상태 (재고 50개)
      const wrapper = mount(ProductOrderView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  selectedProduct: mockProduct,
                  isLoading: false,
                  error: null,
                },
                order: {
                  currentOrder: null,
                  isLoading: false,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // When: 재고보다 많은 수량(100) 입력
      const quantityInput = wrapper.find('[data-testid="order-quantity-input"]')
      await quantityInput.setValue(100)

      // Then: 주문 버튼이 비활성화됨
      const submitButton = wrapper.find('[data-testid="order-submit-button"]')
      expect(submitButton.attributes('disabled')).toBeDefined()

      // Then: 에러 메시지 표시
      expect(wrapper.text()).toContain('재고가 부족합니다')

      wrapper.unmount()
    })

    it('OUT_OF_STOCK 응답 시 payment-error-message가 표시된다', async () => {
      // Given: 에러 상태
      const wrapper = mount(ProductOrderView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  selectedProduct: mockProduct,
                  isLoading: false,
                  error: null,
                },
                order: {
                  currentOrder: null,
                  isLoading: false,
                  error: {
                    code: 'OUT_OF_STOCK',
                    message: '재고가 부족합니다.',
                  },
                },
              },
            }),
            router,
          ],
        },
      })

      // Then: payment-error-message에 에러 표시
      const errorMessage = wrapper.find('[data-testid="payment-error-message"]')
      expect(errorMessage.exists()).toBe(true)
      expect(errorMessage.text()).toBe('재고가 부족합니다.')

      wrapper.unmount()
    })

    it('수량이 0 이하일 때 주문 버튼이 비활성화된다', async () => {
      // Given: 상품 정보가 있는 상태
      const wrapper = mount(ProductOrderView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  selectedProduct: mockProduct,
                  isLoading: false,
                  error: null,
                },
                order: {
                  currentOrder: null,
                  isLoading: false,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // When: 수량을 0으로 설정
      const quantityInput = wrapper.find('[data-testid="order-quantity-input"]')
      await quantityInput.setValue(0)

      // Then: 주문 버튼이 비활성화됨
      const submitButton = wrapper.find('[data-testid="order-submit-button"]')
      expect(submitButton.attributes('disabled')).toBeDefined()

      wrapper.unmount()
    })
  })

  describe('총 금액 계산 (Rule-03)', () => {
    it('수량 변경 시 총 금액이 올바르게 계산된다', async () => {
      // Given: 상품 정보가 있는 상태 (할인가 129,000원)
      const wrapper = mount(ProductOrderView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  selectedProduct: mockProduct,
                  isLoading: false,
                  error: null,
                },
                order: {
                  currentOrder: null,
                  isLoading: false,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // When: 수량을 3으로 변경
      const quantityInput = wrapper.find('[data-testid="order-quantity-input"]')
      await quantityInput.setValue(3)

      // Then: 총 금액이 387,000원 (129,000 × 3)
      expect(wrapper.text()).toContain('387,000원')

      wrapper.unmount()
    })
  })
})

