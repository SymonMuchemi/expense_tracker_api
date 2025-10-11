package com.muchemi.expense_tracker.services;

import com.muchemi.expense_tracker.dto.CategoryRequestDTO;
import com.muchemi.expense_tracker.dto.CategoryResponseDTO;
import com.muchemi.expense_tracker.entities.Category;
import com.muchemi.expense_tracker.exceptions.ResourceConflictException;
import com.muchemi.expense_tracker.exceptions.ResourceNotFoundException;
import com.muchemi.expense_tracker.repositories.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private static CategoryRepository categoryRepository = null;

    public CategoryService(CategoryRepository categoryRepository) {
        CategoryService.categoryRepository = categoryRepository;
    }

    public CategoryResponseDTO createNewCategory(CategoryRequestDTO dto) {
        Category category = new Category();
        category.setName(dto.name());
        try {
            Category savedCategory = categoryRepository.save(category);

            return new CategoryResponseDTO(savedCategory.getId(), savedCategory.getName());
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceConflictException("Category name '" + dto.name() + "' already exists.");
        }
    }

    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
            .stream()
            .map(category -> new CategoryResponseDTO(category.getId(), category.getName()))
            .toList();
    }

    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return new CategoryResponseDTO(category.getId(), category.getName());
    }

    public Long removeCategoryById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
        return id;
    }

    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setName(dto.name());

        try{
            Category updatedCategory = categoryRepository.save(category);

            return new CategoryResponseDTO(updatedCategory.getId(), updatedCategory.getName());
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException("Category name '" + dto.name() + "' already exists or is invalid.");
        }
    }
}
