package com.oscarr.demoappgym.model.dto;

import jakarta.validation.constraints.NotBlank;

public class GymServiceRequest {

    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String serviceName;

    public GymServiceRequest() {
    }

    public GymServiceRequest(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
