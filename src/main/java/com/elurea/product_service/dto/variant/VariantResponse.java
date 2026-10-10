package com.elurea.product_service.dto.variant;

import com.elurea.product_service.entity.ProductVariant;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VariantResponse(
        UUID id,
        UUID productId,
        String sku,
        String size,
        BigDecimal price,
        int stock,
        Instant createdAt,
        Instant updatedAt
) {

    public static VariantResponse from(ProductVariant variant) {
        return new VariantResponse(
                variant.getId(),
                variant.getProduct().getId(), // reading the id does not initialise the lazy proxy
                variant.getSku(),
                variant.getSize(),
                variant.getPrice(),
                variant.getStock(),
                variant.getCreatedAt(),
                variant.getUpdatedAt()
        );
    }
}
