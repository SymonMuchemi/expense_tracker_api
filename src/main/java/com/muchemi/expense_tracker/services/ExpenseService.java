package com.muchemi.expense_tracker.services;

import com.muchemi.expense_tracker.dto.expense.ExpenseRequestDTO;
import com.muchemi.expense_tracker.dto.expense.ExpenseResponseDTO;
import com.muchemi.expense_tracker.dto.mappers.ExpenseResponseDTOMapper;
import com.muchemi.expense_tracker.entities.Category;
import com.muchemi.expense_tracker.entities.Expense;
import com.muchemi.expense_tracker.exceptions.ResourceNotFoundException;
import com.muchemi.expense_tracker.repositories.CategoryRepository;
import com.muchemi.expense_tracker.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public boolean isDescriptionTaken(String description) {
        String normalizedDescription = description.trim().toLowerCase();
        return expenseRepository.existsByDescriptionIgnoreCase(normalizedDescription);
    }

    public ExpenseResponseDTO createNewExpense(ExpenseRequestDTO dto) {
        Expense preparedExpense = prepareExpense(new Expense(), dto);

        try {
            Expense savedExpense = expenseRepository.save(preparedExpense);
            return ExpenseResponseDTOMapper.toDTO(savedExpense);
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred while saving the expense: " + exception.getMessage());
        }
    }

    public List<ExpenseResponseDTO> getAllExpenses() {
        return expenseRepository.findAll()
            .stream()
            .map(ExpenseResponseDTOMapper::toDTO)
            .toList();
    }

    public ExpenseResponseDTO getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return ExpenseResponseDTOMapper.toDTO(expense);
    }

    public Long removeExpenseById(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
        return id;
    }

    public ExpenseResponseDTO updateExpense(Long id, ExpenseRequestDTO dto) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        Expense preparedExpense = prepareExpense(expense, dto);

        try {
            Expense updatedExpense = expenseRepository.save(preparedExpense);
            return ExpenseResponseDTOMapper.toDTO(updatedExpense);
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred while updating the expense: " + exception.getMessage());
        }
    }

    private Expense prepareExpense(Expense expense, ExpenseRequestDTO dto) {
        if (dto.description() == null || dto.description().trim().isEmpty()) {
            throw new IllegalArgumentException("Expense description cannot be empty or null.");
        }
        if (dto.description().length() > 255) {
            throw new IllegalArgumentException("Expense description cannot exceed 255 characters.");
        }
        if (isDescriptionTaken(dto.description())) {
            throw new IllegalArgumentException("Expense description '" + dto.description() + "' already exists.");
        }
        LocalDate expenseDate = dto.date() != null ? dto.date() : LocalDate.now();
        expense.setDate(expenseDate);

        Category category = categoryRepository.findById(dto.categoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.categoryId()));

        expense.setDescription(dto.description());
        expense.setAmount(dto.amount());
        expense.setCategory(category);

        return expense;
    }
}