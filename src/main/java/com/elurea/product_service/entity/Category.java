package com.elurea.product_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "category")
@SQLRestriction("deleted_at IS NULL")
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    private Instant deletedAt;
}
