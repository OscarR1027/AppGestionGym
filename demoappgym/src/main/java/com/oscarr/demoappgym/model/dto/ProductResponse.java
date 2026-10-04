package com.oscarr.demoappgym.model.dto;

public class ProductResponse {
    private Long id;
    private String nameProduct;
    private Double value;
    private Long amount;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String nameProduct, Double value, Long amount) {
        this.id = id;
        this.nameProduct = nameProduct;
        this.value = value;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameProduct() {
        return nameProduct;
    }

    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
