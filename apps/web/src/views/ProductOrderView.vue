<script setup lang="ts">
/**
 * 상품 상세/주문 화면 (SCR-HP-002)
 *
 * UI 요소 (data-testid):
 * - order-quantity-input: 주문 수량 입력 필드
 * - order-submit-button: 주문 생성 요청 버튼
 * - payment-error-message: 결제 오류 메시지 영역
 *
 * 테스트 케이스:
 * - FE_UT_01: 수량 입력 후 주문하기 클릭 시 API 호출
 * - FE_UT_02: 주문 생성 성공 시 PG 결제 플로우로 이동
 *
 * 비즈니스 규칙:
 * - Rule-02: 수량은 1 이상, 재고 이하
 * - Rule-03: totalAmount = discountPrice × quantity
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductStore } from '@/stores/product'
import { useOrderStore } from '@/stores/order'

const route = useRoute()
const router = useRouter()
const productStore = useProductStore()
const orderStore = useOrderStore()

// 주문 수량 (기본값 1)
const quantity = ref(1)

// 상품 ID (라우트 파라미터에서 가져옴)
const productId = computed(() => Number(route.params.id))

// 총 금액 계산 (Rule-03)
const totalAmount = computed(() => {
  if (!productStore.selectedProduct) return 0
  return productStore.selectedProduct.discountPrice * quantity.value
})

// 주문 가능 여부
const canOrder = computed(() => {
  const product = productStore.selectedProduct
  if (!product) return false
  if (!product.available) return false
  if (quantity.value < 1) return false
  if (quantity.value > product.availableStock) return false
  return true
})

// 수량 유효성 에러 메시지
const quantityError = computed(() => {
  const product = productStore.selectedProduct
  if (!product) return null
  if (quantity.value < 1) return '수량은 1 이상이어야 합니다.'
  if (quantity.value > product.availableStock) {
    return `재고가 부족합니다. (최대 ${product.availableStock}개)`
  }
  return null
})

// 컴포넌트 마운트 시 상품 정보 로드
onMounted(async () => {
  orderStore.clearError()
  await productStore.selectProduct(productId.value)
})

/**
 * 주문 제출 핸들러
 * FE_UT_01: 수량 입력 후 주문하기 클릭 시 API 호출
 * FE_UT_02: 주문 생성 성공 시 PG 결제 플로우로 이동
 */
async function handleSubmitOrder(): Promise<void> {
  if (!canOrder.value) return

  // TODO: 실제 userId는 로그인 정보에서 가져와야 함
  const userId = 1

  const success = await orderStore.submitOrder({
    userId,
    productId: productId.value,
    quantity: quantity.value,
  })

  if (success && orderStore.currentOrder) {
    // FE_UT_02: 주문 생성 성공 시 PG 결제 플로우로 이동
    // TODO: 실제로는 PG 결제 화면(SCR-HP-003)을 호출
    // 현재는 결제 승인 시뮬레이션 후 완료 화면으로 이동
    router.push({
      name: 'order-complete',
      params: { orderId: orderStore.currentOrder.orderId },
    })
  }
}

/**
 * 가격 포맷팅
 */
function formatPrice(price: number): string {
  return price.toLocaleString('ko-KR') + '원'
}

/**
 * 목록으로 돌아가기
 */
function goBack(): void {
  router.push({ name: 'product-list' })
}
</script>

<template>
  <div class="product-order-container">
    <!-- 로딩 상태 -->
    <div v-if="productStore.isLoading" class="loading-state">
      상품 정보를 불러오는 중입니다...
    </div>

    <!-- 상품을 찾을 수 없음 -->
    <div v-else-if="productStore.error || !productStore.selectedProduct" class="error-state">
      <p>상품을 찾을 수 없습니다.</p>
      <button @click="goBack">목록으로 돌아가기</button>
    </div>

    <!-- 상품 상세 및 주문 폼 -->
    <div v-else class="product-detail">
      <button class="back-button" @click="goBack">← 목록으로</button>

      <div class="product-info-section">
        <h1 class="product-title">{{ productStore.selectedProduct.name }}</h1>

        <div class="price-section">
          <span
            v-if="productStore.selectedProduct.basePrice !== productStore.selectedProduct.discountPrice"
            class="base-price"
          >
            {{ formatPrice(productStore.selectedProduct.basePrice) }}
          </span>
          <span class="discount-price">
            {{ formatPrice(productStore.selectedProduct.discountPrice) }}
          </span>
        </div>

        <p class="stock-info">
          재고: {{ productStore.selectedProduct.availableStock }}개
        </p>
      </div>

      <!-- 주문 폼 -->
      <div class="order-form">
        <div class="quantity-section">
          <label for="quantity">수량</label>
          <input
            id="quantity"
            v-model.number="quantity"
            type="number"
            min="1"
            :max="productStore.selectedProduct.availableStock"
            data-testid="order-quantity-input"
          />
          <span v-if="quantityError" class="quantity-error">
            {{ quantityError }}
          </span>
        </div>

        <div class="total-section">
          <span class="total-label">총 결제금액</span>
          <span class="total-amount">{{ formatPrice(totalAmount) }}</span>
        </div>

        <!-- 에러 메시지 (FE-SC-02: 재고 부족 등) -->
        <div
          v-if="orderStore.error"
          class="error-message"
          data-testid="payment-error-message"
        >
          {{ orderStore.error.message }}
        </div>

        <!-- 주문 버튼 -->
        <button
          class="submit-button"
          data-testid="order-submit-button"
          :disabled="!canOrder || orderStore.isLoading"
          @click="handleSubmitOrder"
        >
          <span v-if="orderStore.isLoading">주문 처리 중...</span>
          <span v-else>주문하기</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-order-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px;
}

.loading-state,
.error-state {
  text-align: center;
  padding: 48px;
  color: #666;
}

.error-state button {
  margin-top: 16px;
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.back-button {
  background: none;
  border: none;
  color: #1976d2;
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  margin-bottom: 24px;
}

.back-button:hover {
  text-decoration: underline;
}

.product-info-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e0e0e0;
}

.product-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #1a1a1a;
}

.price-section {
  margin-bottom: 12px;
}

.base-price {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
  margin-right: 12px;
}

.discount-price {
  font-size: 28px;
  font-weight: 700;
  color: #e53935;
}

.stock-info {
  font-size: 14px;
  color: #666;
}

.order-form {
  background: #f9f9f9;
  padding: 24px;
  border-radius: 12px;
}

.quantity-section {
  margin-bottom: 24px;
}

.quantity-section label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.quantity-section input {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-sizing: border-box;
}

.quantity-section input:focus {
  outline: none;
  border-color: #1976d2;
}

.quantity-error {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: #e53935;
}

.total-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-top: 1px solid #e0e0e0;
  margin-bottom: 16px;
}

.total-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.total-amount {
  font-size: 24px;
  font-weight: 700;
  color: #1976d2;
}

.error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
}

.submit-button {
  width: 100%;
  padding: 16px;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.submit-button:hover:not(:disabled) {
  background-color: #1565c0;
}

.submit-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>
