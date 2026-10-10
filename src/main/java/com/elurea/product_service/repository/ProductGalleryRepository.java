package com.elurea.product_service.repository;

import com.elurea.product_service.entity.ProductGallery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductGalleryRepository extends JpaRepository<ProductGallery, UUID> {

    List<ProductGallery> findAllByProduct_IdOrderByCreatedAtAsc(UUID productId);
}
