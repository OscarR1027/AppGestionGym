package com.oscarr.demoappgym.model.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class AffiliationRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Long userId;

    @NotNull(message = "El plan es obligatorio")
    private Long typePlanId;

    @NotBlank(message = "El método de pago es obligatorio")
    private String paymentMethod;

    private LocalDate affiliationDate;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate endAffiliation;

    @Valid
    private List<AffiliationCoachRequest> coaches;

    public AffiliationRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTypePlanId() {
        return typePlanId;
    }

    public void setTypePlanId(Long typePlanId) {
        this.typePlanId = typePlanId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public List<AffiliationCoachRequest> getCoaches() {
        return coaches;
    }

    public void setCoaches(List<AffiliationCoachRequest> coaches) {
        this.coaches = coaches;
    }
}
