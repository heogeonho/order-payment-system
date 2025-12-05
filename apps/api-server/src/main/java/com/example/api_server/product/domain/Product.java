package com.example.api_server.product.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Column(nullable = false)
    private Long basePrice;

    @NotNull
    @Column(nullable = false)
    private Long discountPrice;

    @NotNull
    @Column(nullable = false)
    private Integer availableStock;

    @NotNull
    @Column(nullable = false)
    private Boolean available;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isAvailable() {
        return available && availableStock > 0;
    }

    public boolean hasEnoughStock(int quantity) {
        return availableStock >= quantity;
    }

    public void decreaseStock(int quantity) {
        if (!hasEnoughStock(quantity)) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.availableStock -= quantity;
    }
}
