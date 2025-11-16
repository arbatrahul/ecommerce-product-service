package org.example.product.repository.jpa;

import org.example.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByActiveTrue();
    
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
    
    List<Category> findByParentIdIsNull(); // Root categories
    
    List<Category> findByParentId(Long parentId); // Subcategories
    
    List<Category> findByParentIdIsNullAndActiveTrueOrderByDisplayOrderAsc(); // Active root categories
    
    List<Category> findByParentIdAndActiveTrueOrderByDisplayOrderAsc(Long parentId); // Active subcategories
    
    Optional<Category> findByNameAndActiveTrue(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT c FROM Category c WHERE c.active = true AND " +
           "(c.parentId IS NULL OR c.parentId IN " +
           "(SELECT cat.id FROM Category cat WHERE cat.active = true)) " +
           "ORDER BY c.parentId ASC, c.displayOrder ASC")
    List<Category> findAllActiveCategoriesHierarchical();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.categoryId = ?1 AND p.active = true")
    Long countProductsInCategory(Long categoryId);
}


