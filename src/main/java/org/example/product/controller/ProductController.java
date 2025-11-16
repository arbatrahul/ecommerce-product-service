package org.example.product.controller;

import org.example.product.entity.Product;
import org.example.product.service.ProductService;
import org.example.product.service.SearchAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private SearchAnalyticsService searchAnalyticsService;

    // Search products using Elasticsearch
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productService.searchProducts(keyword, pageable);
        
        // Track search analytics via Kafka
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchAnalyticsService.trackSearch(keyword, userId, (int) productPage.getTotalElements());
        }
        
        Map<String, Object> response = createPageResponse(productPage);
        response.put("keyword", keyword);
        
        return ResponseEntity.ok(response);
    }

    // Advanced search with multiple filters
    @GetMapping("/advanced-search")
    public ResponseEntity<Map<String, Object>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage;
        
        if (keyword != null || categoryId != null || minPrice != null || maxPrice != null) {
            productPage = productService.advancedSearch(keyword, categoryId, minPrice, maxPrice, pageable);
        } else {
            productPage = productService.searchProducts(null, pageable);
        }
        
        Map<String, Object> response = createPageResponse(productPage);
        response.put("filters", Map.of(
            "keyword", keyword,
            "categoryId", categoryId,
            "minPrice", minPrice,
            "maxPrice", maxPrice
        ));
        
        return ResponseEntity.ok(response);
    }

    // Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        
        Optional<Product> product = productService.getProductById(id);
        
        if (product.isPresent()) {
            // Track product view analytics via Kafka
            searchAnalyticsService.trackProductView(id, userId);
            return ResponseEntity.ok(product.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get products by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productService.getProductsByCategory(categoryId, pageable);
        
        Map<String, Object> response = createPageResponse(productPage);
        response.put("categoryId", categoryId);
        
        return ResponseEntity.ok(response);
    }

    // Get products by brand
    @GetMapping("/brand/{brand}")
    public ResponseEntity<Map<String, Object>> getProductsByBrand(
            @PathVariable String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productService.getProductsByBrand(brand, pageable);
        
        Map<String, Object> response = createPageResponse(productPage);
        response.put("brand", brand);
        
        return ResponseEntity.ok(response);
    }

    // Get similar products
    @GetMapping("/{id}/similar")
    public ResponseEntity<Map<String, Object>> getSimilarProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.getSimilarProducts(id, pageable);
        
        Map<String, Object> response = createPageResponse(productPage);
        response.put("productId", id);
        
        return ResponseEntity.ok(response);
    }

    // Get all brands
    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAllBrands() {
        List<String> brands = productService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    // Get products by IDs (for cart/order services)
    @PostMapping("/batch")
    public ResponseEntity<List<Product>> getProductsByIds(@RequestBody List<Long> productIds) {
        List<Product> products = productService.getProductsByIds(productIds);
        return ResponseEntity.ok(products);
    }

    // Admin endpoints for product management
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.ok(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        try {
            Product updatedProduct = productService.updateProduct(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Inventory management endpoints
    @PutMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> updateStock(
            @PathVariable Long id, 
            @RequestParam Integer quantity) {
        
        boolean success = productService.updateStock(id, quantity);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Stock updated successfully" : "Insufficient stock");
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/stock/restore")
    public ResponseEntity<Map<String, Object>> restoreStock(
            @PathVariable Long id, 
            @RequestParam Integer quantity) {
        
        productService.restoreStock(id, quantity);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Stock restored successfully");
        
        return ResponseEntity.ok(response);
    }

    // Get low stock products (for admin)
    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.getLowStockProducts(threshold, pageable);
        
        Map<String, Object> response = createPageResponse(productPage);
        response.put("threshold", threshold);
        
        return ResponseEntity.ok(response);
    }

    // Helper method to create paginated response
    private Map<String, Object> createPageResponse(Page<Product> productPage) {
        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        response.put("hasNext", productPage.hasNext());
        response.put("hasPrevious", productPage.hasPrevious());
        return response;
    }
}
