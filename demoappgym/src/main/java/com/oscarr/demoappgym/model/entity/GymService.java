package com.oscarr.demoappgym.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name="Service")
public class GymService {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long idService;

    @Column (name="Service_Name",nullable = false)
    private String serviceName;

    public GymService(){}

    public GymService(Long idService, String serviceName) {
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
