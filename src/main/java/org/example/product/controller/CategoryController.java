package org.example.product.controller;

import org.example.product.entity.Category;
import org.example.product.repository.jpa.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // Get all active categories
    @GetMapping
    public ResponseEntity<List<Category>> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(categories);
    }

    // Get hierarchical categories (root categories with their subcategories)
    @GetMapping("/hierarchy")
    public ResponseEntity<Map<String, Object>> getCategoriesHierarchy() {
        List<Category> rootCategories = categoryRepository.findByParentIdIsNullAndActiveTrueOrderByDisplayOrderAsc();
        
        Map<String, Object> response = new HashMap<>();
        response.put("categories", rootCategories);
        
        // Add subcategories for each root category
        Map<Long, List<Category>> subcategoriesMap = new HashMap<>();
        for (Category rootCategory : rootCategories) {
            List<Category> subcategories = categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrderAsc(rootCategory.getId());
            subcategoriesMap.put(rootCategory.getId(), subcategories);
        }
        response.put("subcategories", subcategoriesMap);
        
        return ResponseEntity.ok(response);
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    // Get subcategories of a parent category
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<Category>> getSubcategories(@PathVariable Long parentId) {
        List<Category> subcategories = categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrderAsc(parentId);
        return ResponseEntity.ok(subcategories);
    }

    // Get category with product count
    @GetMapping("/{id}/with-count")
    public ResponseEntity<Map<String, Object>> getCategoryWithProductCount(@PathVariable Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            Long productCount = categoryRepository.countProductsInCategory(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("productCount", productCount);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Admin endpoints for category management
    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        try {
            if (categoryRepository.existsByName(category.getName())) {
                return ResponseEntity.badRequest().build();
            }
            
            Category savedCategory = categoryRepository.save(category);
            return ResponseEntity.ok(savedCategory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        Optional<Category> existingCategory = categoryRepository.findById(id);
        
        if (existingCategory.isPresent()) {
            category.setId(id);
            Category updatedCategory = categoryRepository.save(category);
            return ResponseEntity.ok(updatedCategory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            
            // Check if category has products
            Long productCount = categoryRepository.countProductsInCategory(id);
            if (productCount > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Cannot delete category with existing products. Move products first.");
                response.put("productCount", productCount);
                
                return ResponseEntity.badRequest().body(response);
            }
            
            // Soft delete - mark as inactive
            category.setActive(false);
            categoryRepository.save(category);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Category deleted successfully");
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleCategoryStatus(@PathVariable Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            category.setActive(!category.getActive());
            categoryRepository.save(category);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Category status updated");
            response.put("active", category.getActive());
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
