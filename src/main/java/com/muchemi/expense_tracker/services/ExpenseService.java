package com.muchemi.expense_tracker.services;

import com.muchemi.expense_tracker.dto.expense.ExpenseRequestDTO;
import com.muchemi.expense_tracker.dto.expense.ExpenseResponseDTO;
import com.muchemi.expense_tracker.entities.Category;
import com.muchemi.expense_tracker.entities.Expense;
import com.muchemi.expense_tracker.exceptions.ResourceNotFoundException;
import com.muchemi.expense_tracker.repositories.CategoryRepository;
import com.muchemi.expense_tracker.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: Refactor to reduce code duplication
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

        if (isDescriptionTaken(dto.description())) {
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

    public static List<ExpenseResponseDTO> getAllExpenses() {
        return expenseRepository.findAll()
            .stream()
            .map(expense -> new ExpenseResponseDTO(expense.getId(), expense.getDescription(), expense.getAmount(), expense.getCategory().getId()))
            .toList();
    }

    public ExpenseResponseDTO getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        return new ExpenseResponseDTO(expense.getId(), expense.getDescription(), expense.getAmount(), expense.getCategory().getId());
    }

    public Long removeExpenseById(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
        return id;
    }

    public ExpenseResponseDTO updateExpense(Long id, ExpenseRequestDTO dto) {
        if (dto.description() == null || dto.description().trim().isEmpty()) {
            throw new IllegalArgumentException("Expense description cannot be empty or null.");
        }

        if (dto.description().length() > 255) {
            throw new IllegalArgumentException("Expense description cannot exceed 255 characters.");
        }

        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        Category category = categoryRepository.findById(dto.categoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.categoryId()));

        expense.setDescription(dto.description());
        expense.setAmount(dto.amount());
        expense.setCategory(category);

        try {
            Expense updatedExpense = expenseRepository.save(expense);

            return new ExpenseResponseDTO(updatedExpense.getId(), updatedExpense.getDescription(), updatedExpense.getAmount(), updatedExpense.getCategory().getId());
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred while updating the expense: " + exception.getMessage());
        }
    }
}
