package com.example.demo.controllers;

import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import com.example.demo.services.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String category,
            HttpServletRequest request) {

        // AuthenticationFilter must have set this attribute
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        if (authenticatedUser == null) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("error", "Unauthorized access"));
        }

        List<Product> products = productService.getProductsByCategory(category);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("name", authenticatedUser.getUsername());
        userInfo.put("role", authenticatedUser.getRole().name());

        List<Map<String, Object>> productList = new ArrayList<>();

        for (Product product : products) {
            Map<String, Object> details = new HashMap<>();
            details.put("product_id", product.getProductId());
            details.put("name", product.getName());
            details.put("description", product.getDescription());
            details.put("price", product.getPrice());
            details.put("stock", product.getStock());

            List<String> images = productService.getProductImages(product.getProductId());
            details.put("images", images);

            productList.add(details);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("user", userInfo);
        response.put("products", productList);

        return ResponseEntity.ok(response);
    }
}
