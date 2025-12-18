package com.example.demo.services;

import com.example.demo.dto.OrderItemResponse;
import com.example.demo.dto.OrderResponse;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.OrderStatus;
import com.example.demo.repositories.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // For customer order history
    public List<OrderResponse> getOrdersForCustomer(Integer customerId) {
        List<Order> orders =
                orderRepository.findByUserIdAndStatus(customerId, OrderStatus.SUCCESS);

        return orders.stream()
                .map(this::mapOrderToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapOrderToResponse(Order order) {
        OrderResponse resp = new OrderResponse();

        // If your Order.id is String, change DTO field type to String and use setOrderId(order.getId()).
        // Here kept as null assuming DTO has Long.
        resp.setOrderId(null);

        resp.setOrderDate(order.getCreatedAt());
        resp.setStatus(order.getStatus().name());

        BigDecimal total = order.getTotalAmount();
        resp.setTotalAmount(total != null ? total.doubleValue() : 0.0);

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        resp.setItems(itemResponses);
        return resp;
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        OrderItemResponse ir = new OrderItemResponse();

        Integer pid = item.getProductId();
        ir.setProductId(pid != null ? pid.longValue() : null);

        ir.setProductName(null);          // no product name in OrderItem entity
        ir.setImageUrl(null);             // no image url in OrderItem entity

        ir.setQuantity(item.getQuantity() != null ? item.getQuantity() : 0);

        BigDecimal ppu = item.getPricePerUnit();
        ir.setPricePerUnit(ppu != null ? ppu.doubleValue() : 0.0);

        return ir;
    }

    /* ========= ADMIN ANALYTICS METHODS ========= */

    public BigDecimal calculateTotalForDate(LocalDate date) {
        Optional<BigDecimal> opt = orderRepository.sumTotalByDate(date);
        return opt.orElse(BigDecimal.ZERO);
    }

    public long countOrdersForDate(LocalDate date) {
        return orderRepository.countByOrderDate(date);
    }

    public BigDecimal calculateTotalForMonth(YearMonth ym) {
        Optional<BigDecimal> opt =
                orderRepository.sumTotalByMonth(ym.getYear(), ym.getMonthValue());
        return opt.orElse(BigDecimal.ZERO);
    }

    public long countOrdersForMonth(YearMonth ym) {
        return orderRepository.countByMonth(ym.getYear(), ym.getMonthValue());
    }

    public BigDecimal calculateTotalForYear(Year year) {
        Optional<BigDecimal> opt = orderRepository.sumTotalByYear(year.getValue());
        return opt.orElse(BigDecimal.ZERO);
    }

    public long countOrdersForYear(Year year) {
        return orderRepository.countByYear(year.getValue());
    }

    public BigDecimal calculateTotalOverall() {
        Optional<BigDecimal> opt = orderRepository.sumTotalOverall();
        return opt.orElse(BigDecimal.ZERO);
    }

    public long countOrdersOverall() {
        return orderRepository.count();
    }
}
