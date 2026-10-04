package com.oscarr.demoappgym.model.dto;

import java.util.List;

public class TypePlanResponse {
    private Long idTypePlan;
    private String namePlan;
    private Integer value;
    private List<GymServiceResponse> services;

    public TypePlanResponse() {
    }

    public TypePlanResponse(Long idTypePlan, String namePlan, Integer value, List<GymServiceResponse> services) {
        this.idTypePlan = idTypePlan;
        this.namePlan = namePlan;
        this.value = value;
        this.services = services;
    }

    public Long getIdTypePlan() {
        return idTypePlan;
    }

    public void setIdTypePlan(Long idTypePlan) {
        this.idTypePlan = idTypePlan;
    }

    public Long getId() {
        return idTypePlan;
    }

    public void setId(Long id) {
        this.idTypePlan = id;
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

    public List<GymServiceResponse> getServices() {
        return services;
    }

    public void setServices(List<GymServiceResponse> services) {
        this.services = services;
    }
}
