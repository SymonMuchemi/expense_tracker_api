package com.muchemi.expense_tracker.dto.mappers;

import com.muchemi.expense_tracker.dto.expense.ExpenseResponseDTO;
import com.muchemi.expense_tracker.entities.Expense;

public class ExpenseResponseDTOMapper {
    public static ExpenseResponseDTO toDTO(Expense expense) {
        return new ExpenseResponseDTO(
            expense.getId(),
            expense.getDescription(),
            expense.getAmount(),
            expense.getDate(),
            expense.getCategory().getId()
        );
    }
}
