package com.muchemi.expense_tracker.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponseDTO(Long id, String description, BigDecimal amount, LocalDate date, Long categoryId) {
}
