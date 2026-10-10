package com.elurea.product_service.repository;

import com.elurea.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Paged product search. Both filters are optional. The category is fetched in the same query to avoid N+1 selects.
     * The search term must already be escaped for LIKE and lower-cased.
     */
    @EntityGraph(attributePaths = "category")
    @Query(value = """
            SELECT p FROM Product p
            WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:search IS NULL OR LOWER(p.title) LIKE CONCAT('%', :search, '%') ESCAPE '\\')
            """,
            countQuery = """
            SELECT COUNT(p) FROM Product p
            WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:search IS NULL OR LOWER(p.title) LIKE CONCAT('%', :search, '%') ESCAPE '\\')
            """)
    Page<Product> search(@Param("categoryId") UUID categoryId, @Param("search") String search, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryById(UUID id);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryBySlug(String slug);

    boolean existsByCategory_Id(UUID categoryId);

    /** Native query so soft-deleted products are counted too: the unique index on slug still applies to them. */
    @Query(value = "SELECT COUNT(*) FROM product WHERE slug = :slug", nativeQuery = true)
    long countBySlugIncludingDeleted(@Param("slug") String slug);
}
