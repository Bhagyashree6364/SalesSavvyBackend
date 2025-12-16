package com.example.demo.services;

import com.example.demo.dto.OrderItemResponse;
import com.example.demo.dto.OrderResponse;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.OrderStatus;
import com.example.demo.repositories.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // userId is Integer on Order
    public List<OrderResponse> getOrdersForCustomer(Integer customerId) {

        List<Order> orders =
                orderRepository.findByUserIdAndStatus(customerId, OrderStatus.SUCCESS);

        return orders.stream().map(order -> {
            OrderResponse resp = new OrderResponse();

            // DTO expects Long, entity has String → cannot convert safely
            // So treat orderId in DTO also as String: change DTO OR cast here.
            // Easiest now: use null for numeric id and send string in status/msg if needed.
            // If you want real value, change DTO.orderId type to String.
            resp.setOrderId(null); // because entity id is String

            resp.setOrderDate(order.getCreatedAt());
            resp.setStatus(order.getStatus().name());

            BigDecimal total = order.getTotalAmount();
            resp.setTotalAmount(total != null ? total.doubleValue() : 0.0);

            List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                    .map(this::mapItemToResponse)
                    .collect(Collectors.toList());

            resp.setItems(itemResponses);
            return resp;
        }).collect(Collectors.toList());
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        OrderItemResponse ir = new OrderItemResponse();

        // OrderItem.productId is Integer, DTO expects Long
        Integer pid = item.getProductId();
        ir.setProductId(pid != null ? pid.longValue() : null);

        // You do not have productName or image in OrderItem, so keep them null
        ir.setProductName(null);

        ir.setQuantity(item.getQuantity() != null ? item.getQuantity() : 0);

        BigDecimal ppu = item.getPricePerUnit();
        ir.setPricePerUnit(ppu != null ? ppu.doubleValue() : 0.0);

        ir.setImageUrl(null);

        return ir;
    }
}
