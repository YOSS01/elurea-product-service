package com.elurea.product_service.repository;

import com.elurea.product_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findAllByProduct_IdOrderByPriceAsc(UUID productId);

    /** Variants with their product loaded in the same query. */
    @EntityGraph(attributePaths = "product")
    List<ProductVariant> findAllWithProductByIdIn(Collection<UUID> ids);

    /**
     * Native query so soft-deleted variants are counted too: the unique index on sku still applies to them.
     * MySQL's default collation makes the comparison case-insensitive.
     */
    @Query(value = "SELECT COUNT(*) FROM product_variant WHERE sku = :sku", nativeQuery = true)
    long countBySkuIncludingDeleted(@Param("sku") String sku);

    /** Single UPDATE statement instead of loading every variant. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ProductVariant v SET v.deletedAt = :deletedAt WHERE v.product.id = :productId")
    int softDeleteAllByProductId(@Param("productId") UUID productId, @Param("deletedAt") Instant deletedAt);

    /**
     * Atomic check-and-decrement: the stock can never go negative, even under concurrent orders,
     * because the condition and the update run as one statement. Returns 0 when stock is insufficient.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE ProductVariant v
            SET v.stock = v.stock - :quantity, v.version = v.version + 1, v.updatedAt = :now
            WHERE v.id = :id AND v.deletedAt IS NULL AND v.stock >= :quantity
            """)
    int decrementStock(@Param("id") UUID id, @Param("quantity") int quantity, @Param("now") Instant now);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE ProductVariant v
            SET v.stock = v.stock + :quantity, v.version = v.version + 1, v.updatedAt = :now
            WHERE v.id = :id AND v.deletedAt IS NULL
            """)
    int incrementStock(@Param("id") UUID id, @Param("quantity") int quantity, @Param("now") Instant now);
}
