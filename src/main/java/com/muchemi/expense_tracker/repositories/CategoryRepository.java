package com.muchemi.expense_tracker.repositories;

import com.muchemi.expense_tracker.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String normalizedName);
}
