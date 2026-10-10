package com.elurea.product_service.service;

import com.elurea.product_service.common.PageResponse;
import com.elurea.product_service.common.Strings;
import com.elurea.product_service.dto.gallery.GalleryImageResponse;
import com.elurea.product_service.dto.product.CreateProductRequest;
import com.elurea.product_service.dto.product.ProductDetailResponse;
import com.elurea.product_service.dto.product.ProductResponse;
import com.elurea.product_service.dto.product.UpdateProductRequest;
import com.elurea.product_service.dto.variant.VariantResponse;
import com.elurea.product_service.entity.Product;
import com.elurea.product_service.exception.ConflictException;
import com.elurea.product_service.exception.ResourceNotFoundException;
import com.elurea.product_service.repository.ProductGalleryRepository;
import com.elurea.product_service.repository.ProductRepository;
import com.elurea.product_service.repository.ProductVariantRepository;
import com.elurea.product_service.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    static final String IMAGE_FOLDER = "products";

    /** Leaves room for a "-NNN" suffix within the 255-character column. */
    private static final int MAX_SLUG_BASE_LENGTH = 240;

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductGalleryRepository galleryRepository;
    private final CategoryService categoryService;
    private final ImageStorage imageStorage;

    /** Paged listing, optionally filtered by category and by a case-insensitive search on the title. */
    public PageResponse<ProductResponse> search(UUID categoryId, String search, Pageable pageable) {
        return PageResponse.of(
                productRepository.search(categoryId, toLikePattern(search), pageable),
                ProductResponse::from);
    }

    public ProductDetailResponse getById(UUID id) {
        Product product = productRepository.findWithCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return toDetail(product);
    }

    public ProductDetailResponse getBySlug(String slug) {
        Product product = productRepository.findWithCategoryBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", slug));
        return toDetail(product);
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Product product = new Product();
        product.setCategory(categoryService.findCategory(request.categoryId()));
        product.setTitle(Strings.trimToNull(request.title()));
        product.setDescription(Strings.trimToNull(request.description()));
        product.setThumbnail(Strings.trimToNull(request.thumbnail()));

        if (request.slug() != null) {
            ensureSlugAvailable(request.slug());
            product.setSlug(request.slug());
        } else {
            product.setSlug(generateUniqueSlug(product.getTitle()));
        }

        // Flush so the response carries the database-generated timestamps.
        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = findProduct(id);

        if (request.categoryId() != null) product.setCategory(categoryService.findCategory(request.categoryId()));
        if (request.title() != null) product.setTitle(Strings.trimToNull(request.title()));
        if (request.description() != null) product.setDescription(Strings.trimToNull(request.description()));
        if (request.thumbnail() != null) replaceThumbnail(product, Strings.trimToNull(request.thumbnail()));
        if (request.slug() != null && !request.slug().equals(product.getSlug())) {
            ensureSlugAvailable(request.slug());
            product.setSlug(request.slug());
        }

        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    @Transactional
    public ProductResponse uploadThumbnail(UUID id, MultipartFile file) {
        Product product = findProduct(id);
        replaceThumbnail(product, imageStorage.store(file, IMAGE_FOLDER));
        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    @Transactional
    public ProductResponse removeThumbnail(UUID id) {
        Product product = findProduct(id);
        replaceThumbnail(product, null);
        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    /** Soft delete of the product and its variants. Gallery rows are kept so the product can be restored intact. */
    @Transactional
    public void delete(UUID id) {
        Instant now = Instant.now();
        findProduct(id).setDeletedAt(now);
        variantRepository.softDeleteAllByProductId(id, now);
    }

    /** Loads an active (not soft-deleted) product or fails with 404. */
    public Product findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    /** Sets the new thumbnail and removes the previous uploaded file once the change is committed. */
    private void replaceThumbnail(Product product, String newUrl) {
        if (!Objects.equals(product.getThumbnail(), newUrl)) {
            imageStorage.deleteAfterCommit(product.getThumbnail());
        }
        product.setThumbnail(newUrl);
    }

    private ProductDetailResponse toDetail(Product product) {
        var variants = variantRepository.findAllByProduct_IdOrderByPriceAsc(product.getId()).stream()
                .map(VariantResponse::from)
                .toList();
        var gallery = galleryRepository.findAllByProduct_IdOrderByCreatedAtAsc(product.getId()).stream()
                .map(GalleryImageResponse::from)
                .toList();
        return ProductDetailResponse.from(product, variants, gallery);
    }

    private void ensureSlugAvailable(String slug) {
        if (productRepository.countBySlugIncludingDeleted(slug) > 0) {
            throw new ConflictException("Slug '%s' is already used by another product".formatted(slug));
        }
    }

    /** "Linen Dress" gives "linen-dress", then "linen-dress-2", "linen-dress-3"... if taken. */
    private String generateUniqueSlug(String title) {
        String base = Strings.slugify(title);
        if (base.isEmpty()) {
            base = "product"; // title had no latin letters or digits
        }
        if (base.length() > MAX_SLUG_BASE_LENGTH) {
            base = base.substring(0, MAX_SLUG_BASE_LENGTH).replaceAll("-+$", "");
        }

        String candidate = base;
        for (int suffix = 2; productRepository.countBySlugIncludingDeleted(candidate) > 0; suffix++) {
            candidate = base + "-" + suffix;
        }
        return candidate;
    }

    /** Lower-cases the term and escapes LIKE wildcards so "50%" matches literally. */
    private static String toLikePattern(String search) {
        String term = Strings.trimToNull(search);
        if (term == null) {
            return null;
        }
        return term.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
