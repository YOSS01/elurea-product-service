package com.elurea.product_serveice.service;

import com.elurea.product_serveice.dto.SaveProductRequest;
import com.elurea.product_serveice.entity.Product;
import com.elurea.product_serveice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Get All Products
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    // Create New Product
    public Product create(SaveProductRequest request) {
        if(productRepository.findBySlug(request.slug).isPresent()){
            throw new RuntimeException("Slug already exists");
        }

        Product product = new Product();
        product.setCategoryId(request.categoryId);
        product.setTitle(request.title);
        product.setDescription(request.description);
        if(request.thumbnail != null) product.setThumbnail(request.thumbnail);
        product.setSlug(request.slug);

        return productRepository.save(product);
    }

    // Update Product
    public Product update(SaveProductRequest request) {
        Product product = productRepository.findById(request.id).orElseThrow(() -> new RuntimeException("Product not found"));

        if(request.categoryId != null) product.setCategoryId(request.categoryId);
        if(request.title != null) product.setTitle(request.title);
        if(request.description != null) product.setDescription(request.description);
        if(request.thumbnail != null) product.setThumbnail(request.thumbnail);
        if(request.slug != null) product.setSlug(request.slug);

        return productRepository.save(product);
    }

    // Soft Delete Product
    public void delete(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        product.setDeletedAt(LocalDateTime.now());

        productRepository.save(product);
    }
}
