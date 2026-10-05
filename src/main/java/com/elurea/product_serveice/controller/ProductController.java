package com.elurea.product_serveice.controller;

import com.elurea.product_serveice.dto.SaveProductRequest;
import com.elurea.product_serveice.entity.Product;
import com.elurea.product_serveice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(){
        return ResponseEntity.ok(productService.getAll());
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody SaveProductRequest req){
        return ResponseEntity.ok(productService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable UUID id, @RequestBody SaveProductRequest req){
        req.id = id;
        return ResponseEntity.ok(productService.update(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id){
        productService.delete(id);
        return ResponseEntity.ok("Product Deleted");
    }
}
