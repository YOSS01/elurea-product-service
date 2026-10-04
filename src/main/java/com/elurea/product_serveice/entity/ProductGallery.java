package com.elurea.product_serveice.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "product_gallery")
public class ProductGallery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String productId;

    private String url;
}
