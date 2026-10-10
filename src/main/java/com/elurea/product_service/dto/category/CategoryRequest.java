package com.elurea.product_service.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Used for both create and full update: a category only has a name.
 */
public record CategoryRequest(
        @NotBlank
        @Size(max = 100)
        String name
) {
}
