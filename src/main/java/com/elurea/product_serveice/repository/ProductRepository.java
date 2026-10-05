package com.elurea.product_serveice.repository;

import com.elurea.product_serveice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findAll();

    Optional<Product> findById(UUID id);

    Optional<Product> findBySlug(String slug);

    Product save(Product product);
}
