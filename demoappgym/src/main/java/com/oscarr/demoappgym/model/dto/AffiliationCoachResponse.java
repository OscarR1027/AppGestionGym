package com.oscarr.demoappgym.model.dto;

import java.time.LocalDate;

public class AffiliationCoachResponse {
    private Long coachId;
    private String coachName;
    private LocalDate startDate;
    private LocalDate endDate;

    public AffiliationCoachResponse() {
    }

    public AffiliationCoachResponse(Long coachId, String coachName, LocalDate startDate, LocalDate endDate) {
        this.coachId = coachId;
        this.coachName = coachName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getCoachId() {
        return coachId;
    }

    public void setCoachId(Long coachId) {
        this.coachId = coachId;
    }

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName;
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
