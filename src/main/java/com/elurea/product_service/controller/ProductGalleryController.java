package com.elurea.product_service.controller;

import com.elurea.product_service.dto.gallery.CreateGalleryImageRequest;
import com.elurea.product_service.dto.gallery.GalleryImageResponse;
import com.elurea.product_service.service.ProductGalleryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-galleries")
@RequiredArgsConstructor
public class ProductGalleryController {

    private final ProductGalleryService galleryService;

    @GetMapping
    public List<GalleryImageResponse> getByProduct(@RequestParam UUID productId) {
        return galleryService.getByProduct(productId);
    }

    /** Multipart upload: form fields "productId" and "file" (JPEG, PNG or WebP, 5 MB max). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GalleryImageResponse> upload(@RequestParam UUID productId,
                                                       @RequestPart("file") MultipartFile file) {
        GalleryImageResponse created = galleryService.upload(productId, file);
        return ResponseEntity.created(Locations.of(created.id())).body(created);
    }

    /** JSON body: registers an image already hosted elsewhere. */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GalleryImageResponse> create(@Valid @RequestBody CreateGalleryImageRequest request) {
        GalleryImageResponse created = galleryService.create(request);
        return ResponseEntity.created(Locations.of(created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        galleryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
