package com.elurea.product_service.dto.product;

import com.elurea.product_service.dto.ValidationPatterns;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

/**
 * Partial update: null fields are left unchanged.
 */
public record UpdateProductRequest(
        UUID categoryId,

        @Size(max = 255)
        @Pattern(regexp = ValidationPatterns.NOT_BLANK, message = ValidationPatterns.NOT_BLANK_MESSAGE)
        String title,

        @Size(max = 5000)
        @Pattern(regexp = ValidationPatterns.NOT_BLANK, message = ValidationPatterns.NOT_BLANK_MESSAGE)
        String description,

        @URL
        @Size(max = 512)
        String thumbnail,

        @Size(max = 255)
        @Pattern(regexp = ValidationPatterns.SLUG, message = ValidationPatterns.SLUG_MESSAGE)
        String slug
) {
}
