/**
 * 타입 정의 - feature-spec.md 2.1 엔티티/VO 정의 기반
 */

// ============================================
// Entity: Product
// ============================================
export interface Product {
  productId: number
  name: string
  basePrice: number
  discountPrice: number
  availableStock: number
  available: boolean
  createdAt: string
}

// ============================================
// Entity: Order
// ============================================
export type OrderStatus = 'PENDING_PAYMENT' | 'PAID' | 'PAYMENT_FAILED'

export interface Order {
  orderId: string
  userId: number
  productId: number
  quantity: number
  totalAmount: number
  status: OrderStatus
  createdAt: string
  updatedAt: string
}

// ============================================
// Entity: Payment
// ============================================
export type PaymentStatus = 'REQUESTED' | 'APPROVED' | 'DECLINED'

export interface Payment {
  id: number
  orderId: string
  paymentKey: string
  amount: number
  status: PaymentStatus
  pgResultCode: string | null
  pgResultMessage: string | null
  createdAt: string
}

// ============================================
// Entity: OrderHistory
// ============================================
export type OrderEventType = 'ORDER_CREATED' | 'PAYMENT_APPROVED' | 'PAYMENT_FAILED'

export interface OrderHistory {
  id: number
  orderId: string
  eventType: OrderEventType
  payloadJson: string
  createdAt: string
}

// ============================================
// API Request/Response Types
// ============================================

// API-ORD-001: POST /api/orders - 주문 생성
export interface CreateOrderRequest {
  userId: number
  productId: number
  quantity: number
}

export interface CreateOrderResponse {
  orderId: string
  userId: number
  productId: number
  quantity: number
  totalAmount: number
  status: OrderStatus
}

// API-PAY-001: POST /api/payments/approve - 결제 승인
export interface ApprovePaymentRequest {
  orderId: string
  paymentKey: string
  amount: number
}

export interface ApprovePaymentResponse {
  orderId: string
  paymentId: number
  paymentKey: string
  amount: number
  paymentStatus: PaymentStatus
  orderStatus: OrderStatus
  approvedAt: string
}

// ============================================
// API Error Response
// ============================================
export type ErrorCode =
  // 주문 생성 관련 (Case-API-01)
  | 'PRODUCT_NOT_FOUND'
  | 'PRODUCT_NOT_AVAILABLE'
  | 'OUT_OF_STOCK'
  | 'QUANTITY_INVALID'
  // 결제 승인 관련 (Case-API-02)
  | 'ORDER_NOT_FOUND'
  | 'ORDER_NOT_PAYABLE'
  | 'AMOUNT_MISMATCH'
  | 'PG_APPROVAL_FAILED'
  | 'PAYMENT_ALREADY_APPROVED'

export interface ApiErrorResponse {
  code: ErrorCode
  message: string
  detail?: string
}
