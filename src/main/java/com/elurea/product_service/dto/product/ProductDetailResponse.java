package com.elurea.product_service.dto.product;

import com.elurea.product_service.dto.category.CategorySummary;
import com.elurea.product_service.dto.gallery.GalleryImageResponse;
import com.elurea.product_service.dto.variant.VariantResponse;
import com.elurea.product_service.entity.Product;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full product page: the product with its variants and gallery images.
 */
public record ProductDetailResponse(
        UUID id,
        CategorySummary category,
        String title,
        String description,
        String thumbnail,
        String slug,
        List<VariantResponse> variants,
        List<GalleryImageResponse> gallery,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProductDetailResponse from(Product product,
                                             List<VariantResponse> variants,
                                             List<GalleryImageResponse> gallery) {
        return new ProductDetailResponse(
                product.getId(),
                CategorySummary.from(product.getCategory()),
                product.getTitle(),
                product.getDescription(),
                product.getThumbnail(),
                product.getSlug(),
                variants,
                gallery,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
