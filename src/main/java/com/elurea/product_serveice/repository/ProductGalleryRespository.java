package com.elurea.product_serveice.repository;

import com.elurea.product_serveice.entity.ProductGallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductGalleryRespository extends JpaRepository<ProductGallery, UUID> {
    List<ProductGallery> findAllByProductId(String productId);

    ProductGallery save(ProductGallery productGallery);

    void deleteById(UUID id);
}
