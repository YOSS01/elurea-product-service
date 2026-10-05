package com.elurea.product_serveice.service;

import com.elurea.product_serveice.dto.SaveProductGalleryRequest;
import com.elurea.product_serveice.entity.ProductGallery;
import com.elurea.product_serveice.repository.ProductGalleryRespository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductGalleryService {
    private final ProductGalleryRespository productGalleryRespository;

    public ProductGalleryService(ProductGalleryRespository productGalleryRespository) {
        this.productGalleryRespository = productGalleryRespository;
    }

    // Get Product Gallery
    public List<ProductGallery> getProductGallery(String productId) {
        return productGalleryRespository.findAllByProductId(productId);
    }

    // Create Product Gallery
    public ProductGallery create(SaveProductGalleryRequest request) {
        ProductGallery productGallery = new ProductGallery();

        // Should handle saving the image and generate the url
        // ...
        productGallery.setProductId(request.productId);
        productGallery.setUrl(request.url);

        return productGalleryRespository.save(productGallery);
    }

    // Delete Product Gallery
    public void delete(UUID id) {
        if(id == null) throw new RuntimeException("Product Gallery ID is required");
        ProductGallery productGallery = productGalleryRespository.findById(id).orElseThrow(() -> new RuntimeException("Product Gallery not found"));

        productGalleryRespository.deleteById(productGallery.getId());
    }
}
