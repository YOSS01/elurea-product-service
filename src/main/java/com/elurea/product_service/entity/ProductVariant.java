package com.elurea.product_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Soft-deletable, because order items keep referencing a variant after it is withdrawn from sale.
 */
@Getter
@Setter
@Entity
@Table(name = "product_variant")
@SQLRestriction("deleted_at IS NULL")
public class ProductVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(nullable = false, length = 20)
    private String size;

    /** Exact decimal: never use float/double for money. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    /** Optimistic locking: concurrent stock updates fail instead of silently overwriting each other. */
    @Version
    private long version;

    private Instant deletedAt;
}
