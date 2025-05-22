package com.project.quanlybanhang.Buy;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.Objects;

public class BuyData {
    private String variantId;
    private String action; // "add" in this context means confirmed purchase
    private int quantity;
    private String productId;
    private String color;
    private String username;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime orderTimestamp;
    private String address; // New field
    private String phone;   // New field

    // Default constructor for Jackson
    public BuyData() {
    }

    public BuyData(String variantId, String action, int quantity, String productId, String color, String username,
                   LocalDateTime orderTimestamp, String address, String phone) {
        this.variantId = variantId;
        this.action = action;
        this.quantity = quantity;
        this.productId = productId;
        this.color = color;
        this.username = username;
        this.orderTimestamp = orderTimestamp;
        this.address = address;
        this.phone = phone;
    }

    // Getters and Setters for all fields (including new ones)
    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getOrderTimestamp() {
        return orderTimestamp;
    }

    public void setOrderTimestamp(LocalDateTime orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "BuyData{" +
                "variantId='" + variantId + '\'' +
                ", action='" + action + '\'' +
                ", quantity=" + quantity +
                ", productId='" + productId + '\'' +
                ", color='" + color + '\'' +
                ", username='" + username + '\'' +
                ", orderTimestamp=" + orderTimestamp +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuyData buyData = (BuyData) o;
        return quantity == buyData.quantity &&
                Objects.equals(variantId, buyData.variantId) &&
                Objects.equals(productId, buyData.productId) &&
                Objects.equals(color, buyData.color) &&
                Objects.equals(username, buyData.username) &&
                // So sánh LocalDateTime chính xác, cần đảm bảo orderTimestamp không null
                (orderTimestamp == buyData.orderTimestamp || (orderTimestamp != null && orderTimestamp.isEqual(buyData.orderTimestamp)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantId, productId, color, username, orderTimestamp, quantity);
    }
}