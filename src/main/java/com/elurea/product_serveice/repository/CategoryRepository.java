package com.elurea.product_serveice.repository;

import com.elurea.product_serveice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAll();

    Optional<Category> findByName(String name);

    Category save(Category category);
}
