package com.muchemi.expense_tracker.Repositories;

import com.muchemi.expense_tracker.Entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
