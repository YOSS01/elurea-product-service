package com.elurea.product_service.dto.product;

import com.elurea.product_service.dto.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

/**
 * The slug is optional: when omitted it is generated from the title.
 */
public record CreateProductRequest(
        @NotNull
        UUID categoryId,

        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        @Size(max = 5000)
        String description,

        @URL
        @Size(max = 512)
        String thumbnail,

        @Size(max = 255)
        @Pattern(regexp = ValidationPatterns.SLUG, message = ValidationPatterns.SLUG_MESSAGE)
        String slug
) {
}
