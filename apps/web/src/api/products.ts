/**
 * 상품 API - API-PRD-001
 */
import apiClient from './client'
import type { Product } from '@/types'

/**
 * 상품 목록 조회
 * API ID: API-PRD-001
 * Method: GET
 * Path: /api/products
 */
export async function fetchProducts(): Promise<Product[]> {
  const response = await apiClient.get<Product[]>('/products')
  return response.data
}

/**
 * 단일 상품 조회
 * @param productId 상품 ID
 */
export async function fetchProduct(productId: number): Promise<Product> {
  const response = await apiClient.get<Product>(`/products/${productId}`)
  return response.data
}
