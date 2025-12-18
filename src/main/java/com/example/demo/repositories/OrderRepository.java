package com.example.demo.repositories;

import com.example.demo.entities.Order;
import com.example.demo.entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    // Customer-specific: all successful orders of a user
    List<Order> findByUserIdAndStatus(Integer userId, OrderStatus status);

    /* ========= ADMIN ANALYTICS QUERIES ========= */
    // If you want only SUCCESS orders counted in analytics,
    // keep "and o.status = 'SUCCESS'" in each query.

    // Daily
    @Query("select coalesce(sum(o.totalAmount), 0) " +
           "from Order o " +
           "where o.createdAt = :date and o.status = 'SUCCESS'")
    Optional<BigDecimal> sumTotalByDate(@Param("date") LocalDate date);

    @Query("select count(o) " +
           "from Order o " +
           "where o.createdAt = :date and o.status = 'SUCCESS'")
    long countByOrderDate(@Param("date") LocalDate date);

    // Monthly
    @Query("select coalesce(sum(o.totalAmount), 0) " +
           "from Order o " +
           "where year(o.createdAt) = :year " +
           "and month(o.createdAt) = :month " +
           "and o.status = 'SUCCESS'")
    Optional<BigDecimal> sumTotalByMonth(@Param("year") int year,
                                         @Param("month") int month);

    @Query("select count(o) " +
           "from Order o " +
           "where year(o.createdAt) = :year " +
           "and month(o.createdAt) = :month " +
           "and o.status = 'SUCCESS'")
    long countByMonth(@Param("year") int year,
                      @Param("month") int month);

    // Yearly
    @Query("select coalesce(sum(o.totalAmount), 0) " +
           "from Order o " +
           "where year(o.createdAt) = :year " +
           "and o.status = 'SUCCESS'")
    Optional<BigDecimal> sumTotalByYear(@Param("year") int year);

    @Query("select count(o) " +
           "from Order o " +
           "where year(o.createdAt) = :year " +
           "and o.status = 'SUCCESS'")
    long countByYear(@Param("year") int year);

    // Overall
    @Query("select coalesce(sum(o.totalAmount), 0) " +
           "from Order o " +
           "where o.status = 'SUCCESS'")
    Optional<BigDecimal> sumTotalOverall();
}
