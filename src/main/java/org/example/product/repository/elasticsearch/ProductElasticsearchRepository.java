package org.example.product.repository.elasticsearch;

import org.example.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<Product, Long> {
    
    // Full-text search across name, description, and brand
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^2\", \"description\", \"brand\"], \"type\": \"best_fields\", \"fuzziness\": \"AUTO\"}}], \"filter\": [{\"term\": {\"active\": true}}]}}")
    Page<Product> searchByKeyword(String keyword, Pageable pageable);
    
    // Search by category
    @Query("{\"bool\": {\"must\": [{\"term\": {\"categoryId\": ?0}}], \"filter\": [{\"term\": {\"active\": true}}]}}")
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    
    // Search by brand
    @Query("{\"bool\": {\"must\": [{\"term\": {\"brand.keyword\": \"?0\"}}], \"filter\": [{\"term\": {\"active\": true}}]}}")
    Page<Product> findByBrand(String brand, Pageable pageable);
    
    // Price range search
    @Query("{\"bool\": {\"must\": [{\"range\": {\"price\": {\"gte\": ?0, \"lte\": ?1}}}], \"filter\": [{\"term\": {\"active\": true}}]}}")
    Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // Advanced search with multiple filters
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^2\", \"description\", \"brand\"], \"fuzziness\": \"AUTO\"}}], \"filter\": [{\"term\": {\"active\": true}}, {\"term\": {\"categoryId\": ?1}}, {\"range\": {\"price\": {\"gte\": ?2, \"lte\": ?3}}}]}}")
    Page<Product> advancedSearch(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // Get products with low stock (for inventory management)
    @Query("{\"bool\": {\"must\": [{\"range\": {\"stockQuantity\": {\"lte\": ?0}}}], \"filter\": [{\"term\": {\"active\": true}}]}}")
    Page<Product> findLowStockProducts(Integer threshold, Pageable pageable);
    
    // Find similar products by brand and category
    @Query("{\"bool\": {\"must\": [{\"term\": {\"brand.keyword\": \"?0\"}}, {\"term\": {\"categoryId\": ?1}}], \"filter\": [{\"term\": {\"active\": true}}], \"must_not\": [{\"term\": {\"_id\": \"?2\"}}]}}")
    Page<Product> findSimilarProducts(String brand, Long categoryId, Long excludeId, Pageable pageable);
}


