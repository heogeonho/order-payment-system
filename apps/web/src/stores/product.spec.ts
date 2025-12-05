/**
 * Product Store 테스트
 *
 * 테스트 범위: 2.2 스토어 테스트 (Pinia)
 * - state 초기화
 * - actions 동작
 * - 비동기 API 호출 로직 (axios mock)
 * - 실패 케이스/에러 상태 처리
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useProductStore } from './product'
import * as productsApi from '@/api/products'
import type { Product } from '@/types'

// API Mock 설정
vi.mock('@/api/products')

describe('useProductStore', () => {
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
      availableStock: 30,
      available: true,
      createdAt: '2025-12-04T11:00:00',
    },
  ]

  beforeEach(() => {
    // Given: 각 테스트 전에 Pinia 초기화
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('state 초기화', () => {
    it('초기 상태는 빈 배열과 null 값을 가진다', () => {
      // Given: Store 인스턴스 생성
      const store = useProductStore()

      // Then: 초기 상태 검증
      expect(store.products).toEqual([])
      expect(store.selectedProduct).toBeNull()
      expect(store.isLoading).toBe(false)
      expect(store.error).toBeNull()
    })
  })

  describe('loadProducts - 상품 목록 조회', () => {
    it('API 호출 성공 시 products 상태가 업데이트된다', async () => {
      // Given: API가 상품 목록을 반환하도록 Mock 설정
      vi.mocked(productsApi.fetchProducts).mockResolvedValue(mockProducts)
      const store = useProductStore()

      // When: 상품 목록 로드 액션 호출
      await store.loadProducts()

      // Then: 상품 목록이 state에 저장됨
      expect(store.products).toEqual(mockProducts)
      expect(store.products).toHaveLength(2)
      expect(store.isLoading).toBe(false)
      expect(store.error).toBeNull()
    })

    it('API 호출 중 isLoading이 true가 된다', async () => {
      // Given: API가 지연되도록 설정
      let resolvePromise: (value: Product[]) => void
      vi.mocked(productsApi.fetchProducts).mockImplementation(
        () =>
          new Promise((resolve) => {
            resolvePromise = resolve
          }),
      )
      const store = useProductStore()

      // When: 상품 목록 로드 시작 (await 없이)
      const loadPromise = store.loadProducts()

      // Then: 로딩 중 상태
      expect(store.isLoading).toBe(true)

      // Cleanup: Promise 해결
      resolvePromise!(mockProducts)
      await loadPromise
    })

    it('API 호출 실패 시 error 상태가 설정된다', async () => {
      // Given: API가 에러를 발생시키도록 Mock 설정
      vi.mocked(productsApi.fetchProducts).mockRejectedValue(
        new Error('Network Error'),
      )
      const store = useProductStore()

      // When: 상품 목록 로드 액션 호출
      await store.loadProducts()

      // Then: 에러 상태 검증
      expect(store.error).not.toBeNull()
      expect(store.error?.message).toBe('Network Error')
      expect(store.products).toEqual([])
      expect(store.isLoading).toBe(false)
    })
  })

  describe('selectProduct - 단일 상품 선택', () => {
    it('API 호출 성공 시 selectedProduct가 설정된다', async () => {
      // Given: API가 단일 상품을 반환하도록 Mock 설정
      const targetProduct = mockProducts[0]!
      vi.mocked(productsApi.fetchProduct).mockResolvedValue(targetProduct)
      const store = useProductStore()

      // When: 상품 선택 액션 호출
      await store.selectProduct(101)

      // Then: 선택된 상품이 state에 저장됨
      expect(store.selectedProduct).toEqual(targetProduct)
      expect(store.selectedProduct?.productId).toBe(101)
      expect(store.isLoading).toBe(false)
      expect(store.error).toBeNull()
    })

    it('API 호출 실패 시 selectedProduct는 null이고 error가 설정된다', async () => {
      // Given: API가 에러를 발생시키도록 Mock 설정
      vi.mocked(productsApi.fetchProduct).mockRejectedValue(
        new Error('Product not found'),
      )
      const store = useProductStore()

      // When: 존재하지 않는 상품 선택
      await store.selectProduct(999)

      // Then: 에러 상태 검증
      expect(store.selectedProduct).toBeNull()
      expect(store.error).not.toBeNull()
      expect(store.error?.code).toBe('PRODUCT_NOT_FOUND')
    })
  })

  describe('clearSelectedProduct - 선택 상품 초기화', () => {
    it('selectedProduct를 null로 초기화한다', async () => {
      // Given: 상품이 선택된 상태
      vi.mocked(productsApi.fetchProduct).mockResolvedValue(mockProducts[0]!)
      const store = useProductStore()
      await store.selectProduct(101)
      expect(store.selectedProduct).not.toBeNull()

      // When: 선택 상품 초기화
      store.clearSelectedProduct()

      // Then: selectedProduct가 null
      expect(store.selectedProduct).toBeNull()
    })
  })

  describe('clearError - 에러 초기화', () => {
    it('error를 null로 초기화한다', async () => {
      // Given: 에러가 발생한 상태
      vi.mocked(productsApi.fetchProducts).mockRejectedValue(
        new Error('Error'),
      )
      const store = useProductStore()
      await store.loadProducts()
      expect(store.error).not.toBeNull()

      // When: 에러 초기화
      store.clearError()

      // Then: error가 null
      expect(store.error).toBeNull()
    })
  })
})

