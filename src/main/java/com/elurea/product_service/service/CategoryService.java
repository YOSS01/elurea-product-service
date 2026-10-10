package com.elurea.product_service.service;

import com.elurea.product_service.common.PageResponse;
import com.elurea.product_service.common.Strings;
import com.elurea.product_service.dto.category.CategoryRequest;
import com.elurea.product_service.dto.category.CategoryResponse;
import com.elurea.product_service.entity.Category;
import com.elurea.product_service.exception.ConflictException;
import com.elurea.product_service.exception.ResourceNotFoundException;
import com.elurea.product_service.repository.CategoryRepository;
import com.elurea.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public PageResponse<CategoryResponse> getAll(Pageable pageable) {
        return PageResponse.of(categoryRepository.findAll(pageable), CategoryResponse::from);
    }

    public CategoryResponse getById(UUID id) {
        return CategoryResponse.from(findCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = Strings.trimToNull(request.name());
        ensureNameAvailable(name);

        Category category = new Category();
        category.setName(name);
        // Flush so the response carries the database-generated timestamps.
        return CategoryResponse.from(categoryRepository.saveAndFlush(category));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = findCategory(id);
        String name = Strings.trimToNull(request.name());
        if (!name.equalsIgnoreCase(category.getName())) {
            ensureNameAvailable(name);
        }
        category.setName(name);

        return CategoryResponse.from(categoryRepository.saveAndFlush(category));
    }

    /** Soft delete. Refused while products still reference the category, so no product is left orphaned. */
    @Transactional
    public void delete(UUID id) {
        Category category = findCategory(id);
        if (productRepository.existsByCategory_Id(id)) {
            throw new ConflictException("Category still has products. Move or delete them first");
        }
        category.setDeletedAt(Instant.now());
    }

    /** Loads an active (not soft-deleted) category or fails with 404. */
    public Category findCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private void ensureNameAvailable(String name) {
        if (categoryRepository.countByNameIncludingDeleted(name) > 0) {
            throw new ConflictException("A category named '%s' already exists".formatted(name));
        }
    }
}
