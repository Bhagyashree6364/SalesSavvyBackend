package com.example.demo.repositories;

import com.example.demo.entities.CartItem;
import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    // Used in CartService
    Optional<CartItem> findByUserAndProduct(User user, Product product);

    // Used in CartService buildCartResponse
    List<CartItem> findByUser(User user);

    // Used in PaymentService
    List<CartItem> findByUserId(Integer userId);

    // Used in PaymentService to clear cart
    void deleteAllByUserId(Integer userId);
}
