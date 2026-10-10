package com.elurea.product_service.repository;

import com.elurea.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Native query so soft-deleted categories are counted too: the unique index on name still applies to them.
     * MySQL's default collation makes the comparison case-insensitive.
     */
    @Query(value = "SELECT COUNT(*) FROM category WHERE name = :name", nativeQuery = true)
    long countByNameIncludingDeleted(@Param("name") String name);
}
