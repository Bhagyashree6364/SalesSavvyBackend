package com.example.demo.repositories;

import com.example.demo.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Existing method is enough to filter by category:
    List<Product> findByCategory_CategoryId(Integer categoryId);

    // Remove any query that references p.status, p.active, or p.available
    // e.g. delete this if you have it:
    // @Query("select p from Product p where p.category.categoryId = :categoryId and p.status = true")
    // List<Product> findByCategoryAndStatus(Integer categoryId);
}
