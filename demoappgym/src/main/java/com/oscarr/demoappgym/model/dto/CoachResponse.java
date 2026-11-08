package com.oscarr.demoappgym.model.dto;

public class CoachResponse {

    private Long idCoach;
    private String nameCoach;
    private String photoData;

    public CoachResponse() {
    }

    public CoachResponse(Long idCoach, String nameCoach) {
        this(idCoach, nameCoach, null);
    }

    public CoachResponse(Long idCoach, String nameCoach, String photoData) {
        this.idCoach = idCoach;
        this.nameCoach = nameCoach;
        this.photoData = photoData;
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

    public String getPhotoData() {
        return photoData;
    }

    public void setPhotoData(String photoData) {
        this.photoData = photoData;
    }
}
