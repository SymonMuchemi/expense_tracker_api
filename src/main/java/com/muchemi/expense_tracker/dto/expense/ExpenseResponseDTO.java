package com.muchemi.expense_tracker.dto.expense;

import java.math.BigDecimal;

public record ExpenseResponseDTO(Long id, String description, BigDecimal amount, Long categoryId) {
}
