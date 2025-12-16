package com.example.demo.dto;

public class OrderItemResponse {

 private Long productId;
 private String productName;
 private int quantity;
 private double pricePerUnit;
 private String imageUrl;

 public Long getProductId() {
     return productId;
 }

 public void setProductId(Long productId) {
     this.productId = productId;
 }

 public String getProductName() {
     return productName;
 }

 public void setProductName(String productName) {
     this.productName = productName;
 }

 public int getQuantity() {
     return quantity;
 }

 public void setQuantity(int quantity) {
     this.quantity = quantity;
 }

 public double getPricePerUnit() {
     return pricePerUnit;
 }

 public void setPricePerUnit(double pricePerUnit) {
     this.pricePerUnit = pricePerUnit;
 }

 public String getImageUrl() {
     return imageUrl;
 }

 public void setImageUrl(String imageUrl) {
     this.imageUrl = imageUrl;
 }
}
