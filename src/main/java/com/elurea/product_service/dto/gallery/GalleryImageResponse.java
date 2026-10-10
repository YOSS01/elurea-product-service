package com.elurea.product_service.dto.gallery;

import com.elurea.product_service.entity.ProductGallery;

import java.time.Instant;
import java.util.UUID;

public record GalleryImageResponse(
        UUID id,
        UUID productId,
        String url,
        Instant createdAt
) {

    public static GalleryImageResponse from(ProductGallery image) {
        return new GalleryImageResponse(
                image.getId(),
                image.getProduct().getId(), // reading the id does not initialise the lazy proxy
                image.getUrl(),
                image.getCreatedAt()
        );
    }
}
