package com.example.api_server.product.service;

import com.example.api_server.common.exception.OutOfStockException;
import com.example.api_server.common.exception.ProductNotAvailableException;
import com.example.api_server.common.exception.ProductNotFoundException;
import com.example.api_server.product.domain.Product;
import com.example.api_server.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("ProductService 테스트")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품 조회 성공")
    void getProductOrThrow_성공() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .productId(productId)
                .name("무선 청소기")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        Product result = productService.getProductOrThrow(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("무선 청소기");
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("상품 조회 실패 - 상품 없음")
    void getProductOrThrow_상품없음() {
        // given
        Long productId = 999L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProductOrThrow(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("상품 판매 가능 여부 검증 - 판매 가능")
    void validateProductAvailability_판매가능() {
        // given
        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();

        // when & then
        assertThatCode(() -> productService.validateProductAvailability(product))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("상품 판매 가능 여부 검증 - 판매 불가")
    void validateProductAvailability_판매불가() {
        // given
        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(false)
                .build();

        // when & then
        assertThatThrownBy(() -> productService.validateProductAvailability(product))
                .isInstanceOf(ProductNotAvailableException.class)
                .hasMessageContaining("판매 불가능한 상품입니다");
    }

    @Test
    @DisplayName("재고 가용성 검증 - 재고 충분")
    void validateStockAvailability_재고충분() {
        // given
        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();

        // when & then
        assertThatCode(() -> productService.validateStockAvailability(product, 5))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("재고 가용성 검증 - 재고 부족")
    void validateStockAvailability_재고부족() {
        // given
        Product product = Product.builder()
                .productId(1L)
                .name("무선 청소기")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();

        // when & then
        assertThatThrownBy(() -> productService.validateStockAvailability(product, 15))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("재고가 부족합니다");
    }
}
