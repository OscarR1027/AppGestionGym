package com.oscarr.demoappgym.model.dto;

public class GymServiceResponse {
    private Long idService;
    private String serviceName;

    public GymServiceResponse() {
    }

    public GymServiceResponse(Long idService, String serviceName) {
        this.idService = idService;
        this.serviceName = serviceName;
    }

    public Long getIdService() {
        return idService;
    }

    public void setIdService(Long idService) {
        this.idService = idService;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
