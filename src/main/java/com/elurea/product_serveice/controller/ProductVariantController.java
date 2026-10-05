package com.elurea.product_serveice.controller;

import com.elurea.product_serveice.dto.SaveProductVariantRequest;
import com.elurea.product_serveice.entity.ProductVariant;
import com.elurea.product_serveice.service.ProductVariantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product_variants")
public class ProductVariantController {
    private final ProductVariantService productVariantService;

    public ProductVariantController(ProductVariantService productVariantService) {
        this.productVariantService = productVariantService;
    }

    @GetMapping
    public ResponseEntity<List<ProductVariant>> getProductVariants(String productId){
        return ResponseEntity.ok(productVariantService.getProductVariants(productId));
    }

    @PostMapping
    public ResponseEntity<ProductVariant> create(@RequestBody SaveProductVariantRequest request){
        return ResponseEntity.ok(productVariantService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductVariant> update(@PathVariable UUID id, @RequestBody SaveProductVariantRequest req){
        req.id = id;
        return ResponseEntity.ok(productVariantService.update(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id){
        productVariantService.delete(id);
        return ResponseEntity.ok("Product Variant Deleted");
    }
}
