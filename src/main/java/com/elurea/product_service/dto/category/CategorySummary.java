package com.elurea.product_service.dto.category;

import com.elurea.product_service.entity.Category;

import java.util.UUID;

/**
 * Compact category embedded in product responses.
 */
public record CategorySummary(UUID id, String name) {

    public static CategorySummary from(Category category) {
        return new CategorySummary(category.getId(), category.getName());
    }
}
