package com.oscarr.demoappgym.model.dto;

public class MonthlyRevenueResponse {

    private final Double totalValue;

    public MonthlyRevenueResponse(Double totalValue) {
        this.totalValue = totalValue;
    }

    public Double getTotalValue() {
        return totalValue;
    }
}
