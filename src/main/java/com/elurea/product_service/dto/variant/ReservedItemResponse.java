package com.elurea.product_service.dto.variant;

import com.elurea.product_service.entity.Product;
import com.elurea.product_service.entity.ProductVariant;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One reserved line, with everything the order service needs to snapshot the purchase
 * (product name, image and the price at the time of the order).
 */
public record ReservedItemResponse(
        UUID variantId,
        UUID productId,
        String productTitle,
        String productSlug,
        String thumbnail,
        String sku,
        String size,
        BigDecimal unitPrice,
        int quantity,
        int remainingStock
) {

    public static ReservedItemResponse from(ProductVariant variant, int quantity) {
        Product product = variant.getProduct();
        return new ReservedItemResponse(
                variant.getId(),
                product.getId(),
                product.getTitle(),
                product.getSlug(),
                product.getThumbnail(),
                variant.getSku(),
                variant.getSize(),
                variant.getPrice(),
                quantity,
                variant.getStock()
        );
    }
}
