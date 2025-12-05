<script setup lang="ts">
/**
 * 상품 목록 화면 (SCR-HP-001)
 *
 * UI 요소 (data-testid):
 * - product-card: 상품 카드
 * - product-name: 상품명 텍스트
 * - product-price: 할인가 텍스트
 * - product-select-button: 상품 상세로 이동 버튼
 */
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProductStore } from '@/stores/product'

const router = useRouter()
const productStore = useProductStore()

// 컴포넌트 마운트 시 상품 목록 로드
onMounted(() => {
  productStore.loadProducts()
})

/**
 * 상품 선택 시 상세 화면으로 이동
 * @param productId 상품 ID
 */
function handleSelectProduct(productId: number): void {
  router.push({ name: 'product-order', params: { id: productId } })
}

/**
 * 가격 포맷팅 (예: 129000 → "129,000원")
 */
function formatPrice(price: number): string {
  return price.toLocaleString('ko-KR') + '원'
}
</script>

<template>
  <div class="product-list-container">
    <h1 class="page-title">상품 목록</h1>

    <!-- 로딩 상태 -->
    <div v-if="productStore.isLoading" class="loading-state">
      상품을 불러오는 중입니다...
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="productStore.error" class="error-state">
      <p>{{ productStore.error.message }}</p>
      <button @click="productStore.loadProducts()">다시 시도</button>
    </div>

    <!-- 상품 목록 -->
    <div v-else class="product-grid">
      <div
        v-for="product in productStore.products"
        :key="product.productId"
        class="product-card"
        data-testid="product-card"
      >
        <div class="product-info">
          <h2 class="product-name" data-testid="product-name">
            {{ product.name }}
          </h2>
          <div class="price-info">
            <span
              v-if="product.basePrice !== product.discountPrice"
              class="base-price"
            >
              {{ formatPrice(product.basePrice) }}
            </span>
            <span class="discount-price" data-testid="product-price">
              {{ formatPrice(product.discountPrice) }}
            </span>
          </div>
          <p class="stock-info">
            재고: {{ product.availableStock }}개
          </p>
        </div>
        <button
          class="select-button"
          data-testid="product-select-button"
          :disabled="!product.available || product.availableStock === 0"
          @click="handleSelectProduct(product.productId)"
        >
          자세히 보기
        </button>
      </div>
    </div>

    <!-- 상품이 없을 때 -->
    <div
      v-if="!productStore.isLoading && !productStore.error && productStore.products.length === 0"
      class="empty-state"
    >
      등록된 상품이 없습니다.
    </div>
  </div>
</template>

<style scoped>
.product-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 24px;
  color: #1a1a1a;
}

.loading-state,
.error-state,
.empty-state {
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

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.product-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  background: white;
  transition: box-shadow 0.2s ease;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-info {
  margin-bottom: 16px;
}

.product-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #1a1a1a;
}

.price-info {
  margin-bottom: 8px;
}

.base-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-right: 8px;
}

.discount-price {
  font-size: 20px;
  font-weight: 700;
  color: #e53935;
}

.stock-info {
  font-size: 14px;
  color: #666;
}

.select-button {
  width: 100%;
  padding: 12px;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.select-button:hover:not(:disabled) {
  background-color: #1565c0;
}

.select-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>

