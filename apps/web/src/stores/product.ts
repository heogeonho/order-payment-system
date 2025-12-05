/**
 * Product Store - 상품 관련 상태 관리
 *
 * 관련 API: API-PRD-001 (GET /api/products)
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchProducts, fetchProduct } from '@/api'
import type { Product, ApiErrorResponse } from '@/types'

export const useProductStore = defineStore('product', () => {
  // ============================================
  // State
  // ============================================
  const products = ref<Product[]>([])
  const selectedProduct = ref<Product | null>(null)
  const isLoading = ref(false)
  const error = ref<ApiErrorResponse | null>(null)

  // ============================================
  // Actions
  // ============================================

  /**
   * 상품 목록 조회
   * API: GET /api/products
   */
  async function loadProducts(): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      products.value = await fetchProducts()
    } catch (e) {
      // TODO: 실제 에러 응답 구조 확인 필요
      if (e instanceof Error) {
        error.value = {
          code: 'PRODUCT_NOT_FOUND',
          message: e.message,
        }
      }
      products.value = []
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 단일 상품 조회 및 선택
   * @param productId 상품 ID
   */
  async function selectProduct(productId: number): Promise<void> {
    isLoading.value = true
    error.value = null

    try {
      selectedProduct.value = await fetchProduct(productId)
    } catch (e) {
      if (e instanceof Error) {
        error.value = {
          code: 'PRODUCT_NOT_FOUND',
          message: e.message,
        }
      }
      selectedProduct.value = null
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 선택된 상품 초기화
   */
  function clearSelectedProduct(): void {
    selectedProduct.value = null
  }

  /**
   * 에러 초기화
   */
  function clearError(): void {
    error.value = null
  }

  // ============================================
  // Return
  // ============================================
  return {
    // State
    products,
    selectedProduct,
    isLoading,
    error,
    // Actions
    loadProducts,
    selectProduct,
    clearSelectedProduct,
    clearError,
  }
})

