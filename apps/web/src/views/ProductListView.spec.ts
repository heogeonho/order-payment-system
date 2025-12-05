/**
 * ProductListView 컴포넌트 테스트 (SCR-HP-001)
 *
 * 테스트 범위: 2.1 컴포넌트 테스트
 * - 초기 렌더링
 * - API 호출 후 UI 상태 변화
 * - 사용자 입력 처리 (버튼 클릭)
 *
 * DOM 접근: data-testid 기반만 사용 (4. DOM 접근 규칙)
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { createRouter, createMemoryHistory } from 'vue-router'
import ProductListView from './ProductListView.vue'
import { useProductStore } from '@/stores/product'
import type { Product } from '@/types'

// 테스트용 라우터 설정
const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/products', name: 'product-list', component: ProductListView },
    { path: '/products/:id', name: 'product-order', component: { template: '<div>Order</div>' } },
  ],
})

// 테스트용 상품 데이터 (feature-spec.md 기반)
const mockProducts: Product[] = [
  {
    productId: 101,
    name: '무선 청소기 프리미엄',
    basePrice: 150000,
    discountPrice: 129000,
    availableStock: 50,
    available: true,
    createdAt: '2025-12-04T10:00:00',
  },
  {
    productId: 102,
    name: '공기청정기',
    basePrice: 200000,
    discountPrice: 180000,
    availableStock: 0,
    available: true,
    createdAt: '2025-12-04T11:00:00',
  },
]

describe('ProductListView (SCR-HP-001)', () => {
  beforeEach(async () => {
    // Given: 라우터 초기화
    router.push('/products')
    await router.isReady()
  })

  describe('초기 렌더링', () => {
    it('컴포넌트 마운트 시 loadProducts가 호출된다', async () => {
      // Given: Pinia 스토어 Mock 설정
      const wrapper = mount(ProductListView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              stubActions: false,
            }),
            router,
          ],
        },
      })
      const store = useProductStore()

      // Then: loadProducts 액션이 호출됨
      expect(store.loadProducts).toHaveBeenCalled()

      wrapper.unmount()
    })

    it('로딩 중일 때 로딩 메시지가 표시된다', async () => {
      // Given: 로딩 상태로 설정
      const wrapper = mount(ProductListView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  isLoading: true,
                  products: [],
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // Then: 로딩 메시지 표시
      expect(wrapper.text()).toContain('상품을 불러오는 중입니다')

      wrapper.unmount()
    })
  })

  describe('상품 목록 표시', () => {
    it('상품 목록이 로드되면 product-card들이 렌더링된다', async () => {
      // Given: 상품 목록이 있는 상태
      const wrapper = mount(ProductListView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  isLoading: false,
                  products: mockProducts,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // Then: product-card가 상품 수만큼 렌더링됨
      const productCards = wrapper.findAll('[data-testid="product-card"]')
      expect(productCards).toHaveLength(2)

      wrapper.unmount()
    })

    it('각 상품 카드에 product-name이 표시된다', async () => {
      // Given: 상품 목록이 있는 상태
      const wrapper = mount(ProductListView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  isLoading: false,
                  products: mockProducts,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // Then: product-name에 상품명이 표시됨
      const productNames = wrapper.findAll('[data-testid="product-name"]')
      expect(productNames[0]?.text()).toBe('무선 청소기 프리미엄')
      expect(productNames[1]?.text()).toBe('공기청정기')

      wrapper.unmount()
    })

    it('각 상품 카드에 product-price가 표시된다', async () => {
      // Given: 상품 목록이 있는 상태
      const wrapper = mount(ProductListView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  isLoading: false,
                  products: mockProducts,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // Then: product-price에 할인가가 표시됨
      const productPrices = wrapper.findAll('[data-testid="product-price"]')
      expect(productPrices[0]?.text()).toBe('129,000원')
      expect(productPrices[1]?.text()).toBe('180,000원')

      wrapper.unmount()
    })
  })

  describe('상품 선택 (product-select-button)', () => {
    it('자세히 보기 버튼 클릭 시 상품 상세 페이지로 이동한다', async () => {
      // Given: 상품 목록이 있는 상태
      const wrapper = mount(ProductListView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  isLoading: false,
                  products: mockProducts,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // When: 첫 번째 상품의 자세히 보기 버튼 클릭
      const selectButtons = wrapper.findAll('[data-testid="product-select-button"]')
      await selectButtons[0]?.trigger('click')
      await flushPromises()

      // Then: 상품 상세 페이지로 라우팅됨
      expect(router.currentRoute.value.name).toBe('product-order')
      expect(router.currentRoute.value.params.id).toBe('101')

      wrapper.unmount()
    })

    it('재고가 0인 상품은 버튼이 비활성화된다', async () => {
      // Given: 재고가 0인 상품이 있는 상태
      const wrapper = mount(ProductListView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  isLoading: false,
                  products: mockProducts,
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // Then: 두 번째 상품(재고 0)의 버튼이 비활성화됨
      const selectButtons = wrapper.findAll('[data-testid="product-select-button"]')
      expect(selectButtons[1]?.attributes('disabled')).toBeDefined()

      wrapper.unmount()
    })
  })

  describe('에러 상태', () => {
    it('에러 발생 시 에러 메시지가 표시된다', async () => {
      // Given: 에러 상태
      const wrapper = mount(ProductListView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  isLoading: false,
                  products: [],
                  error: {
                    code: 'PRODUCT_NOT_FOUND',
                    message: '상품을 찾을 수 없습니다.',
                  },
                },
              },
            }),
            router,
          ],
        },
      })

      // Then: 에러 메시지 표시
      expect(wrapper.text()).toContain('상품을 찾을 수 없습니다')

      wrapper.unmount()
    })
  })

  describe('빈 상태', () => {
    it('상품이 없으면 빈 상태 메시지가 표시된다', async () => {
      // Given: 상품이 없는 상태
      const wrapper = mount(ProductListView, {
        global: {
          plugins: [
            createTestingPinia({
              createSpy: vi.fn,
              initialState: {
                product: {
                  isLoading: false,
                  products: [],
                  error: null,
                },
              },
            }),
            router,
          ],
        },
      })

      // Then: 빈 상태 메시지 표시
      expect(wrapper.text()).toContain('등록된 상품이 없습니다')

      wrapper.unmount()
    })
  })
})

