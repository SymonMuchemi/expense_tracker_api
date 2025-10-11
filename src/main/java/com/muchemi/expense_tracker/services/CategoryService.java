package com.muchemi.expense_tracker.services;

import com.muchemi.expense_tracker.dto.CategoryRequestDTO;
import com.muchemi.expense_tracker.dto.CategoryResponseDTO;
import com.muchemi.expense_tracker.entities.Category;
import com.muchemi.expense_tracker.repositories.CategoryRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private static CategoryRepository categoryRepository = null;

    public CategoryService(CategoryRepository categoryRepository) {
        CategoryService.categoryRepository = categoryRepository;
    }

    public CategoryResponseDTO createNewCategory(CategoryRequestDTO dto) {
        try {
            Category category = new Category();
            category.setName(dto.name());

            Category savedCategory = categoryRepository.save(category);

            return new CategoryResponseDTO(savedCategory.getId(), savedCategory.getName());
        } catch (DataAccessException exception) {
            throw new RuntimeException("Failed to create category: " + exception.getMessage());
        } catch (Exception exception) {
            throw new RuntimeException("Unexpected error while creating category: " + exception.getMessage());
        }
    }
}
