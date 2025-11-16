package org.example.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.product.entity.Category;
import org.example.product.repository.jpa.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CategoryController.class, excludeAutoConfiguration = {ElasticsearchDataAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@TestPropertySource(properties = {
    "spring.data.jpa.repositories.enabled=false",
    "spring.data.elasticsearch.repositories.enabled=false"
})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Category parentCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        // Setup parent category
        parentCategory = new Category();
        parentCategory.setId(1L);
        parentCategory.setName("Electronics");
        parentCategory.setDescription("Electronic devices and gadgets");
        parentCategory.setActive(true);
        parentCategory.setDisplayOrder(1);
        parentCategory.setCreatedAt(LocalDateTime.now());

        // Setup child category
        childCategory = new Category();
        childCategory.setId(2L);
        childCategory.setName("Smartphones");
        childCategory.setDescription("Mobile phones and accessories");
        childCategory.setParentId(1L);
        childCategory.setActive(true);
        childCategory.setDisplayOrder(1);
        childCategory.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllActiveCategories_Success() throws Exception {
        // Given
        List<Category> categories = Arrays.asList(parentCategory, childCategory);
        when(categoryRepository.findByActiveTrueOrderByDisplayOrderAsc()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Smartphones"));

        verify(categoryRepository).findByActiveTrueOrderByDisplayOrderAsc();
    }

    @Test
    void getCategoriesHierarchy_Success() throws Exception {
        // Given
        List<Category> rootCategories = Arrays.asList(parentCategory);
        List<Category> subcategories = Arrays.asList(childCategory);
        
        when(categoryRepository.findByParentIdIsNullAndActiveTrueOrderByDisplayOrderAsc()).thenReturn(rootCategories);
        when(categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrderAsc(1L)).thenReturn(subcategories);

        // When & Then
        mockMvc.perform(get("/api/categories/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[0].id").value(1))
                .andExpect(jsonPath("$.categories[0].name").value("Electronics"))
                .andExpect(jsonPath("$.subcategories").exists())
                .andExpect(jsonPath("$.subcategories.1").isArray())
                .andExpect(jsonPath("$.subcategories.1[0].id").value(2))
                .andExpect(jsonPath("$.subcategories.1[0].name").value("Smartphones"));

        verify(categoryRepository).findByParentIdIsNullAndActiveTrueOrderByDisplayOrderAsc();
        verify(categoryRepository).findByParentIdAndActiveTrueOrderByDisplayOrderAsc(1L);
    }

    @Test
    void getCategoryById_Success() throws Exception {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));

        // When & Then
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Electronic devices and gadgets"));

        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_NotFound() throws Exception {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isNotFound());

        verify(categoryRepository).findById(1L);
    }

    @Test
    void getSubcategories_Success() throws Exception {
        // Given
        List<Category> subcategories = Arrays.asList(childCategory);
        when(categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrderAsc(1L)).thenReturn(subcategories);

        // When & Then
        mockMvc.perform(get("/api/categories/1/subcategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Smartphones"))
                .andExpect(jsonPath("$[0].parentId").value(1));

        verify(categoryRepository).findByParentIdAndActiveTrueOrderByDisplayOrderAsc(1L);
    }

    @Test
    void getCategoryWithProductCount_Success() throws Exception {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.countProductsInCategory(1L)).thenReturn(25L);

        // When & Then
        mockMvc.perform(get("/api/categories/1/with-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("Electronics"))
                .andExpect(jsonPath("$.productCount").value(25));

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).countProductsInCategory(1L);
    }

    @Test
    void getCategoryWithProductCount_NotFound() throws Exception {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/categories/1/with-count"))
                .andExpect(status().isNotFound());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).countProductsInCategory(anyLong());
    }

    @Test
    void createCategory_Success() throws Exception {
        // Given
        Category newCategory = new Category();
        newCategory.setName("Books");
        newCategory.setDescription("Books and literature");
        
        when(categoryRepository.existsByName("Books")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);

        // When & Then
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Books"))
                .andExpect(jsonPath("$.description").value("Books and literature"));

        verify(categoryRepository).existsByName("Books");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_NameAlreadyExists() throws Exception {
        // Given
        Category newCategory = new Category();
        newCategory.setName("Electronics");
        newCategory.setDescription("Electronic devices");
        
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isBadRequest());

        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_Success() throws Exception {
        // Given
        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Electronics Updated");
        updatedCategory.setDescription("Updated description");
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // When & Then
        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics Updated"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_NotFound() throws Exception {
        // Given
        Category updatedCategory = new Category();
        updatedCategory.setName("Electronics Updated");
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCategory)))
                .andExpect(status().isNotFound());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_Success() throws Exception {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.countProductsInCategory(1L)).thenReturn(0L);
        when(categoryRepository.save(any(Category.class))).thenReturn(parentCategory);

        // When & Then
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).countProductsInCategory(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deleteCategory_HasProducts() throws Exception {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.countProductsInCategory(1L)).thenReturn(5L);

        // When & Then
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot delete category with existing products. Move products first."))
                .andExpect(jsonPath("$.productCount").value(5));

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).countProductsInCategory(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_NotFound() throws Exception {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNotFound());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).countProductsInCategory(anyLong());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void toggleCategoryStatus_Success() throws Exception {
        // Given
        parentCategory.setActive(true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(parentCategory);

        // When & Then
        mockMvc.perform(put("/api/categories/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category status updated"))
                .andExpect(jsonPath("$.active").value(false)); // Should be toggled

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void toggleCategoryStatus_NotFound() throws Exception {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/categories/1/toggle"))
                .andExpect(status().isNotFound());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
