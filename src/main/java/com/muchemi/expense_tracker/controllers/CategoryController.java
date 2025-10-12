package com.muchemi.expense_tracker.controllers;

import com.muchemi.expense_tracker.dto.category.CategoryRequestDTO;
import com.muchemi.expense_tracker.dto.category.CategoryResponseDTO;
import com.muchemi.expense_tracker.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO request) {
        CategoryResponseDTO response = categoryService.createNewCategory(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categoryResponseDTOList = categoryService.getAllCategories();

        return ResponseEntity.ok(categoryResponseDTOList);
    }

    @GetMapping("{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        CategoryResponseDTO response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO updatedCategory = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Long> deleteCategoryById(@PathVariable Long id) {
        Long deletedId = categoryService.removeCategoryById(id);
        return ResponseEntity.ok(deletedId);
    }
}
