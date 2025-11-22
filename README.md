# Ecommerce Product Service

Microservice for product catalog management with Elasticsearch integration for advanced search capabilities.

## Overview

The Product Service manages the entire product catalog including:
- Product CRUD operations
- Category management
- Advanced search with Elasticsearch
- Inventory management
- Search analytics tracking
- Product recommendations

## Features

- ✅ **Product Management**: Full CRUD operations for products
- ✅ **Category Management**: Hierarchical category structure
- ✅ **Elasticsearch Integration**: Fast, full-text search
- ✅ **Advanced Search**: Filter by keyword, category, price range
- ✅ **Inventory Management**: Stock tracking and updates
- ✅ **Search Analytics**: Track search queries and product views
- ✅ **Kafka Integration**: Event-driven architecture
- ✅ **Service Discovery**: Eureka client integration
- ✅ **MySQL Database**: Persistent storage
- ✅ **RESTful API**: Comprehensive REST endpoints

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0+ (Port 3307)
- Elasticsearch 7.x+ (Port 9200)
- Kafka (Port 9092)
- Eureka Server (Port 8761)

### Database Setup

1. **Create MySQL Database**:
   ```sql
   CREATE DATABASE product_service_db;
   ```

2. **Start Elasticsearch**:
   ```bash
   # Using Docker
   docker run -d -p 9200:9200 -p 9300:9300 \
     -e "discovery.type=single-node" \
     elasticsearch:7.17.0
   ```

### Running Locally

1. **Build the project**:
   ```bash
   mvn clean package
   ```

2. **Run the application**:
   ```bash
   java -jar target/product-service-1.0.0.jar
   ```

3. **Or use Maven**:
   ```bash
   mvn spring-boot:run
   ```

The service will start on `http://localhost:8082`

## API Endpoints

### Product Endpoints

#### Search Products
```http
GET /api/products/search?keyword=laptop&page=0&size=12&sortBy=price&sortDir=asc
```

#### Advanced Search
```http
GET /api/products/advanced-search?keyword=laptop&categoryId=1&minPrice=100&maxPrice=1000&page=0&size=12
```

#### Get Product by ID
```http
GET /api/products/{id}?userId=1
```

#### Get Products by Category
```http
GET /api/products/category/{categoryId}?page=0&size=12
```

#### Get Products by Brand
```http
GET /api/products/brand/{brand}?page=0&size=12
```

#### Get Similar Products
```http
GET /api/products/{id}/similar?page=0&size=6
```

#### Get All Brands
```http
GET /api/products/brands
```

#### Get Products by IDs (Batch)
```http
POST /api/products/batch
Content-Type: application/json

[1, 2, 3, 4, 5]
```

#### Create Product (Admin)
```http
POST /api/products
Content-Type: application/json

{
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "categoryId": 1,
  "brand": "TechBrand",
  "stock": 100,
  "sku": "LAP-001"
}
```

#### Update Product (Admin)
```http
PUT /api/products/{id}
Content-Type: application/json

{
  "name": "Updated Laptop",
  "price": 899.99
}
```

#### Delete Product (Admin)
```http
DELETE /api/products/{id}
```

#### Update Stock
```http
PUT /api/products/{id}/stock?quantity=50
```

#### Restore Stock
```http
PUT /api/products/{id}/stock/restore?quantity=10
```

#### Get Low Stock Products (Admin)
```http
GET /api/products/low-stock?threshold=10&page=0&size=20
```

### Category Endpoints

#### Get All Active Categories
```http
GET /api/categories
```

#### Get Categories Hierarchy
```http
GET /api/categories/hierarchy
```

#### Get Category by ID
```http
GET /api/categories/{id}
```

#### Get Subcategories
```http
GET /api/categories/{parentId}/subcategories
```

#### Get Category with Product Count
```http
GET /api/categories/{id}/with-count
```

#### Create Category (Admin)
```http
POST /api/categories
Content-Type: application/json

{
  "name": "Electronics",
  "description": "Electronic products",
  "displayOrder": 1
}
```

#### Update Category (Admin)
```http
PUT /api/categories/{id}
Content-Type: application/json

{
  "name": "Updated Electronics"
}
```

#### Delete Category (Admin)
```http
DELETE /api/categories/{id}
```

#### Toggle Category Status (Admin)
```http
PUT /api/categories/{id}/toggle
```

## Configuration

### Application Configuration (application.yml)

```yaml
server:
  port: 8082

spring:
  application:
    name: product-service
  datasource:
    url: jdbc:mysql://localhost:3307/product_service_db
    username: root
    password: password
  elasticsearch:
    uris: http://localhost:9200
  kafka:
    bootstrap-servers: localhost:9092
```

### Database Configuration

- **MySQL**: Port 3307
- **Database**: `product_service_db`
- **Hibernate**: Auto DDL update enabled

### Elasticsearch Configuration

- **Host**: localhost:9200
- **Connection Timeout**: 10s
- **Socket Timeout**: 60s

## Usage Examples

### Search Products

```bash
curl "http://localhost:8082/api/products/search?keyword=laptop&page=0&size=12"
```

### Advanced Search with Filters

```bash
curl "http://localhost:8082/api/products/advanced-search?keyword=laptop&categoryId=1&minPrice=500&maxPrice=2000"
```

### Create Product

```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "price": 1299.99,
    "categoryId": 1,
    "brand": "GamingTech",
    "stock": 50,
    "sku": "GAM-LAP-001"
  }'
```

### Update Stock

```bash
curl -X PUT "http://localhost:8082/api/products/1/stock?quantity=25"
```

### Get Products by Category

```bash
curl "http://localhost:8082/api/products/category/1?page=0&size=12"
```

## Architecture

### Data Flow

1. **Product Creation** → MySQL (Primary Storage)
2. **Index to Elasticsearch** → Search Index
3. **Search Request** → Elasticsearch Query
4. **Analytics Event** → Kafka Producer
5. **Cart Events** → Kafka Consumer (Stock Updates)

### Components

1. **ProductController**: REST endpoints for products
2. **CategoryController**: REST endpoints for categories
3. **ProductService**: Business logic
4. **ProductJpaRepository**: MySQL operations
5. **ProductElasticsearchRepository**: Elasticsearch operations
6. **SearchAnalyticsService**: Search tracking
7. **CartEventConsumer**: Handles cart events from Kafka

## Kafka Integration

### Topics Consumed

- `cart-events`: Cart operations (add, remove, checkout)

### Topics Produced

- `search-analytics`: Search query tracking
- `product-view-analytics`: Product view tracking

## Testing

### Run Tests

```bash
mvn test
```

### Manual Testing

1. Start MySQL, Elasticsearch, Kafka, Eureka
2. Start Product Service
3. Create categories
4. Create products
5. Test search functionality
6. Verify Elasticsearch indexing

## Deployment

### Docker

```bash
# Build image
docker build -t ecommerce/product-service .

# Run container
docker run -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-product:3306/product_service_db \
  -e SPRING_ELASTICSEARCH_URIS=http://elasticsearch:9200 \
  ecommerce/product-service
```

### Production Considerations

1. **Elasticsearch**: 
   - Use cluster setup for high availability
   - Configure proper index settings
   - Set up index templates
2. **MySQL**: 
   - Use connection pooling
   - Configure read replicas
   - Set up backups
3. **Kafka**: 
   - Configure proper partitions
   - Set up consumer groups
4. **Performance**: 
   - Cache frequently accessed data
   - Optimize Elasticsearch queries
   - Use pagination for large result sets

## Troubleshooting

### Common Issues

1. **Elasticsearch Connection Failed**:
   - Verify Elasticsearch is running
   - Check connection URL
   - Verify network connectivity

2. **Products Not Indexed**:
   - Check Elasticsearch logs
   - Verify index creation
   - Check product creation logs

3. **Search Returns No Results**:
   - Verify products are indexed
   - Check Elasticsearch index
   - Review search query syntax

4. **Database Connection Issues**:
   - Verify MySQL is running
   - Check credentials
   - Verify database exists

### Logs

```bash
# Enable debug logging
java -jar target/product-service-1.0.0.jar \
  --logging.level.org.example.product=DEBUG \
  --logging.level.org.springframework.data.elasticsearch=DEBUG
```

## Dependencies

- Spring Boot 3.2.0
- Spring Data JPA (MySQL)
- Spring Data Elasticsearch
- Spring Cloud Netflix Eureka Client
- Spring Kafka
- MySQL Connector
- Validation API

## Project Structure

```
src/
├── main/
│   ├── java/org/example/product/
│   │   ├── ProductServiceApplication.java
│   │   ├── controller/
│   │   │   ├── ProductController.java
│   │   │   └── CategoryController.java
│   │   ├── service/
│   │   │   ├── ProductService.java
│   │   │   ├── SearchAnalyticsService.java
│   │   │   └── CategoryInitializationService.java
│   │   ├── entity/
│   │   │   ├── Product.java
│   │   │   └── Category.java
│   │   ├── repository/
│   │   │   ├── jpa/
│   │   │   │   ├── ProductJpaRepository.java
│   │   │   │   └── CategoryRepository.java
│   │   │   └── elasticsearch/
│   │   │       └── ProductElasticsearchRepository.java
│   │   ├── consumer/
│   │   │   └── CartEventConsumer.java
│   │   └── config/
│   │       ├── ElasticsearchRepositoryConfig.java
│   │       └── JpaRepositoryConfig.java
│   └── resources/
│       └── application.yml
└── test/
```

## Contributing

1. Follow Spring Boot best practices
2. Write comprehensive tests
3. Update Elasticsearch mappings when needed
4. Document API changes
5. Handle errors gracefully

## License

This project is part of the Ecommerce Microservices Platform.
