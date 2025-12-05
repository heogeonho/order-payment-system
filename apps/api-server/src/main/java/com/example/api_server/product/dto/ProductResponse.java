package com.example.api_server.product.dto;

import com.example.api_server.product.domain.Product;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductResponse {
    private final Long productId;
    private final String name;
    private final Long basePrice;
    private final Long discountPrice;
    private final Integer availableStock;
    private final Boolean available;
    private final LocalDateTime createdAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .basePrice(product.getBasePrice())
                .discountPrice(product.getDiscountPrice())
                .availableStock(product.getAvailableStock())
                .available(product.isAvailable())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
