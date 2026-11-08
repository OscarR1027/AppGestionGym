package com.oscarr.demoappgym.model.dto;

import java.time.LocalDate;
import java.util.List;

public class AffiliationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userDocumentNumber;
    private Long userPhoneNumber;
    private LocalDate affiliationDate;
    private LocalDate endAffiliation;
    private Long typePlanId;
    private String typePlanName;
    private Double paidValue;
    private String paymentMethod;
    private boolean renewal;
    private boolean invoiceCancelled;
    private List<AffiliationCoachResponse> coaches;

    public AffiliationResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserDocumentNumber() {
        return userDocumentNumber;
    }

    public void setUserDocumentNumber(String userDocumentNumber) {
        this.userDocumentNumber = userDocumentNumber;
    }

    public Long getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(Long userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
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

    public Long getTypePlanId() {
        return typePlanId;
    }

    public void setTypePlanId(Long typePlanId) {
        this.typePlanId = typePlanId;
    }

    public String getTypePlanName() {
        return typePlanName;
    }

    public void setTypePlanName(String typePlanName) {
        this.typePlanName = typePlanName;
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

    public List<AffiliationCoachResponse> getCoaches() {
        return coaches;
    }

    public void setCoaches(List<AffiliationCoachResponse> coaches) {
        this.coaches = coaches;
    }
}
