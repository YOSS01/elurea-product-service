package com.elurea.product_serveice.controller;

import com.elurea.product_serveice.dto.SaveCategoryRequest;
import com.elurea.product_serveice.entity.Category;
import com.elurea.product_serveice.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<Category> create(@RequestBody SaveCategoryRequest req) {
        return ResponseEntity.ok(categoryService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable UUID id, @RequestBody SaveCategoryRequest req) {
        req.id = id;
        return ResponseEntity.ok(categoryService.update(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Category> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.delete(id));
    }
}
