package com.elurea.product_service.dto.category;

import com.elurea.product_service.entity.Category;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getCreatedAt(), category.getUpdatedAt());
    }
}
