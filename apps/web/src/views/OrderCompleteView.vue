<script setup lang="ts">
/**
 * 주문 완료 화면 (SCR-HP-004)
 *
 * UI 요소 (data-testid):
 * - order-complete-message: 주문 완료 메시지
 * - order-complete-order-id: 주문번호 표시 영역
 * - payment-error-message: 결제 오류 메시지 영역
 * - payment-retry-button: 결제 재시도 버튼
 * - payment-loading-indicator: 결제 진행 중 표시
 *
 * 테스트 케이스:
 * - FE_UT_03: 결제 승인 성공 시 주문 완료 화면 렌더링
 * - FE_UT_04: 결제 실패 시 오류 메시지 및 재시도 버튼 노출
 */
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import { usePaymentStore } from '@/stores/payment'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const paymentStore = usePaymentStore()

// 주문 ID (라우트 파라미터에서 가져옴)
const orderId = computed(() => route.params.orderId as string)

// 결제 성공 여부
const isPaymentSuccess = computed(() => {
  return paymentStore.paymentResult?.paymentStatus === 'APPROVED'
})

// 결제 실패 여부
const isPaymentFailed = computed(() => {
  return paymentStore.error !== null
})

// 컴포넌트 마운트 시 결제 승인 요청
onMounted(async () => {
  // 주문 정보가 있으면 결제 승인 요청
  if (orderStore.currentOrder) {
    // TODO: 실제로는 PG에서 받은 paymentKey를 사용해야 함
    // 현재는 시뮬레이션용 더미 값 사용
    await paymentStore.requestApproval({
      orderId: orderStore.currentOrder.orderId,
      paymentKey: `pay_${Date.now()}`,
      amount: orderStore.currentOrder.totalAmount,
    })
  }
})

/**
 * 결제 재시도 핸들러
 */
async function handleRetryPayment(): Promise<void> {
  paymentStore.clearError()

  if (orderStore.currentOrder) {
    await paymentStore.requestApproval({
      orderId: orderStore.currentOrder.orderId,
      paymentKey: `pay_${Date.now()}`,
      amount: orderStore.currentOrder.totalAmount,
    })
  }
}

/**
 * 홈으로 이동
 */
function goToHome(): void {
  // 상태 초기화
  orderStore.reset()
  paymentStore.reset()
  router.push({ name: 'product-list' })
}

/**
 * 가격 포맷팅
 */
function formatPrice(price: number): string {
  return price.toLocaleString('ko-KR') + '원'
}
</script>

<template>
  <div class="order-complete-container">
    <!-- 결제 진행 중 -->
    <div
      v-if="paymentStore.isLoading"
      class="loading-state"
      data-testid="payment-loading-indicator"
    >
      <div class="spinner"></div>
      <p>결제 처리 중입니다...</p>
    </div>

    <!-- 결제 성공 (FE_UT_03) -->
    <div v-else-if="isPaymentSuccess" class="success-state">
      <div class="success-icon">✓</div>
      <h1 class="complete-message" data-testid="order-complete-message">
        주문이 정상적으로 완료되었습니다.
      </h1>
      <div class="order-info">
        <p class="order-id" data-testid="order-complete-order-id">
          주문번호: {{ orderId }}
        </p>
        <p v-if="paymentStore.paymentResult" class="payment-info">
          결제금액: {{ formatPrice(paymentStore.paymentResult.amount) }}
        </p>
      </div>
      <button class="home-button" @click="goToHome">
        쇼핑 계속하기
      </button>
    </div>

    <!-- 결제 실패 (FE_UT_04) -->
    <div v-else-if="isPaymentFailed" class="error-state">
      <div class="error-icon">!</div>
      <h1 class="error-title">결제에 실패했습니다</h1>
      <p class="error-message" data-testid="payment-error-message">
        {{ paymentStore.error?.message }}
      </p>
      <div class="error-actions">
        <button
          class="retry-button"
          data-testid="payment-retry-button"
          @click="handleRetryPayment"
        >
          다시 결제하기
        </button>
        <button class="home-button secondary" @click="goToHome">
          홈으로 돌아가기
        </button>
      </div>
    </div>

    <!-- 주문 정보 없음 -->
    <div v-else class="empty-state">
      <p>주문 정보를 찾을 수 없습니다.</p>
      <button class="home-button" @click="goToHome">
        홈으로 돌아가기
      </button>
    </div>
  </div>
</template>

<style scoped>
.order-complete-container {
  max-width: 500px;
  margin: 0 auto;
  padding: 48px 24px;
  text-align: center;
}

.loading-state {
  padding: 48px;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e0e0e0;
  border-top-color: #1976d2;
  border-radius: 50%;
  margin: 0 auto 24px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.success-state {
  padding: 24px;
}

.success-icon {
  width: 80px;
  height: 80px;
  background-color: #4caf50;
  color: white;
  font-size: 48px;
  line-height: 80px;
  border-radius: 50%;
  margin: 0 auto 24px;
}

.complete-message {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 24px;
}

.order-info {
  background-color: #f5f5f5;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 32px;
}

.order-id {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}

.payment-info {
  font-size: 18px;
  font-weight: 600;
  color: #1976d2;
}

.error-state {
  padding: 24px;
}

.error-icon {
  width: 80px;
  height: 80px;
  background-color: #f44336;
  color: white;
  font-size: 48px;
  line-height: 80px;
  border-radius: 50%;
  margin: 0 auto 24px;
}

.error-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.error-message {
  font-size: 16px;
  color: #666;
  margin-bottom: 32px;
}

.error-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.retry-button {
  width: 100%;
  padding: 16px;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.retry-button:hover {
  background-color: #1565c0;
}

.home-button {
  width: 100%;
  padding: 16px;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.home-button:hover {
  background-color: #1565c0;
}

.home-button.secondary {
  background-color: #f5f5f5;
  color: #333;
}

.home-button.secondary:hover {
  background-color: #e0e0e0;
}

.empty-state {
  padding: 48px;
  color: #666;
}

.empty-state button {
  margin-top: 24px;
}
</style>
