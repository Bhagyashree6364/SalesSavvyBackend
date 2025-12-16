package com.example.demo.repositories;

import com.example.demo.entities.Order;
import com.example.demo.entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    // Order has Integer userId and OrderStatus status
    List<Order> findByUserIdAndStatus(Integer userId, OrderStatus status);
}
