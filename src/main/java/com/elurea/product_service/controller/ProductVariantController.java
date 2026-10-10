package com.elurea.product_service.controller;

import com.elurea.product_service.dto.variant.CreateVariantRequest;
import com.elurea.product_service.dto.variant.ReservedItemResponse;
import com.elurea.product_service.dto.variant.StockRequest;
import com.elurea.product_service.dto.variant.UpdateVariantRequest;
import com.elurea.product_service.dto.variant.VariantResponse;
import com.elurea.product_service.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    /** A product has few variants, so the list is not paginated. */
    @GetMapping
    public List<VariantResponse> getByProduct(@RequestParam UUID productId) {
        return variantService.getByProduct(productId);
    }

    @GetMapping("/{id}")
    public VariantResponse getById(@PathVariable UUID id) {
        return variantService.getById(id);
    }

    @PostMapping
    public ResponseEntity<VariantResponse> create(@Valid @RequestBody CreateVariantRequest request) {
        VariantResponse created = variantService.create(request);
        return ResponseEntity.created(Locations.of(created.id())).body(created);
    }

    /**
     * Reserves stock for an order. Callable by administrators or by back-end services
     * with the X-Internal-Api-Key header. 409 if any line is short; nothing is reserved then.
     */
    @PostMapping("/stock/reserve")
    public List<ReservedItemResponse> reserveStock(@Valid @RequestBody StockRequest request) {
        return variantService.reserveStock(request);
    }

    /** Returns previously reserved stock, e.g. when an order is cancelled. */
    @PostMapping("/stock/release")
    public List<VariantResponse> releaseStock(@Valid @RequestBody StockRequest request) {
        return variantService.releaseStock(request);
    }

    @PatchMapping("/{id}")
    public VariantResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateVariantRequest request) {
        return variantService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        variantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
