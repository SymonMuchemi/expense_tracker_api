package com.muchemi.expense_tracker.services;

import com.muchemi.expense_tracker.dto.CategoryRequestDTO;
import com.muchemi.expense_tracker.dto.CategoryResponseDTO;
import com.muchemi.expense_tracker.exceptions.ResourceNotFoundException;
import com.muchemi.expense_tracker.repositories.CategoryRepository;
import com.muchemi.expense_tracker.entities.Category;
import com.muchemi.expense_tracker.exceptions.ResourceConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("CreateNewCategory() should save and return CategoryResponseDTO")
    void testCreateNewCategory_Success() {
    CategoryRequestDTO dto = new CategoryRequestDTO("Food");
    Category savedCategory = new Category();
    savedCategory.setId(1L);
    savedCategory.setName("Food");

    when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

    CategoryResponseDTO response = categoryService.createNewCategory(dto);
    }

    @Test
    @DisplayName("CreateNewCategory() should throw ResourceConflictException when category name exists")
    void testCreateNewCategory_DuplicateName() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Food");

        when(categoryRepository.save(any(Category.class)))
            .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate name"));

        Exception exception = assertThrows(ResourceConflictException.class, () -> {
            categoryService.createNewCategory(dto);
        });

        String expectedMessage = "Category name 'Food' already exists.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("createNewCategory() should throw ResourceConflictException when category name is invalid")
    void testCreateNewCategory_InvalidName() {
        CategoryRequestDTO dto = new CategoryRequestDTO("");

        when(categoryRepository.save(any(Category.class)))
            .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Invalid name"));

        Exception exception = assertThrows(ResourceConflictException.class, () -> {
            categoryService.createNewCategory(dto);
        });

        String expectedMessage = "Category name cannot be empty or null.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("getAllCategories() should return list of CategoryResponseDTOs")
    void testGetAllCategories_Success() {
        Category c1 = new Category(); c1.setId(1L); c1.setName("Food");
        Category c2 = new Category(); c2.setId(2L); c2.setName("Transport");

        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        assertEquals(2, result.size());
        assertEquals("Transport", result.get(1).name());
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("getAllCategories() should return empty list when no categories exist")
    void testGetAllCategories_Empty() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getCategoryById() should return CategoryResponseDTO when found")
    void testGetCategoryById_Found() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Groceries");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponseDTO response = categoryService.getCategoryById(1L);

        assertEquals("Groceries", response.name());
        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("getCategoryById() should throw ResourceNotFoundException when not found")
    void testGetCategoryById_NotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    @DisplayName("removeCategoryById() should delete and return id when exists")
    void testRemoveCategoryById_Success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        Long deletedId = categoryService.removeCategoryById(1L);

        assertEquals(1L, deletedId);
        verify(categoryRepository).deleteById(1L);
    }
}