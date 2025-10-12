package com.muchemi.expense_tracker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muchemi.expense_tracker.dto.category.CategoryRequestDTO;
import com.muchemi.expense_tracker.dto.category.CategoryResponseDTO;
import com.muchemi.expense_tracker.services.CategoryService;
import com.muchemi.expense_tracker.exceptions.ResourceConflictException;
import com.muchemi.expense_tracker.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private static final String API_BASE_URL = "/api/categories";

    private CategoryRequestDTO getSampleRequestDTO() {
        return new CategoryRequestDTO("Groceries");
    }

    private CategoryResponseDTO getSampleResponseDTO() {
        return new CategoryResponseDTO(1L, "Groceries");
    }

    @Test
    @DisplayName("POST /api/categories - Should create a new category and return 200 OK")
    void createCategory_ValidRequest_ReturnsCreatedCategory() throws Exception {
        CategoryRequestDTO requestDTO = getSampleRequestDTO();
        CategoryResponseDTO expectedResponse = getSampleResponseDTO();

        when(categoryService.createNewCategory(any(CategoryRequestDTO.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Groceries"));
    }

    @Test
    @DisplayName("GET /api/categories - Should return a list of all categories")
    void getAllCategories_ReturnsListOfCategories() throws Exception {
        CategoryResponseDTO response1 = new CategoryResponseDTO(1L, "Food");
        CategoryResponseDTO response2 = new CategoryResponseDTO(2L, "Travel");
        List<CategoryResponseDTO> expectedList = List.of(response1, response2);

        when(categoryService.getAllCategories()).thenReturn(expectedList);

        mockMvc.perform(get(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[1].name").value("Travel"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} - Should return a category by ID")
    void getCategoryById_ExistingId_ReturnsCategory() throws Exception {
        Long categoryId = 1L;
        CategoryResponseDTO expectedResponse = getSampleResponseDTO();

        when(categoryService.getCategoryById(categoryId)).thenReturn(expectedResponse);

        mockMvc.perform(get(API_BASE_URL + "/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Groceries"));
    }

    @Test
    @DisplayName("PUT /api/categories/{id} - Should update category and return 200 OK")
    void updateCategory_ValidRequest_ReturnsUpdatedCategory() throws Exception {
        Long categoryId = 1L;
        CategoryRequestDTO requestDTO = new CategoryRequestDTO("Household");
        CategoryResponseDTO expectedResponse = new CategoryResponseDTO(categoryId, "Household");

        when(categoryService.updateCategory(eq(categoryId), any(CategoryRequestDTO.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(put(API_BASE_URL + "/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Household"));
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} - Should delete a category and return ID")
    void deleteCategoryById_ExistingId_ReturnsDeletedId() throws Exception {
        Long categoryId = 1L;

        when(categoryService.removeCategoryById(categoryId)).thenReturn(categoryId);

        mockMvc.perform(delete(API_BASE_URL + "/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(categoryId)));
    }

    @Test
    @DisplayName("POST /api/categories - Conflict when creating duplicate category name")
    void createCategory_DuplicateName_ReturnsConflict() throws Exception {
        CategoryRequestDTO requestDTO = getSampleRequestDTO();
        when(categoryService.createNewCategory(any(CategoryRequestDTO.class)))
                .thenThrow(new ResourceConflictException("Category name already exists"));

        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/categories - Invalid input returns Bad Request")
    void createCategory_InvalidInput_ReturnsBadRequest() throws Exception {
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO(""); // empty name
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/categories/{id} - Non-existent ID returns Not Found")
    void getCategoryById_NonExistentId_ReturnsNotFound() throws Exception {
        Long nonExistentId = 999L;
        when(categoryService.getCategoryById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(get(API_BASE_URL + "/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/categories/{id} - Invalid ID format returns Bad Request")
    void getCategoryById_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get(API_BASE_URL + "/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/categories/{id} - Non-existent ID returns Not Found")
    void updateCategory_NonExistentId_ReturnsNotFound() throws Exception {
        Long nonExistentId = 999L;
        CategoryRequestDTO requestDTO = getSampleRequestDTO();
        when(categoryService.updateCategory(eq(nonExistentId), any(CategoryRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(put(API_BASE_URL + "/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/categories/{id} - Duplicate name on update returns Conflict")
    void updateCategory_DuplicateName_ReturnsConflict() throws Exception {
        Long categoryId = 1L;
        CategoryRequestDTO requestDTO = new CategoryRequestDTO("ExistingName");
        when(categoryService.updateCategory(eq(categoryId), any(CategoryRequestDTO.class)))
                .thenThrow(new ResourceConflictException("Category name already exists"));

        mockMvc.perform(put(API_BASE_URL + "/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/categories/{id} - Invalid input returns Bad Request")
    void updateCategory_InvalidInput_ReturnsBadRequest() throws Exception {
        Long categoryId = 1L;
        CategoryRequestDTO invalidRequest = new CategoryRequestDTO(""); // empty name
        mockMvc.perform(put(API_BASE_URL + "/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} - Non-existent ID returns Not Found")
    void deleteCategoryById_NonExistentId_ReturnsNotFound() throws Exception {
        Long nonExistentId = 999L;
        when(categoryService.removeCategoryById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(delete(API_BASE_URL + "/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} - Constraint violation returns Conflict")
    void deleteCategoryById_ConstraintViolation_ReturnsConflict() throws Exception {
        Long categoryId = 1L;
        when(categoryService.removeCategoryById(categoryId))
                .thenThrow(new ResourceConflictException("Category is in use"));

        mockMvc.perform(delete(API_BASE_URL + "/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}