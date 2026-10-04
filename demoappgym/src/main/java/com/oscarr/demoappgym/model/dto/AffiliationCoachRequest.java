package com.oscarr.demoappgym.model.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public class AffiliationCoachRequest {

    @NotNull(message = "El entrenador es obligatorio")
    private Long coachId;

    private LocalDate startDate;
    private LocalDate endDate;

    public AffiliationCoachRequest() {
    }

    public Long getCoachId() {
        return coachId;
    }

    public void setCoachId(Long coachId) {
        this.coachId = coachId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
