package com.oscarr.demoappgym.model.dto;

import jakarta.validation.constraints.NotBlank;

public class CoachRequest {

    @NotBlank(message = "El nombre del coach es obligatorio")
    private String nameCoach;

    public CoachRequest() {
    }

    public CoachRequest(String nameCoach) {
        this.nameCoach = nameCoach;
    }

    public String getNameCoach() {
        return nameCoach;
    }

    public void setNameCoach(String nameCoach) {
        this.nameCoach = nameCoach;
    }
}
