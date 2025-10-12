package com.muchemi.expense_tracker.services;

import com.muchemi.expense_tracker.dto.expense.ExpenseRequestDTO;
import com.muchemi.expense_tracker.dto.expense.ExpenseResponseDTO;
import com.muchemi.expense_tracker.entities.Category;
import com.muchemi.expense_tracker.entities.Expense;
import com.muchemi.expense_tracker.repositories.CategoryRepository;
import com.muchemi.expense_tracker.repositories.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private Category mockCategory;
    private final Long categoryId = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockCategory = new Category();
        mockCategory.setId(categoryId);
        mockCategory.setName("Food");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));
    }

    @Test
    @DisplayName("createNewExpense should save and return ExpenseResponseDTO")
    void testCreateNewExpense_Success() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO("Lunch", new BigDecimal("15.00"), categoryId, null);

        when(expenseRepository.existsByDescriptionIgnoreCase("lunch")).thenReturn(false);
        when(expenseRepository.save(any())).thenAnswer(invocation -> {
            Expense savedExpense = invocation.getArgument(0);
            savedExpense.setId(1L);
            return savedExpense;
        });

        var response = expenseService.createNewExpense(dto);

        assertNotNull(response);
        assertEquals("Lunch", response.description());
        assertEquals(new BigDecimal("15.00"), response.amount());
        assertEquals(categoryId, response.categoryId());
        verify(expenseRepository).save(any());
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("getAllExpenses should return list of ExpenseResponseDTO")
    void testGetAllExpenses() {
        Expense expense1 = new Expense(1L, "Lunch", new BigDecimal("15.00"), LocalDate.now(), mockCategory, null, null);
        Expense expense2 = new Expense(2L, "Dinner", new BigDecimal("25.00"), LocalDate.now(), mockCategory, null, null);

        List<ExpenseResponseDTO> expenses = List.of(
                new ExpenseResponseDTO(1L, "Lunch", new BigDecimal("15.00"), LocalDate.now(), categoryId),
                new ExpenseResponseDTO(2L, "Dinner", new BigDecimal("25.00"), LocalDate.now(), categoryId)
        );

        when(expenseRepository.findAll()).thenReturn(List.of(expense1, expense2));

        var response = expenseService.getAllExpenses();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(expenses, response);
        verify(expenseRepository).findAll();
    }

    @Test
    @DisplayName("getExpenseById should return ExpenseResponseDTO when found")
    void testGetExpenseById_Found() {
        Long expenseId = 1L;
        Expense expense = new Expense(expenseId, "Lunch", new BigDecimal("15.00"), LocalDate.now(), mockCategory, null, null);

        ExpenseResponseDTO expectedResponse = new ExpenseResponseDTO(expenseId, "Lunch", new BigDecimal("15.00"), LocalDate.now(), categoryId);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        var response = expenseService.getExpenseById(expenseId);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(expenseRepository).findById(expenseId);
    }

    @Test
    @DisplayName("createNewExpense should throw IllegalArgumentException when description is null")
    void testCreateNewExpense_DescriptionNull() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(null, new BigDecimal("10.00"), categoryId, null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> expenseService.createNewExpense(dto));
        assertEquals("Expense description cannot be empty or null.", exception.getMessage());
    }

    @Test
    @DisplayName("createNewExpense should throw IllegalArgumentException when description is empty or spaces")
    void testCreateNewExpense_DescriptionEmptyOrSpaces() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO("   ", new BigDecimal("10.00"), categoryId, null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> expenseService.createNewExpense(dto));
        assertEquals("Expense description cannot be empty or null.", exception.getMessage());
    }

    @Test
    @DisplayName("createNewExpense should throw IllegalArgumentException when description exceeds 255 characters")
    void testCreateNewExpense_DescriptionTooLong() {
        String longDescription = "A".repeat(256);
        ExpenseRequestDTO dto = new ExpenseRequestDTO(longDescription, new BigDecimal("10.00"), categoryId, null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> expenseService.createNewExpense(dto));
        assertEquals("Expense description cannot exceed 255 characters.", exception.getMessage());
    }

    @Test
    @DisplayName("createNewExpense should throw IllegalArgumentException for duplicate description (case-insensitive)")
    void testCreateNewExpense_DuplicateDescription() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO("lunch", new BigDecimal("10.00"), categoryId, null);
        when(expenseRepository.existsByDescriptionIgnoreCase("lunch")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> expenseService.createNewExpense(dto));
        assertEquals("Expense description 'lunch' already exists.", exception.getMessage());
    }

    @Test
    @DisplayName("createNewExpense should throw ResourceNotFoundException when category not found")
    void testCreateNewExpense_CategoryNotFound() {
        Long missingCategoryId = 99L;
        ExpenseRequestDTO dto = new ExpenseRequestDTO("Lunch", new BigDecimal("10.00"), missingCategoryId, null);
        when(categoryRepository.findById(missingCategoryId)).thenReturn(Optional.empty());
        when(expenseRepository.existsByDescriptionIgnoreCase("lunch")).thenReturn(false);

        Exception exception = assertThrows(com.muchemi.expense_tracker.exceptions.ResourceNotFoundException.class, () -> expenseService.createNewExpense(dto));
        assertEquals("Category not found with id: 99", exception.getMessage());
    }

    @Test
    @DisplayName("createNewExpense should wrap and rethrow repository save exception")
    void testCreateNewExpense_RepositorySaveThrows() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO("Lunch", new BigDecimal("10.00"), categoryId, null);
        when(expenseRepository.existsByDescriptionIgnoreCase("lunch")).thenReturn(false);
        when(expenseRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        Exception exception = assertThrows(RuntimeException.class, () -> expenseService.createNewExpense(dto));
        assertEquals("An error occurred while saving the expense: DB error", exception.getMessage());
    }

    @Test
    @DisplayName("isDescriptionTaken should return true if description exists")
    void testIsDescriptionTaken_True() {
        String description = "Lunch";
        when(expenseRepository.existsByDescriptionIgnoreCase("lunch")).thenReturn(true);

        boolean result = expenseService.isDescriptionTaken(description);

        assertTrue(result);
        verify(expenseRepository).existsByDescriptionIgnoreCase("lunch");
    }

    @Test
    @DisplayName("getExpenseById should throw ResourceNotFoundException when not found")
    void testGetExpenseById_NotFound() {
        Long expenseId = 99L;
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(com.muchemi.expense_tracker.exceptions.ResourceNotFoundException.class,
                () -> expenseService.getExpenseById(expenseId));
        assertEquals("Expense not found with id: 99", exception.getMessage());
    }

    @Test
    @DisplayName("getAllExpenses should return empty list when no expenses exist")
    void testGetAllExpenses_Empty() {
        when(expenseRepository.findAll()).thenReturn(List.of());
        var response = expenseService.getAllExpenses();
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(expenseRepository).findAll();
    }

    @Test
    @DisplayName("getAllExpenses should handle nullable date field")
    void testGetAllExpenses_NullDate() {
        Expense expense = new Expense(1L, "Lunch", new BigDecimal("15.00"), null, mockCategory, null, null);
        when(expenseRepository.findAll()).thenReturn(List.of(expense));
        var response = expenseService.getAllExpenses();
        assertNotNull(response);
        assertEquals(1, response.size());
        assertNull(response.get(0).date());
    }

    @Test
    @DisplayName("removeExpenseById should delete and return id when expense exists")
    void testRemoveExpenseById_Exists() {
        Long expenseId = 1L;
        when(expenseRepository.existsById(expenseId)).thenReturn(true);
        Long result = expenseService.removeExpenseById(expenseId);
        assertEquals(expenseId, result);
        verify(expenseRepository).deleteById(expenseId);
    }

    @Test
    @DisplayName("removeExpenseById should throw ResourceNotFoundException when expense not found")
    void testRemoveExpenseById_NotFound() {
        Long expenseId = 99L;
        when(expenseRepository.existsById(expenseId)).thenReturn(false);
        Exception exception = assertThrows(com.muchemi.expense_tracker.exceptions.ResourceNotFoundException.class,
                () -> expenseService.removeExpenseById(expenseId));
        assertEquals("Expense not found with id: 99", exception.getMessage());
    }

    @Test
    @DisplayName("updateExpense should update and return updated DTO when expense exists and input is valid")
    void testUpdateExpense_Success() {
        Long expenseId = 1L;
        Expense existingExpense = new Expense(expenseId, "Lunch", new BigDecimal("15.00"), LocalDate.now(), mockCategory, null, null);
        ExpenseRequestDTO dto = new ExpenseRequestDTO("Dinner", new BigDecimal("20.00"), categoryId, LocalDate.now());
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(existingExpense));
        when(expenseRepository.existsByDescriptionIgnoreCase("dinner")).thenReturn(false);
        when(expenseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var response = expenseService.updateExpense(expenseId, dto);
        assertNotNull(response);
        assertEquals("Dinner", response.description());
        assertEquals(new BigDecimal("20.00"), response.amount());
        assertEquals(categoryId, response.categoryId());
    }

    @Test
    @DisplayName("updateExpense should throw ResourceNotFoundException when expense not found")
    void testUpdateExpense_NotFound() {
        Long expenseId = 99L;
        ExpenseRequestDTO dto = new ExpenseRequestDTO("Lunch", new BigDecimal("10.00"), categoryId, null);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(com.muchemi.expense_tracker.exceptions.ResourceNotFoundException.class,
                () -> expenseService.updateExpense(expenseId, dto));
        assertEquals("Expense not found with id: 99", exception.getMessage());
    }

    @Test
    @DisplayName("updateExpense should throw IllegalArgumentException for null description")
    void testUpdateExpense_DescriptionNull() {
        Long expenseId = 1L;
        Expense existingExpense = new Expense(expenseId, "Lunch", new BigDecimal("15.00"), LocalDate.now(), mockCategory, null, null);
        ExpenseRequestDTO dto = new ExpenseRequestDTO(null, new BigDecimal("10.00"), categoryId, null);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(existingExpense));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> expenseService.updateExpense(expenseId, dto));
        assertEquals("Expense description cannot be empty or null.", exception.getMessage());
    }

    @Test
    @DisplayName("updateExpense should throw IllegalArgumentException for empty description")
    void testUpdateExpense_DescriptionEmpty() {
        Long expenseId = 1L;
        Expense existingExpense = new Expense(expenseId, "Lunch", new BigDecimal("15.00"), LocalDate.now(), mockCategory, null, null);
        ExpenseRequestDTO dto = new ExpenseRequestDTO("   ", new BigDecimal("10.00"), categoryId, null);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(existingExpense));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> expenseService.updateExpense(expenseId, dto));
        assertEquals("Expense description cannot be empty or null.", exception.getMessage());
    }

    @Test
    @DisplayName("updateExpense should throw IllegalArgumentException for description too long")
    void testUpdateExpense_DescriptionTooLong() {
        Long expenseId = 1L;
        Expense existingExpense = new Expense(expenseId, "Lunch", new BigDecimal("15.00"), LocalDate.now(), mockCategory, null, null);
        String longDescription = "A".repeat(256);
        ExpenseRequestDTO dto = new ExpenseRequestDTO(longDescription, new BigDecimal("10.00"), categoryId, null);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(existingExpense));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> expenseService.updateExpense(expenseId, dto));
        assertEquals("Expense description cannot exceed 255 characters.", exception.getMessage());
    }

    @Test
    @DisplayName("updateExpense should throw IllegalArgumentException for duplicate description")
    void testUpdateExpense_DuplicateDescription() {
        Long expenseId = 1L;
        Expense existingExpense = new Expense(expenseId, "Lunch", new BigDecimal("15.00"), LocalDate.now(), mockCategory, null, null);
        ExpenseRequestDTO dto = new ExpenseRequestDTO("Dinner", new BigDecimal("10.00"), categoryId, null);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(existingExpense));
        when(expenseRepository.existsByDescriptionIgnoreCase("dinner")).thenReturn(true);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> expenseService.updateExpense(expenseId, dto));
        assertEquals("Expense description 'Dinner' already exists.", exception.getMessage());
    }

    @Test
    @DisplayName("updateExpense should wrap and rethrow repository save exception")
    void testUpdateExpense_RepositorySaveThrows() {
        Long expenseId = 1L;
        Expense existingExpense = new Expense(expenseId, "Lunch", new BigDecimal("15.00"), LocalDate.now(), mockCategory, null, null);
        ExpenseRequestDTO dto = new ExpenseRequestDTO("Dinner", new BigDecimal("10.00"), categoryId, null);
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(existingExpense));
        when(expenseRepository.existsByDescriptionIgnoreCase("dinner")).thenReturn(false);
        when(expenseRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        Exception exception = assertThrows(RuntimeException.class, () -> expenseService.updateExpense(expenseId, dto));
        assertEquals("An error occurred while updating the expense: DB error", exception.getMessage());
    }

    @Test
    @DisplayName("isDescriptionTaken should trim and normalize description")
    void testIsDescriptionTaken_TrimAndNormalize() {
        when(expenseRepository.existsByDescriptionIgnoreCase("lunch")).thenReturn(true);
        boolean result = expenseService.isDescriptionTaken(" LUNCH ");
        assertTrue(result);
        verify(expenseRepository).existsByDescriptionIgnoreCase("lunch");
    }

    @Test
    @DisplayName("isDescriptionTaken should return false when repository returns false")
    void testIsDescriptionTaken_False() {
        when(expenseRepository.existsByDescriptionIgnoreCase("dinner")).thenReturn(false);
        boolean result = expenseService.isDescriptionTaken("Dinner");
        assertFalse(result);
        verify(expenseRepository).existsByDescriptionIgnoreCase("dinner");
    }
}