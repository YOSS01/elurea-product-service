package com.elurea.product_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

/**
 * Soft-deletable: rows with a deleted_at value are invisible to every JPA query.
 */
@Getter
@Setter
@Entity
@Table(name = "product", indexes = @Index(name = "idx_product_title", columnList = "title"))
@SQLRestriction("deleted_at IS NULL")
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    @Column(length = 512)
    private String thumbnail;

    @Column(nullable = false, unique = true)
    private String slug;

    private Instant deletedAt;
}
