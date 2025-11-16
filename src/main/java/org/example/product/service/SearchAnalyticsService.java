package org.example.product.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SearchAnalyticsService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void trackSearch(String keyword, Long userId, int resultsCount) {
        SearchEvent searchEvent = new SearchEvent(
            keyword, 
            userId, 
            resultsCount, 
            LocalDateTime.now()
        );
        
        // Send search analytics to Kafka
        kafkaTemplate.send("search-events", "search-performed", searchEvent);
    }

    public void trackProductView(Long productId, Long userId) {
        ProductViewEvent viewEvent = new ProductViewEvent(
            productId, 
            userId, 
            LocalDateTime.now()
        );
        
        // Send product view analytics to Kafka
        kafkaTemplate.send("product-events", "product-viewed", viewEvent);
    }

    // Inner classes for Kafka events
    public static class SearchEvent {
        private String keyword;
        private Long userId;
        private Integer resultsCount;
        private LocalDateTime timestamp;
        
        public SearchEvent(String keyword, Long userId, Integer resultsCount, LocalDateTime timestamp) {
            this.keyword = keyword;
            this.userId = userId;
            this.resultsCount = resultsCount;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Integer getResultsCount() { return resultsCount; }
        public void setResultsCount(Integer resultsCount) { this.resultsCount = resultsCount; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class ProductViewEvent {
        private Long productId;
        private Long userId;
        private LocalDateTime timestamp;
        
        public ProductViewEvent(Long productId, Long userId, LocalDateTime timestamp) {
            this.productId = productId;
            this.userId = userId;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
