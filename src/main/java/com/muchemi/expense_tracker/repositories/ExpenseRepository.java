package com.muchemi.expense_tracker.repositories;

import com.muchemi.expense_tracker.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    boolean existsByDescriptionIgnoreCase(String normalizedDescription);
}
