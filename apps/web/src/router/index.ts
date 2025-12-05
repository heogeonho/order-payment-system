import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      redirect: '/products',
    },
    {
      // SCR-HP-001: 상품 목록 화면
      path: '/products',
      name: 'product-list',
      component: () => import('../views/ProductListView.vue'),
    },
    {
      // SCR-HP-002: 상품 상세/주문 화면
      path: '/products/:id',
      name: 'product-order',
      component: () => import('../views/ProductOrderView.vue'),
    },
    {
      // SCR-HP-004: 주문 완료 화면
      path: '/orders/:orderId/complete',
      name: 'order-complete',
      component: () => import('../views/OrderCompleteView.vue'),
    },
  ],
})

export default router
