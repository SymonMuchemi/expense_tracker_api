package com.muchemi.expense_tracker.services;

import com.muchemi.expense_tracker.dto.CategoryRequestDTO;
import com.muchemi.expense_tracker.dto.CategoryResponseDTO;
import com.muchemi.expense_tracker.exceptions.ResourceNotFoundException;
import com.muchemi.expense_tracker.repositories.CategoryRepository;
import com.muchemi.expense_tracker.entities.Category;
import com.muchemi.expense_tracker.exceptions.ResourceConflictException;
import org.springframework.dao.DataIntegrityViolationException;
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
    @DisplayName("createNewCategory() should throw ResourceConflictException when category name exceeds 100 characters")
    void testCreateNewCategory_NameTooLong() {
        // Arrange
        String longName = "A".repeat(101); // 101 characters
        CategoryRequestDTO dto = new CategoryRequestDTO(longName);

        when(categoryRepository.save(any(Category.class)))
            .thenThrow(new DataIntegrityViolationException("Category name cannot exceed 100 characters"));

        ResourceConflictException exception = assertThrows(ResourceConflictException.class, () -> {
            categoryService.createNewCategory(dto);
        });

        String expectedMessagePart = "Category name cannot exceed 100 characters";
        assertTrue(exception.getMessage().contains(expectedMessagePart));
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

    @Test
    @DisplayName("updateCategory() should throw ResourceConflictException when updating to an existing name")
    void testUpdateCategory_DuplicateName() {
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Food");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class)))
            .thenThrow(new DataIntegrityViolationException("Duplicate name"));

        CategoryRequestDTO dto = new CategoryRequestDTO("Transport");
        assertThrows(ResourceConflictException.class, () -> categoryService.updateCategory(1L, dto));
    }

    @Test
    @DisplayName("updateCategory() should throw ResourceConflictException when name is empty or null")
    void testUpdateCategory_EmptyOrNullName() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Food");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryRequestDTO emptyDto = new CategoryRequestDTO("");
        Exception emptyException = assertThrows(ResourceConflictException.class, () -> categoryService.updateCategory(1L, emptyDto));
        assertTrue(emptyException.getMessage().contains("Category name cannot be empty or null."));

        CategoryRequestDTO nullDto = new CategoryRequestDTO(null);
        Exception nullException = assertThrows(ResourceConflictException.class, () -> categoryService.updateCategory(1L, nullDto));
        assertTrue(nullException.getMessage().contains("Category name cannot be empty or null."));
    }

    @Test
    @DisplayName("removeCategoryById() should throw ResourceNotFoundException when category does not exist")
    void testRemoveCategoryById_NotFound() {
        when(categoryRepository.existsById(999L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> categoryService.removeCategoryById(999L));
    }

    @Test
    @DisplayName("getCategoryById() should throw ResourceNotFoundException for negative or zero ID")
    void testGetCategoryById_NegativeOrZeroId() {
        when(categoryRepository.findById(-1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(-1L));

        when(categoryRepository.findById(0L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(0L));
    }

    @Test
    @DisplayName("create/updateCategory() should allow special characters in name")
    void testCategoryNameWithSpecialCharacters() {
        CategoryRequestDTO dto = new CategoryRequestDTO("@Food#2024!");
        Category savedCategory = new Category();
        savedCategory.setId(2L);
        savedCategory.setName("@Food#2024!");

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponseDTO response = categoryService.createNewCategory(dto);
        assertEquals("@Food#2024!", response.name());

        Category category = new Category();
        category.setId(2L);
        category.setName("OldName");
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponseDTO updateResponse = categoryService.updateCategory(2L, dto);
        assertEquals("@Food#2024!", updateResponse.name());
    }

    @Test
    @DisplayName("Service should handle repository exceptions (e.g., database unavailable)")
    void testRepositoryExceptionHandling() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Food");
        when(categoryRepository.save(any(Category.class)))
            .thenThrow(new RuntimeException("Database unavailable"));

        Exception exception = assertThrows(RuntimeException.class, () -> categoryService.createNewCategory(dto));
        assertTrue(exception.getMessage().contains("Database unavailable"));
    }

    @Test
    @DisplayName("createNewCategory() should throw ResourceConflictException when category name differs only by case")
    void testCreateNewCategory_DuplicateNameDifferentCase() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Food");

        when(categoryRepository.existsByNameIgnoreCase("Food")).thenReturn(true);

        ResourceConflictException exception = assertThrows(ResourceConflictException.class, () -> {
            categoryService.createNewCategory(dto);
        });

        String expectedMessage = "Category name 'Food' already exists.";
        assertEquals(expectedMessage, exception.getMessage());

    }

}