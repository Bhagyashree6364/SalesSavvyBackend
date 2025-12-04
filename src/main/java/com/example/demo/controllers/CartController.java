package com.example.demo.controllers;

import com.example.demo.services.AuthService;
import com.example.demo.services.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CartController {

    private final CartService cartService;
    private final AuthService authService;

    @Autowired
    public CartController(CartService cartService, AuthService authService) {
        this.cartService = cartService;
        this.authService = authService;
    }

    private String getUsernameFromRequest(HttpServletRequest request) {
        String token = authService.getAuthTokenFromCookies(request);
        if (token == null || !authService.validateToken(token)) {
            throw new RuntimeException("Unauthorized: invalid token");
        }
        String username = authService.extractUsername(token);
        if (username == null) {
            throw new RuntimeException("Unauthorized: username not found");
        }
        return username;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        String username = getUsernameFromRequest(request);

        if (body == null || body.get("productId") == null) {
            throw new RuntimeException("productId is missing in request body");
        }

        Integer productId = ((Number) body.get("productId")).intValue();
        int quantity = ((Number) body.getOrDefault("quantity", 1)).intValue();

        Map<String, Object> response = cartService.addToCart(username, productId, quantity);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(HttpServletRequest request) {
        String username = getUsernameFromRequest(request);
        Map<String, Object> response = cartService.getCart(username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCartQuantity(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        String username = getUsernameFromRequest(request);

        if (body == null || body.get("productId") == null || body.get("quantity") == null) {
            throw new RuntimeException("productId and quantity are required");
        }

        Integer productId = ((Number) body.get("productId")).intValue();
        int quantity = ((Number) body.get("quantity")).intValue();

        Map<String, Object> response = cartService.updateCartItemQuantity(username, productId, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteCartItem(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        String username = getUsernameFromRequest(request);

        if (body == null || body.get("productId") == null) {
            throw new RuntimeException("productId is required");
        }

        Integer productId = ((Number) body.get("productId")).intValue();

        Map<String, Object> response = cartService.removeFromCart(username, productId);
        return ResponseEntity.ok(response);
    }

}
