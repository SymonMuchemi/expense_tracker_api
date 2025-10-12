package com.muchemi.expense_tracker.services;

import com.muchemi.expense_tracker.dto.expense.ExpenseRequestDTO;
import com.muchemi.expense_tracker.dto.expense.ExpenseResponseDTO;
import com.muchemi.expense_tracker.entities.Category;
import com.muchemi.expense_tracker.entities.Expense;
import com.muchemi.expense_tracker.exceptions.ResourceNotFoundException;
import com.muchemi.expense_tracker.repositories.CategoryRepository;
import com.muchemi.expense_tracker.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {
    private static ExpenseRepository expenseRepository = null;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        ExpenseService.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public static boolean isDescriptionTaken(String description) {
        String normalizedDescription = description.trim().toLowerCase();
        return expenseRepository.existsByDescriptionIgnoreCase(normalizedDescription);
    }

   public ExpenseResponseDTO createNewExpense(ExpenseRequestDTO dto) {
        if (dto.description() == null || dto.description().trim().isEmpty()) {
            throw new IllegalArgumentException("Expense description cannot be empty or null.");
        }

        if (dto.description().length() > 255) {
            throw new IllegalArgumentException("Expense description cannot exceed 255 characters.");
        }

        String normalizedDescription = dto.description().trim();

        boolean descriptionExists = expenseRepository.existsByDescriptionIgnoreCase(normalizedDescription);

        if (descriptionExists) {
            throw new IllegalArgumentException("Expense description '" + dto.description() + "' already exists.");
        }

        Category category = categoryRepository.findById(dto.categoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.categoryId()));

        Expense expense = new Expense();

        expense.setDescription(dto.description());
        expense.setAmount(dto.amount());
        expense.setCategory(category);

        try {
            Expense savedExpense = expenseRepository.save(expense);

            return new ExpenseResponseDTO(savedExpense.getId(), savedExpense.getDescription(), savedExpense.getAmount(), savedExpense.getCategory().getId());
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred while saving the expense: " + exception.getMessage());
        }
   }
}
