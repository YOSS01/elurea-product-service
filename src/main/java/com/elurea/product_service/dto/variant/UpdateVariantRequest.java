package com.elurea.product_service.dto.variant;

import com.elurea.product_service.dto.ValidationPatterns;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Partial update: null fields are left unchanged. A variant cannot be moved to another product.
 */
public record UpdateVariantRequest(
        @Size(max = 64)
        @Pattern(regexp = ValidationPatterns.SKU, message = ValidationPatterns.SKU_MESSAGE)
        String sku,

        @Size(max = 20)
        @Pattern(regexp = ValidationPatterns.NOT_BLANK, message = ValidationPatterns.NOT_BLANK_MESSAGE)
        String size,

        @DecimalMin("0.01")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @PositiveOrZero
        Integer stock
) {
}
