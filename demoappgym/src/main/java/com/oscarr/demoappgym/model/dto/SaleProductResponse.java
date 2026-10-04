package com.oscarr.demoappgym.model.dto;

public class SaleProductResponse {
    private Long productId;
    private String productName;
    private Long amount;
    private Double unitPrice;
    private Double subtotal;

    public SaleProductResponse() {
    }

    public SaleProductResponse(Long productId, String productName, Long amount, Double unitPrice, Double subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

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

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
