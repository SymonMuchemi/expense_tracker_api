package com.muchemi.expense_tracker.controllers;

import com.muchemi.expense_tracker.dto.CategoryRequestDTO;
import com.muchemi.expense_tracker.dto.CategoryResponseDTO;
import com.muchemi.expense_tracker.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryRequestDTO request) {
        CategoryResponseDTO response = categoryService.createNewCategory(request);

        return ResponseEntity.ok(response);
    }
}
