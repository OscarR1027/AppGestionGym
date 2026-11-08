package com.oscarr.demoappgym.model.dto;

import java.time.LocalDate;
import java.util.List;

public class SaleResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userDocumentType;
    private String userDocumentNumber;
    private Long userPhoneNumber;
    private LocalDate saleDate;
    private Double totalValue;
    private String paymentMethod;
    private List<SaleProductResponse> products;

    public SaleResponse() {
    }

    public SaleResponse(Long id, Long userId, String userName, String userDocumentType, String userDocumentNumber,
            Long userPhoneNumber,
            LocalDate saleDate, Double totalValue, String paymentMethod, List<SaleProductResponse> products) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userDocumentType = userDocumentType;
        this.userDocumentNumber = userDocumentNumber;
        this.userPhoneNumber = userPhoneNumber;
        this.saleDate = saleDate;
        this.totalValue = totalValue;
        this.paymentMethod = paymentMethod;
        this.products = products;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserDocumentNumber() {
        return userDocumentNumber;
    }

    public String getUserDocumentType() {
        return userDocumentType;
    }

    public void setUserDocumentType(String userDocumentType) {
        this.userDocumentType = userDocumentType;
    }

    public void setUserDocumentNumber(String userDocumentNumber) {
        this.userDocumentNumber = userDocumentNumber;
    }

    public Long getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(Long userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<SaleProductResponse> getProducts() {
        return products;
    }

    public void setProducts(List<SaleProductResponse> products) {
        this.products = products;
    }
}
