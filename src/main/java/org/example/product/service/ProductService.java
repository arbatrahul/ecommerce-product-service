package org.example.product.service;

import org.example.product.entity.Product;
import org.example.product.repository.elasticsearch.ProductElasticsearchRepository;
import org.example.product.repository.jpa.ProductJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductJpaRepository productJpaRepository;
    
    @Autowired
    private ProductElasticsearchRepository productElasticsearchRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    // JPA Operations (for CRUD)
    public Product createProduct(Product product) {
        Product savedProduct = productJpaRepository.save(product);
        
        // Index in Elasticsearch
        productElasticsearchRepository.save(savedProduct);
        
        // Send event to Kafka
        kafkaTemplate.send("product-events", "product-created", savedProduct);
        
        return savedProduct;
    }
    
    public Product updateProduct(Long id, Product product) {
        Optional<Product> existingProduct = productJpaRepository.findById(id);
        if (existingProduct.isPresent()) {
            product.setId(id);
            Product updatedProduct = productJpaRepository.save(product);
            
            // Update in Elasticsearch
            productElasticsearchRepository.save(updatedProduct);
            
            // Send event to Kafka
            kafkaTemplate.send("product-events", "product-updated", updatedProduct);
            
            return updatedProduct;
        }
        throw new RuntimeException("Product not found with id: " + id);
    }
    
    public void deleteProduct(Long id) {
        Optional<Product> product = productJpaRepository.findById(id);
        if (product.isPresent()) {
            Product prod = product.get();
            prod.setActive(false);
            productJpaRepository.save(prod);
            
            // Update in Elasticsearch
            productElasticsearchRepository.save(prod);
            
            // Send event to Kafka
            kafkaTemplate.send("product-events", "product-deleted", prod);
        } else {
            throw new RuntimeException("Product not found with id: " + id);
        }
    }
    
    public Optional<Product> getProductById(Long id) {
        return productJpaRepository.findById(id);
    }
    
    public List<Product> getProductsByIds(List<Long> ids) {
        return productJpaRepository.findByIdIn(ids);
    }
    
    // Elasticsearch Operations (for search)
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productJpaRepository.findByActiveTrue(pageable);
        }
        return productElasticsearchRepository.searchByKeyword(keyword.trim(), pageable);
    }
    
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productElasticsearchRepository.findByCategoryId(categoryId, pageable);
    }
    
    public Page<Product> getProductsByBrand(String brand, Pageable pageable) {
        return productElasticsearchRepository.findByBrand(brand, pageable);
    }
    
    public Page<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productElasticsearchRepository.findByPriceRange(minPrice, maxPrice, pageable);
    }
    
    public Page<Product> advancedSearch(String keyword, Long categoryId, BigDecimal minPrice, 
                                       BigDecimal maxPrice, Pageable pageable) {
        return productElasticsearchRepository.advancedSearch(keyword, categoryId, minPrice, maxPrice, pageable);
    }
    
    public Page<Product> getSimilarProducts(Long productId, Pageable pageable) {
        Optional<Product> product = productJpaRepository.findById(productId);
        if (product.isPresent()) {
            Product prod = product.get();
            return productElasticsearchRepository.findSimilarProducts(
                prod.getBrand(), prod.getCategoryId(), productId, pageable);
        }
        throw new RuntimeException("Product not found with id: " + productId);
    }
    
    public Page<Product> getLowStockProducts(Integer threshold, Pageable pageable) {
        return productElasticsearchRepository.findLowStockProducts(threshold, pageable);
    }
    
    public List<String> getAllBrands() {
        return productJpaRepository.findAllBrands();
    }
    
    // Inventory management
    public boolean updateStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productJpaRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getStockQuantity() >= quantity) {
                product.setStockQuantity(product.getStockQuantity() - quantity);
                productJpaRepository.save(product);
                
                // Update in Elasticsearch
                productElasticsearchRepository.save(product);
                
                // Send inventory update event
                kafkaTemplate.send("inventory-events", "stock-updated", 
                    new StockUpdateEvent(productId, product.getStockQuantity(), quantity));
                
                return true;
            }
        }
        return false;
    }
    
    public void restoreStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productJpaRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStockQuantity(product.getStockQuantity() + quantity);
            productJpaRepository.save(product);
            
            // Update in Elasticsearch
            productElasticsearchRepository.save(product);
            
            // Send inventory restore event
            kafkaTemplate.send("inventory-events", "stock-restored", 
                new StockUpdateEvent(productId, product.getStockQuantity(), quantity));
        }
    }
    
    // Inner class for Kafka events
    public static class StockUpdateEvent {
        private Long productId;
        private Integer currentStock;
        private Integer quantityChanged;
        
        public StockUpdateEvent(Long productId, Integer currentStock, Integer quantityChanged) {
            this.productId = productId;
            this.currentStock = currentStock;
            this.quantityChanged = quantityChanged;
        }
        
        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Integer getCurrentStock() { return currentStock; }
        public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }
        
        public Integer getQuantityChanged() { return quantityChanged; }
        public void setQuantityChanged(Integer quantityChanged) { this.quantityChanged = quantityChanged; }
    }
}
