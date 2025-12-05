package com.example.api_server.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Product 엔티티 테스트")
class ProductTest {

    @Test
    @DisplayName("상품 재고가 충분하면 isAvailable은 true를 반환한다")
    void isAvailable_충분한_재고와_판매가능_상태이면_true() {
        // given
        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기 프리미엄")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();

        // when
        boolean result = product.isAvailable();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("재고가 0이면 isAvailable은 false를 반환한다")
    void isAvailable_재고가_0이면_false() {
        // given
        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기 프리미엄")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(0)
                .available(true)
                .build();

        // when
        boolean result = product.isAvailable();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("판매 불가 상태면 isAvailable은 false를 반환한다")
    void isAvailable_판매불가_상태면_false() {
        // given
        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기 프리미엄")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(false)
                .build();

        // when
        boolean result = product.isAvailable();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("요청 수량이 재고 이하면 hasEnoughStock은 true를 반환한다")
    void hasEnoughStock_요청수량이_재고이하면_true() {
        // given
        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기 프리미엄")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();

        // when
        boolean result = product.hasEnoughStock(5);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("요청 수량이 재고보다 많으면 hasEnoughStock은 false를 반환한다")
    void hasEnoughStock_요청수량이_재고초과하면_false() {
        // given
        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기 프리미엄")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();

        // when
        boolean result = product.hasEnoughStock(15);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("재고 차감 시 정상적으로 차감된다")
    void decreaseStock_정상_차감() {
        // given
        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기 프리미엄")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();

        // when
        product.decreaseStock(3);

        // then
        assertThat(product.getAvailableStock()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고보다 많은 수량 차감 시도 시 예외가 발생한다")
    void decreaseStock_재고부족_예외() {
        // given
        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기 프리미엄")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();

        // when & then
        assertThatThrownBy(() -> product.decreaseStock(15))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고가 부족합니다");
    }
}