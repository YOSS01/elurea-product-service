package com.elurea.product_serveice.dto;

import java.util.UUID;

public class SaveProductVariantRequest {
    public UUID id;
    public String productId;
    public String sku;
    public String size;
    public float price;
    public int stock;
}
