package com.example.demo.services;

import com.example.demo.entities.Product;
import com.example.demo.entities.ProductImage;
import com.example.demo.repositories.ProductImageRepository;
import com.example.demo.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    /* ===== existing customer methods ===== */

    public List<Product> getProductsByCategory(String category) {
        if (category == null || category.isBlank()) {
            return productRepository.findAll();
        }
        return productRepository.findByCategoryName(category);
    }

    public List<String> getProductImages(Integer productId) {
        List<ProductImage> images = productImageRepository.findByProductProductId(productId);
        return images.stream()
                .map(ProductImage::getImageUrl)
                .toList();
    }

    /* ===== ADMIN METHODS used in ProductController ===== */

    // create / add product
    public Product addProduct(Product product) {
        // if id is auto-increment in DB, ensure incoming id is null
        product.setProductId(null);
        return productRepository.save(product);
    }

    // delete product
    public void deleteProduct(Integer id) {
        if (productRepository.existsById(id)) {
            // also delete images if you want cascade manually
            productImageRepository.deleteByProductProductId(id);
            productRepository.deleteById(id);
        }
    }

    // get single product
    public Product getProductById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }
}
