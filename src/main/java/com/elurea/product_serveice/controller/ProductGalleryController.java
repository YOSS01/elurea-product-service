package com.elurea.product_serveice.controller;

import com.elurea.product_serveice.dto.SaveProductGalleryRequest;
import com.elurea.product_serveice.entity.ProductGallery;
import com.elurea.product_serveice.service.ProductGalleryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product_galleries")
public class ProductGalleryController {
    private final ProductGalleryService productGalleryService;

    public ProductGalleryController(ProductGalleryService productGalleryService) {
        this.productGalleryService = productGalleryService;
    }

    @GetMapping
    public ResponseEntity<List<ProductGallery>> getProductGallery(String productId){
        return ResponseEntity.ok(productGalleryService.getProductGallery(productId));
    }

    @PostMapping
    public ResponseEntity<ProductGallery> create(@RequestBody SaveProductGalleryRequest request){
        return ResponseEntity.ok(productGalleryService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id){
        productGalleryService.delete(id);
        return ResponseEntity.ok("Product Gallery Deleted");
    }
}
