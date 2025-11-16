package org.example.product.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Document(indexName = "products")
public class Product {
    
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    @Column(nullable = false)
    private String name;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    @Column(length = 1000)
    private String description;
    
    @Field(type = FieldType.Double)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Field(type = FieldType.Keyword)
    @Column(nullable = false)
    private String brand;
    
    @Field(type = FieldType.Long)
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    @Field(type = FieldType.Keyword)
    @Column(name = "category_name")
    private String categoryName;
    
    @Field(type = FieldType.Integer)
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    @Field(type = FieldType.Text)
    @Column(name = "image_url")
    private String imageUrl;
    
    @Field(type = FieldType.Boolean)
    @Column(nullable = false)
    private Boolean active = true;
    
    @Field(type = FieldType.Date)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Date)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Product() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Product(String name, String description, BigDecimal price, String brand, 
                  Long categoryId, String categoryName, Integer stockQuantity) {
        this();
        this.name = name;
        this.description = description;
        this.price = price;
        this.brand = brand;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.stockQuantity = stockQuantity;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
