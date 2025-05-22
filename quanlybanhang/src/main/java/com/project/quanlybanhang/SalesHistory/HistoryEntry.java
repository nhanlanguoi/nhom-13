package com.project.quanlybanhang.SalesHistory;

import com.project.quanlybanhang.Buy.BuyData; // Sử dụng lại BuyData
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class HistoryEntry {
    private String id;
    @JsonProperty("orderData") // Đảm bảo khớp với tên trường trong JSON
    private BuyData orderData;
    private String employeeUsername;
    private String employeeFullname;
    private LocalDateTime acceptedTimestamp;
    private LocalDateTime completedTimestamp;
    private double totalAmount; // Thêm trường này để lưu tổng tiền

    // Constructors
    public HistoryEntry() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BuyData getOrderData() {
        return orderData;
    }

    public void setOrderData(BuyData orderData) {
        this.orderData = orderData;
    }

    public String getEmployeeUsername() {
        return employeeUsername;
    }

    public void setEmployeeUsername(String employeeUsername) {
        this.employeeUsername = employeeUsername;
    }

    public String getEmployeeFullname() {
        return employeeFullname;
    }

    public void setEmployeeFullname(String employeeFullname) {
        this.employeeFullname = employeeFullname;
    }

    public LocalDateTime getAcceptedTimestamp() {
        return acceptedTimestamp;
    }

    public void setAcceptedTimestamp(LocalDateTime acceptedTimestamp) {
        this.acceptedTimestamp = acceptedTimestamp;
    }

    public LocalDateTime getCompletedTimestamp() {
        return completedTimestamp;
    }

    public void setCompletedTimestamp(LocalDateTime completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}