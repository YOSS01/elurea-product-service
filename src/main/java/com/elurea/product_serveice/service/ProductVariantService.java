package com.elurea.product_serveice.service;

import com.elurea.product_serveice.dto.SaveProductVariantRequest;
import com.elurea.product_serveice.entity.ProductVariant;
import com.elurea.product_serveice.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductVariantService {
    private final ProductVariantRepository productVariantRepository;

    public ProductVariantService(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    // Get Product Variants
    public List<ProductVariant> getProductVariants(String productId) {
        return productVariantRepository.findAllByProductId(productId);
    }

    // Create Product Variant
    public ProductVariant create(SaveProductVariantRequest request) {
        ProductVariant productVariant = new ProductVariant();

        productVariant.setProductId(request.productId);
        productVariant.setSku(request.sku);
        productVariant.setSize(request.size);
        productVariant.setPrice(request.price);
        productVariant.setStock(request.stock);

        return productVariantRepository.save(productVariant);
    }

    // Update Product Variant
    public ProductVariant update(SaveProductVariantRequest request) {
        ProductVariant productVariant = productVariantRepository.findById(request.id).orElseThrow(() -> new RuntimeException("Product Variant not found"));

        if(request.sku != null) productVariant.setSku(request.sku);
        if(request.size != null) productVariant.setSize(request.size);
        productVariant.setPrice(request.price);
        productVariant.setStock(request.stock);

        return productVariantRepository.save(productVariant);
    }

    // Delete Product Variant
    public void delete(UUID id) {
        if(id == null) throw new RuntimeException("Product Variant ID is required");
        ProductVariant productVariant = productVariantRepository.findById(id).orElseThrow(() -> new RuntimeException("Product Variant not found"));

        productVariantRepository.deleteById(productVariant.getId());
    }
}
