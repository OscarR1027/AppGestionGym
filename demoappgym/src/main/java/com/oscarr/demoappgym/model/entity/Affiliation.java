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
@Table (name="Affiliation")
public class Affiliation {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)    
    private Long numberAffiliation;

    @ManyToOne 
    @JoinColumn (name="codeUser",nullable = false)
    private User user;

    @Column (name="Affiliation_Date",nullable = false)
    private LocalDate affiliationDate;

    @Column (name="End_Affiliation",nullable = false)
    private LocalDate endAffiliation;

    @ManyToOne 
    @JoinColumn (name="idTypePlan",nullable = false)
    private TypePlan typePlan;

    @Column(name = "paid_value", precision = 12)
    private Double paidValue;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "is_renewal", nullable = false)
    private boolean renewal;

    @Column(name = "invoice_cancelled", nullable = false)
    private boolean invoiceCancelled;

    public Affiliation(){}

    public Affiliation(Long numberAffiliation, User user, LocalDate affiliationDate, 
        LocalDate endAffiliation,TypePlan typePlan) 
        {
        this.numberAffiliation = numberAffiliation;
        this.user = user;
        this.affiliationDate = affiliationDate;
        this.endAffiliation = endAffiliation;
        this.typePlan = typePlan;
    }

    public Long getNumberAffiliation() {
        return numberAffiliation;
    }

    public void setNumberAffiliation(Long numberAffiliation) {
        this.numberAffiliation = numberAffiliation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getAffiliationDate() {
        return affiliationDate;
    }

    public void setAffiliationDate(LocalDate affiliationDate) {
        this.affiliationDate = affiliationDate;
    }

    public LocalDate getEndAffiliation() {
        return endAffiliation;
    }

    public void setEndAffiliation(LocalDate endAffiliation) {
        this.endAffiliation = endAffiliation;
    }

    public TypePlan getTypePlan() {
        return typePlan;
    }

    public void setTypePlan(TypePlan typePlan) {
        this.typePlan = typePlan;
    }

    public Double getPaidValue() {
        return paidValue;
    }

    public void setPaidValue(Double paidValue) {
        this.paidValue = paidValue;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isRenewal() {
        return renewal;
    }

    public void setRenewal(boolean renewal) {
        this.renewal = renewal;
    }

    public boolean isInvoiceCancelled() {
        return invoiceCancelled;
    }

    public void setInvoiceCancelled(boolean invoiceCancelled) {
        this.invoiceCancelled = invoiceCancelled;
    }

    
}
