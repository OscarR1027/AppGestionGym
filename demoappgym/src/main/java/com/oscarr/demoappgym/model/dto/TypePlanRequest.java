package com.oscarr.demoappgym.model.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TypePlanRequest {

    @NotBlank(message = "El nombre del plan es obligatorio")
    private String namePlan;

    @NotNull(message = "El valor del plan es obligatorio")
    @Min(value = 0, message = "El valor no puede ser negativo")
    private Integer value;

    private List<Long> serviceIds;

    public TypePlanRequest() {
    }

    public String getNamePlan() {
        return namePlan;
    }

    public void setNamePlan(String namePlan) {
        this.namePlan = namePlan;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public List<Long> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<Long> serviceIds) {
        this.serviceIds = serviceIds;
    }
}
