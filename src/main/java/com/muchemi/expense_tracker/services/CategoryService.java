package com.muchemi.expense_tracker.services;

import com.muchemi.expense_tracker.dto.category.CategoryRequestDTO;
import com.muchemi.expense_tracker.dto.category.CategoryResponseDTO;
import com.muchemi.expense_tracker.dto.mappers.CategoryResponseDTOMapper;
import com.muchemi.expense_tracker.entities.Category;
import com.muchemi.expense_tracker.exceptions.ResourceConflictException;
import com.muchemi.expense_tracker.exceptions.ResourceNotFoundException;
import com.muchemi.expense_tracker.repositories.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: Refactor to reduce code duplication
@Service
public class CategoryService {
    private static CategoryRepository categoryRepository = null;

    public CategoryService(CategoryRepository categoryRepository) {
        CategoryService.categoryRepository = categoryRepository;
    }

    public CategoryResponseDTO createNewCategory(CategoryRequestDTO dto) {
        if (dto.name() == null || dto.name().trim().isEmpty() || dto.name().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty or null.");
        }

        if (dto.name().length() > 100) {
            throw new ResourceConflictException("Category name cannot exceed 100 characters.");
        }

        String normalizedName = dto.name().trim();
        boolean exists = categoryRepository.existsByNameIgnoreCase(normalizedName);

        if (exists) {
            throw new ResourceConflictException("Category name '" + dto.name() + "' already exists.");
        }

        Category category = new Category();
        category.setName(dto.name());
        try {
            Category savedCategory = categoryRepository.save(category);

            return CategoryResponseDTOMapper.toDTO(savedCategory);
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceConflictException("Category name '" + dto.name() + "' already exists.");
        }
    }

    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
            .stream()
            .map(CategoryResponseDTOMapper::toDTO)
            .toList();
    }

    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return CategoryResponseDTOMapper.toDTO(category);
    }

    public Long removeCategoryById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
        return id;
    }

    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
         if (dto.name() == null || dto.name().trim().isEmpty()) {
            throw new ResourceConflictException("Category name cannot be empty or null.");
        }

        if (dto.name().length() > 100) {
            throw new ResourceConflictException("Category name cannot exceed 100 characters.");
        }

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setName(dto.name());

        try{
            Category updatedCategory = categoryRepository.save(category);

            return CategoryResponseDTOMapper.toDTO(updatedCategory);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceConflictException("Category name '" + dto.name() + "' already exists or is invalid.");
        }
    }
}
