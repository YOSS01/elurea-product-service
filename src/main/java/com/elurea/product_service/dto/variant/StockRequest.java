package com.elurea.product_service.dto.variant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Stock change for several variants at once, e.g. all lines of an order. Applied all-or-nothing.
 */
public record StockRequest(
        @NotEmpty
        @Size(max = 100)
        List<@Valid @NotNull Item> items
) {

    public record Item(
            @NotNull
            UUID variantId,

            @NotNull
            @Min(1)
            @Max(10_000)
            Integer quantity
    ) {
    }
}
