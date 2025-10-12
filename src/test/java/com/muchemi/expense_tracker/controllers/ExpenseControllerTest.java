package com.muchemi.expense_tracker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muchemi.expense_tracker.dto.category.CategoryResponseDTO;
import com.muchemi.expense_tracker.dto.expense.ExpenseRequestDTO;
import com.muchemi.expense_tracker.dto.expense.ExpenseResponseDTO;
import com.muchemi.expense_tracker.services.CategoryService;
import com.muchemi.expense_tracker.services.ExpenseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private CategoryService categoryService;

    private static final String API_BASE_URL = "/api/expenses";

    private ExpenseRequestDTO getSampleRequestDTO() {
        return new ExpenseRequestDTO(
                "Lunch at Local Cafe",
                BigDecimal.valueOf(15.50),
                1L,
                LocalDate.now()
        );
    }

    private ExpenseResponseDTO getSampleResponseDTO(Long id) {
        return new ExpenseResponseDTO(
                id,
                "Lunch at Local Cafe",
                BigDecimal.valueOf(15.50),
                LocalDate.now(),
                new CategoryResponseDTO(1L, "Food").id()
        );
    }

    @Test
    @DisplayName("POST /api/expenses - Should create a new expense and return 200 OK")
    void createExpense_ValidRequest_ReturnsCreatedExpense() throws Exception {
        ExpenseRequestDTO requestDTO = getSampleRequestDTO();
        ExpenseResponseDTO expectedResponse = getSampleResponseDTO(1L);

        when(expenseService.createNewExpense(any(ExpenseRequestDTO.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Lunch at Local Cafe"))
                .andExpect(jsonPath("$.amount").value(15.50));
    }

    @Test
    @DisplayName("GET /api/expenses - Should return a list of all expenses")
    void getAllExpenses_ReturnsListOfExpenses() throws Exception {
        ExpenseResponseDTO expense1 = getSampleResponseDTO(1L);
        ExpenseResponseDTO expense2 = getSampleResponseDTO(2L);
        List<ExpenseResponseDTO> expectedList = List.of(expense1, expense2);

        when(expenseService.getAllExpenses()).thenReturn(expectedList);

        mockMvc.perform(get(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("GET /api/expenses/{id} - Should return an expense by ID")
    void getExpenseById_ExistingId_ReturnsExpense() throws Exception {
        Long expenseId = 5L;
        ExpenseResponseDTO expectedResponse = getSampleResponseDTO(expenseId);

        when(expenseService.getExpenseById(expenseId)).thenReturn(expectedResponse);

        mockMvc.perform(get(API_BASE_URL + "/{id}", expenseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.amount").value(15.50));
    }

    @Test
    @DisplayName("PUT /api/expenses/{id} - Should update expense and return 200 OK")
    void updateExpense_ValidRequest_ReturnsUpdatedExpense() throws Exception {
        Long expenseId = 3L;
        ExpenseRequestDTO requestDTO = new ExpenseRequestDTO(
                "Updated Expense Desc",
                BigDecimal.valueOf(25.00),
                2L,
                LocalDate.now()
        );
        ExpenseResponseDTO expectedResponse = new ExpenseResponseDTO(
                expenseId,
                "Updated Expense Desc",
                BigDecimal.valueOf(25.00),
                LocalDate.now(),
                2L
        );

        when(expenseService.updateExpense(eq(expenseId), any(ExpenseRequestDTO.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(put(API_BASE_URL + "/{id}", expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.description").value("Updated Expense Desc"))
                .andExpect(jsonPath("$.amount").value(25.00));
    }

    @Test
    @DisplayName("DELETE /api/expenses/{id} - Should delete an expense and return ID")
    void deleteExpenseById_ExistingId_ReturnsDeletedId() throws Exception {
        Long expenseId = 10L;

        when(expenseService.removeExpenseById(expenseId)).thenReturn(expenseId);

        mockMvc.perform(delete(API_BASE_URL + "/{id}", expenseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expenseId)));
    }
}