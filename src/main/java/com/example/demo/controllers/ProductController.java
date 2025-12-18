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

    // Common method to read authenticated user from filter
    private User getAuthenticatedUser(HttpServletRequest request) {
        return (User) request.getAttribute("authenticatedUser");
    }

    /* =============== PUBLIC / CUSTOMER APIs =============== */

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String category,
            HttpServletRequest request) {

        User authenticatedUser = getAuthenticatedUser(request);
        if (authenticatedUser == null) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("error", "Unauthorized access"));
        }

        List<Product> products = productService.getProductsByCategory(category);

        Map<String, String> userInfo = Map.of(
                "name", authenticatedUser.getUsername(),
                "role", authenticatedUser.getRole().name()
        );

        List<Map<String, Object>> productList = new ArrayList<>();
        for (Product product : products) {
            Map<String, Object> details = new HashMap<>();
            details.put("product_id", product.getProductId());
            details.put("name", product.getName());
            details.put("description", product.getDescription());
            details.put("price", product.getPrice());
            details.put("stock", product.getStock());
            details.put("images", productService.getProductImages(product.getProductId()));
            productList.add(details);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("user", userInfo);
        response.put("products", productList);

        return ResponseEntity.ok(response);
    }

    /* =============== ADMIN PRODUCT MANAGEMENT APIs =============== */

    // Add new product (used by admin dashboard)
    @PostMapping("/admin/add")
    public ResponseEntity<?> addProduct(@RequestBody Product product, HttpServletRequest request) {
        User authenticatedUser = getAuthenticatedUser(request);
        if (authenticatedUser == null || authenticatedUser.getRole() == null
                || !"ADMIN".equals(authenticatedUser.getRole().name())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        Product saved = productService.addProduct(product);
        return ResponseEntity.ok(saved);
    }

    // Delete product by id
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") Integer id, HttpServletRequest request) {
        User authenticatedUser = getAuthenticatedUser(request);
        if (authenticatedUser == null || authenticatedUser.getRole() == null
                || !"ADMIN".equals(authenticatedUser.getRole().name())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Optional: get single product details for admin edit/view
    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") Integer id, HttpServletRequest request) {
        User authenticatedUser = getAuthenticatedUser(request);
        if (authenticatedUser == null || authenticatedUser.getRole() == null
                || !"ADMIN".equals(authenticatedUser.getRole().name())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Product not found"));
        }
        return ResponseEntity.ok(product);
    }
}
