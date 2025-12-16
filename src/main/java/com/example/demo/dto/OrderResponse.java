package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

 private Long orderId;
 private LocalDateTime orderDate;
 private String status;
 private double totalAmount;
 private List<OrderItemResponse> items;

 public Long getOrderId() {
     return orderId;
 }

 public void setOrderId(Long orderId) {
     this.orderId = orderId;
 }

 public LocalDateTime getOrderDate() {
     return orderDate;
 }

 public void setOrderDate(LocalDateTime orderDate) {
     this.orderDate = orderDate;
 }

 public String getStatus() {
     return status;
 }

 public void setStatus(String status) {
     this.status = status;
 }

 public double getTotalAmount() {
     return totalAmount;
 }

 public void setTotalAmount(double totalAmount) {
     this.totalAmount = totalAmount;
 }

 public List<OrderItemResponse> getItems() {
     return items;
 }

 public void setItems(List<OrderItemResponse> items) {
     this.items = items;
 }
}
