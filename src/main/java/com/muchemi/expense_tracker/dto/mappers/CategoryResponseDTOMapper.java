package com.muchemi.expense_tracker.dto.mappers;

import com.muchemi.expense_tracker.entities.Category;
import com.muchemi.expense_tracker.dto.category.CategoryResponseDTO;

public class CategoryResponseDTOMapper {
    public static CategoryResponseDTO toDTO(Category category) {
        return new CategoryResponseDTO(
            category.getId(),
            category.getName()
        );
    }
}
