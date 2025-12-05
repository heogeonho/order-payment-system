/**
 * 주문 API - API-ORD-001
 */
import apiClient from './client'
import type { CreateOrderRequest, CreateOrderResponse } from '@/types'

/**
 * 주문 생성
 * API ID: API-ORD-001
 * Method: POST
 * Path: /api/orders
 *
 * 비즈니스 규칙:
 * - Rule-01: 단일 상품 주문 제한
 * - Rule-02: 수량 및 재고 규칙 (quantity > availableStock → OUT_OF_STOCK)
 * - Rule-03: 금액 계산 규칙 (totalAmount = discountPrice × quantity)
 */
export async function createOrder(
  request: CreateOrderRequest,
): Promise<CreateOrderResponse> {
  const response = await apiClient.post<CreateOrderResponse>('/orders', request)
  return response.data
}

