package com.elurea.product_service.service;

import com.elurea.product_service.common.Strings;
import com.elurea.product_service.dto.gallery.CreateGalleryImageRequest;
import com.elurea.product_service.dto.gallery.GalleryImageResponse;
import com.elurea.product_service.entity.ProductGallery;
import com.elurea.product_service.exception.ResourceNotFoundException;
import com.elurea.product_service.repository.ProductGalleryRepository;
import com.elurea.product_service.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductGalleryService {

    private final ProductGalleryRepository galleryRepository;
    private final ProductService productService;
    private final ImageStorage imageStorage;

    /** Gallery images of one product, in upload order. Fails with 404 if the product does not exist. */
    public List<GalleryImageResponse> getByProduct(UUID productId) {
        productService.findProduct(productId);
        return galleryRepository.findAllByProduct_IdOrderByCreatedAtAsc(productId).stream()
                .map(GalleryImageResponse::from)
                .toList();
    }

    /** Uploads an image file and adds it to the product's gallery. */
    @Transactional
    public GalleryImageResponse upload(UUID productId, MultipartFile file) {
        ProductGallery image = new ProductGallery();
        image.setProduct(productService.findProduct(productId));
        image.setUrl(imageStorage.store(file, ProductService.IMAGE_FOLDER));

        return GalleryImageResponse.from(galleryRepository.saveAndFlush(image));
    }

    /** Adds an image that is already hosted elsewhere (e.g. a CDN), by URL. */
    @Transactional
    public GalleryImageResponse create(CreateGalleryImageRequest request) {
        ProductGallery image = new ProductGallery();
        image.setProduct(productService.findProduct(request.productId()));
        image.setUrl(Strings.trimToNull(request.url()));

        // Flush so the response carries the database-generated timestamp.
        return GalleryImageResponse.from(galleryRepository.saveAndFlush(image));
    }

    @Transactional
    public void delete(UUID id) {
        ProductGallery image = galleryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery image", id));
        galleryRepository.delete(image);
        imageStorage.deleteAfterCommit(image.getUrl());
    }
}
