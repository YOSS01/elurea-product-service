package com.elurea.product_service.dto.product;

import com.elurea.product_service.dto.category.CategorySummary;
import com.elurea.product_service.entity.Product;

import java.time.Instant;
import java.util.UUID;

/**
 * Product as shown in listings, without variants and gallery.
 */
public record ProductResponse(
        UUID id,
        CategorySummary category,
        String title,
        String description,
        String thumbnail,
        String slug,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                CategorySummary.from(product.getCategory()),
                product.getTitle(),
                product.getDescription(),
                product.getThumbnail(),
                product.getSlug(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
