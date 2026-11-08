package com.oscarr.demoappgym.model.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name="Sales")
public class Sale {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long idBillSale;

    @ManyToOne 
    @JoinColumn (name= "codeUser",nullable = false)
    private User user;

    @Column (name="Sale_Date",nullable = false)
    private LocalDate saleDate;

    @Column (name="Total_Value",precision = 12,nullable = false)
    private Double totalValue;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    public Sale(){}

    public Sale(Long idBillSale, User user, LocalDate saleDate, Double totalValue) {
        this.idBillSale = idBillSale;
        this.user = user;
        this.saleDate = saleDate;
        this.totalValue = totalValue;
    }

    public Long getIdBillSale() {
        return idBillSale;
    }

    public void setIdBillSale(Long idBillSale) {
        this.idBillSale = idBillSale;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    

}
