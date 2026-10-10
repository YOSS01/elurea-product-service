package com.elurea.product_service.dto.gallery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

public record CreateGalleryImageRequest(
        @NotNull
        UUID productId,

        @NotBlank
        @URL
        @Size(max = 512)
        String url
) {
}
