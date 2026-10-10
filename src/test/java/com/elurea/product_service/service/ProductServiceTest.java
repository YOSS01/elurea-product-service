package com.elurea.product_service.service;

import com.elurea.product_service.dto.product.CreateProductRequest;
import com.elurea.product_service.dto.product.ProductResponse;
import com.elurea.product_service.entity.Category;
import com.elurea.product_service.entity.Product;
import com.elurea.product_service.exception.ConflictException;
import com.elurea.product_service.repository.ProductGalleryRepository;
import com.elurea.product_service.repository.ProductRepository;
import com.elurea.product_service.repository.ProductVariantRepository;
import com.elurea.product_service.storage.ImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private final UUID categoryId = UUID.randomUUID();
    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        CategoryService categoryService = mock(CategoryService.class);
        productService = new ProductService(productRepository, mock(ProductVariantRepository.class),
                mock(ProductGalleryRepository.class), categoryService, mock(ImageStorage.class));

        Category category = new Category();
        category.setName("Dresses");
        when(categoryService.findCategory(categoryId)).thenReturn(category);
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createGeneratesSlugFromTitleWhenMissing() {
        ProductResponse response = productService.create(request("  Robe d'été Élégante ", null));

        assertThat(response.slug()).isEqualTo("robe-d-ete-elegante");
        assertThat(response.title()).isEqualTo("Robe d'été Élégante");
        assertThat(response.category().name()).isEqualTo("Dresses");
    }

    @Test
    void createAppendsSuffixWhenGeneratedSlugIsTaken() {
        when(productRepository.countBySlugIncludingDeleted("linen-dress")).thenReturn(1L);
        when(productRepository.countBySlugIncludingDeleted("linen-dress-2")).thenReturn(1L);

        ProductResponse response = productService.create(request("Linen Dress", null));

        assertThat(response.slug()).isEqualTo("linen-dress-3");
    }

    @Test
    void createFallsBackToGenericSlugForNonLatinTitle() {
        ProductResponse response = productService.create(request("فستان", null));

        assertThat(response.slug()).isEqualTo("product");
    }

    @Test
    void createRejectsExplicitSlugThatIsTaken() {
        when(productRepository.countBySlugIncludingDeleted("linen-dress")).thenReturn(1L);

        assertThatThrownBy(() -> productService.create(request("Linen Dress", "linen-dress")))
                .isInstanceOf(ConflictException.class);
        verify(productRepository, never()).saveAndFlush(any());
    }

    private CreateProductRequest request(String title, String slug) {
        return new CreateProductRequest(categoryId, title, "A description", null, slug);
    }
}
