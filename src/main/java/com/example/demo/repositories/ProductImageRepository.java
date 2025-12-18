package com.example.demo.repositories;

import com.example.demo.entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    List<ProductImage> findByProductProductId(Integer productId);

    void deleteByProductProductId(Integer productId);
}
