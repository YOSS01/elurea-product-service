package com.elurea.product_serveice.service;

import com.elurea.product_serveice.dto.SaveCategoryRequest;
import com.elurea.product_serveice.entity.Category;
import com.elurea.product_serveice.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Get All Categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Create New Category
    public Category create(SaveCategoryRequest request) {
        if(categoryRepository.findByName(request.name).isPresent()){
            throw new RuntimeException("Category already exists");
        }

        Category category = new Category();
        category.setName(request.name);

        return categoryRepository.save(category);
    }

    // Update Category
    public Category update(SaveCategoryRequest request) {
        Category category = categoryRepository.findById(request.id).orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(request.name);

        return categoryRepository.save(category);
    }

    // Soft Detele Category
    public Category delete(UUID id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));

        category.setDeletedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }
}
