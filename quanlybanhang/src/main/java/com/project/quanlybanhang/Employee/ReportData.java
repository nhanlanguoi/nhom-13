package com.project.quanlybanhang.Employee; // Hoặc một package mới ví dụ com.project.quanlybanhang.Report

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.project.quanlybanhang.Buy.BuyData; // Để lưu thông tin đơn hàng gốc

import java.time.LocalDateTime;
import java.util.UUID;

public class ReportData {
    private String reportId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime reportTimestamp;

    private String employeeUsername;
    private String employeeFullname;
    private String reportReason;

    private BuyData reportedOrderData; 

    private String originalDangGiaoId;
    private String originalAcceptingEmployee;

    public ReportData() {
        this.reportId = UUID.randomUUID().toString();
        this.reportTimestamp = LocalDateTime.now();
    }

    // Constructor tiện lợi
    public ReportData(String employeeUsername, String employeeFullname, String reportReason, BuyData reportedOrderData,
            String originalDangGiaoId, String originalAcceptingEmployee) {
        this();
        this.employeeUsername = employeeUsername;
        this.employeeFullname = employeeFullname;
        this.reportReason = reportReason;
        this.reportedOrderData = reportedOrderData;
        this.originalDangGiaoId = originalDangGiaoId;
        this.originalAcceptingEmployee = originalAcceptingEmployee;
    }

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public LocalDateTime getReportTimestamp() {
        return reportTimestamp;
    }

    public void setReportTimestamp(LocalDateTime reportTimestamp) {
        this.reportTimestamp = reportTimestamp;
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

    public String getReportReason() {
        return reportReason;
    }

    public void setReportReason(String reportReason) {
        this.reportReason = reportReason;
    }

    public BuyData getReportedOrderData() {
        return reportedOrderData;
    }

    public void setReportedOrderData(BuyData reportedOrderData) {
        this.reportedOrderData = reportedOrderData;
    }

    public String getOriginalDangGiaoId() {
        return originalDangGiaoId;
    }

    public void setOriginalDangGiaoId(String originalDangGiaoId) {
        this.originalDangGiaoId = originalDangGiaoId;
    }

    public String getOriginalAcceptingEmployee() {
        return originalAcceptingEmployee;
    }

    public void setOriginalAcceptingEmployee(String originalAcceptingEmployee) {
        this.originalAcceptingEmployee = originalAcceptingEmployee;
    }
}