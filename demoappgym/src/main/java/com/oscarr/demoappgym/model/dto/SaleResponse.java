package com.oscarr.demoappgym.model.dto;

import java.time.LocalDate;
import java.util.List;

public class SaleResponse {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDate saleDate;
    private Double totalValue;
    private List<SaleProductResponse> products;

    public SaleResponse() {
    }

    public SaleResponse(Long id, Long userId, String userName, LocalDate saleDate, Double totalValue,
            List<SaleProductResponse> products) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.saleDate = saleDate;
        this.totalValue = totalValue;
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

    public List<SaleProductResponse> getProducts() {
        return products;
    }

    public void setProducts(List<SaleProductResponse> products) {
        this.products = products;
    }
}
