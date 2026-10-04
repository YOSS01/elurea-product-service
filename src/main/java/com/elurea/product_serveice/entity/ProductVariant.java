package com.elurea.product_serveice.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "product_variant")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private float price = 0;

    @Column(nullable = false)
    private int stock = 0;

    @Column(nullable = false, unique = true)
    private String sku;
}
