package com.oscarr.demoappgym.model.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class SaleRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Long userId;

    @NotEmpty(message = "La venta debe contener al menos un producto")
    @Valid
    private List<SaleProductRequest> products;

    public SaleRequest() {
    }

    public SaleRequest(Long userId, List<SaleProductRequest> products) {
        this.userId = userId;
        this.products = products;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<SaleProductRequest> getProducts() {
        return products;
    }

    public void setProducts(List<SaleProductRequest> products) {
        this.products = products;
    }
}
