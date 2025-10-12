package com.muchemi.expense_tracker.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequestDTO(
        String description,
        BigDecimal amount,
        Long categoryId,
        LocalDate date
) {}
