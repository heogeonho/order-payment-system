package com.example.api_server.product.service;

import com.example.api_server.common.exception.OutOfStockException;
import com.example.api_server.common.exception.ProductNotAvailableException;
import com.example.api_server.common.exception.ProductNotFoundException;
import com.example.api_server.product.domain.Product;
import com.example.api_server.product.dto.ProductResponse;
import com.example.api_server.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 서비스
 * 상품 조회 및 검증 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 전체 상품 목록 조회
     */
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 단일 상품 조회
     */
    public ProductResponse getProduct(Long productId) {
        Product product = getProductOrThrow(productId);
        return ProductResponse.from(product);
    }

    /**
     * 상품 조회 (없으면 예외 발생)
     *
     * @param productId 상품 ID
     * @return 상품 엔티티
     * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
     */
    public Product getProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    /**
     * 상품 판매 가능 여부 검증
     *
     * @param product 상품 엔티티
     * @throws ProductNotAvailableException 상품이 판매 불가 상태인 경우
     */
    public void validateProductAvailability(Product product) {
        if (!product.isAvailable()) {
            throw new ProductNotAvailableException(product.getProductId());
        }
    }

    /**
     * 재고 가용성 검증
     *
     * @param product 상품 엔티티
     * @param quantity 요청 수량
     * @throws OutOfStockException 재고가 부족한 경우
     */
    public void validateStockAvailability(Product product, int quantity) {
        if (!product.hasEnoughStock(quantity)) {
            throw new OutOfStockException(quantity, product.getAvailableStock());
        }
    }
}
