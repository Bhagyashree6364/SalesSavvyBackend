package com.example.demo.controllers;

import com.example.demo.entities.OrderItem;
import com.example.demo.entities.User;
import com.example.demo.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Utility: get authenticated user from request (set by your AuthenticationFilter)
    private User getAuthenticatedUser(HttpServletRequest request) {
        Object userObj = request.getAttribute("authenticatedUser");
        if (userObj instanceof User) {
            return (User) userObj;
        }
        return null;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createPaymentOrder(@RequestBody Map<String, Object> requestBody,
                                                     HttpServletRequest request,
                                                     Principal principal) {
        try {
            // Debug: see what frontend actually sends
            System.out.println("Payment requestBody = " + requestBody);

            User user = getAuthenticatedUser(request);
            if (user == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            // ---- Read and validate totalAmount ----
            Object totalAmountObj = requestBody.get("totalAmount");
            if (totalAmountObj == null) {
                return ResponseEntity.badRequest().body("totalAmount is missing in request body");
            }
            BigDecimal totalAmount = new BigDecimal(totalAmountObj.toString());

            // ---- Read and validate cartItems ----
            Object cartItemsObj = requestBody.get("cartItems");
            if (!(cartItemsObj instanceof List)) {
                return ResponseEntity.badRequest().body("cartItems must be a list");
            }

            List<?> rawList = (List<?>) cartItemsObj;
            List<OrderItem> cartItems = new ArrayList<>();

            for (Object o : rawList) {
                if (!(o instanceof Map)) {
                    return ResponseEntity.badRequest().body("Invalid cartItems format");
                }
                Map<?, ?> itemMap = (Map<?, ?>) o;

                Object productIdObj = itemMap.get("productId");
                Object quantityObj = itemMap.get("quantity");
                Object pricePerUnitObj = itemMap.get("price_per_unit");

                if (productIdObj == null || quantityObj == null || pricePerUnitObj == null) {
                    return ResponseEntity.badRequest()
                            .body("Each cart item must contain productId, quantity and price_per_unit");
                }

                Integer productId = Integer.valueOf(productIdObj.toString());
                Integer quantity = Integer.valueOf(quantityObj.toString());
                BigDecimal pricePerUnit = new BigDecimal(pricePerUnitObj.toString());

                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(productId);
                orderItem.setQuantity(quantity);
                orderItem.setPricePerUnit(pricePerUnit);
                orderItem.setTotalPrice(pricePerUnit.multiply(BigDecimal.valueOf(quantity)));

                cartItems.add(orderItem);
            }

            // ---- Call service to create Razorpay order + DB order ----
            String razorpayOrderId =
                    paymentService.createOrder(user.getId(), totalAmount, cartItems);

            return ResponseEntity.ok(razorpayOrderId);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Error during checkout: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody Map<String, String> payload,
                                                HttpServletRequest request) {
        try {
            User user = getAuthenticatedUser(request);
            if (user == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            String razorpayOrderId = payload.get("razorpay_order_id");
            String razorpayPaymentId = payload.get("razorpay_payment_id");
            String razorpaySignature = payload.get("razorpay_signature");

            if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
                return ResponseEntity.badRequest().body("Missing payment verification fields");
            }

            boolean success = paymentService.verifyPayment(
                    razorpayOrderId,
                    razorpayPaymentId,
                    razorpaySignature,
                    user.getId()
            );

            if (success) {
                return ResponseEntity.ok("Payment verified and order placed successfully");
            } else {
                return ResponseEntity.badRequest().body("Payment verification failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Error verifying payment: " + e.getMessage());
        }
    }
}
