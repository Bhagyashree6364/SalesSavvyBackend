package com.example.demo.controllers;

import com.example.demo.dto.OrderResponse;
import com.example.demo.entities.User;
import com.example.demo.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersForCustomer(Authentication authentication) {
        User authUser = (User) authentication.getPrincipal();
        // User.id must be Integer to match Order.userId
        Integer customerId = authUser.getId();

        List<OrderResponse> orders = orderService.getOrdersForCustomer(customerId);
        return ResponseEntity.ok(orders);
    }
}
