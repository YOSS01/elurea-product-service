package com.elurea.product_service.dto.variant;

import com.elurea.product_service.dto.ValidationPatterns;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateVariantRequest(
        @NotNull
        UUID productId,

        @NotBlank
        @Size(max = 64)
        @Pattern(regexp = ValidationPatterns.SKU, message = ValidationPatterns.SKU_MESSAGE)
        String sku,

        @NotBlank
        @Size(max = 20)
        String size,

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @NotNull
        @PositiveOrZero
        Integer stock
) {
}
