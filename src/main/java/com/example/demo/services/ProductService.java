package com.example.demo.services;

import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.entities.ProductImage;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductImageRepository;
import com.example.demo.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getProductsByCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return productRepository.findAll();
        }

        Optional<Category> categoryOpt = categoryRepository.findByCategoryName(categoryName);
        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found: " + categoryName);
        }

        Integer categoryId = categoryOpt.get().getCategoryId();
        return productRepository.findByCategory_CategoryId(categoryId);
    }

    public List<String> getProductImages(Integer productId) {
        List<ProductImage> productImages =
                productImageRepository.findByProduct_ProductId(productId);

        List<String> urls = new ArrayList<>();
        for (ProductImage img : productImages) {
            urls.add(img.getImageUrl());
        }
        return urls;
    }
}
