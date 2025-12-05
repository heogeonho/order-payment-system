package com.example.api_server.product.repository;

import com.example.api_server.product.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("ProductRepository 테스트")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품을 저장하고 조회할 수 있다")
    void save_조회_성공() {
        // given
        Product product = Product.builder()
                .name("무선 청소기 프리미엄")
                .basePrice(150000L)
                .discountPrice(129000L)
                .availableStock(10)
                .available(true)
                .build();

        // when
        Product saved = productRepository.save(product);
        productRepository.flush();

        // then
        Optional<Product> found = productRepository.findById(saved.getProductId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("무선 청소기 프리미엄");
        assertThat(found.get().getDiscountPrice()).isEqualTo(129000L);
        assertThat(found.get().getAvailableStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 Optional.empty를 반환한다")
    void findById_존재하지않으면_empty() {
        // when
        Optional<Product> found = productRepository.findById(999L);

        // then
        assertThat(found).isEmpty();
    }
}