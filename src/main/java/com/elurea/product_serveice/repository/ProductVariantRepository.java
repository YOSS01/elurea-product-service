package com.elurea.product_serveice.repository;

import com.elurea.product_serveice.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    List<ProductVariant> findAllByProductId(String productId);

    ProductVariant save(ProductVariant productVariant);

    void deleteById(UUID id);
}
