package com.muchemi.expense_tracker.Repositories;

import com.muchemi.expense_tracker.Entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
