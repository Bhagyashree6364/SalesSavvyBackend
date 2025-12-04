package com.example.demo.services;

import com.example.demo.entities.CartItem;
import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import com.example.demo.repositories.CartItemRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // helper to get user
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // helper to get product
    private Product getProductById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public Map<String, Object> addToCart(String username,
                                         Integer productId,
                                         Integer quantity) {
        User user = getUserByUsername(username);
        Product product = getProductById(productId);

        // check if cart item already exists
        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setUser(user);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    return ci;
                });

        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItemRepository.save(cartItem);

        return buildCartResponse(user);
    }

    @Transactional
    public Map<String, Object> updateCartItemQuantity(String username,
                                                      Integer productId,
                                                      Integer quantity) {
        User user = getUserByUsername(username);
        Product product = getProductById(productId);

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new RuntimeException("Cart item not found for this product"));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return buildCartResponse(user);
    }

    @Transactional
    public Map<String, Object> removeFromCart(String username, Integer productId) {
        User user = getUserByUsername(username);
        Product product = getProductById(productId);

        cartItemRepository.findByUserAndProduct(user, product)
                .ifPresent(cartItemRepository::delete);

        return buildCartResponse(user);
    }

    @Transactional
    public Map<String, Object> getCart(String username) {
        User user = getUserByUsername(username);
        return buildCartResponse(user);
    }

    // builds response with items + total
    private Map<String, Object> buildCartResponse(User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);

        List<Map<String, Object>> itemList = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : items) {
            Product product = item.getProduct();
            BigDecimal lineTotal =
                    product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);

            Map<String, Object> row = new HashMap<>();
            row.put("cartItemId", item.getId());
            row.put("productId", product.getProductId());
            row.put("name", product.getName());
            row.put("price", product.getPrice());
            row.put("quantity", item.getQuantity());
            row.put("lineTotal", lineTotal);
            itemList.add(row);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("items", itemList);
        response.put("totalAmount", total);
        return response;
    }
}
