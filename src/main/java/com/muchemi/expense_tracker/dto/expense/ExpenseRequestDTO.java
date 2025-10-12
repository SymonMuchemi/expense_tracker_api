package com.muchemi.expense_tracker.dto.expense;

import java.math.BigDecimal;

public record ExpenseRequestDTO(String description, BigDecimal amount, Long categoryId) {
}
