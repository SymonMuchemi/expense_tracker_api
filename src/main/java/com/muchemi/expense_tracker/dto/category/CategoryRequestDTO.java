package com.muchemi.expense_tracker.dto.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDTO(@NotBlank(message = "Category name cannot be empty") String name) {
}
