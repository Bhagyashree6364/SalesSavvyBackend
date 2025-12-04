package com.example.demo.services;

import com.example.demo.entities.CartItem;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.OrderStatus;
import com.example.demo.repositories.CartItemRepository;
import com.example.demo.repositories.OrderItemRepository;
import com.example.demo.repositories.OrderRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    public PaymentService(OrderRepository orderRepository,
                          OrderItemRepository orderItemRepository,
                          CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public String createOrder(Integer userId,
                              BigDecimal totalAmount,
                              List<OrderItem> cartItems) throws RazorpayException {

        // Create Razorpay client
        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        // Prepare Razorpay order request
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", totalAmount.multiply(BigDecimal.valueOf(100)).intValue()); // in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "order_rcpt_" + System.currentTimeMillis());

        // Create Razorpay order (fully qualified to avoid clash with entity Order)
        com.razorpay.Order rpOrder = client.Orders.create(orderRequest);
        String razorpayOrderId = rpOrder.get("id");

        // Save order entity in DB
        Order order = new Order();
        order.setOrderId(razorpayOrderId);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        return razorpayOrderId;
    }

    @Transactional
    public boolean verifyPayment(String razorpayOrderId,
                                 String razorpayPaymentId,
                                 String razorpaySignature,
                                 Integer userId) {
        try {
            // Signature validation
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            boolean isValidSignature =
                    com.razorpay.Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

            if (!isValidSignature) {
                return false;
            }

            // Update order status
            Order order = orderRepository.findById(razorpayOrderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            order.setStatus(OrderStatus.SUCCESS);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            // Fetch cart items for user and save as order items
            List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

            for (CartItem cartItem : cartItems) {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProductId(cartItem.getProduct().getProductId());
                item.setQuantity(cartItem.getQuantity());
                BigDecimal pricePerUnit = cartItem.getProduct().getPrice();
                item.setPricePerUnit(pricePerUnit);
                item.setTotalPrice(
                        pricePerUnit.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                orderItemRepository.save(item);
            }

            // Clear cart
            cartItemRepository.deleteAllByUserId(userId);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
