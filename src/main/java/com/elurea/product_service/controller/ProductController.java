package com.elurea.product_service.controller;

import com.elurea.product_service.common.PageResponse;
import com.elurea.product_service.common.SortWhitelist;
import com.elurea.product_service.dto.product.CreateProductRequest;
import com.elurea.product_service.dto.product.ProductDetailResponse;
import com.elurea.product_service.dto.product.ProductResponse;
import com.elurea.product_service.dto.product.UpdateProductRequest;
import com.elurea.product_service.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Set<String> SORTABLE = Set.of("title", "createdAt", "updatedAt");

    private final ProductService productService;

    /** Paged listing. Optional filters: categoryId, and search (case-insensitive match on the title). */
    @GetMapping
    public PageResponse<ProductResponse> search(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @Size(max = 100) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.search(categoryId, search, SortWhitelist.validate(pageable, SORTABLE));
    }

    /** Product with its variants and gallery. */
    @GetMapping("/{id}")
    public ProductDetailResponse getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    /** Same as getById, for SEO-friendly product page URLs. */
    @GetMapping("/slug/{slug}")
    public ProductDetailResponse getBySlug(@PathVariable String slug) {
        return productService.getBySlug(slug);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = productService.create(request);
        return ResponseEntity.created(Locations.of(created.id())).body(created);
    }

    @PatchMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
        return productService.update(id, request);
    }

    /** Multipart upload, form field "file": JPEG, PNG or WebP, 5 MB max. Replaces the current thumbnail. */
    @PutMapping(path = "/{id}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse uploadThumbnail(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return productService.uploadThumbnail(id, file);
    }

    @DeleteMapping("/{id}/thumbnail")
    public ProductResponse removeThumbnail(@PathVariable UUID id) {
        return productService.removeThumbnail(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
