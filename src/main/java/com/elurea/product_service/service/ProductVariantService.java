package com.elurea.product_service.service;

import com.elurea.product_service.common.Strings;
import com.elurea.product_service.dto.variant.CreateVariantRequest;
import com.elurea.product_service.dto.variant.ReservedItemResponse;
import com.elurea.product_service.dto.variant.StockRequest;
import com.elurea.product_service.dto.variant.UpdateVariantRequest;
import com.elurea.product_service.dto.variant.VariantResponse;
import com.elurea.product_service.entity.ProductVariant;
import com.elurea.product_service.exception.ConflictException;
import com.elurea.product_service.exception.ResourceNotFoundException;
import com.elurea.product_service.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductService productService;

    /** Variants of one product, cheapest first. Fails with 404 if the product does not exist. */
    public List<VariantResponse> getByProduct(UUID productId) {
        productService.findProduct(productId);
        return variantRepository.findAllByProduct_IdOrderByPriceAsc(productId).stream()
                .map(VariantResponse::from)
                .toList();
    }

    public VariantResponse getById(UUID id) {
        return VariantResponse.from(findVariant(id));
    }

    @Transactional
    public VariantResponse create(CreateVariantRequest request) {
        String sku = normalizeSku(request.sku());
        ensureSkuAvailable(sku);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(productService.findProduct(request.productId()));
        variant.setSku(sku);
        variant.setSize(Strings.trimToNull(request.size()));
        variant.setPrice(toMoney(request.price()));
        variant.setStock(request.stock());

        // Flush so the response carries the database-generated timestamps.
        return VariantResponse.from(variantRepository.saveAndFlush(variant));
    }

    @Transactional
    public VariantResponse update(UUID id, UpdateVariantRequest request) {
        ProductVariant variant = findVariant(id);

        if (request.sku() != null) {
            String sku = normalizeSku(request.sku());
            if (!sku.equals(variant.getSku())) {
                ensureSkuAvailable(sku);
                variant.setSku(sku);
            }
        }
        if (request.size() != null) variant.setSize(Strings.trimToNull(request.size()));
        if (request.price() != null) variant.setPrice(toMoney(request.price()));
        if (request.stock() != null) variant.setStock(request.stock());

        return VariantResponse.from(variantRepository.saveAndFlush(variant));
    }

    /** Soft delete: order items may still reference this variant. */
    @Transactional
    public void delete(UUID id) {
        findVariant(id).setDeletedAt(Instant.now());
    }

    /**
     * Takes stock for every item, all-or-nothing: if one line is short, nothing is reserved (409).
     * Returns each reserved line with the product name, image and current price, so the caller can snapshot them.
     */
    @Transactional
    public List<ReservedItemResponse> reserveStock(StockRequest request) {
        Instant now = Instant.now();
        Map<UUID, Integer> quantities = mergeByVariant(request);
        quantities.forEach((variantId, quantity) -> {
            if (variantRepository.decrementStock(variantId, quantity, now) == 0) {
                ProductVariant variant = findVariant(variantId); // 404 if missing or withdrawn
                throw new ConflictException("Insufficient stock for SKU %s: requested %d, available %d"
                        .formatted(variant.getSku(), quantity, variant.getStock()));
            }
        });
        return variantRepository.findAllWithProductByIdIn(quantities.keySet()).stream()
                .map(variant -> ReservedItemResponse.from(variant, quantities.get(variant.getId())))
                .toList();
    }

    /** Puts stock back, e.g. when an order is cancelled. All-or-nothing as well. */
    @Transactional
    public List<VariantResponse> releaseStock(StockRequest request) {
        Instant now = Instant.now();
        Map<UUID, Integer> quantities = mergeByVariant(request);
        quantities.forEach((variantId, quantity) -> {
            if (variantRepository.incrementStock(variantId, quantity, now) == 0) {
                throw new ResourceNotFoundException("Product variant", variantId);
            }
        });
        return reload(quantities);
    }

    /**
     * Sums duplicate lines and orders them by id. A stable lock order prevents deadlocks
     * between two transactions reserving the same variants.
     */
    private static Map<UUID, Integer> mergeByVariant(StockRequest request) {
        Map<UUID, Integer> quantities = new TreeMap<>();
        request.items().forEach(item -> quantities.merge(item.variantId(), item.quantity(), Integer::sum));
        return quantities;
    }

    private List<VariantResponse> reload(Map<UUID, Integer> quantities) {
        return variantRepository.findAllById(quantities.keySet()).stream()
                .map(VariantResponse::from)
                .toList();
    }

    private ProductVariant findVariant(UUID id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant", id));
    }

    private void ensureSkuAvailable(String sku) {
        if (variantRepository.countBySkuIncludingDeleted(sku) > 0) {
            throw new ConflictException("SKU '%s' is already used".formatted(sku));
        }
    }

    /** Always two decimals ("39.5" becomes "39.50"). Validation already guarantees at most two. */
    private static BigDecimal toMoney(BigDecimal price) {
        return price.setScale(2, RoundingMode.UNNECESSARY);
    }

    /** SKUs are stored upper-case so "ab-1" and "AB-1" are the same SKU. */
    private static String normalizeSku(String sku) {
        return sku.strip().toUpperCase(Locale.ROOT);
    }
}
