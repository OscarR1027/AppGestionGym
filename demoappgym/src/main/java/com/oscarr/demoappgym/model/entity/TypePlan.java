package com.oscarr.demoappgym.model.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity 
@Table(name="Type_Plans")
public class TypePlan {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long idTypePlan;

    @Column(name="Name_Plan",nullable=false)
    private String namePlan;
    
    @Column(name="Value_Plan",precision=8,scale=0,nullable = false)
    private Integer value;
    @ManyToMany
    @JoinTable(
        name = "Type_Plan_Services", joinColumns = @JoinColumn(name = "idTypePlan"), 
        inverseJoinColumns = @JoinColumn(name = "idService") 
    )
    private List<GymService> services;
    public TypePlan(){}

    public TypePlan(Long idTypePlan, String namePlan, Integer value) {
        this.idTypePlan = idTypePlan;
        this.namePlan = namePlan;
        this.value = value;
    }

    public Long getIdTypePlan() {
        return idTypePlan;
    }

    public void setIdTypePlan(Long idTypePlan) {
        this.idTypePlan = idTypePlan;
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

    public List<GymService> getServices() {
        return services;
    }

    public void setServices(List<GymService> services) {
        this.services = services;
    }
}
