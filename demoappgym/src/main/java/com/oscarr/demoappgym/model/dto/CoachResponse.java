package com.oscarr.demoappgym.model.dto;

public class CoachResponse {

    private Long idCoach;
    private String nameCoach;

    public CoachResponse() {
    }

    public CoachResponse(Long idCoach, String nameCoach) {
        this.idCoach = idCoach;
        this.nameCoach = nameCoach;
    }

    public Long getIdCoach() {
        return idCoach;
    }

    public void setIdCoach(Long idCoach) {
        this.idCoach = idCoach;
    }

    public String getNameCoach() {
        return nameCoach;
    }

    public void setNameCoach(String nameCoach) {
        this.nameCoach = nameCoach;
    }
}
