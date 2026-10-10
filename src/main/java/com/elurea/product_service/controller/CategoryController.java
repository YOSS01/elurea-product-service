package com.elurea.product_service.controller;

import com.elurea.product_service.common.PageResponse;
import com.elurea.product_service.common.SortWhitelist;
import com.elurea.product_service.dto.category.CategoryRequest;
import com.elurea.product_service.dto.category.CategoryResponse;
import com.elurea.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private static final Set<String> SORTABLE = Set.of("name", "createdAt", "updatedAt");

    private final CategoryService categoryService;

    @GetMapping
    public PageResponse<CategoryResponse> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return categoryService.getAll(SortWhitelist.validate(pageable, SORTABLE));
    }

    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable UUID id) {
        return categoryService.getById(id);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.create(request);
        return ResponseEntity.created(Locations.of(created.id())).body(created);
    }

    /** Full replacement: a category only has a name, so PUT is the natural verb here. */
    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
